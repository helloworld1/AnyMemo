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

import android.app.Activity;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ListEditActionsFragment extends DialogFragment {
    public static String EXTRA_DBPATH = "dbpath";
    public static String EXTRA_CARD_ID = "id";
    private AMActivity mActivity;
    private String dbPath;
    private int currentId;

    private View editItem;
    private View previewItem;
    private View detailItem;

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
        currentId = args.getInt(EXTRA_CARD_ID);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        View v = inflater.inflate(R.layout.list_edit_actions_layout, container, false);

        editItem = v.findViewById(R.id.edit);
        editItem.setOnClickListener(buttonClickListener);

        previewItem = v.findViewById(R.id.preview);
        previewItem.setOnClickListener(buttonClickListener);

        detailItem = v.findViewById(R.id.detail);
        detailItem.setOnClickListener(buttonClickListener);

        return v;
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == editItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, CardEditor.class);
                myIntent.putExtra(CardEditor.EXTRA_DBPATH, dbPath);
                myIntent.putExtra(CardEditor.EXTRA_CARD_ID, currentId);
                startActivityForResult(myIntent, 1);
            }

            if (v == previewItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, PreviewEditActivity.class);
                myIntent.putExtra(PreviewEditActivity.EXTRA_DBPATH, dbPath);
                myIntent.putExtra(PreviewEditActivity.EXTRA_CARD_ID, currentId);
                startActivityForResult(myIntent, 2);
            }

            if (v == detailItem) {
                Intent myIntent = new Intent();
                myIntent.setClass(mActivity, DetailScreen.class);
                myIntent.putExtra(DetailScreen.EXTRA_DBPATH, dbPath);
                myIntent.putExtra(DetailScreen.EXTRA_CARD_ID, currentId);
                startActivityForResult(myIntent, 3);
            }
            dismiss();
        }
    };
}

