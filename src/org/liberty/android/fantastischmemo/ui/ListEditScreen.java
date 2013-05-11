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

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;

import android.app.Activity;

import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;

import android.support.v4.app.DialogFragment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.app.ProgressDialog;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class ListEditScreen extends AMActivity {
	private String dbPath;

	private static String TAG = ListEditScreen.class.getCanonicalName();

	private CardListAdapter mAdapter;

    private AnyMemoDBOpenHelper dbOpenHelper;
    
	private ListView listView;
	
	private AMPrefUtil amPrefUtil;
	
	/* Initial position in the list */

	private List<Card> cards;

	public static String EXTRA_DBPATH = "dbpath";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_edit_screen);
		        
        amPrefUtil = new AMPrefUtil(this);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dbPath = extras.getString(ListEditScreen.EXTRA_DBPATH);
		}

		InitTask initTask = new InitTask();
		initTask.execute((Void) null);
		
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
			questionView.setText(getItem(position).getQuestion());
			answerView.setText(getItem(position).getAnswer());

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

	
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_edit_screen_sort_menu, menu);
        menu.setHeaderTitle(R.string.sort_by_text);
        
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
            case R.id.by_ord:
            	new SortListTask().execute("ord");
                return true;
            case R.id.by_question:
            	new SortListTask().execute("question");
                return true;
            case R.id.by_answer:
            	new SortListTask().execute("answer");
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
	
    
    /*
     * Async task that sort the list based on user input
     */
	private class SortListTask extends AsyncTask<String, Void, String> {
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


		public void onPostExecute(final String sortBy) {
			if(sortBy.equals("ord")){
				mAdapter.sort(new Comparator<Card>(){
					@Override
					public int compare(Card c1, Card c2) {
							return c1.getOrdinal() - c2.getOrdinal();
					};
			    });			
			}
			else if(sortBy.equals("question")){
				mAdapter.sort(new Comparator<Card>(){
					@Override
					public int compare(Card c1, Card c2) {
							return c1.getQuestion().compareTo(c2.getQuestion());
					};
			    });			
			} else {
				mAdapter.sort(new Comparator<Card>(){
					@Override
					public int compare(Card c1, Card c2) {
							return c1.getAnswer().compareTo(c2.getAnswer());
					};
			    });
			}
			
			progressDialog.dismiss();
		}

		@Override
		protected String doInBackground(String... sortBy) {
			return sortBy[0];
		}

	}

	
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

			try {
				CardDao cardDao = dbOpenHelper.getCardDao();
				cards = cardDao.queryForAll();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		public void onPostExecute(Void result) {
            int initPosition = amPrefUtil.getSavedId(AMPrefKeys.LIST_EDIT_SCREEN_PREFIX, dbPath, 0);
			mAdapter = new CardListAdapter(ListEditScreen.this, cards);
					
			listView = (ListView) findViewById(R.id.item_list);
			listView.setAdapter(mAdapter);
			listView.setSelection(initPosition);
			listView.setOnItemClickListener(listItemClickListener);
			progressDialog.dismiss();
			
				
		}

	}

    @Override
    public void onDestroy() {
        amPrefUtil.setSavedId(AMPrefKeys.LIST_EDIT_SCREEN_PREFIX, dbPath, listView.getFirstVisiblePosition());
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
