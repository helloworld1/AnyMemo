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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.BaseFragment;
import org.liberty.android.fantastischmemo.downloader.anymemo.AnyMemoDownloaderActivity;
import org.liberty.android.fantastischmemo.downloader.dropbox.DropboxOauth2AccountActivity;
import org.liberty.android.fantastischmemo.downloader.google.GoogleAccountActivity;
import org.liberty.android.fantastischmemo.downloader.quizlet.QuizletLauncher;

/*
 * This class is invoked when the user share the card from other
 * apps like ColorDict
 */
public class DownloadTabFragment extends BaseFragment implements View.OnClickListener{
    private View amButton;
    private View quizletButton;
    private View dropboxButton;
    private View googleButton;
    private Activity mActivity;

    public DownloadTabFragment() { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.downloader_tab, container, false);
        amButton = v.findViewById(R.id.source_anymemo);
        quizletButton = v.findViewById(R.id.source_quizlet);
        dropboxButton = v.findViewById(R.id.source_dropbox);
        googleButton = v.findViewById(R.id.source_google);
        amButton.setOnClickListener(this);
        quizletButton.setOnClickListener(this);
        dropboxButton.setOnClickListener(this);
        googleButton.setOnClickListener(this);
        return v;
    }
    @Override
    public void onClick(View v){
        if (v == amButton){
            startActivity(new Intent(mActivity, AnyMemoDownloaderActivity.class));
        }
        if (v == quizletButton){
            startActivity(new Intent(mActivity, QuizletLauncher.class));
        }
        if (v == dropboxButton){
        	startActivity(new Intent(mActivity, DropboxOauth2AccountActivity.class));
        }
        if (v == googleButton) {
            startActivity(new Intent(mActivity, GoogleAccountActivity.class));
        }
    }
}
