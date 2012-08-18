/*
Copyright (C) 2012 Haowen Ning

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
package org.liberty.android.fantastischmemo.ui;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.utils.AMUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OpenActionsFragment extends DialogFragment {
    public static String EXTRA_DBPATH = "dbpath";
    private AMActivity mActivity;
    private String dbPath;
    private View studyItem;
    private View editItem;
    private View listItem;
    private View cramItem;
    private View settingsItem;
    private View statisticsItem;
    private View deleteItem;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity)activity;
    }
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle args = this.getArguments();
        dbPath = args.getString(EXTRA_DBPATH);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        View v = inflater.inflate(R.layout.open_actions_layout, container, false);
        studyItem = v.findViewById(R.id.study);
        studyItem.setOnClickListener(buttonClickListener);

        editItem = v.findViewById(R.id.edit);
        editItem.setOnClickListener(buttonClickListener);

        listItem = v.findViewById(R.id.list);
        listItem.setOnClickListener(buttonClickListener);

        cramItem = v.findViewById(R.id.cram);
        cramItem.setOnClickListener(buttonClickListener);

        settingsItem = v.findViewById(R.id.settings);
        settingsItem.setOnClickListener(buttonClickListener);

        deleteItem = v.findViewById(R.id.delete);
        deleteItem.setOnClickListener(buttonClickListener);

        statisticsItem = v.findViewById(R.id.statistics);
        statisticsItem.setOnClickListener(buttonClickListener);
        return v;
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	final RecentListUtil rlu = new RecentListUtil(mActivity);
            if (v == studyItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, MemoScreen.class);
                myIntent.putExtra(MemoScreen.EXTRA_DBPATH, dbPath);
                startActivity(myIntent);
                rlu.addToRecentList(dbPath);
            }

            if (v == editItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, EditScreen.class);
                myIntent.putExtra(EditScreen.EXTRA_DBPATH, dbPath);
                startActivity(myIntent);
                rlu.addToRecentList(dbPath);
            }

            if (v == listItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, ListEditScreen.class);
                myIntent.putExtra(MemoScreen.EXTRA_DBPATH, dbPath);
                startActivity(myIntent);
                rlu.addToRecentList(dbPath);
            }

            if (v == cramItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, MemoScreen.class);
                myIntent.putExtra(MemoScreen.EXTRA_DBPATH, dbPath);
                myIntent.putExtra(MemoScreen.EXTRA_CRAM, true);
                startActivity(myIntent);
                rlu.addToRecentList(dbPath);
            }

            if (v == settingsItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, SettingsScreen.class);
                myIntent.putExtra(SettingsScreen.EXTRA_DBPATH, dbPath);
                startActivity(myIntent);
            }

            if (v == statisticsItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, StatisticsScreen.class);
                myIntent.putExtra(SettingsScreen.EXTRA_DBPATH, dbPath);
                startActivity(myIntent);
            }
            if (v == deleteItem) {
                new AlertDialog.Builder(mActivity)
                    .setTitle(getString(R.string.delete_text))
                    .setMessage(getString(R.string.fb_delete_message))
                    .setPositiveButton(getString(R.string.delete_text), new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which ){
                            AMUtil.deleteDbSafe(dbPath);
                            rlu.deleteFromRecentList(dbPath);
                            /* Refresh the list */
                            mActivity.restartActivity();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel_text), null)
                    .create()
                    .show();
            }
            dismiss();
        }
    };
}

