package org.liberty.android.fantastischmemo.ui;

import java.lang.Void;

import java.sql.SQLException;

import java.util.List;

import org.apache.mycommons.lang3.StringUtils;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CategoryDao;

import org.liberty.android.fantastischmemo.domain.Category;

import android.app.Activity;

import android.content.Context;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;

public class CategoryEditorFragment extends DialogFragment implements View.OnClickListener {
    public static String EXTRA_DBPATH = "dbpath";
    public static String EXTRA_CATEGORY_ID = "id";
    private AMActivity mActivity;
    private String dbPath;
    private CategoryDao categoryDao;
    private int currentCategoryId;
    private static final String TAG = "CategoryEditorFragment";
    private CategoryAdapter categoryAdapter;
    private ListView categoryList;
    private Button okButton;
    private Button newButton;
    private Button deleteButton;
    private Button editButton;
    private EditText categoryEdit;
    private CategoryEditorResultListener resultListener;
    private AnyMemoDBOpenHelper dbOpenHelper;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity)activity;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle args = this.getArguments();
        dbPath = args.getString(EXTRA_DBPATH);
        currentCategoryId = args.getInt(EXTRA_CATEGORY_ID, 1);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    public void setResultListener(CategoryEditorResultListener resultListener) {
        this.resultListener = resultListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.category_dialog, container, false);
        categoryList = (ListView)v.findViewById(R.id.category_list);
        categoryList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        categoryList.setOnItemClickListener(listItemClickListener);
        InitTask initTask = new InitTask();
        initTask.execute((Void)null);
        newButton = (Button)v.findViewById(R.id.button_new);
        okButton = (Button)v.findViewById(R.id.button_ok);
        editButton = (Button)v.findViewById(R.id.button_edit);
        deleteButton = (Button)v.findViewById(R.id.button_delete);
        categoryEdit = (EditText)v.findViewById(R.id.category_dialog_edit);

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
    }


    public void onClick(View v) {
        if (v == okButton) {
            mActivity.setProgressBarIndeterminateVisibility(false);
            int position = categoryList.getCheckedItemPosition();
            Category selectedCategory;
            if (position == AdapterView.INVALID_POSITION) {
                selectedCategory = null;
            } else {
                selectedCategory = categoryAdapter.getItem(position);
            }
            if (resultListener != null) {
                resultListener.onReceiveCategory(selectedCategory);
            }
            dismiss();
        }
        if (v == newButton) {
            NewCategoryTask task = new NewCategoryTask();
            task.execute((Void)null);
        }
        if (v == editButton) {
            EditCategoryTask task = new EditCategoryTask();
            task.execute((Void)null);
        }
        if (v == deleteButton) {
            DeleteCategoryTask task = new DeleteCategoryTask();
            task.execute((Void)null);
        }
    }

    /*
     * This task will mainly populate the categoryList
     */
    private class InitTask extends AsyncTask<Void, Void, Integer> {
        private List<Category> categories;

		@Override
        public void onPreExecute() {
            mActivity.setProgressBarIndeterminateVisibility(true);
            categoryAdapter = new CategoryAdapter(mActivity, android.R.layout.simple_list_item_single_choice);
            assert categoryList != null : "Couldn't find categoryList view";
            assert categoryAdapter != null : "New adapter is null";
            categoryList.setAdapter(categoryAdapter);
        }

        @Override
        public Integer doInBackground(Void... params) {
            Category currentCategory;
            try {
                dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(mActivity, dbPath);
                categoryDao = dbOpenHelper.getCategoryDao();
                categories = categoryDao.queryForAll();
                currentCategory = categoryDao.queryForId(currentCategoryId);
            } catch (SQLException e) {
                Log.e(TAG, "Error creating daos", e);
                throw new RuntimeException("Dao creation error");
            }
            int categorySize = categories.size();
            Integer position = null;
            if (currentCategory != null) {
                for (int i = 0; i < categorySize; i++) {
                    if (categories.get(i).getName().equals(currentCategory.getName())) {
                        position = i;
                        break;
                    }
                }
            } else {
                position = 0;
            }
            assert position != null : "The card has no category. This shouldn't happen.";
            return position;
        }

        @Override
        public void onPostExecute(Integer pos){
            categoryAdapter.addAll(categories);
            categoryList.setItemChecked(pos, true);
            // This is needed to scroll to checked position
            categoryList.setSelection(pos);

            categoryEdit.setText(categoryAdapter.getItem(pos).getName());
            enableListeners();
            mActivity.setProgressBarIndeterminateVisibility(false);
        }
    }

    /*
     * This task will edit the category in the list
     */
    private class EditCategoryTask extends AsyncTask<Void, Category, Void> {
        private Category selectedCategory;
        private String editText;

		@Override
        public void onPreExecute() {
            disableListeners();
            mActivity.setProgressBarIndeterminateVisibility(true);
            editText = categoryEdit.getText().toString();
            int position = categoryList.getCheckedItemPosition();
            if (position == AdapterView.INVALID_POSITION || "".equals(editText)) {
                cancel(true);
                return;
            }
            selectedCategory = categoryAdapter.getItem(position);
            // Should deduplicate before editing the category
            // Point to the correct category if necessary.
            int categorySize = categoryAdapter.getCount();
            for (int i = 0; i < categorySize; i++) {
                if (categoryAdapter.getItem(i).getName().equals(editText)) {
                    position = i;
                    categoryList.setItemChecked(position, true);
                    categoryList.setSelection(position);
                    cancel(true);
                    return;
                }
            }
        }

        @Override
        public Void doInBackground(Void... params) {
            assert selectedCategory != null : "Null category is selected. This shouldn't happen";
            assert editText != null : "Category's EditText shouldn't get null";
            try {
                selectedCategory.setName(editText);
                categoryDao.update(selectedCategory);
            } catch (SQLException e) {
                Log.e(TAG, "Error updating the category", e);
                throw new RuntimeException("Error updating the category");
            }
            return null;
        }

        @Override
        public void onCancelled(){
            enableListeners();
        }

        @Override
        public void onPostExecute(Void result){
            categoryAdapter.notifyDataSetChanged();
            mActivity.setProgressBarIndeterminateVisibility(false);
            enableListeners();
        }
    }

    private class NewCategoryTask extends AsyncTask<Void, Void, Category> {
        private String editText;

		@Override
        public void onPreExecute() {
            disableListeners();
            mActivity.setProgressBarIndeterminateVisibility(true);
            editText = categoryEdit.getText().toString();
            int categorySize = categoryAdapter.getCount();
            for (int i = 0; i < categorySize; i++) {
                if (categoryAdapter.getItem(i).getName().equals(editText)) {
                    categoryList.setItemChecked(i, true);
                    categoryList.setSelection(i);
                    cancel(true);
                    return;
                }
            }
            assert editText != null : "Category's EditText shouldn't get null";
        }

        @Override
        public Category doInBackground(Void... params) {
            try {
                Category c = new Category();
                c.setName(editText);
                categoryDao.create(c);
                return c;
            } catch (SQLException e) {
                Log.e(TAG, "Error updating the category", e);
                throw new RuntimeException("Error updating the category");
            }
        }

        @Override
        public void onCancelled(){
            enableListeners();
        }

        @Override
        public void onPostExecute(Category result){
            categoryAdapter.add(result);
            categoryAdapter.notifyDataSetChanged();
            
            // Select and scroll to newly created category
            int lastPos = categoryAdapter.getCount() - 1;
            categoryList.setItemChecked(lastPos, true);
            categoryList.setSelection(lastPos);
            mActivity.setProgressBarIndeterminateVisibility(false);
            enableListeners();
        }
    }

    private class DeleteCategoryTask extends AsyncTask<Void, Void, Void> {
        private Category selectedCategory;

		@Override
        public void onPreExecute() {
            int position = categoryList.getCheckedItemPosition();
            if (position == AdapterView.INVALID_POSITION) {
                cancel(true);
                return;
            }
            selectedCategory = categoryAdapter.getItem(position);
            assert selectedCategory != null : "Null category selected!";
        }

        @Override
        public Void doInBackground(Void... params) {
            // We don't want to remove the "Uncategorized"
            if (StringUtils.isEmpty(selectedCategory.getName())) {
                return null;
            }
            categoryDao.removeCategory(selectedCategory);
            return null;
        }

        @Override
        public void onCancelled(){
            enableListeners();
        }

        @Override
        public void onPostExecute(Void result){
            if (StringUtils.isNotEmpty(selectedCategory.getName())) {
                categoryAdapter.remove(selectedCategory);
            }
            categoryAdapter.notifyDataSetChanged();
            
            // Move to first category (Uncategorized)
            categoryList.setItemChecked(0, true);
            categoryList.setSelection(0);
            categoryEdit.setText("");
            mActivity.setProgressBarIndeterminateVisibility(false);
            enableListeners();
        }
    }

    protected class CategoryAdapter extends ArrayAdapter<Category>{

        public CategoryAdapter(Context context, int textViewResourceId){
            super(context, textViewResourceId);
        }

        public void addAll(List<Category> lc) {
            for (Category c : lc) {
                add(c);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            CheckedTextView v = (CheckedTextView)convertView;
            if(v == null){
                LayoutInflater li = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                /* Reuse the filebrowser's resources */
                v = (CheckedTextView)li.inflate(android.R.layout.simple_list_item_single_choice, null);
            }
            Category item = getItem(position);
            if (item.getName().equals("")) {
                v.setText(R.string.uncategorized_text);
            } else {
                v.setText(item.getName());
            }
            return v;
        }
    }

    private OnItemClickListener listItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Category c = categoryAdapter.getItem(position);
            assert c != null : "Select a null category. This shouldn't happen";
            categoryEdit.setText(c.getName());
        }
    };

    private void enableListeners() {
        okButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        newButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
    }

    private void disableListeners() {
        okButton.setOnClickListener(null);
        deleteButton.setOnClickListener(null);
        newButton.setOnClickListener(null);
        editButton.setOnClickListener(null);
    }

    public static interface CategoryEditorResultListener {
        void onReceiveCategory(Category c);
    }
}
