package org.liberty.android.fantastischmemo.downloader.quizlet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.liberty.android.fantastischmemo.R;

public class QuizletUserPrivateActivity extends QuizletAccountActivity {

    private String oauthToken;

    private String userId;

    private final static int UPLOAD_ACTIVITY = 1;

    @Override
    protected void onAuthenticated(final String[] authTokens) {

        oauthToken = authTokens[0];
        userId = authTokens[1];

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment newFragment = new CardsetsListFragment();
        Bundle args = new Bundle();
        args.putString(CardsetsListFragment.EXTRA_AUTH_TOKEN, oauthToken);
        args.putString(CardsetsListFragment.EXTRA_USER_ID, userId);
        args.putString(CardsetsListFragment.EXTRA_SEARCH_TERM, null);
        args.putString(CardsetsListFragment.EXTRA_SEARCH_METHOD,
                CardsetsListFragment.SearchMethod.ByUserPrivate.toString());
        newFragment.setArguments(args);
        ft.add(R.id.cardsets_list, newFragment);
        ft.commit();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.cardsets_list_screen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quizlet_cardsets_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.upload: {
            startActivityForResult(
                    new Intent(this, QuizletUploadActivity.class),
                    UPLOAD_ACTIVITY);
            return true;
        }
        case R.id.logout: {
            invalidateSavedToken();
            // After mark saved token to null, we should exit.
            finish();
            return true;
        }

        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        switch (requestCode) {
        case UPLOAD_ACTIVITY: {
            restartActivity();
            break;
        }
        }
    }
}
