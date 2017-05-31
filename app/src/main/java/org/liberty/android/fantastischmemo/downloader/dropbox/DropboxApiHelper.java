package org.liberty.android.fantastischmemo.downloader.dropbox;

import android.support.annotation.NonNull;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMApplication;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.downloader.common.DownloadItem;
import org.liberty.android.fantastischmemo.downloader.dropbox.entity.UserInfo;
import org.liberty.android.fantastischmemo.modules.ForApplication;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@ForApplication
public class DropboxApiHelper {
    private static final String USER_INFO_ENDPOINT = "https://api.dropboxapi.com/2/users/get_current_account";
    private static final String CREATE_FOLDER_ENDPOINT = "https://api.dropboxapi.com/2/files/create_folder";
    private static final String LIST_FOLDER_ENDPOINT = "https://api.dropboxapi.com/2/files/list_folder";
    private static final String CONTINUE_LIST_FOLDER_ENDPOINT = "https://api.dropboxapi.com/2/files/list_folder/continue";
    private static final String TEMPORARY_LINK_ENDPOINT = "https://api.dropboxapi.com/2/files/get_temporary_link";
    private static final String UPLOAD_ENDPOINT = "https://content.dropboxapi.com/2/files/upload";

    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType OCTET_STREAM_TYPE = MediaType.parse("application/octet-stream");

    private final AMFileUtil amFileUtil;

    private final OkHttpClient okHttpClient;

    private final AMApplication application;

    private final RecentListUtil recentListUtil;

    @Inject
    public DropboxApiHelper(@NonNull AMApplication application,
                            @NonNull AMFileUtil amFileUtil,
                            @NonNull OkHttpClient okHttpClient,
                            @NonNull RecentListUtil recentListUtil) {
        this.amFileUtil = amFileUtil;
        this.okHttpClient = okHttpClient;
        this.application = application;
        this.recentListUtil = recentListUtil;
    }

    public Single<UserInfo> getUserInfo(@NonNull final String token) {
        return Single.create(new SingleOnSubscribe<UserInfo>() {
            @Override
            public void subscribe(@NonNull final SingleEmitter<UserInfo> emitter) throws Exception {
                RequestBody requestBody = RequestBody.create(null, new byte[0]);
                Request request = new Request.Builder()
                        .url(USER_INFO_ENDPOINT)
                        .addHeader("Authorization", "Bearer " + token)
                        .post(requestBody)
                        .build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        emitter.onError(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            emitter.onError(new IOException(getResponseErrorString(call.request(), response)));
                            return;
                        }
                        UserInfo userInfo = new UserInfo();
                        try {
                            JSONObject userInfoObject = new JSONObject(response.body().string());
                            userInfo.accountId = userInfoObject.getString("account_id");
                            userInfo.email = userInfoObject.getString("email");
                            JSONObject nameObject = userInfoObject.getJSONObject("name");
                            userInfo.displayName = nameObject.getString("display_name");
                            emitter.onSuccess(userInfo);
                        } catch (JSONException e) {
                            emitter.onError(e);
                        }
                    }
                });
            }
        });
    }

    public Completable createFolder(@NonNull final String token, @NonNull final String folderName) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter emitter) throws Exception {
                RequestBody requestBody = RequestBody.create(JSON_TYPE, String.format("{\"path\": \"/%1$s\",\"autorename\": false}",
                        folderName));
                Request request = new Request.Builder()
                        .url(CREATE_FOLDER_ENDPOINT)
                        .addHeader("Authorization", "Bearer " + token)
                        .post(requestBody)
                        .build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        emitter.onError(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        emitter.onComplete();
                    }
                });
            }
        });
    }

    public Observable<List<DownloadItem>> listFiles(@NonNull final String token, @NonNull final String folderName) {
        return Observable.create(new ObservableOnSubscribe<List<DownloadItem>>() {
            @Override
            public void subscribe(ObservableEmitter<List<DownloadItem>> emitter) throws Exception {
                RequestBody requestBody = RequestBody.create(JSON_TYPE, String.format(
                        "{\"path\": \"/%1$s\",\"recursive\": false,\"include_media_info\": false,\"include_deleted\": false,\"include_has_explicit_shared_members\": false}",
                        folderName));
                Request request = new Request.Builder()
                        .url(LIST_FOLDER_ENDPOINT)
                        .addHeader("Authorization", "Bearer " + token)
                        .post(requestBody)
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    emitter.onError(new IOException(getResponseErrorString(request, response)));
                    return;
                }

                JSONObject listFolderObject = new JSONObject(response.body().string());
                boolean hasMore = listFolderObject.getBoolean("has_more");
                String cursor = listFolderObject.getString("cursor");
                JSONArray entryArray = listFolderObject.getJSONArray("entries");
                emitter.onNext(parseListFolderResponse(entryArray));

                // Now use continuation token to emit paginated results
                while (hasMore) {
                    RequestBody continueRequestBody = RequestBody.create(JSON_TYPE, String.format(
                            "{\"cursor\": \"%1$s\"}",
                            cursor));
                    Request continueRequest = new Request.Builder()
                            .url(CONTINUE_LIST_FOLDER_ENDPOINT)
                            .addHeader("Authorization", "Bearer " + token)
                            .post(continueRequestBody)
                            .build();
                    Response continueResponse = okHttpClient.newCall(continueRequest).execute();
                    if (!continueResponse.isSuccessful()) {
                        emitter.onError(new IOException(getResponseErrorString(continueRequest, continueResponse)));
                        return;
                    }

                    JSONObject continueListFolderObject = new JSONObject(continueResponse.body().string());
                    hasMore = continueListFolderObject.getBoolean("has_more");
                    cursor = continueListFolderObject.getString("cursor");
                    JSONArray continueEntryArray = continueListFolderObject.getJSONArray("entries");
                    emitter.onNext(parseListFolderResponse(continueEntryArray));
                }
                emitter.onComplete();
            }
        });
    }

    public Single<String> downloadFile(@NonNull final String token, @NonNull final String filePath) {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                RequestBody requestBody = RequestBody.create(JSON_TYPE, String.format(
                        "{\"path\": \"%1$s\"}",
                        filePath));
                Request request = new Request.Builder()
                        .url(TEMPORARY_LINK_ENDPOINT)
                        .addHeader("Authorization", "Bearer " + token)
                        .post(requestBody)
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException(getResponseErrorString(request, response));
                }

                JSONObject temporaryLinkObject = new JSONObject(response.body().string());
                String downloadLink = temporaryLinkObject.getString("link");
                JSONObject metadataObject = temporaryLinkObject.getJSONObject("metadata");
                String fileName = metadataObject.getString("name");

                Request downloadRequest = new Request.Builder()
                        .url(downloadLink)
                        .get()
                        .build();
                Response downloadResponse = okHttpClient.newCall(downloadRequest).execute();
                InputStream inputStream = downloadResponse.body().byteStream();

                File outputFile = new File(AMEnv.DEFAULT_ROOT_PATH + fileName);

                FileUtils.copyInputStreamToFile(inputStream, outputFile);
                recentListUtil.addToRecentList(outputFile.getAbsolutePath());

                return outputFile.getAbsolutePath();
            }
        });
    }

    public Completable uploadDropbox(@NonNull final String token, @NonNull final File fileToUpload, @NonNull final String uploadPath) {
        return Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (!fileToUpload.exists()) {
                    throw new FileNotFoundException("Could not find file: " + fileToUpload.getAbsolutePath());
                }

                RequestBody requestBody = RequestBody.create(OCTET_STREAM_TYPE, fileToUpload);
                Request request = new Request.Builder()
                        .url(UPLOAD_ENDPOINT)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Dropbox-API-Arg", String.format("{\"path\": \"%1$s\",\"mode\": \"add\",\"autorename\": true,\"mute\": false}",
                                uploadPath))
                        .post(requestBody)
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException(getResponseErrorString(request, response));
                }

                return null;
            }
        });
    }

    private String getResponseErrorString(Request request, Response response) throws IOException {
        return String.format(Locale.US, "HTTP request: %1$s, response code: %2$d, response: %3$s",
                request.url(),
                response.code(),
                response.body().string()
        );
    }

    private List<DownloadItem> parseListFolderResponse(@NonNull JSONArray entryArray) throws JSONException {
        List<DownloadItem> downloadItems = new ArrayList<>(entryArray.length());
        for (int i = 0; i < entryArray.length(); i++) {
            JSONObject entryObject = entryArray.getJSONObject(i);
            String tag = entryObject.getString(".tag");

            // We only list files here, folders are ignored
            if (!"file".equals(tag)) {
                continue;
            }
            DownloadItem downloadItem = new DownloadItem();
            downloadItem.setTitle(entryObject.getString("name"));
            downloadItem.setAddress(entryObject.getString("path_display"));
            downloadItem.setDescription(String.format(application.getString(R.string.dropbox_item_description),
                    entryObject.getString("server_modified"),
                    entryObject.getLong("size")));
            downloadItems.add(downloadItem);
        }
        return downloadItems;
    }
}
