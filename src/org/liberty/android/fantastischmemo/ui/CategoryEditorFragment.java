package org.liberty.android.fantastischmemo.ui;

import java.util.Comparator;
import java.util.List;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.domain.Category;

import roboguice.fragment.RoboDialogFragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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

import com.google.common.base.Strings;

public class CategoryEditorFragment extends RoboDialogFragment {
    public static String EXTRA_DBPATH = "dbpath";
    public static String EXTRA_CATEGORY_ID = "id";
    private AMActivity mActivity;
    private String dbPath;
    private CategoryDao categoryDao;
    private int currentCategoryId;
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
        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(mActivity, dbPath);
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

    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
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
    };

    /*
     * This task will mainly populate the categoryList
     */
    private class InitTask extends AsyncTask<Void, Void, Category> {
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
        public Category doInBackground(Void... params) {
            Category currentCategory;
            categoryDao = dbOpenHelper.getCategoryDao();
            categories = categoryDao.queryForAll();
            currentCategory = categoryDao.queryForId(currentCategoryId);
            return currentCategory;
        }

        @Override
        public void onPostExecute(Category currentCategory){
            categoryAdapter.addAll(categories);
            categoryAdapter.sort();

            // This is needed to scroll to checked position
            int position = 0;
            if (currentCategory != null) {
                position = categoryAdapter.indexOf(currentCategory);
            }

            assert position != AdapterView.INVALID_POSITION: "The card has no category. This shouldn't happen.";

            categoryList.setItemChecked(position, true);
            categoryList.setSelection(position);

            categoryEdit.setText(categoryAdapter.getItem(position).getName());

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
            selectedCategory.setName(editText);
            categoryDao.update(selectedCategory);
            return null;
        }

        @Override
        public void onCancelled(){
            enableListeners();
        }

        @Override
        public void onPostExecute(Void result){
            categoryAdapter.sort();
            categoryAdapter.notifyDataSetChanged();

            // After sorting, we need to find position of edited category 
            // to select in the list.
            int position = categoryAdapter.indexOf(selectedCategory);
            categoryList.setItemChecked(position, true);
            categoryList.setSelection(position);

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
            assert editText != null : "Category's EditText shouldn't get null";
        }

        @Override
        public Category doInBackground(Void... params) {
            return categoryDao.createOrReturn(editText);
        }

        @Override
        public void onCancelled(){
        }

        @Override
        public void onPostExecute(Category result){
            assert result != null : "The result should never be null here";

            if (!categoryAdapter.hasItem(result)) {
                categoryAdapter.add(result);
            }

            categoryAdapter.sort();

            int position = categoryAdapter.indexOf(result);

            // Select and scroll to newly created category
            categoryList.setItemChecked(position, true);
            categoryList.setSelection(position);

            categoryAdapter.notifyDataSetChanged();
            categoryAdapter.sort();

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
            if (Strings.isNullOrEmpty(selectedCategory.getName())) {
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
            if (!Strings.isNullOrEmpty(selectedCategory.getName())) {
                categoryAdapter.remove(selectedCategory);
            }
            categoryAdapter.sort();
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


        public void sort() {
            sort(categoryComparator);
        }

        /**
         * @return true if the adapter has the category with the same name.
         */
        public boolean hasItem(Category category) {
            return indexOf(category) != AdapterView.INVALID_POSITION;
        }

        /**
         * Get the index of the category of the same name.
         * @param category the category
         * @return the index or AdapterView.INVALID_POSITION if the category is not found.
         */
        public int indexOf(Category category) {
            for (int i = 0; i < getCount(); i++) {
                if (categoryAdapter.getItem(i).getName().equals(category.getName())) {
                    return i;
                }
            }
            return AdapterView.INVALID_POSITION;
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

        /**
         * This comparator compares the category based on the following logic:
         * "Uncategorized" (id = 0) is always the top.
         * Other category are sorted in the lexicographical order.
         */
        private Comparator<Category> categoryComparator = new Comparator<Category>() {
            @Override
            public int compare(Category lhs, Category rhs) {
                if (lhs.getId() == 0 && rhs.getId() != 0) {
                    return -1;
                }

                if (lhs.getId() != 0 && rhs.getId() == 0) {
                    return 1;
                }

                return lhs.getName().compareTo(rhs.getName());
            }
        };
    }

    private OnItemClickListener listItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Category c = categoryAdapter.getItem(position);
            assert c != null : "Select a null category. This shouldn't happen";
            categoryEdit.setText(c.getName());
        }
    };

    private void enableListeners() {
        okButton.setOnClickListener(buttonOnClickListener);
        deleteButton.setOnClickListener(buttonOnClickListener);
        newButton.setOnClickListener(buttonOnClickListener);
        editButton.setOnClickListener(buttonOnClickListener);
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
