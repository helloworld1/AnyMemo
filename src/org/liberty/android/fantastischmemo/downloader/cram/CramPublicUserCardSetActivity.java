/*
Copyright (C) 2014 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.downloader.cram;

import org.liberty.android.fantastischmemo.AMActivity;
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

public class CramPublicUserCardSetActivity extends AMActivity {
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.cram_cardset_list_layout);
        showUserInputDialog();
    }

    private void showUserInputDialog() {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor= settings.edit();
        final EditText et = new EditText(this);
        et.setText(settings.getString(AMPrefKeys.FE_SAVED_USER_KEY, ""));
        new AlertDialog.Builder(this)
            .setTitle(R.string.search_tag)
            .setMessage(R.string.fe_search_user_message)
            .setView(et)
            .setPositiveButton(R.string.search_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    String userName = et.getText().toString();
                    editor.putString(AMPrefKeys.FE_SAVED_USER_KEY, userName);
                    editor.commit();
                    displayFragment(userName);
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface arg0) {
                    finish();
                }
            })
            .create()
            .show();
    }

    private void displayFragment(String userName) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment newFragment = new CramUserCardSetListFragment();
        Bundle args = new Bundle();
        args.putString(CramUserCardSetListFragment.EXTRA_AUTH_TOKEN, null);
        args.putString(CramUserCardSetListFragment.EXTRA_USER_NAME, userName);
        newFragment.setArguments(args);
        ft.add(R.id.cardset_list, newFragment);
        ft.commit();
    }

}
