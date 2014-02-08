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

public class CramSearchPublicCardSetActivity extends AMActivity {
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.cram_cardset_list_layout);
        showSearchTitleDialog();
    }

    private void showSearchTitleDialog(){
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor= settings.edit();
        final EditText et = new EditText(this);
        et.setText(settings.getString(AMPrefKeys.FE_SAVED_SEARCH_KEY, ""));
        new AlertDialog.Builder(this)
            .setTitle(R.string.search_by_title_text)
            .setMessage(R.string.search_by_title_message)
            .setView(et)
            .setPositiveButton(R.string.search_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    String title = et.getText().toString();
                    editor.putString(AMPrefKeys.FE_SAVED_SEARCH_KEY, title);
                    editor.commit();
                    displaySearchTitleFragment(title);
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

    private void displaySearchTitleFragment(String title) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment newFragment = new CramCardSetListFragment();
        Bundle args = new Bundle();
        args.putString(CramCardSetListFragment.EXTRA_AUTH_TOKEN, null);
        args.putString(CramCardSetListFragment.EXTRA_SEARCH_TERM, title);
        args.putString(CramCardSetListFragment.EXTRA_SEARCH_METHOD, CramCardSetListFragment.SearchMethod.ByTitle.toString());
        newFragment.setArguments(args);
        ft.add(R.id.cardset_list, newFragment);
        ft.commit();
    }

}
