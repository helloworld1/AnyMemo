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

import java.io.File;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.ui.FileBrowserFragment;

import android.app.Activity;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;

public class FileBrowserActivity extends AMActivity {
    public final static String EXTRA_RESULT_PATH = "result_path";
    public final static String EXTRA_DEFAULT_ROOT = "default_root";
    public final static String EXTRA_FILE_EXTENSIONS = "file_extension";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.file_browser_activity);
        Bundle extras = getIntent().getExtras();
        assert extras != null : "Pass null to extras in File Browser";
        String defaultPath = extras.getString(EXTRA_DEFAULT_ROOT);
        String fileExtensions = extras.getString(EXTRA_FILE_EXTENSIONS);

        FileBrowserFragment fragment = new FileBrowserFragment();
        Bundle b = new Bundle();
        b.putString(FileBrowserFragment.EXTRA_DEFAULT_ROOT, defaultPath);
        b.putString(FileBrowserFragment.EXTRA_FILE_EXTENSIONS, fileExtensions);

        fragment.setArguments(b);
        fragment.setOnFileClickListener(fileClickListener);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.root, fragment);
        transaction.commit();
    }

    protected void fileClickAction(File file) {
        String fullpath = file.getAbsolutePath();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_RESULT_PATH, fullpath);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private FileBrowserFragment.OnFileClickListener fileClickListener
        = new FileBrowserFragment.OnFileClickListener() {
        public void onClick(File file) {
            fileClickAction(file);
        }
    };
}

