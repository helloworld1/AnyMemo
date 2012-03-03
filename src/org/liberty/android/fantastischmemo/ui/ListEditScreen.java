/*
Copyright (C) 2010 Haowen Ning, Xinxin Wang

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

import org.liberty.android.fantastischmemo.*;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
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

public class ListEditScreen extends AMActivity implements OnItemClickListener {
	private String dbPath;
	@SuppressWarnings("unused")
	private static String TAG = "org.liberty.android.fantastischmemo.ListEditScreen";
	private CardListAdapter mAdapter;
	
	/* Initial position in the list */
	private int initPosition = 0;
	private List<Card> cards;

	public static String EXTRA_DBPATH = "dbpath";
	
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

	@Override
	public void onItemClick(AdapterView<?> parentView, View childView,
			int position, long id) {
		/* Click to go back to EditScreern with specific card cliced */
		Intent resultIntent = new Intent();
		resultIntent.putExtra(ListEditScreen.EXTRA_DBPATH,
				mAdapter.getItem(position).getId());
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	
	private class CardListAdapter extends ArrayAdapter<Card> implements
			SectionIndexer {
		/* quick index sections */
		private String[] sections;
		
		public CardListAdapter(Context context, int textViewResourceId,
				List<Card> cards) {
			super(context, textViewResourceId, cards);
			
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

			idView.setText("" + getItem(position).getId());
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
            case R.id.by_id:
            	new SortListTask().execute("id");
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
	
    
    /*
     * Async task that sort the list based on user input
     * */
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
			if(sortBy.equals("id")){
				mAdapter.sort(new Comparator<Card>(){
					@Override
					public int compare(Card c1, Card c2) {
							return c1.getId() - c2.getId();
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
			AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(
					ListEditScreen.this, dbPath);

			try {
				CardDao cardDao = helper.getCardDao();
				cards = cardDao.queryForAll();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		public void onPostExecute(Void result) {
			mAdapter = new CardListAdapter(ListEditScreen.this, initPosition, cards);
					
			ListView listView = (ListView) findViewById(R.id.item_list);
			listView.setAdapter(mAdapter);
			listView.setSelection(initPosition);
			listView.setOnItemClickListener(ListEditScreen.this);
			progressDialog.dismiss();
			
				
		}

	}
}
