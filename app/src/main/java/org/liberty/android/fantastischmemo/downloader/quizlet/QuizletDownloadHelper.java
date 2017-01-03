package org.liberty.android.fantastischmemo.downloader.quizlet;

import android.net.Uri;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.downloader.common.DownloadItem;
import org.liberty.android.fantastischmemo.downloader.common.DownloaderUtils;
import org.liberty.android.fantastischmemo.entity.Card;
import org.liberty.android.fantastischmemo.entity.Category;
import org.liberty.android.fantastischmemo.entity.LearningData;
import org.liberty.android.fantastischmemo.modules.PerApplication;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

@PerApplication
public class QuizletDownloadHelper {
    private static final String TAG = QuizletDownloadHelper.class.getSimpleName();

    private DownloaderUtils downloaderUtils;

    private AMFileUtil amFileUtil;

    @Inject
    public QuizletDownloadHelper(DownloaderUtils downloaderUtils, AMFileUtil amFileUtil) {
        this.downloaderUtils = downloaderUtils;
        this.amFileUtil = amFileUtil;
    }

    /**
     * Fetch cardsets list from Quizlet
     * 
     * @param userId
     *            user name
     * @param authToken
     *            oauth token
     * @return cardsets list
     * @throws IOException
     *             IOException If http response code is not 2xx
     * @throws JSONException
     *             If the response is invalid JSON
     */
    public List<DownloadItem> getUserPrivateCardsetsList(String userId,
            String authToken) throws IOException, JSONException {
        List<DownloadItem> downloadItemList = new ArrayList<DownloadItem>();
        URL url = new URL(AMEnv.QUIZLET_API_ENDPOINT + "/users/" + userId
                + "/sets");
        String response = makeApiCall(url, authToken);

        JSONArray jsonArray = new JSONArray(response);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonItem = jsonArray.getJSONObject(i);

            String address = jsonItem.getString("url");
            String description = new StringBuilder().append("<br />")
                    .append(jsonItem.getInt("term_count")).append("<br />")
                    .append(jsonItem.getLong("created_date")).append("<br />")
                    .append(jsonItem.getString("description")).append("<br />")
                    .append(jsonItem.getString("created_by")).toString();

            DownloadItem item = new DownloadItem(
                    DownloadItem.ItemType.Database,
                    jsonItem.getString("title"), description, address);
            item.setExtras("id", "" + jsonItem.getInt("id"));
            downloadItemList.add(item);
        }

        return downloadItemList;
    }

    /**
     * Download cardsets list from Quizlet and save to a db file
     * 
     * @param setId
     *            cardset ID
     * @param authToken
     *            oauth token
     * @return The path of saved db file
     * @throws IOException
     *             IOException If http response code is not 2xx
     * @throws JSONException
     *             If the response is invalid JSON
     */
    public String downloadCardset(String setId, String authToken)
            throws IOException, JSONException {
        URL url;

        // Public cardset download needs cilent id, private cardset download
        // needs authtoken
        if (authToken != null) {
            url = new URL(AMEnv.QUIZLET_API_ENDPOINT + "/sets/" + setId);
        } else {
            String urlString = String.format(AMEnv.QUIZLET_API_ENDPOINT
                    + "/sets/" + "%1$s?client_id=%2$s",
                    URLEncoder.encode(setId, "UTF-8"),
                    URLEncoder.encode(AMEnv.QUIZLET_CLIENT_ID, "UTF-8"));
            url = new URL(urlString);
        }

        String response = makeApiCall(url, authToken);

        JSONObject rootObject = new JSONObject(response);
        JSONArray flashcardsArray = rootObject.getJSONArray("terms");
        int termCount = rootObject.getInt("term_count");
        boolean hasImage = rootObject.getBoolean("has_images");
        List<Card> cardList = new ArrayList<Card>(termCount);

        // handle image
        String dbname = downloaderUtils.validateDBName(rootObject
                .getString("title")) + ".db";
        String imagePath = AMEnv.DEFAULT_IMAGE_PATH + dbname + "/";
        if (hasImage) {
            FileUtils.forceMkdir(new File(imagePath));
        }

        for (int i = 0; i < flashcardsArray.length(); i++) {
            JSONObject jsonItem = flashcardsArray.getJSONObject(i);
            String question = jsonItem.getString("term");
            String answer = jsonItem.getString("definition");

            // Download images, ignore image downloading error.
            try {
                if (jsonItem.has("image") && !jsonItem.isNull("image")
                        && hasImage) {
                    JSONObject imageItem = jsonItem.getJSONObject("image");
                    String imageUrl = imageItem.getString("url");
                    String downloadFilename = Uri.parse(imageUrl)
                            .getLastPathSegment();
                    downloaderUtils.downloadFile(imageUrl, imagePath
                            + downloadFilename);
                    answer += "<br /><img src=\"" + downloadFilename + "\"/>";
                }
            } catch (Exception e) {
                Log.e(TAG, "Error downloading image.", e);
            }
            Card card = new Card();
            card.setQuestion(question);
            card.setAnswer(answer);
            card.setCategory(new Category());
            card.setLearningData(new LearningData());
            cardList.add(card);
        }

        /* Make a valid dbname from the title */
        String dbpath = AMEnv.DEFAULT_ROOT_PATH;
        String fullpath = dbpath + dbname;
        amFileUtil.deleteFileWithBackup(fullpath);
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager
                .getHelper(fullpath);
        try {
            CardDao cardDao = helper.getCardDao();
            cardDao.createCards(cardList);
            long count = helper.getCardDao().getTotalCount(null);
            if (count <= 0L) {
                throw new RuntimeException("Downloaded empty db.");
            }
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

        return fullpath;
    }

    /**
     * Make API call to Quizlet server with oauth
     * 
     * @param url
     *            API call endpoint
     * @param authToken
     *            oauth auth token
     * @return Response of API call
     * @throws IOException
     *             If http response code is not 2xx
     */
    private String makeApiCall(URL url, String authToken) throws IOException {
        HttpsURLConnection conn = null;
        try {
            conn = (HttpsURLConnection) url.openConnection();
            if (authToken != null) {
                conn.addRequestProperty("Authorization", "Bearer " + authToken);
            }

            String response = new String(IOUtils.toByteArray(conn
                    .getInputStream()));
            if (conn.getResponseCode() / 100 >= 3) {
                throw new IOException("Response code: "
                        + conn.getResponseCode() + " Response is: " + response);
            }
            return response;
        } finally {
            conn.disconnect();
        }
    }

    // Following functions for public card sets

    /**
     * Search for public card sets from Quizlet by title
     * 
     * @param title
     *            card title
     * @param page
     *            current page
     * @return Search result of card sets list
     * @throws IOException
     *             IOException If http response code is not 2xx
     */
    public List<DownloadItem> getCardListByTitle(String title, int page)
            throws IOException {
        String urlString = String.format(AMEnv.QUIZLET_API_ENDPOINT
                + "/search/sets?client_id=%1$s&q=%2$s&page=%3$d",
                URLEncoder.encode(AMEnv.QUIZLET_CLIENT_ID, "UTF-8"),
                URLEncoder.encode(title, "UTF-8"), page);

        URL url = new URL(urlString);

        String responseString = makeApiCall(url, null);

        try {
            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray setsArray = jsonObject.getJSONArray("sets");
            return parseSetsJSONArray(setsArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Search for public card sets from Quizlet by user name
     * 
     * @param username
     *            card creator user name
     * @param page
     *            current page
     * @return Search result of card sets list
     * @throws IOException
     *             IOException If http response code is not 2xx
     */
    public List<DownloadItem> getCardListByUser(String username, int page)
            throws IOException {
        String urlString = String.format(AMEnv.QUIZLET_API_ENDPOINT
                + "/search/sets?client_id=%1$s&creator=%2$s&page=%3$d",
                URLEncoder.encode(AMEnv.QUIZLET_CLIENT_ID, "UTF-8"),
                URLEncoder.encode(username, "UTF-8"), page);

        URL url = new URL(urlString);

        String responseString = makeApiCall(url, null);

        try {
            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray setsArray = jsonObject.getJSONArray("sets");
            return parseSetsJSONArray(setsArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to parse the response of set list class
     * 
     * @param setsArray
     *            A JSON array of sets
     * @return a list of download items
     */
    private List<DownloadItem> parseSetsJSONArray(JSONArray setsArray)
            throws IOException {
        try {
            List<DownloadItem> itemList = new ArrayList<DownloadItem>(
                    setsArray.length());
            for (int i = 0; i < setsArray.length(); i++) {
                JSONObject jsonItem = setsArray.getJSONObject(i);

                String address = jsonItem.getString("url");
                String description = new StringBuilder().append("<br />")
                        .append(jsonItem.getInt("term_count")).append("<br />")
                        .append(jsonItem.getLong("created_date"))
                        .append("<br />")
                        .append(jsonItem.getString("description"))
                        .append("<br />")
                        .append(jsonItem.getString("created_by")).toString();

                DownloadItem item = new DownloadItem(
                        DownloadItem.ItemType.Database,
                        jsonItem.getString("title"), description, address);
                item.setExtras("id", "" + jsonItem.getInt("id"));
                itemList.add(item);
            }
            return itemList;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}