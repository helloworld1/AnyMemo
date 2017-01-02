package org.liberty.android.fantastischmemo.downloader.dropbox;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.BaseActivity;

public class DropboxListActivity extends BaseActivity {

    public static final String EXTRA_AUTH_TOKEN = "authToken";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dropbox_list_activity);

        Bundle args = getIntent().getExtras();

        Fragment dropboxListFragment = new DropboxListFragment();
        // Passing the Activity args to Fragment.
        dropboxListFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.download_list_fragment, dropboxListFragment)
                .commit();
    }
}
