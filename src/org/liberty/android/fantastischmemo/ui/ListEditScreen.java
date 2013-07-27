/*
Copyright (C) 2012 Haowen Ning, Xinxin Wang

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

import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import org.liberty.android.fantastischmemo.utils.CardTextUtil;
import org.liberty.android.fantastischmemo.utils.CardTextUtilFactory;

import roboguice.RoboGuice;
import roboguice.inject.ContextScope;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class ListEditScreen extends AMActivity {
    private String dbPath;

    private CardListAdapter mAdapter;

    private AnyMemoDBOpenHelper dbOpenHelper;

    private ListView listView;

    private AMPrefUtil amPrefUtil;

    private enum SortMethod{ORDINAL, QUESTION, ANSWER};

    private CardTextUtilFactory cardTextUtilFactory;

    private CardTextUtil cardTextUtil;

    /* Initial position in the list */

    private List<Card> cards;

    public static String EXTRA_DBPATH = "dbpath";

    @Inject
    public void setAmPrefUtil(AMPrefUtil amPrefUtil) {
        this.amPrefUtil = amPrefUtil;
    }

    @Inject
    public void setCardTextUtilFactory(CardTextUtilFactory cardTextUtilFactory) {
        this.cardTextUtilFactory = cardTextUtilFactory;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_edit_screen);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dbPath = extras.getString(ListEditScreen.EXTRA_DBPATH);
        }

        InitTask initTask = new InitTask();
        initTask.execute((Void) null);

    }
    
    private void sortList(SortMethod sort) { 
        switch(sort)
        {
          case ORDINAL:
              mAdapter.sort(new Comparator<Card>(){
                  @Override
                  public int compare(Card c1, Card c2) {
                          return c1.getOrdinal() - c2.getOrdinal();
                  };
              });
              break;
           case QUESTION:
               mAdapter.sort(new Comparator<Card>(){
                   @Override
                   public int compare(Card c1, Card c2) {
                           return c1.getQuestion().compareTo(c2.getQuestion());
                   };
               });
               break;
           case ANSWER:
               mAdapter.sort(new Comparator<Card>(){
                   @Override
                   public int compare(Card c1, Card c2) {
                           return c1.getAnswer().compareTo(c2.getAnswer());
                   };
               });
               break;
         }
    }

    private class CardListAdapter extends ArrayAdapter<Card> implements
            SectionIndexer {
        /* quick index sections */
        private String[] sections;

        public CardListAdapter(Context context, List<Card> cards) {
            super(context, 0, cards);

            int sectionSize = getCount() / 100;
            sections = new String[sectionSize];
            for (int i = 0; i < sectionSize; i++) {
                sections[i] = String.valueOf(i*100);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(R.layout.list_edit_screen_item, null);
            }
            TextView idView = (TextView) convertView.findViewById(R.id.item_id);
            TextView questionView = (TextView) convertView
                    .findViewById(R.id.item_question);
            TextView answerView = (TextView) convertView
                    .findViewById(R.id.item_answer);

            idView.setText("" + getItem(position).getOrdinal());

            // 0 -> question 1-> answer
            List<Spannable> fields = cardTextUtil.getFieldsToDisplay(getItem(position));
            questionView.setText(fields.get(0));
            answerView.setText(fields.get(1));

            return convertView;
        }

        /* Display the quick index when the user is scrolling */
        @Override
        public int getPositionForSection(int section) {
            return section * 100;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 1;
        }

        @Override
        public Object[] getSections() {
            return sections;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_edit_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
           case R.id.sort:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setSingleChoiceItems(R.array.sort_by_options, SortMethod.valueOf(amPrefUtil.getSavedString(AMPrefKeys.LIST_SORT_BY_METHOD_PREFIX, dbPath, getResources().getStringArray(R.array.sort_by_options_values)[0])).ordinal(), 
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {                              
                                    String[] items = getResources().getStringArray(R.array.sort_by_options_values);
                                    ListEditScreen.this.sortList(SortMethod.valueOf(items[which]));  
                                    amPrefUtil.putSavedString(AMPrefKeys.LIST_SORT_BY_METHOD_PREFIX, dbPath, items[which]);
                                    dialog.dismiss();
                            }
                        })
                        .show();              
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private OnItemClickListener listItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parentView, View childView,
                int position, long id) {
            DialogFragment df = new ListEditActionsFragment();
            Bundle b = new Bundle();
            b.putString(ListEditActionsFragment.EXTRA_DBPATH, dbPath);
            b.putInt(ListEditActionsFragment.EXTRA_CARD_ID, mAdapter.getItem(position).getId());
            df.setArguments(b);
            df.show(getSupportFragmentManager(), "ListEditActions");
        }
    };

    private class InitTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute() {
            progressDialog = new ProgressDialog(ListEditScreen.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(
                    ListEditScreen.this, dbPath);

            CardDao cardDao = dbOpenHelper.getCardDao();
            cards = cardDao.queryForAll();

            ContextScope scope = RoboGuice.getInjector(ListEditScreen.this).getInstance(ContextScope.class);
            // Make sure the method is running under the context
            // The AsyncTask thread does not have the context, so we need
            // to manually enter the scope.
            synchronized(ContextScope.class) {
                scope.enter(ListEditScreen.this);
                try {
                    cardTextUtil = cardTextUtilFactory.create(dbPath);
                } finally {
                    scope.exit(ListEditScreen.this);
                }
            }

            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            int initPosition = amPrefUtil.getSavedInt(AMPrefKeys.LIST_EDIT_SCREEN_PREFIX, dbPath, 0);
            mAdapter = new CardListAdapter(ListEditScreen.this, cards);

            listView = (ListView) findViewById(R.id.item_list);
            listView.setAdapter(mAdapter);
            listView.setSelection(initPosition);
            listView.setOnItemClickListener(listItemClickListener);
            
            ListEditScreen.this.sortList(SortMethod.valueOf(amPrefUtil.getSavedString(AMPrefKeys.LIST_SORT_BY_METHOD_PREFIX, dbPath, getResources().getStringArray(R.array.sort_by_options_values)[0])));
            progressDialog.dismiss();
        }

    }

    @Override
    public void onDestroy() {
        amPrefUtil.putSavedInt(AMPrefKeys.LIST_EDIT_SCREEN_PREFIX, dbPath, listView.getFirstVisiblePosition());
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
        super.onDestroy();
    }

    @Override
    public void restartActivity() {
        Intent myIntent = new Intent(this, ListEditScreen.class);
        assert dbPath != null : "Use null dbPath to restartAcitivity";
        myIntent.putExtra(EXTRA_DBPATH, dbPath);
        finish();
        startActivity(myIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        // Refresh activity when the data has been changed.
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        } else {
            restartActivity();
        }
    }

    }
