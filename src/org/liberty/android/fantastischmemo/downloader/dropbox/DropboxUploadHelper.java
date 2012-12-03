package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.myhttp.entity.mime.MultipartEntity;
import org.apache.myhttp.entity.mime.content.FileBody;
import org.apache.myhttp.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.downloader.DownloadItem;
import org.liberty.android.fantastischmemo.downloader.DownloadItem.ItemType;
import org.liberty.android.fantastischmemo.downloader.google.Cells;
import org.liberty.android.fantastischmemo.downloader.google.CellsFactory;
import org.liberty.android.fantastischmemo.downloader.google.Document;
import org.liberty.android.fantastischmemo.downloader.google.DocumentFactory;
import org.liberty.android.fantastischmemo.downloader.google.Folder;
import org.liberty.android.fantastischmemo.downloader.google.FolderFactory;
import org.liberty.android.fantastischmemo.downloader.google.Spreadsheet;
import org.liberty.android.fantastischmemo.downloader.google.SpreadsheetFactory;
import org.liberty.android.fantastischmemo.downloader.google.Worksheet;
import org.liberty.android.fantastischmemo.downloader.google.WorksheetFactory;

import android.content.Context;
import android.util.Log;

public class DropboxUploadHelper {

    
    private Context mContext;

    private final String authToken;
    private final String authTokenSecret;
    
    private final String FILE_UPLOAD_URL="https://api-content.dropbox.com/1/files_put/dropbox/"; //<path>?param=val";

    private static SimpleDateFormat ISO8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public DropboxUploadHelper(Context context, String authToken, String authTokenSecret) {
        this.authToken = authToken;
        this.authTokenSecret = authTokenSecret;
        mContext = context;
    }

    public boolean upload(String fileName, String filePath){
        
        HttpClient httpclient = new DefaultHttpClient();
        
        String headerValue = "OAuth oauth_version=\"1.0\", "
              + "oauth_signature_method=\"PLAINTEXT\", "
              + "oauth_consumer_key=\"" + AMEnv.DROPBOX_CONSUMER_KEY + "\", "
              + "oauth_token=\"" + authToken + "\", "
              + "oauth_signature=\"" + AMEnv.DROPBOX_CONSUMER_SECRET + "&"
              + authTokenSecret + "\"";

        try {
            String url = "https://api-content.dropbox.com/1/files_put/dropbox/" + URLEncoder.encode(fileName, "utf-8"); 
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Authorization", headerValue);

            File f = new File(filePath);
            FileBody bin = new FileBody(f);

            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("file1", bin);
            
            httppost.addHeader("Content-Type", "application/x-sqlite3");

            httppost.setEntity(reqEntity);

            Log.v("xinxin**** ", "executing request " + httppost.getRequestLine());
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();

            Log.v("xinxin**** ", response.getStatusLine().toString());
            if (resEntity != null) {
                    InputStream is = resEntity.getContent();
                  String resultString = DropboxDownloadHelper.convertStreamToString(is);
                  JSONObject jsonResponse = new JSONObject(resultString);
                  if(jsonResponse.getString("modified") != null){
                      return true;
                  }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
        
        return false;
        
    };
    
//    private String getRev(String path){
//        String rev = null;
//        String url = "https://api.dropbox.com/1/metadata/dropbox/" + path;
//        
//        InputStream is = null;
//        try {
//            String headerValue = "OAuth oauth_version=\"1.0\", "
//                    + "oauth_signature_method=\"PLAINTEXT\", "
//                    + "oauth_consumer_key=\"" + AMEnv.DROPBOX_CONSUMER_KEY + "\", "
//                    + "oauth_token=\"" + authToken + "\", "
//                    + "oauth_signature=\"" + AMEnv.DROPBOX_CONSUMER_SECRET + "&"
//                    + authTokenSecret + "\"";
//
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpGet httpGet = new HttpGet(url);
//            httpGet.setHeader("Authorization", headerValue);
//            HttpResponse response = null;
//            response = httpClient.execute(httpGet);
//            HttpEntity entity = response.getEntity();
//            is = entity.getContent();
//            JSONObject jsonResponse = new JSONObject(DropboxDownloadHelper.convertStreamToString(is));
//            JSONArray filesJSON = jsonResponse.getJSONArray("contents");
//            JSONObject entryJSON;
//            List<DownloadItem> spreadsheetList = new ArrayList<DownloadItem>(); 
//            for(int i = 0 ; i < filesJSON.length(); i++){
//                entryJSON = filesJSON.getJSONObject(i);
//                if(entryJSON.getString("path").endsWith(".db")){
//                    spreadsheetList.add(new DownloadItem(ItemType.Spreadsheet, entryJSON.getString("path"), entryJSON.getString("modified"),  ""));
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if(is != null){
//                    is.close();
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            
//        }
//        return rev;
//    }
    
    public Spreadsheet createSpreadsheet(String title, String dbPath) throws Exception {


        // First read card because if it failed we don't even bother uploading.
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mContext, dbPath);
        List<Card> cardList = null;
        try {
            final CardDao cardDao = helper.getCardDao();
            final CategoryDao categoryDao = helper.getCategoryDao();
            final LearningDataDao learningDataDao = helper.getLearningDataDao();
            cardList = cardDao.callBatchTasks(new Callable<List<Card>>() {
                public List<Card> call() throws Exception {
                    List<Card> cards = cardDao.queryForAll();
                    for (Card c: cards) {
                        categoryDao.refresh(c.getCategory());
                        learningDataDao.refresh(c.getLearningData());
                    }
                    return cards;
                }
            });
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

        // Find the spreadsheets to delete after the process is done
        List<Document> spreadsheetsToDelete = DocumentFactory.findDocuments(title, authToken);



        // Create the AnyMemo folder if needed
        Folder folder = FolderFactory.createOrReturnFolder("AnyMemo", authToken);

        // Create new spreadsheet
        Document newSpreadsheetDocument = DocumentFactory.createSpreadsheet(title, authToken);
        List<Spreadsheet> spreadsheetList = SpreadsheetFactory.getSpreadsheets(authToken);
        Spreadsheet newSpreadsheet = spreadsheetList.get(0);

        // Create worksheets
        List<Worksheet> worksheetsToDelete = WorksheetFactory.getWorksheets(newSpreadsheet, authToken);

        // setting up the worksheet size is critical.
        Worksheet cardsWorksheet = WorksheetFactory.createWorksheet(newSpreadsheet, "cards", cardList.size() + 1, 4, authToken);

        Cells cardCells = new Cells();

        // Add the header for cards first
        cardCells.addCell(1, 1, "question");
        cardCells.addCell(1, 2, "answer");
        cardCells.addCell(1, 3, "category");
        cardCells.addCell(1, 4, "note");
        for (int i = 0; i < cardList.size(); i++) {
            Card c = cardList.get(i);
            // THe first row is the header.
            cardCells.addCell(i + 2, 1, c.getQuestion());
            cardCells.addCell(i + 2, 2, c.getAnswer());
            cardCells.addCell(i + 2, 3, c.getCategory().getName());
            cardCells.addCell(i + 2, 4, c.getNote());
        }

        // upload card's rows into worksheet
        CellsFactory.uploadCells(newSpreadsheet, cardsWorksheet, cardCells, authToken);

        // Let GC free up memory
        cardCells = null;

        // Now deal with learning data
        Worksheet learningDataWorksheet =
            WorksheetFactory.createWorksheet(newSpreadsheet, "learning_data", cardList.size() + 1, 9, authToken);
        Cells learningDataCells = new Cells();

        // The first row is the header.
        learningDataCells.addCell(1, 1, "acqReps");
        learningDataCells.addCell(1, 2, "acqRepsSinceLapse");
        learningDataCells.addCell(1, 3, "easiness");
        learningDataCells.addCell(1, 4, "grade");
        learningDataCells.addCell(1, 5, "lapses");
        learningDataCells.addCell(1, 6, "lastLearnDate");
        learningDataCells.addCell(1, 7, "nextLearnDate");
        learningDataCells.addCell(1, 8, "retReps");
        learningDataCells.addCell(1, 9, "retRepsSinceLapse");
        for (int i = 0; i < cardList.size(); i++) {
            LearningData ld = cardList.get(i).getLearningData();
            learningDataCells.addCell(i + 2, 1, Integer.toString(ld.getAcqReps()));
            learningDataCells.addCell(i + 2, 2, Integer.toString(ld.getAcqRepsSinceLapse()));
            learningDataCells.addCell(i + 2, 3, Float.toString(ld.getEasiness()));
            learningDataCells.addCell(i + 2, 4, Integer.toString(ld.getGrade()));
            learningDataCells.addCell(i + 2, 5, Integer.toString(ld.getLapses()));
            learningDataCells.addCell(i + 2, 6, ISO8601_FORMATTER.format(ld.getLastLearnDate()));
            learningDataCells.addCell(i + 2, 7, ISO8601_FORMATTER.format(ld.getNextLearnDate()));
            learningDataCells.addCell(i + 2, 8, Integer.toString(ld.getRetReps()));
            learningDataCells.addCell(i + 2, 9, Integer.toString(ld.getRetRepsSinceLapse()));
        }

        // upload learning data rows into worksheet
        CellsFactory.uploadCells(newSpreadsheet, learningDataWorksheet, learningDataCells, authToken);
        learningDataCells = null;


        // Put new spreadsheet into the folder
        FolderFactory.addDocumentToFolder(newSpreadsheetDocument, folder, authToken);

        // Finally delete the unneeded worksheets ...
        for (Worksheet ws : worksheetsToDelete) {
            WorksheetFactory.deleteWorksheet(newSpreadsheet, ws, authToken);
        }
        // ... And spreadsheets with duplicated names.
        for (Document ss : spreadsheetsToDelete) {
            DocumentFactory.deleteDocument(ss, authToken);
        }
        return null;
    }
}
