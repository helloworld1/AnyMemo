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

import android.graphics.Rect;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class QuizLauncherDialogFragment extends DialogFragment {

    public static String EXTRA_DBPATH = "dbpath";

    private String dbPath = null;

    private AMActivity mActivity;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle extras = getArguments();
        if (extras != null) {
            dbPath = extras.getString(EXTRA_DBPATH);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().setTitle(R.string.quiz_text);
        View v = inflater.inflate(R.layout.quiz_launcher_dialog, container, false);
        // We have to set up the dialog's webview size manually or the webview will be zero size.
        // This should be a bug of Android.
        Rect displayRectangle = new Rect();
        Window window = mActivity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        v.setMinimumWidth((int)(displayRectangle.width() * 0.9f));

        return v;
    }
}

