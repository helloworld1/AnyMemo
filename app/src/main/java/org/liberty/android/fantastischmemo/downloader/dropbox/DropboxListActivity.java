package org.liberty.android.fantastischmemo.downloader.dropbox;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.databinding.DropboxListActivityBinding;
import org.liberty.android.fantastischmemo.ui.CardFragment;

public class DropboxListActivity extends BaseActivity {

    public static final String EXTRA_AUTH_TOKEN = "authToken";

    private static int OPEN_UPLOAD_DROPBOX_REQUEST_CODE = 1;

    private String authToken;

    private DropboxListActivityBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dropbox_list_activity);
        binding = DataBindingUtil.setContentView(this, R.layout.dropbox_list_activity);

        Bundle args = getIntent().getExtras();
        authToken = args.getString(EXTRA_AUTH_TOKEN);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(binding.toolbar);

        Fragment dropboxListFragment = new DropboxListFragment();
        // Passing the Activity args to Fragment.
        dropboxListFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.download_list_fragment, dropboxListFragment)
                .commit();

        binding.uploadFab.setOnClickListener(new CardFragment.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUploadDropbox();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dropbox_list_menu, menu);
        Drawable drawable = DrawableCompat.wrap(menu.findItem(R.id.logout).getIcon()).mutate();
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this,R.color.menu_icon));
        menu.findItem(R.id.logout).setIcon(drawable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            activityComponents().oauth2TokenUtil().invalidateSavedToken();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_UPLOAD_DROPBOX_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            refreshList();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void refreshList() {
        DropboxListFragment fragment = (DropboxListFragment) getSupportFragmentManager().findFragmentById(R.id.download_list_fragment);
        if (fragment != null) {
            fragment.refreshList();
        }

    }

    private void openUploadDropbox() {
        Intent intent = new Intent(this, UploadDropboxActivity.class);
        intent.putExtra(UploadDropboxActivity.EXTRA_AUTH_TOKEN, authToken);
        startActivityForResult(intent, OPEN_UPLOAD_DROPBOX_REQUEST_CODE);
    }
}
