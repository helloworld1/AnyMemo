
/*
Copyright (C) 2014 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.downloader.cram;


import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.downloader.DownloadItem;
import org.liberty.android.fantastischmemo.downloader.DownloaderUtils;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

import roboguice.util.Ln;

import android.content.Context;
import android.net.Uri;

import com.google.common.base.Strings;

/**
 * Helper class for downloading using Cram API.
 */
public class CramDownloadHelper {

    private Context context;

    private AMFileUtil amFileUtil;

    private DownloaderUtils downloaderUtils;

    @Inject
    public CramDownloadHelper(Context context) {
        this.context = context;
    }

    @Inject
    public void setAmFileUtil(AMFileUtil amFileUtil) {
        this.amFileUtil = amFileUtil;
    }

    @Inject
    public void setDownloaderUtils(DownloaderUtils downloaderUtils) {
        this.downloaderUtils = downloaderUtils;
    }

    /**
     * Get a list of card sets for a user.
     *
     * @param authToken the oauth token. If it is null, the call will only be able to access public sets.
     * @param userName the name of the user. Usually it is email address.
     * @return a list of download items.
     */
    public List<DownloadItem> getCardSetListByUserName(String authToken, String userName) throws IOException {
        String urlString = AMEnv.CRAM_API_ENDPOINT + "/users/"
            + URLEncoder.encode(userName, "UTF-8") + "/sets?client_id=" + AMEnv.CRAM_CLIENT_ID;
        URL url = new URL(urlString);

        String responseString = getResponseString(url, authToken);
        try {
            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray setsArray = jsonObject.getJSONArray("sets");
            return parseSetsJSONArray(setsArray);
        } catch(JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Get a list of card sets for a user.
     * 
     * @param authToken the oauth token. If it is null, the call will only be able to access public sets.
     * @param title the title to search
     * @return a list of download items.
     */
    public List<DownloadItem> getCardListByTitle(String authToken, String title, int page) throws IOException {
        String urlString = String.format(AMEnv.CRAM_API_ENDPOINT + "/search/sets?qstr=%1$s&sortedby=most_studied&page=%2$d&limit=100&client_id=%3$s",
            URLEncoder.encode(title, "UTF-8"),
            page,
            AMEnv.CRAM_CLIENT_ID);

        URL url = new URL(urlString);

        String responseString = getResponseString(url, authToken);
        try {
            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray setsArray = jsonObject.getJSONArray("results");
            return parseSetsJSONArray(setsArray);
        } catch(JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Download a cardset from Cram
     *
     * @param authToken the oauth token. If it is null, the call will only be able to access public sets.
     * @param cardSetId the card set id
     * @return  the path of saved db.
     */
    public String downloadCardSet(String authToken, String cardSetId) throws IOException {
        String urlString = AMEnv.CRAM_API_ENDPOINT + "/sets?id="
            + URLEncoder.encode(cardSetId, "UTF-8") + "&client_id=" + AMEnv.CRAM_CLIENT_ID;
        URL url = new URL(urlString);

        String responseString = getResponseString(url, authToken);

        try {
            JSONArray jsonArray = new JSONArray(responseString);
            if (jsonArray.length() == 0) {
                throw new IOException("Could not found a card set with id: " + cardSetId);
            }
            // Only the first one is used here.
            // The API should ensure there are not multiple sets ahring the same set id
            JSONObject setObject = jsonArray.getJSONObject(0);

            JSONArray cardsArray = setObject.getJSONArray("cards");

            String dbName = setObject.getString("title");
            if (!dbName.endsWith(".db")) {
                dbName += ".db";
            }
            String saveDbPath= AMEnv.DEFAULT_ROOT_PATH + "/" + dbName;
            amFileUtil.deleteFileWithBackup(saveDbPath);

            AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(saveDbPath);

            List<Card> cardList = new ArrayList<Card>(cardsArray.length());
            try {
                for (int i = 0; i < cardsArray.length(); i++) {
                    JSONObject cardObject = cardsArray.getJSONObject(i);

                    String question = "";
                    String answer = "";

                    if (cardObject.has("front") && !cardObject.isNull("front")) {
                        question  = cardObject.getString("front");
                    }

                    if (cardObject.has("back") && !cardObject.isNull("back")) {
                        answer = cardObject.getString("back");
                    }

                    // Download iamges
                    String imagePath = AMEnv.DEFAULT_IMAGE_PATH + dbName + "/";
                    // Front image
                    if (cardObject.has("image_front") && !cardObject.isNull("image_front")) {
                        String questionImageUrl = cardObject.getString("image_front");
                        String downloadFilename = Uri.parse(questionImageUrl).getLastPathSegment();
                        downloaderUtils.downloadFile(questionImageUrl, imagePath + "q-" + downloadFilename);
                        question = question + "<br /><img src=\"" + "q-" + downloadFilename + "\" />";
                    }

                    // Back image
                    if (cardObject.has("image_url") && !cardObject.isNull("image_url")) {
                        String answerImageUrl = cardObject.getString("image_url");
                        String downloadFilename = Uri.parse(answerImageUrl).getLastPathSegment();
                        downloaderUtils.downloadFile(answerImageUrl, imagePath + "a-" + downloadFilename);
                        answer = answer + "<br /><img src=\"" + "a-" + downloadFilename + "\" />";
                    }

                    Card card = new Card();
                    card.setQuestion(question);
                    card.setAnswer(answer);
                    card.setCategory(new Category());
                    card.setLearningData(new LearningData());
                    cardList.add(card);
                }
                helper.getCardDao().createCards(cardList);
                return saveDbPath;
            } finally {
                AnyMemoDBOpenHelperManager.releaseHelper(helper);
            }
        } catch (JSONException e) {
            Ln.e(e, "Error parsing response string: " + responseString);
            throw new IOException(e);
        }
    }

    /**
     * Get the response String from an url with optional auth token.
     *
     * @param url the request url
     * @param authToken the oauth auth token.
     * @return the response in string.
     */
    private String getResponseString(URL url, String authToken) throws IOException {
        if (!Strings.isNullOrEmpty(authToken)) {
            // TODO: Add oauth here
        }

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new IOException("Request: " + url + " HTTP response code is :" + conn.getResponseCode() + " Error: " + s);
        }
        return new String(IOUtils.toByteArray(conn.getInputStream()), "UTF-8");

    } 

    /**
     * Helper method to parse the response of set list class
     *
     * @param setsArray A JSON array of sets
     * @return a list of download items
     */
    private List<DownloadItem> parseSetsJSONArray(JSONArray setsArray) throws IOException {
        try {
            List<DownloadItem> itemList = new ArrayList<DownloadItem>(
                    setsArray.length());
            for (int i = 0; i < setsArray.length(); i++) {
                JSONObject setObject = setsArray.getJSONObject(i);
                DownloadItem downloadItem = new DownloadItem();
                downloadItem.setTitle(setObject.getString("title"));
                downloadItem.setType(DownloadItem.ItemType.Database);
                downloadItem.setAddress(setObject.getString("set_id"));

                if (setObject.has("description") && !setObject.isNull("description")) {
                    downloadItem.setDescription(setObject.getString("description"));
                }
                itemList.add(downloadItem);
            }
            return itemList;
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }
}
