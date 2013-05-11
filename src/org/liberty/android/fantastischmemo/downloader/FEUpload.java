/*
Copyright (C) 2010 Haowen Ning

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
package org.liberty.android.fantastischmemo.downloader;

import java.util.List;
import java.net.URLEncoder;
import java.io.IOException;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Card;

import org.liberty.android.fantastischmemo.ui.FileBrowserActivity;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import org.json.JSONObject;

import oauth.signpost.OAuthConsumer;


import oauth.signpost.basic.DefaultOAuthConsumer;

public class FEUpload extends AMActivity{
    private static final int FILE_BROWSER = 6;
    private static final String FE_API_KEY = "anymemo_android";
    private static final String FE_API_ADD_CARDSET= "http://api.flashcardexchange.com/v1/add_card_set" + "?api_key=" + FE_API_KEY;
    private static final String FE_API_ADD_CARD= "http://api.flashcardexchange.com/v1/add_card?api_key=" + FE_API_KEY;
    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.FEUpload";
    private String oauthToken= null;
    private String oauthTokenSecret = null;
    private OAuthConsumer consumer;

    private DownloaderUtils downloaderUtils;

	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        downloaderUtils = new DownloaderUtils(this);
        if(extras != null){
            oauthToken = extras.getString("oauth_token");
            oauthTokenSecret = extras.getString("oauth_token_secret");
            consumer = new DefaultOAuthConsumer(FEOauth.CONSUMER_KEY, FEOauth.CONSUMER_SECRET);
            consumer.setTokenWithSecret(oauthToken, oauthTokenSecret);
            Intent myIntent = new Intent(this, FileBrowserActivity.class);
            myIntent.putExtra(FileBrowserActivity.EXTRA_DEFAULT_ROOT, AMEnv.DEFAULT_ROOT_PATH);
            myIntent.putExtra(FileBrowserActivity.EXTRA_FILE_EXTENSIONS, ".db");
            startActivityForResult(myIntent, FILE_BROWSER);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "Result: " + requestCode + " " + resultCode + " " + data);
        if(resultCode == Activity.RESULT_OK){
            switch(requestCode){
                case FILE_BROWSER:
                {
                    Bundle resultExtras = data.getExtras();
                    if(resultExtras != null){
                        final String dbPath = resultExtras.getString(FileBrowserActivity.EXTRA_RESULT_PATH);
                        AMGUIUtility.doProgressTask(this, R.string.loading_please_wait, R.string.upload_wait, new AMGUIUtility.ProgressTask(){
                            private String authUrl;
                            public void doHeavyTask() throws Exception{
                                uploadDB(dbPath);
                            }
                            public void doUITask(){
                                new AlertDialog.Builder(FEUpload.this)
                                    .setTitle(R.string.upload_finish)
                                    .setMessage(dbPath + " " + getString(R.string.upload_finish_message))
                                    .setPositiveButton(R.string.ok_text, AMGUIUtility.getDialogFinishListener(FEUpload.this))
                                    .create()
                                    .show();
                            }
                        });
                    }
                    break;
                }

            }
        }
    }

    private void uploadDB(String dbPath) throws Exception{
        final String dbName = AMFileUtil.getFilenameFromPath(dbPath);
        int cardId = addCardSet(dbName, "Import from AnyMemo");
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(this, dbPath);
        CardDao cardDao = helper.getCardDao();
        List<Card> cards = cardDao.queryForAll();

        for (Card card : cards) {
            String question = card.getQuestion();
            String answer = card.getAnswer();
            addCard(cardId, question, answer);

        }
    }

    private int addCardSet(String title, String description) throws Exception{
        String urlTitle = URLEncoder.encode(title);
        String urlDescription = URLEncoder.encode(description);
        String url = FE_API_ADD_CARDSET + "&title="+ urlTitle + "&tags=" + urlTitle + "&description=" + urlDescription + "&private=false&oauth_token_secret=" + oauthTokenSecret+ "&oauth_token=" + oauthToken;
        url = consumer.sign(url);
        String jsonString = downloaderUtils.downloadJSONString(url);
        Log.v(TAG, "Request url: " + url);
        Log.v(TAG, jsonString);
        JSONObject rootObject = new JSONObject(jsonString);
        String status = rootObject.getString("response_type");
        if(!status.equals("ok")){
            throw new IOException("Status is not OK. Status: " + status);
        }
        JSONObject resultObject=  rootObject.getJSONObject("results");
        int cardSetId = resultObject.getInt("card_set_id");
        return cardSetId;
    }

    private void addCard(int cardSetId, String question, String answer) throws Exception{
        String url = FE_API_ADD_CARD + "&card_set_id=" + cardSetId + "&question=" + URLEncoder.encode(question) + "&answer=" + URLEncoder.encode(answer) + "&oauth_token_secret=" + oauthTokenSecret + "&oauth_token=" + oauthToken;
        Log.v(TAG, "Request url_old: " + url);
        url = consumer.sign(url);
        Log.v(TAG, "Request url_signed: " + url);

        String jsonString = downloaderUtils.downloadJSONString(url);
        JSONObject rootObject = new JSONObject(jsonString);
        String status = rootObject.getString("response_type");
        if(!status.equals("ok")){
            Log.v(TAG, jsonString);
            throw new IOException("Adding card is not OK. Status: " + status);
        }
    }
}
