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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMPrefKeys;
import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.common.BaseDialogFragment;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;
import org.liberty.android.fantastischmemo.utils.ShareUtil;

import javax.inject.Inject;

public class OpenActionsFragment extends BaseDialogFragment {
    public static String EXTRA_DBPATH = "dbpath";
    private BaseActivity mActivity;

    private String dbPath;

    private View studyItem;
    private View editItem;
    private View listItem;
    private View quizItem;
    private View cardPlayerItem;
    private View settingsItem;
    private View statisticsItem;
    private View shareItem;
    private View deleteItem;

    @Inject AMFileUtil amFileUtil;

    @Inject RecentListUtil recentListUtil;

    @Inject ShareUtil shareUtil;

    @Inject AMPrefUtil amPrefUtil;


    public OpenActionsFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        fragmentComponents().inject(this);
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

        quizItem = v.findViewById(R.id.quiz);
        quizItem.setOnClickListener(buttonClickListener);

        settingsItem = v.findViewById(R.id.settings);
        settingsItem.setOnClickListener(buttonClickListener);

        deleteItem = v.findViewById(R.id.delete);
        deleteItem.setOnClickListener(buttonClickListener);

        statisticsItem = v.findViewById(R.id.statistics);
        statisticsItem.setOnClickListener(buttonClickListener);

        shareItem = v.findViewById(R.id.share);
        shareItem.setOnClickListener(buttonClickListener);

        cardPlayerItem = v.findViewById(R.id.card_player);
        cardPlayerItem.setOnClickListener(buttonClickListener);

        return v;
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == studyItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, StudyActivity.class);
                myIntent.putExtra(StudyActivity.EXTRA_DBPATH, dbPath);
                startActivity(myIntent);
                recentListUtil.addToRecentList(dbPath);
            }

            if (v == editItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, PreviewEditActivity.class);
                myIntent.putExtra(PreviewEditActivity.EXTRA_DBPATH, dbPath);
                int startId = amPrefUtil.getSavedInt(AMPrefKeys.PREVIEW_EDIT_START_ID_PREFIX, dbPath, 1);
                myIntent.putExtra(PreviewEditActivity.EXTRA_CARD_ID, startId);
                startActivity(myIntent);
                recentListUtil.addToRecentList(dbPath);
            }

            if (v == listItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, CardListActivity.class);
                myIntent.putExtra(StudyActivity.EXTRA_DBPATH, dbPath);
                startActivity(myIntent);
                recentListUtil.addToRecentList(dbPath);
            }

            if (v == quizItem) {
                QuizLauncherDialogFragment df = new QuizLauncherDialogFragment();
                Bundle b = new Bundle();
                b.putString(CategoryEditorFragment.EXTRA_DBPATH, dbPath);
                df.setArguments(b);
                df.show(mActivity.getSupportFragmentManager(), "QuizLauncherDialog");
                recentListUtil.addToRecentList(dbPath);
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

            if (v == shareItem) {
                shareUtil.shareDb(dbPath);
            }

            if (v == deleteItem) {
                new AlertDialog.Builder(mActivity)
                    .setTitle(getString(R.string.delete_text))
                    .setMessage(getString(R.string.fb_delete_message))
                    .setPositiveButton(getString(R.string.delete_text), new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which ){
                            amFileUtil.deleteDbSafe(dbPath);
                            recentListUtil.deleteFromRecentList(dbPath);
                            /* Refresh the list */
                            mActivity.restartActivity();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel_text), null)
                    .create()
                    .show();
            }

            if (v == cardPlayerItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, CardPlayerActivity.class);
                myIntent.putExtra(CardPlayerActivity.EXTRA_DBPATH, dbPath);
                startActivity(myIntent);
            }

            dismiss();
        }
    };
}

