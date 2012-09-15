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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.mycommons.lang3.StringUtils;

import org.liberty.android.fantastischmemo.AMEnv;

import org.liberty.android.fantastischmemo.queue.LearnQueueManager;
import org.liberty.android.fantastischmemo.ui.DetailScreen;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.ui.SettingsScreen;
import org.liberty.android.fantastischmemo.ui.StudyActivity;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;
import org.liberty.android.fantastischmemo.utils.AnyMemoExecutor;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;

import org.liberty.android.fantastischmemo.queue.QueueManager;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;

import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTSImpl;

import org.liberty.android.fantastischmemo.utils.DictionaryUtil;

import com.example.android.apis.graphics.FingerPaint;

import android.os.AsyncTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import android.view.Gravity;
import android.view.Menu;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.util.Log;
import android.net.Uri;

import org.liberty.android.fantastischmemo.ui.CategoryEditorFragment.CategoryEditorResultListener;

import android.widget.Toast;

public class QuizActivity extends QACardActivity {
    public static String EXTRA_DBPATH = "dbpath";
    public static String EXTRA_CATEGORY_ID = "category_id";
    public static String EXTRA_START_CARD_ORD = "start_card_ord";
    public static String EXTRA_QUIZ_SIZE = "quiz_size";


    @Override
    public void onInit() {
    }

    @Override
	public void onCreate(Bundle savedInstanceState){
        Bundle extras = getIntent().getExtras();
        super.onCreate(savedInstanceState);
    }

}
