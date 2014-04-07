package org.liberty.android.fantastischmemo.downloader.quizlet;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.liberty.android.fantastischmemo.downloader.AbstractDownloaderFragment;
import org.liberty.android.fantastischmemo.downloader.DownloadItem;

import android.os.Bundle;

public class CardsetsListFragment extends AbstractDownloaderFragment {

    public static final String EXTRA_AUTH_TOKEN = "authToken";

    public static final String EXTRA_USER_ID = "userId";

    // Following two items for public search
    public static final String EXTRA_SEARCH_TERM = "searchTerm";

    public static final String EXTRA_SEARCH_METHOD = "searchMethod";

    private String authToken;

    private String userId;

    private String searchTerm = null;

    private SearchMethod searchMethod;

    private QuizletDownloadHelper quizletDownloadHelper;

    /**
     * Keep track of the next page to load.
     */
    private int nextPage = 1;

    private boolean isLastPage = true;

    @Inject
    public void setQuizletDownloadHelper(
            QuizletDownloadHelper quizletDownloadHelper) {
        this.quizletDownloadHelper = quizletDownloadHelper;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle args = getArguments();

        // authToken and userId can be empty to indicate accessing public cards
        // only
        this.authToken = args.getString(EXTRA_AUTH_TOKEN);
        this.userId = args.getString(EXTRA_USER_ID);

        this.searchTerm = args.getString(EXTRA_SEARCH_TERM);
        this.searchMethod = SearchMethod.valueOf(args
                .getString(EXTRA_SEARCH_METHOD));

        assert StringUtils.isNotEmpty(searchTerm) : "Search term should not be empty";
    }

    @Override
    protected List<DownloadItem> initialRetrieve() throws Exception {
        return loadMore();
    }

    @Override
    protected String fetchDatabase(DownloadItem di) throws Exception {
        String setId = di.getExtras("id");
        return quizletDownloadHelper.downloadCardset(setId, authToken);
    }

    @Override
    protected void openCategory(DownloadItem di) {
        // Do nothing
    }

    @Override
    protected void goBack() {
        // Do nothing
    }

    @Override
    protected List<DownloadItem> loadMore() throws Exception {
        List<DownloadItem> result;

        if (this.searchMethod == SearchMethod.ByUserPrivate) {
            if (authToken == null) {
                throw new IllegalArgumentException(
                        "Search private card without authToken");
            }
            // Quzilet does not return private cards with pages
            return quizletDownloadHelper.getUserPrivateCardsetsList(userId,
                    authToken);
        } else if (this.searchMethod == SearchMethod.ByUserName) {
            result = quizletDownloadHelper.getCardListByUser(searchTerm,
                    nextPage);
        } else if (this.searchMethod == SearchMethod.ByTitle) {
            result = quizletDownloadHelper.getCardListByTitle(searchTerm,
                    nextPage);
        } else {
            throw new IllegalArgumentException(
                    "initialRetrieve does not know how to handle search method: "
                            + this.searchMethod);
        }

        // Keep track if this is the last page
        if (result.size() != 0) {
            nextPage++;
            isLastPage = false;
        } else {
            isLastPage = true;
        }
        return result;
    }

    @Override
    protected boolean hasMore() {
        if (authToken != null) {
            return false;
        } else {
            return !isLastPage;
        }
    }

    public enum SearchMethod {
        ByTitle, ByUserName, ByUserPrivate,
    }
}