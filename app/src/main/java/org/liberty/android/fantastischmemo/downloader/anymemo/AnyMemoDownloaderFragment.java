package org.liberty.android.fantastischmemo.downloader.anymemo;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.downloader.common.AbstractDownloaderFragment;
import org.liberty.android.fantastischmemo.downloader.common.DownloadItem;
import org.liberty.android.fantastischmemo.downloader.common.DownloaderUtils;
import org.liberty.android.fantastischmemo.utils.AMZipUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

public class AnyMemoDownloaderFragment extends AbstractDownloaderFragment {

    private static final String LIST_DATABASES_PATH = "/api/legacy_database/list";

    private static final String DOWNLOAD_DATABASE_PATH = "/api/legacy_database/download";

    private Map<String, List<DownloadItem>> categoryDatabasesMap;

    @Inject DownloaderUtils downloaderUtils;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentComponents().inject(this);
    }

    @Override
    protected List<DownloadItem> initialRetrieve() throws Exception {
        retrieveAllDatabases();

        return getCategories();
    }

    @Override
    protected List<DownloadItem> loadMore() throws Exception {
        return null;
    }

    @Override
    protected boolean hasMore() {
        return false;
    }

    @Override
    protected List<DownloadItem> openCategory(DownloadItem di) {
        String category = di.getTitle();

        List<DownloadItem> items = categoryDatabasesMap.get(category);
        List<DownloadItem> result = new ArrayList<>(items.size() + 1);
        result.add(new DownloadItem(DownloadItem.ItemType.Back, "..","", ""));
        result.addAll(items);
        return result;
    }

    @Override
    protected List<DownloadItem> goBack() {
        return getCategories();
    }

    @Override
    protected String fetchDatabase(DownloadItem di) throws Exception {
        String address = di.getAddress();
        String fileName = downloaderUtils.getLastPartFromUrl(address);

        if (fileName.toLowerCase().endsWith(".db")) {
            String filePath = AMEnv.DEFAULT_ROOT_PATH + fileName;
            downloaderUtils.downloadFile(address, filePath);
            return filePath;
        } else if (fileName.toLowerCase().endsWith(".zip")) {
            String zipFilePath = AMEnv.DEFAULT_TMP_PATH + fileName;
            downloaderUtils.downloadFile(address, zipFilePath);

            AMZipUtils.unZipFile(new File(zipFilePath), new File(AMEnv.DEFAULT_ROOT_PATH));

            return AMEnv.DEFAULT_ROOT_PATH + fileName.replace(".zip", ".db");
        }

        throw new IllegalStateException("Downloading file: " + fileName + " that is neither .db or .zip");
    }

    private void retrieveAllDatabases() throws Exception {
        categoryDatabasesMap = new HashMap<>();
        String jsonString = downloaderUtils.downloadJSONString(String.format("%s%s",
                AMEnv.ANYMEMO_SERVICE_ENDPOINT,
                LIST_DATABASES_PATH));

        JSONArray jsonArray = new JSONArray(jsonString);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            DownloadItem downloadItem = new DownloadItem();
            downloadItem.setType(DownloadItem.ItemType.Database);
            downloadItem.setTitle(jsonObject.getString("title"));
            downloadItem.setDescription(jsonObject.getString("description"));
            downloadItem.setAddress(String.format("%s%s/%s",
                    AMEnv.ANYMEMO_SERVICE_ENDPOINT,
                    DOWNLOAD_DATABASE_PATH,
                    jsonObject.get("name")));

            String category = jsonObject.getString("category");

            if (!categoryDatabasesMap.containsKey(category)) {
                categoryDatabasesMap.put(category, new ArrayList<DownloadItem>(30));
            }
            categoryDatabasesMap.get(category).add(downloadItem);
        }

        // Need to sort all the download items by name
        for (List<DownloadItem> downloadItemList : categoryDatabasesMap.values()) {
            Collections.sort(downloadItemList, new Comparator<DownloadItem>() {
                @Override
                public int compare(DownloadItem lhs, DownloadItem rhs) {
                    return lhs.getTitle().compareTo(rhs.getTitle());
                }
            });
        }
    }

    private List<DownloadItem> getCategories() {
        Set<String> categoryNames = categoryDatabasesMap.keySet();

        List<DownloadItem> resultItems = new ArrayList<>(50);
        for (String categoryName : categoryNames) {
            DownloadItem item = new DownloadItem(DownloadItem.ItemType.Category, categoryName, "", "");
            resultItems.add(item);
        }

        Collections.sort(resultItems, new Comparator<DownloadItem>() {
            @Override
            public int compare(DownloadItem lhs, DownloadItem rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });

        return resultItems;
    }
}
