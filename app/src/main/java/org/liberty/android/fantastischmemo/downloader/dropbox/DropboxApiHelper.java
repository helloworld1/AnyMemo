package org.liberty.android.fantastischmemo.downloader.dropbox;

import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.AMApplication;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.downloader.DownloadItem;
import org.liberty.android.fantastischmemo.downloader.dropbox.entity.UserInfo;
import org.liberty.android.fantastischmemo.modules.AppComponents;
import org.liberty.android.fantastischmemo.modules.ForApplication;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

@ForApplication
public class DropboxApiHelper {
    private static final String USER_INFO_ENDPOINT = "https://api.dropboxapi.com/2/users/get_current_account";
    private static final String CREATE_FOLDER_ENDPOINT = "https://api.dropboxapi.com/2/files/create_folder";
    private static final String LIST_FOLDER_ENDPOINT = "https://api.dropboxapi.com/2/files/list_folder";
    private static final String CONTINUE_LIST_FOLDER_ENDPOINT = "https://api.dropboxapi.com/2/files/list_folder/continue";

    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final AMFileUtil amFileUtil;

    private final OkHttpClient okHttpClient;

    private final AMApplication application;

    @Inject
    public DropboxApiHelper(@NonNull AMApplication application,
                            @NonNull AMFileUtil amFileUtil,
                            @NonNull OkHttpClient okHttpClient) {
        this.amFileUtil = amFileUtil;
        this.okHttpClient = okHttpClient;
        this.application = application;
    }

    public Observable<UserInfo> getUserInfo(@NonNull final String token) {
        return Observable.create(new ObservableOnSubscribe<UserInfo>() {

            @Override
            public void subscribe(@NonNull final ObservableEmitter<UserInfo> emitter) throws Exception {
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
                            emitter.onNext(userInfo);
                            emitter.onComplete();
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
