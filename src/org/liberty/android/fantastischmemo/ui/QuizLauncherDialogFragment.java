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

import org.apache.mycommons.lang3.StringUtils;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Category;

import android.app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Rect;

import android.os.AsyncTask;
import android.os.Bundle;

import android.preference.PreferenceManager;

import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class QuizLauncherDialogFragment extends DialogFragment {

    public static final String EXTRA_DBPATH = "dbpath";

    private static final long MAX_GROUP_SIZE = 100; 

    private static final long DEFAULT_GROUP_SIZE = 100; 

    private AnyMemoDBOpenHelper dbOpenHelper;

    private CardDao cardDao;

    private String dbPath = null;

    private AMActivity mActivity;

    private Button startQuizButton;

    private RadioButton quizByGroupRadio;

    private RadioButton quizeByCategoryRadio;

    private TextView quizGroupSizeTitle;

    private EditText quizGroupSizeEdit;

    private TextView quizGroupNumberTitle;

    private EditText quizGroupNumberEdit;

    private Button categoryButton;

    private long totalCardNumber;

    private long groupSize;

    private long groupNumber;

    private SharedPreferences settings;

    private SharedPreferences.Editor editor;

    private Category filterCategory;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle extras = getArguments();
        if (extras != null) {
            dbPath = extras.getString(EXTRA_DBPATH);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().setTitle(R.string.quiz_text);
        View v = inflater.inflate(R.layout.quiz_launcher_dialog, container, false);

        startQuizButton = (Button) v.findViewById(R.id.start_quiz_button);

        startQuizButton.setOnClickListener(startQuizButtonOnClickListener);

        quizByGroupRadio = (RadioButton) v.findViewById(R.id.quiz_by_group_radio);

        quizeByCategoryRadio = (RadioButton) v.findViewById(R.id.quiz_by_category_radio);

        quizGroupSizeTitle = (TextView) v.findViewById(R.id.quiz_group_size_title);

        quizGroupSizeEdit = (EditText) v.findViewById(R.id.quiz_group_size);

        quizGroupNumberTitle = (TextView) v.findViewById(R.id.quiz_group_number_title);

        quizGroupNumberEdit = (EditText) v.findViewById(R.id.quiz_group_number);

        categoryButton = (Button) v.findViewById(R.id.category_button);
    
        // We have to set up the dialog's webview size manually or the webview will be zero size.
        // This should be a bug of Android.
        Rect displayRectangle = new Rect();
        Window window = mActivity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        v.setMinimumWidth((int)(displayRectangle.width() * 0.9f));

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(mActivity, dbPath);
        InitTask task = new InitTask();
        settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        task.execute((Void)null);
    }

    private View.OnClickListener startQuizButtonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
            Intent intent = new Intent(mActivity, QuizActivity.class);
            intent.putExtra(QuizActivity.EXTRA_DBPATH, dbPath);
            intent.putExtra(QuizActivity.EXTRA_START_CARD_ORD, 1);
            intent.putExtra(QuizActivity.EXTRA_QUIZ_SIZE, 50);
            startActivity(intent);
			
		}
    };

    /*
     * This task will mainly populate the categoryList
     */
    private class InitTask extends AsyncTask<Void, Void, Void> {

		@Override
        public void onPreExecute() {
        }

        @Override
        public Void doInBackground(Void... params) {
            cardDao = dbOpenHelper.getCardDao();
            totalCardNumber = cardDao.getTotalCount(filterCategory);
            return null;
        }

        @Override
        public void onPostExecute(Void nothing) {
            //quizGroupSizeTitle.setText(quizGroupSizeTitle.getText());
            groupSize = settings.getLong("quiz_group_size", DEFAULT_GROUP_SIZE);
            groupNumber = settings.getLong("quiz_group_number", 1);
            setGroupSizeText();
            setGroupNumberText();
        }
    }

    private void setGroupSizeText() {
        if (totalCardNumber < groupSize) {
            groupSize = totalCardNumber;
        }
        long maxGroupSize = Math.min(totalCardNumber, MAX_GROUP_SIZE);
        quizGroupSizeTitle.setText(getString(R.string.quiz_group_size_text)
                + " (1-" + maxGroupSize + ")");
        if (StringUtils.isEmpty(quizGroupSizeEdit.getText())) {
            quizGroupSizeEdit.setText("" + groupSize);
        }
    }

    private void setGroupNumberText() {
        long maxGroupNumber = (totalCardNumber - 1) / groupSize + 1;
        if (groupNumber > maxGroupNumber) {
            groupNumber = maxGroupNumber;
        }
        quizGroupNumberTitle.setText(getString(R.string.quiz_group_number_text) + " (1-" + maxGroupNumber + ")");
        if (StringUtils.isEmpty(quizGroupNumberEdit.getText())) {
            quizGroupNumberEdit.setText("" + groupNumber);
        }
    }
}

