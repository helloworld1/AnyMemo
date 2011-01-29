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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Comparator;
import java.util.Collections;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.database.SQLException;
import android.util.Log;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Handler;


import org.json.JSONArray;
import org.json.JSONException;


/*
 * This class include the most GUI utility for the DatabaseHelper
 * the DatabaseUtility wrap this class to provide more database * global operations
 */
public class DatabaseUtility{
    private final static String TAG = "org.liberty.android.fantastischmemo.DatabaseUtility";
    private AMActivity mActivity;
    private String dbPath, dbName;
    private ProgressDialog mProgressDialog = null;
    private Handler mHandler;


    /* Remember to close it when not used */
    public DatabaseUtility(AMActivity activity, String dbpath, String dbname) throws SQLException{
        mActivity = activity;
        dbPath = dbpath;
        dbName = dbname;
        mHandler = new Handler();
    }

    public void wipeLearningData(){
        new AlertDialog.Builder(mActivity)
            .setTitle(R.string.warning_text)
            .setIcon(R.drawable.alert_dialog_icon)
            .setMessage(R.string.settings_wipe_warning)
            .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface arg0, int arg1){
                    /* Be careful, the name in dbHelper is different */
                    AMGUIUtility.doProgressTask(mActivity, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){ public void doHeavyTask(){
                            DatabaseHelper dbHelper = new DatabaseHelper((Context)mActivity, dbPath, dbName);
                            /* Be careful, the name in dbHelper is different */
                            dbHelper.wipeLearnData();
                            dbHelper.close();
                        }
                        public void doUITask(){
                            mActivity.restartActivity();
                        }
                    });
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }

    public void swapAllQA(){
        new AlertDialog.Builder(mActivity)
            .setTitle(R.string.warning_text)
            .setIcon(R.drawable.alert_dialog_icon)
            .setMessage(R.string.settings_inverse_warning)
            .setPositiveButton(R.string.swap_text, new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface arg0, int arg1){
                    AMGUIUtility.doProgressTask(mActivity, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
                        public void doHeavyTask(){
                            DatabaseHelper dbHelper = new DatabaseHelper((Context)mActivity, dbPath, dbName);
                            dbHelper.inverseQA();
                            dbHelper.close();
                        }
                        public void doUITask(){
                            mActivity.restartActivity();
                        }
                    });
                }
            })
            .setNeutralButton(R.string.swapdup_text, new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface arg0, int arg1){
                    AMGUIUtility.doProgressTask(mActivity, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
                        public void doHeavyTask(){
                            DatabaseHelper dbHelper = new DatabaseHelper((Context)mActivity, dbPath, dbName);
                            dbHelper.swapDuplicate();
                            dbHelper.close();
                        }
                        public void doUITask(){
                            mActivity.restartActivity();
                        }
                    });
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();

    }

    public void removeDuplicates(){
        new AlertDialog.Builder(mActivity)
            .setTitle(R.string.remove_dup_text)
            .setMessage(R.string.removing_dup_warning)
            .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    AMGUIUtility.doProgressTask(mActivity, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
                        public void doHeavyTask(){
                            DatabaseHelper dbHelper = new DatabaseHelper((Context)mActivity, dbPath, dbName);
                            dbHelper.removeDuplicates();
                            dbHelper.close();
                        }
                        public void doUITask(){
                            mActivity.restartActivity();
                        }
                    });
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }

    public void deleteItemFromDb(final Item item){
        new AlertDialog.Builder(mActivity)
            .setTitle(R.string.detail_delete)
            .setMessage(R.string.delete_warning)
            .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    AMGUIUtility.doProgressTask(mActivity, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
                        public void doHeavyTask(){
                            DatabaseHelper dbHelper = new DatabaseHelper((Context)mActivity, dbPath, dbName);
                            dbHelper.deleteItem(item);
                            dbHelper.close();
                        }
                        public void doUITask(){
                            mActivity.restartActivity();
                        }
                    });
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }

    public void skipItemFromDb(final Item item){
        new AlertDialog.Builder(mActivity)
            .setTitle(R.string.skip_text)
            .setMessage(R.string.skip_warning)
            .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    AMGUIUtility.doProgressTask(mActivity, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
                        public void doHeavyTask(){
                            DatabaseHelper dbHelper = new DatabaseHelper((Context)mActivity, dbPath, dbName);
                            /* Set interval to a large number so it will never appear */
                            Item newItem = new Item.Builder(item)
                                .setInterval(100000)
                                .setEasiness(4.0)
                                .setAcqReps(10)
                                .build();
                            dbHelper.addOrReplaceItem(newItem);
                            dbHelper.close();
                        }
                        public void doUITask(){
                            mActivity.restartActivity();
                        }
                    });
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }

    public void shuffleDatabase(){
        new AlertDialog.Builder(mActivity)
            .setTitle(R.string.warning_text)
            .setIcon(R.drawable.alert_dialog_icon)
            .setMessage(R.string.settings_shuffle_warning)
            .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface arg0, int arg1){
                    AMGUIUtility.doProgressTask(mActivity, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
                        public void doHeavyTask(){
                            DatabaseHelper dbHelper = new DatabaseHelper((Context)mActivity, dbPath, dbName);
                            dbHelper.shuffleDatabase();
                            dbHelper.close();
                        }
                        public void doUITask(){
                            mActivity.restartActivity();
                        }
                    });
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }
    public void swapSingelItem(final Item item){
        AMGUIUtility.doProgressTask(mActivity, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
            public void doHeavyTask(){
                DatabaseHelper dbHelper = new DatabaseHelper((Context)mActivity, dbPath, dbName);
                Item newItem = item.inverseQA();
                dbHelper.addOrReplaceItem(newItem);
                dbHelper.close();
            }
            public void doUITask(){
                mActivity.restartActivity();
            }
        });
    }

    public void resetCurrentLearningData(final Item item){
        new AlertDialog.Builder(mActivity)
            .setTitle(R.string.detail_reset)
            .setMessage(R.string.reset_current_warning)
            .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    AMGUIUtility.doProgressTask(mActivity, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
                        public void doHeavyTask(){
                            DatabaseHelper dbHelper = new DatabaseHelper((Context)mActivity, dbPath, dbName);
                            Item newItem = new Item.Builder()
                                .setId(item.getId())
                                .setQuestion(item.getQuestion())
                                .setAnswer(item.getAnswer())
                                .setCategory(item.getCategory())
                                .build();
                            dbHelper.addOrReplaceItem(newItem);
                            dbHelper.close();
                        }
                        public void doUITask(){
                            mActivity.restartActivity();
                        }
                    });
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }
}

    






