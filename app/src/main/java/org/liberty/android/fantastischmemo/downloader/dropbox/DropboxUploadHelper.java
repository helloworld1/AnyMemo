package org.liberty.android.fantastischmemo.downloader.dropbox;

import android.content.Context;

import org.json.JSONException;

import java.io.IOException;

import javax.inject.Inject;

public class DropboxUploadHelper {

    private final String authToken;
    private final String authTokenSecret;

    private static final String FILE_UPLOAD_URL="https://api-content.dropbox.com/1/files_put/dropbox/AnyMemo/";

    @Inject
    public DropboxUploadHelper(Context context, String authToken, String authTokenSecret) {
        this.authToken = authToken;
        this.authTokenSecret = authTokenSecret;
    }

    // Return true if upload succeeded. false if something goes wrong.
    public boolean upload(String fileName, String filePath) throws IOException, JSONException{
        // Todo: Stub
        return false;
    };

}
