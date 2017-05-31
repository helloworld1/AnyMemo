package org.liberty.android.fantastischmemo.downloader.quizlet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.EditText;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMPrefKeys;
import org.liberty.android.fantastischmemo.common.BaseActivity;

/**
 * Input title to search and display card set list.
 */
public class QuizletSearchByTitleActivity extends BaseActivity {
    private SharedPreferences settings;

    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.cardsets_list_screen);
        showSearchTagDialog();
    }

    private void showSearchTagDialog() {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = settings.edit();
        final EditText et = new EditText(this);
        et.setText(settings.getString(AMPrefKeys.QUIZLET_SAVED_SEARCH, ""));
        new AlertDialog.Builder(this)
            .setTitle(R.string.search_tag)
            .setMessage(R.string.quizlet_search_tag_message)
            .setView(et)
            .setPositiveButton(R.string.search_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,
                    int which) {
                    String title = et.getText().toString();
                    editor.putString(AMPrefKeys.QUIZLET_SAVED_SEARCH, title);
                    editor.commit();
                    displaySearchTagFragment(title);
                }
            })
            .setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,
                    int which) {
                    finish();
                }
            })
            .setCancelable(false)
            .show();
    }

    private void displaySearchTagFragment(String title) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment newFragment = new CardsetsListFragment();
        Bundle args = new Bundle();
        args.putString(CardsetsListFragment.EXTRA_AUTH_TOKEN, null);
        args.putString(CardsetsListFragment.EXTRA_USER_ID, null);
        args.putString(CardsetsListFragment.EXTRA_SEARCH_TERM, title);
        args.putString(CardsetsListFragment.EXTRA_SEARCH_METHOD,
                CardsetsListFragment.SearchMethod.ByTitle.toString());
        newFragment.setArguments(args);
        ft.add(R.id.cardsets_list, newFragment);
        ft.commit();
    }
}
