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

import org.liberty.android.fantastischmemo.*;
import org.liberty.android.fantastischmemo.tts.*;

import org.amr.arabic.ArabicUtilities;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.gesture.GestureOverlayView;
import android.widget.Button;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.util.Log;
import android.os.SystemClock;
import android.os.Environment;
import android.graphics.Typeface;
import android.text.Html.TagHandler;
import android.text.Html.ImageGetter;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;
import android.net.Uri;
import android.database.SQLException;

/* 
 * This utility class provides some useful static rucurring GUI methods
 * that generate dialogs or etc
 */
public class AMGUIUtility{
    private final static String TAG = "org.liberty.android.fantastischmemo.AMGUIUtility";
    private AMGUIUtility(){
        /* Shouldn't be invoked */
    }

    public static void displayError(final Activity activity, final String title, final String text, final Exception e){
        new AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(text + "\n" + activity.getString(R.string.exception_text) +": " + e.toString())
            .setPositiveButton(activity.getString(R.string.back_menu_text), new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1){
                    activity.finish();
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener(){
                public void onCancel(DialogInterface dialog){
                    activity.finish();
                }
            })
            .show();
    }

    public static void displayException(final Context context, final String title, final String text, final Exception e){
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(text + "\n" + context.getString(R.string.exception_text) +": " + e.toString())
            .setPositiveButton(context.getString(R.string.back_menu_text), null)
            .show();
    }

    public static void doProgressTask(final Context context, final int progressTitleId, final int progressMessageId, final ProgressTask task){
        final ProgressDialog mProgressDialog = ProgressDialog.show(context, context.getString(progressTitleId), context.getString(progressMessageId), true);
        final Handler handler = new Handler();
        new Thread(){
            public void run(){
                try{
                    task.doHeavyTask();
                    handler.post(new Runnable(){
                        public void run(){
                            task.doUITask();
                            mProgressDialog.dismiss();
                        }
                    });
                }
                catch(final Exception e){
                    handler.post(new Runnable(){
                        public void run(){
                            mProgressDialog.dismiss();
                            displayException(context, context.getString(R.string.exception_text), context.getString(R.string.exception_message), e);
                            Log.e(TAG, "Error running progress task", e);
                        }
                    });
                }
            }
        }.start();
    }

    public static interface ProgressTask{
        public void doHeavyTask() throws Exception;
        public void doUITask();
    }

    public static DialogInterface.OnClickListener getDialogFinishListener(final Activity activity){
        return new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                activity.finish();
            }
        };
    }
}

