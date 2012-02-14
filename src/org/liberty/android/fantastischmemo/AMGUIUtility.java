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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

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
        Log.e(TAG, "displayException", e);
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

    public static void displayException(final Activity activity, final String title, final String text, final Exception e){
        Log.e(TAG, "displayException", e);
        new AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(text + "\n" + activity.getString(R.string.exception_text) + ": " + e.toString())
           // .setPositiveButton(activity.getString(R.string.back_menu_text), null)
            .setNeutralButton(R.string.back_menu_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    activity.finish();
                }
            })
            .show();
    }

    public static void doProgressTask(final Activity activity, final String progressTitle, final String progressMessage, final ProgressTask task){
        final ProgressDialog mProgressDialog = ProgressDialog.show(activity, progressTitle, progressMessage, true);
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
                            displayException(activity, activity.getString(R.string.exception_text), activity.getString(R.string.exception_message), e);
                            Log.e(TAG, "Error running progress task", e);
                        }
                    });
                }
            }
        }.start();
    }

    public static void doProgressTask(final Activity activity, final int progressTitleId, final int progressMessageId, final ProgressTask task){
        String progressTitle = activity.getString(progressTitleId);
        String progressMessage= activity.getString(progressMessageId);
        doProgressTask(activity, progressTitle, progressMessage, task);
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

