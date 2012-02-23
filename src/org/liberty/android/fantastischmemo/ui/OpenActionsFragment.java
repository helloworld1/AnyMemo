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

import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OpenActionsFragment extends DialogFragment {
    public static String EXTRA_DBPATH = "dbpath";
    private AMActivity mActivity;
    private String dbPath;

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
        View v = inflater.inflate(R.layout.open_actions_layout, container, false);
        return v;
    }
}

