package org.liberty.android.fantastischmemo.downloader.quizlet;

import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.EditText;

public class QuizletSearchByUsernameActivity extends QuizletAccountActivity {
    private SharedPreferences settings;

    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.cardsets_list_screen);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
        showSearchUserDialog();
    }

    private void showSearchUserDialog() {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = settings.edit();
        final EditText et = new EditText(this);
        et.setText(settings.getString(AMPrefKeys.QUIZLET_SAVED_USER, ""));
        new AlertDialog.Builder(this)
                .setTitle(R.string.search_user)
                .setMessage(R.string.quizlet_search_user_message)
                .setView(et)
                .setPositiveButton(R.string.search_text,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                String username = et.getText().toString();
                                editor.putString(AMPrefKeys.QUIZLET_SAVED_USER,
                                        username);
                                editor.commit();
                                displaySearchUserFragment(username);
                            }
                        }).setNegativeButton(R.string.cancel_text, null)
                .create().show();
    }

    private void displaySearchUserFragment(String username) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment newFragment = new CardsetsListFragment();
        Bundle args = new Bundle();
        args.putString(CardsetsListFragment.EXTRA_AUTH_TOKEN, null);
        args.putString(CardsetsListFragment.EXTRA_USER_ID, null);
        args.putString(CardsetsListFragment.EXTRA_SEARCH_TERM, username);
        args.putString(CardsetsListFragment.EXTRA_SEARCH_METHOD,
                CardsetsListFragment.SearchMethod.ByUserName.toString());
        newFragment.setArguments(args);
        ft.add(R.id.cardsets_list, newFragment);
        ft.commit();
    }
}
