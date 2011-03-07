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

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.util.Log;
import android.preference.PreferenceManager;

public class ShareScreen extends Activity{
    private static final String TAG = "org.liberty.android.fantastischmemo.ShareReceiver";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_screen);
        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_SEND)) {
            Bundle extras = intent.getExtras();
            String subject = extras.getString(Intent.EXTRA_SUBJECT);
            String text = extras.getString(Intent.EXTRA_TEXT);
            Log.v(TAG, "Subject: "+ subject);
            Log.v(TAG, "Text: "+ text);
        }
    }
}
