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

import java.util.ArrayList;

import android.os.Bundle;
import android.content.Context;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.util.Log;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class DownloaderAnyMemo extends DownloaderBase{
    private static final String TAG = "org.liberty.android.fantastischmemo.DownloaderAnyMemo";
    private DownloadListAdapter dlAdapter;

    @Override
    protected void initialRetrieve(){
        dlAdapter = new DownloadListAdapter(this, R.layout.filebrowser_item);
        ListView listView = (ListView)findViewById(R.id.file_list);
        listView.setAdapter(dlAdapter);
        DownloadItem dItem = new DownloadItem(1, "hello", "world", "");
        DownloadItem dItem2 = new DownloadItem(2, "hello2", "world", "");
        dlAdapter.add(dItem);
        dlAdapter.add(dItem2);



    }

    @Override
    protected void openCategory(DownloadItem di){
    }

    @Override
    protected DownloadItem getDownloadItem(int position){
        return null;
    }

    @Override
    protected void goBack(){
    }

    @Override
    protected void fetchDatabase(DownloadItem di){
    }



}
