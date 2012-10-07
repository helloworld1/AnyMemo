package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.mycommons.io.IOUtils;
import org.apache.mycommons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.downloader.FEOauth;
import org.liberty.android.fantastischmemo.downloader.dropbox.DropboxOAuth1AccessCodeRetrievalFragment;
import org.liberty.android.fantastischmemo.downloader.google.GoogleAccountActivity;

import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.app.Fragment.SavedState;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class DropboxAccountActivity extends AMActivity {
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private AccountManager accountManager;
    
    private String OAUTH_ACCESS_TOKEN;
    private String OAUTH_ACCESS_TOKEN_SECRET;
    private String UID;

    public void onCreate(Bundle bundle){
    	super.onCreate(bundle);
    	setContentView(R.layout.spreadsheet_list_screen);
    	settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();

    	String savedDropboxAccessToken = settings.getString("dropbox_auth_token", null);
    	
        // Request new one if nothing saved.
//        if (savedDropboxAccessToken == null) {
            showGetTokenDialog();
//        } else {
//            ValidateAccessTokenAndRunCallbackTask task = new ValidateAccessTokenAndRunCallbackTask();
//            task.execute(savedDropboxAccessToken);
//        }    	
    }
    
    
    private void showGetTokenDialog() {
        DropboxOAuth1AccessCodeRetrievalFragment df = new DropboxOAuth1AccessCodeRetrievalFragment();
        df.setAuthCodeReceiveListener(authCodeReceiveListener);
        df.show(getSupportFragmentManager(), TAG);
//        df.show(((FragmentActivity)this).getSupportFragmentManager(), "GoogleAutoFragment");
    }
    
    private DropboxOAuth1AccessCodeRetrievalFragment.AuthCodeReceiveListener authCodeReceiveListener = 
            new DropboxOAuth1AccessCodeRetrievalFragment.AuthCodeReceiveListener() {
    			public void onAuthCodeReceived(String uid) {
                    GetAccessTokenTask task = new GetAccessTokenTask();
                    task.execute(uid);
    			}
    			public void onAuthCodeError(String error) {
//                    showAuthErrorDialog(error);
    			}
                public void onCancelled() {
                    finish();
                }
     };
     
     
     private class GetAccessTokenTask extends AsyncTask<String, Void, String> {

         private ProgressDialog progressDialog;

 		@Override
         public void onPreExecute() {
             super.onPreExecute();
             progressDialog = new ProgressDialog(DropboxAccountActivity.this);
             progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
             progressDialog.setTitle(getString(R.string.loading_please_wait));
             progressDialog.setMessage(getString(R.string.loading_auth_text));
             progressDialog.setCancelable(false);
             progressDialog.show();
         }

         @Override
         public String doInBackground(String... accessCodes) {
             BufferedReader reader = null;
//             String OAUTH_ACCESS_TOKEN;
//             String OAUTH_ACCESS_TOKEN_SECRET;
//             String UID;
        	 
        	 String uid = accessCodes[0];
        	 String sigMethod = "PLAINTEXT"; //"HMAC-SHA1"; //"PLAINTEXT";
        	 long oauthTimeStamp= System.currentTimeMillis()/1000;
        	 String oauthNonce = "abcde"; //RandomStringUtils.random(6); 
//        	 int oauthNonce = new Random().nextInt(100);
        	 String consumer_key = "q2rclqr44ux8pe7";
        	 String consumer_secret = "bmgikjefor073dh";
        	 OAuthConsumer oauthConsumer = new DefaultOAuthConsumer(consumer_key, consumer_secret);
        	 oauthConsumer.setTokenWithSecret(DropboxUtils.OAUTH_REQUEST_TOKEN, DropboxUtils.OAUTH_REQUEST_TOKEN_SECRET);

             try {
            	 
            	 String payload = String.format("oauth_consumer_key=%s&oauth_token=%s&oauth_signiture_method=%s&oauth_signiture=%s&oauth_timestamp=%s&oauth_nonce=%s",
            			 "q2rclqr44ux8pe7",
                         DropboxUtils.OAUTH_REQUEST_TOKEN,
                         sigMethod,
                         URLEncoder.encode("bmgikjefor073dh&" + DropboxUtils.OAUTH_REQUEST_TOKEN_SECRET, "UTF-8"), 
                         String.valueOf(oauthTimeStamp),
                         URLEncoder.encode(oauthNonce, "UTF-8"));
                         
            	 
            	 
            	 String headerValue = "OAuth oauth_version=\"1.0\", " +
   					  "oauth_signature_method=\"PLAINTEXT\", " +
            			 "oauth_token=\"" + DropboxUtils.OAUTH_REQUEST_TOKEN + "\", " +
                         "oauth_consumer_key=\"q2rclqr44ux8pe7\", "+
						 "oauth_signature=\""+ "bmgikjefor073dh&" + DropboxUtils.OAUTH_REQUEST_TOKEN_SECRET +"\""; // +
                 
                 String url = new String("https://api.dropbox.com/1/oauth/access_token"); //+ URLEncoder.encode(payload, "UTF-8"));
                 HttpClient httpClient = new DefaultHttpClient();
                 
               HttpPost httpPost = new HttpPost(url);
               httpPost.setHeader("Authorization", headerValue);
               HttpResponse response = null;
               response = httpClient.execute(httpPost);
           	HttpEntity entity = response.getEntity();
           	Log.v("xinxin header value***********", headerValue);
//           	Log.d("xinxin payload*****************", payload);
           	Log.d("xinxin*****************", response.getStatusLine().toString());
               InputStream instream = entity.getContent();
             reader = new BufferedReader(new InputStreamReader(instream));
             String result = reader.readLine();
               	 String[] parsedResult = result.split("&");
               	 OAUTH_ACCESS_TOKEN_SECRET=parsedResult[0].split("=")[1];
               	 OAUTH_ACCESS_TOKEN=parsedResult[1].split("=")[1];
               	 UID=parsedResult[2].split("=")[1];
               	 Log.v("xinxin********** oauth token: ", OAUTH_ACCESS_TOKEN);
               	 Log.v("xinxin********** oauth token secret: ", OAUTH_ACCESS_TOKEN_SECRET);
               	 Log.v("xinxin********** oauth UID: ", UID);
                 
               	 return OAUTH_ACCESS_TOKEN;
             } catch (Exception e) {
                 Log.e(TAG, "Error redeeming access token", e);
             }
             return null;
         }
         
         
         
         @Override
         public void onPostExecute(String accessToken){
             progressDialog.dismiss();
             editor.putString("dropbox_auth_token", accessToken);
             editor.commit();
             if (accessToken == null) {
//                 showAuthErrorDialog(null);
                 
             } else {
//                 onAuthenticated(accessToken);
            	new InformationFetchingTask().execute(accessToken);
             }
         }
         
         
         private void onAuthenticated(String accessToken){
             try {
             	
             	String url = "https://api.dropbox.com/1/account/info"; //"https://api.dropbox.com/1/metadata/dropbox/?list=true";
             	String oauth_consumer_key = "q2rclqr44ux8pe7";
             	String appSecret = "bmgikjefor073dh";
             	String headerValue = "OAuth oauth_version=\"1.0\", " +
             						"oauth_signature_method=\"PLAINTEXT\", " +
             						"oauth_consumer_key=\""+ oauth_consumer_key +"\", " +
             						"oauth_token=\""+ OAUTH_ACCESS_TOKEN +"\", " +
             						"oauth_signature=\"" + appSecret +"&" + OAUTH_ACCESS_TOKEN_SECRET + "\"";
             	
             	
             	
             	
             	
                 HttpClient httpClient = new DefaultHttpClient();
                 HttpGet httpGet= new HttpGet(url);
                 httpGet.setHeader("Authorization", headerValue);
                 HttpResponse response = null;
                 response = httpClient.execute(httpGet);
                 HttpEntity entity = response.getEntity();
                 InputStream is = entity.getContent();
                 JSONObject json = new JSONObject(convertStreamToString(is));
                 JSONArray names = json.names();
                 for(int i = 0 ; i  < names.length(); i++){
                 	Log.v("xinxin**** names: ", names.getString(i));
                 }

                 Log.v("xinxin *************done parse json", "done");

             } catch (ClientProtocolException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		} catch (IOException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		} catch (IllegalStateException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		} catch (JSONException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
         }
    }
     
     
    
    
    
    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
     
     private class InformationFetchingTask extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... info) {
			   try {
	             	
	             	String url = "https://api.dropbox.com/1/account/info"; //"https://api.dropbox.com/1/metadata/dropbox/?list=true";
	             	String oauth_consumer_key = "q2rclqr44ux8pe7";
	             	String appSecret = "bmgikjefor073dh";
	             	String headerValue = "OAuth oauth_version=\"1.0\", " +
	             						"oauth_signature_method=\"PLAINTEXT\", " +
	             						"oauth_consumer_key=\""+ oauth_consumer_key +"\", " +
	             						"oauth_token=\""+ OAUTH_ACCESS_TOKEN +"\", " +
	             						"oauth_signature=\"" + appSecret +"&" + OAUTH_ACCESS_TOKEN_SECRET + "\"";
	             	
	             	
	             	
	             	
	             	
	                 HttpClient httpClient = new DefaultHttpClient();
	                 HttpGet httpGet= new HttpGet(url);
	                 httpGet.setHeader("Authorization", headerValue);
	                 HttpResponse response = null;
	                 response = httpClient.execute(httpGet);
	                 HttpEntity entity = response.getEntity();
	                 InputStream is = entity.getContent();
	                 JSONObject json = new JSONObject(convertStreamToString(is));
	                 JSONArray names = json.names();
	                 for(int i = 0 ; i  < names.length(); i++){
	                 	Log.v("xinxin**** names: ", names.getString(i));
	                 }

	                 Log.v("xinxin *************done parse json", "done");

	             } catch (ClientProtocolException e) {
	     			// TODO Auto-generated catch block
	     			e.printStackTrace();
	     		} catch (IOException e) {
	     			// TODO Auto-generated catch block
	     			e.printStackTrace();
	     		} catch (IllegalStateException e) {
	     			// TODO Auto-generated catch block
	     			e.printStackTrace();
	     		} catch (JSONException e) {
	     			// TODO Auto-generated catch block
	     			e.printStackTrace();
	     		}
			return null;
		}
    	 
     }
     
     private class ValidateAccessTokenAndRunCallbackTask extends AsyncTask<String, Void, Boolean> {

         private ProgressDialog progressDialog;

         private String token = null;

 		@Override
         public void onPreExecute() {
             super.onPreExecute();
             progressDialog = new ProgressDialog(DropboxAccountActivity.this);
             progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
             progressDialog.setTitle(getString(R.string.loading_please_wait));
             progressDialog.setMessage(getString(R.string.loading_connect_net));
             progressDialog.setCancelable(false);
             progressDialog.show();
         }

 	   @Override
       public Boolean doInBackground(String... accessCodes) {
           BufferedReader reader = null;
           String UID;
      	 
      	 String uid = accessCodes[0];
      	 String sigMethod = "PLAINTEXT"; //"HMAC-SHA1"; //"PLAINTEXT";
      	 long oauthTimeStamp= System.currentTimeMillis()/1000;
      	 String oauthNonce = "abcde"; //RandomStringUtils.random(6); 
//      	 int oauthNonce = new Random().nextInt(100);
      	 String consumer_key = "q2rclqr44ux8pe7";
      	 String consumer_secret = "bmgikjefor073dh";
      	 OAuthConsumer oauthConsumer = new DefaultOAuthConsumer(consumer_key, consumer_secret);
      	 oauthConsumer.setTokenWithSecret(DropboxUtils.OAUTH_REQUEST_TOKEN, DropboxUtils.OAUTH_REQUEST_TOKEN_SECRET);

           try {
          	 
          	 String payload = String.format("oauth_consumer_key=%s&oauth_token=%s&oauth_signiture_method=%s&oauth_signiture=%s&oauth_timestamp=%s&oauth_nonce=%s",
          			 "q2rclqr44ux8pe7",
                       DropboxUtils.OAUTH_REQUEST_TOKEN,
                       sigMethod,
                       URLEncoder.encode("bmgikjefor073dh&" + DropboxUtils.OAUTH_REQUEST_TOKEN_SECRET, "UTF-8"), 
                       String.valueOf(oauthTimeStamp),
                       URLEncoder.encode(oauthNonce, "UTF-8"));
                       
          	 
          	 
          	 String headerValue = "OAuth oauth_version=\"1.0\", " +
 					  "oauth_signature_method=\"PLAINTEXT\", " +
          			 "oauth_token=\"" + DropboxUtils.OAUTH_REQUEST_TOKEN + "\", " +
                       "oauth_consumer_key=\"q2rclqr44ux8pe7\", "+
						 "oauth_signature=\""+ "bmgikjefor073dh&" + DropboxUtils.OAUTH_REQUEST_TOKEN_SECRET +"\""; // +
               
               String url = new String("https://api.dropbox.com/1/oauth/access_token"); //+ URLEncoder.encode(payload, "UTF-8"));
               HttpClient httpClient = new DefaultHttpClient();
               
             HttpPost httpPost = new HttpPost(url);
             httpPost.setHeader("Authorization", headerValue);
             HttpResponse response = null;
             response = httpClient.execute(httpPost);
         	HttpEntity entity = response.getEntity();
         	Log.d("xinxin payload*****************", payload);
         	Log.d("xinxin*****************", response.getStatusLine().toString());
             InputStream instream = entity.getContent();
           reader = new BufferedReader(new InputStreamReader(instream));
           String result = reader.readLine();
             	 String[] parsedResult = result.split("&");
             	 OAUTH_ACCESS_TOKEN_SECRET=parsedResult[0].split("=")[1];
             	 OAUTH_ACCESS_TOKEN=parsedResult[1].split("=")[1];
             	 UID=parsedResult[2].split("=")[1];
             	 Log.v("xinxin********** oauth token: ", OAUTH_ACCESS_TOKEN);
             	 Log.v("xinxin********** oauth token secret: ", OAUTH_ACCESS_TOKEN_SECRET);
             	 Log.v("xinxin********** oauth UID: ", UID);
               
           } catch (Exception e) {
               Log.e(TAG, "Error redeeming access token", e);
           }
           return null;
       }

         
//         @Override
//         public void onPostExecute(Boolean isTokenValid){
//             if (isTokenValid) {
//                 onAuthenticated(token);
//             } else {
//                 invalidateSavedToken();
//                 showGetTokenDialog();
//             }
//             progressDialog.dismiss();
//         }
     }

}
