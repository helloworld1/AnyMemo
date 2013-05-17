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

You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.ui;

import java.io.File;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import android.app.Activity;
import android.content.Intent;

public class EditTabFragment extends FileBrowserFragment {
    Activity mActivity;

    private RecentListUtil recentListUtil;

    @Inject
    public void setRecentListUtil(RecentListUtil recentListUtil) {
        this.recentListUtil = recentListUtil;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        setOnFileClickListener(fileClickListener);
    }

    private FileBrowserFragment.OnFileClickListener fileClickListener
        = new FileBrowserFragment.OnFileClickListener() {
            public void onClick(File file) {
                String fullpath = file.getAbsolutePath();
                recentListUtil.addToRecentList(fullpath);
                Intent myIntent = new Intent(mActivity, PreviewEditActivity.class);
                myIntent.putExtra(PreviewEditActivity.EXTRA_DBPATH, fullpath);
                startActivity(myIntent);
            }
    };

}
