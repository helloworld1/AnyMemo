/*
Copyright (C) 2010 Haowen Ning

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
package org.liberty.android.fantastischmemo;

import org.liberty.android.fantastischmemo.downloader.*;
import org.liberty.android.fantastischmemo.cardscreen.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import android.app.Activity;
import android.app.TabActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Display;
import android.widget.Button;
import android.widget.TextView;
import android.content.res.Configuration;
import android.content.pm.ActivityInfo;
import android.text.method.LinkMovementMethod;
import android.text.Html;
import android.content.Context;
import android.widget.TabHost;


public class MainTabs extends TabActivity{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tabs);

        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;

        intent = new Intent().setClass(this, FileBrowser.class);
        spec = tabHost.newTabSpec("recent").setIndicator("Recent",
                res.getDrawable(R.drawable.text))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, FileBrowser.class);
        spec = tabHost.newTabSpec("open").setIndicator("Open",
                res.getDrawable(R.drawable.open))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, EditScreenTab.class);
        spec = tabHost.newTabSpec("edit").setIndicator("Edit",
                res.getDrawable(R.drawable.edit))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, FileBrowser.class);
        spec = tabHost.newTabSpec("edit").setIndicator("Download",
                res.getDrawable(R.drawable.download))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, FileBrowser.class);
        spec = tabHost.newTabSpec("misc").setIndicator("Misc",
                res.getDrawable(R.drawable.misc))
            .setContent(intent);
        tabHost.addTab(spec);

    }

}
