/*
Copyright (C) 2013 Haowen Ning

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

import java.util.HashMap;
import java.util.Map;

import org.apache.mycommons.lang3.StringUtils;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMPrefKeys;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class QuizLauncherDialogFragment extends DialogFragment {

    public static final String EXTRA_DBPATH = "dbpath";

    private static final int MAX_GROUP_SIZE = 100; 

    private static final int DEFAULT_GROUP_SIZE = 100; 

    private AnyMemoDBOpenHelper dbOpenHelper;

    private CardDao cardDao;

    private String dbPath = null;

    private AMActivity mActivity;

    private Button startQuizButton;

    private RadioButton quizByGroupRadio;

    private RadioButton quizByCategoryRadio;

    private TextView quizGroupSizeTitle;

    private EditText quizGroupSizeEdit;

    private TextView quizGroupNumberTitle;

    private EditText quizGroupNumberEdit;

    private CheckBox shuffleCheckbox;

    private Button categoryButton;

    private int totalCardNumber;

    private int groupSize;

    private int groupNumber;

    // Default category id is "uncategorized".
    private int categoryId = 0;

    private SharedPreferences settings;

    private SharedPreferences.Editor editor;

    private Category filterCategory;

    private Map<CompoundButton, View> radioButtonSettingsMapping;

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

        quizByGroupRadio.setOnCheckedChangeListener(onCheckedChangeListener);

        quizByCategoryRadio = (RadioButton) v.findViewById(R.id.quiz_by_category_radio);

        quizByCategoryRadio.setOnCheckedChangeListener(onCheckedChangeListener);

        quizGroupSizeTitle = (TextView) v.findViewById(R.id.quiz_group_size_title);

        quizGroupSizeEdit = (EditText) v.findViewById(R.id.quiz_group_size);
        // Make sure the text value is sanity and update other information
        // about the group size and etc accordingly.
        quizGroupSizeEdit.addTextChangedListener(editTextWatcher);
        quizGroupSizeEdit.setOnFocusChangeListener(sanitizeInputListener);

        quizGroupNumberTitle = (TextView) v.findViewById(R.id.quiz_group_number_title);

        quizGroupNumberEdit = (EditText) v.findViewById(R.id.quiz_group_number);
        quizGroupNumberEdit.addTextChangedListener(editTextWatcher);
        quizGroupNumberEdit.setOnFocusChangeListener(sanitizeInputListener);

        categoryButton = (Button) v.findViewById(R.id.category_button);
        categoryButton.setOnClickListener(categoryButtonListener);

        radioButtonSettingsMapping = new HashMap<CompoundButton, View>(2);
        radioButtonSettingsMapping.put(quizByGroupRadio, v.findViewById(R.id.quiz_by_group_settings));
        radioButtonSettingsMapping.put(quizByCategoryRadio, v.findViewById(R.id.quiz_by_category_settings));

        shuffleCheckbox = (CheckBox) v.findViewById(R.id.shuffle_checkbox);
    
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
        settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        editor = settings.edit();
        InitTask task = new InitTask();
        task.execute((Void)null);
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener
        = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
                View settingsView = radioButtonSettingsMapping.get(buttonView);
                if (isChecked) {
                    settingsView.setVisibility(View.VISIBLE);
                } else {
                    settingsView.setVisibility(View.GONE);
                }
            }
        };


    private View.OnClickListener startQuizButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (quizByCategoryRadio.isChecked()) {
                Intent intent = new Intent(mActivity, QuizActivity.class);
                intent.putExtra(QuizActivity.EXTRA_DBPATH, dbPath);
                intent.putExtra(QuizActivity.EXTRA_CATEGORY_ID, categoryId);
                intent.putExtra(QuizActivity.EXTRA_SHUFFLE_CARDS, shuffleCheckbox.isChecked());
                startActivity(intent);
            } else {
                Intent intent = new Intent(mActivity, QuizActivity.class);
                editor.putInt(AMPrefKeys.QUIZ_GROUP_SIZE_KEY, groupSize);
                editor.putInt(AMPrefKeys.QUIZ_GROUP_NUMBER_KEY, groupNumber);
                editor.commit();

                int startOrd = (groupNumber - 1) * groupSize + 1;
                intent.putExtra(QuizActivity.EXTRA_DBPATH, dbPath);
                intent.putExtra(QuizActivity.EXTRA_START_CARD_ORD, startOrd);
                intent.putExtra(QuizActivity.EXTRA_QUIZ_SIZE, groupSize);
                intent.putExtra(QuizActivity.EXTRA_SHUFFLE_CARDS, shuffleCheckbox.isChecked());
                startActivity(intent);
            }
        }
    };

    private View.OnClickListener categoryButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showCategoriesDialog();
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
            totalCardNumber = (int)cardDao.getTotalCount(filterCategory);
            return null;
        }

        @Override
        public void onPostExecute(Void nothing) {
            groupSize = settings.getInt(AMPrefKeys.QUIZ_GROUP_SIZE_KEY, DEFAULT_GROUP_SIZE);
            groupNumber = settings.getInt(AMPrefKeys.QUIZ_GROUP_NUMBER_KEY, 1);
            setGroupSizeText();
            setGroupNumberText();
        }
    }

    private void setGroupSizeText() {
        if (totalCardNumber < groupSize) {
            groupSize = totalCardNumber;
        }
        int maxGroupSize = Math.min(totalCardNumber, MAX_GROUP_SIZE);

        //  If maxGroupNumberis 0, Math.min(maxGroupSize, 1) will display 0
        quizGroupSizeTitle.setText(getString(R.string.quiz_group_size_text)
                + " (" + Math.min(maxGroupSize, 1) + "-" + maxGroupSize + ")");
        if (StringUtils.isEmpty(quizGroupSizeEdit.getText())) {
            quizGroupSizeEdit.setText("" + groupSize);
        }
    }

    private void setGroupNumberText() {
        // The groupSize can be 0, so use max (1, groupSize) to
        // fix divided by zero problem
        int maxGroupNumber = (totalCardNumber - 1) / Math.max(1, groupSize) + 1;
        if (groupNumber > maxGroupNumber) {
            groupNumber = maxGroupNumber;
        }
        //  If maxGroupNumberis 0, Math.min(maxGroupNumber, 1) will display 0
        quizGroupNumberTitle.setText(getString(R.string.quiz_group_number_text)
                + " (" + Math.min(maxGroupNumber, 1) + "-" + maxGroupNumber + ")");
        if (StringUtils.isEmpty(quizGroupNumberEdit.getText())) {
            quizGroupNumberEdit.setText("" + groupNumber);
        }
    }

    private TextWatcher editTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            // Nothing happened
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            // Nothing happened
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (StringUtils.isEmpty(s)) {
                return;
            }
            try {
                groupSize = Integer.valueOf(quizGroupSizeEdit.getText().toString());
                if (groupSize <= 0) {
                    groupSize = 1;
                }
                if (groupSize > MAX_GROUP_SIZE) {
                    groupSize = MAX_GROUP_SIZE;
                }

            } catch (NumberFormatException e) {
                groupSize = MAX_GROUP_SIZE;
            }
            try {
                groupNumber = Integer.valueOf(quizGroupNumberEdit.getText().toString());
                if (groupNumber < 1) {
                    groupNumber = 1;
                }
            } catch (NumberFormatException e) {
                groupNumber = 1;
            }
            setGroupNumberText();
            setGroupSizeText();
        }
    };

    View.OnFocusChangeListener sanitizeInputListener =
        new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == false) {
                    quizGroupSizeEdit.setText("" + groupSize);
                    quizGroupNumberEdit.setText("" + groupNumber);
                }
            }
        };

    private void showCategoriesDialog() {
        CategoryEditorFragment df = new CategoryEditorFragment();
        df.setResultListener(categoryResultListener);
        Bundle b = new Bundle();
        b.putString(CategoryEditorFragment.EXTRA_DBPATH, dbPath);
        b.putInt(CategoryEditorFragment.EXTRA_CATEGORY_ID, categoryId);
        df.setArguments(b);
        df.show(mActivity.getSupportFragmentManager(), "CategoryEditDialog");
        mActivity.getSupportFragmentManager().findFragmentByTag("CategoryEditDialog");
    }

    // When a category is selected in category fragment.
    private CategoryEditorFragment.CategoryEditorResultListener categoryResultListener = 
        new CategoryEditorFragment.CategoryEditorResultListener() {
            public void onReceiveCategory(Category c) {
                assert c != null : "The category got shouldn't be null.";
                categoryId = c.getId();
                if (StringUtils.isEmpty(c.getName())) {
                    categoryButton.setText(R.string.uncategorized_text);
                } else {
                    categoryButton.setText(c.getName());
                }
            }
        };
}

