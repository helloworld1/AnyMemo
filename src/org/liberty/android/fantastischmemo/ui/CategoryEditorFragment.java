package org.liberty.android.fantastischmemo.ui;

import java.sql.SQLException;

import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CategoryDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import org.liberty.android.fantastischmemo.ui.CardEditor;
import org.liberty.android.fantastischmemo.ui.CardEditor;
import org.liberty.android.fantastischmemo.ui.CardEditor;
import org.liberty.android.fantastischmemo.ui.CardEditor;

import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Context;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

public class CategoryEditorFragment extends DialogFragment {

    private CardEditor mActivity;
    private CategoryDao categoryDao;
    private Card currentCard;
    private List<Category> categories;
    private static final String TAG = "CategoryEditorFragment";
    private CategoryAdapter categoryAdapter;
    private ListView categoryList;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (CardEditor)activity;
        categoryDao = mActivity.categoryDao;
        currentCard = mActivity.currentCard;
    }
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.category_dialog, container, false);
        categoryList = (ListView)v.findViewById(R.id.category_list);
        categoryList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        InitTask initTask = new InitTask();
        initTask.execute((Void)null);
        return v;
    }

    private class InitTask extends AsyncTask<Void, Void, Void> {

		@Override
        public void onPreExecute() {
            mActivity.setProgressBarIndeterminateVisibility(true);
            categoryAdapter = new CategoryAdapter(mActivity, android.R.layout.simple_list_item_single_choice);
            assert categoryList != null : "Couldn't find categoryList view";
            assert categoryAdapter != null : "New adapter is null";
            categoryList.setAdapter(categoryAdapter);
        }

        @Override
        public Void doInBackground(Void... params) {
            try {
                categories = categoryDao.queryForAll();
            } catch (SQLException e) {
                Log.e(TAG, "Error creating daos", e);
                throw new RuntimeException("Dao creation error");
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result){
            categoryAdapter.addAll(categories);
            mActivity.setProgressBarIndeterminateVisibility(false);
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
}
