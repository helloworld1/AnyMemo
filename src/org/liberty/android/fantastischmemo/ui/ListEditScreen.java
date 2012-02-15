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
package org.liberty.android.fantastischmemo.ui;

import org.liberty.android.fantastischmemo.*;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
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
	private static String TAG = "org.liberty.android.fantastischmemo.ListEditScreen";
	private CardListAdapter mAdapter;
	
	/* Initial position in the list */
	private int initPosition = 1;
	private List<Card> cards;

	public static String EXTRA_DBPATH = "dbpath";
	public static String EXTRA_CARD_ID = "id";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_edit_screen);
		Log.v(TAG, "xinxin_test\n");
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dbPath = extras.getString(ListEditScreen.EXTRA_DBPATH);
		}

		Log.i(TAG, "dbPath: " + dbPath + ", initPosition: " + initPosition);
		InitTask initTask = new InitTask();
		initTask.execute((Void) null);

		final Button byID = (Button) findViewById(R.id.button_col1);
		byID.setOnClickListener(sortButtonOnClickListener);
		
		final Button byQuestion = (Button) findViewById(R.id.button_col2);
		byQuestion.setOnClickListener(sortButtonOnClickListener);

		final Button byAnswer = (Button) findViewById(R.id.button_col3);
		byAnswer.setOnClickListener(sortButtonOnClickListener);
		
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
				sections[i] = "" + (i * 100);
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
			return position / 100;
		}

		@Override
		public Object[] getSections() {
			return sections;
		}
	}

	private View.OnClickListener sortButtonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.button_col1:
				Log.v(TAG, "sort by id");
				new SortListTask().execute("id");
				break;
			case R.id.button_col2:
				Log.v(TAG, "sort by question");
				new SortListTask().execute("question");
				break;
			case R.id.button_col3:
				Log.v(TAG, "sort by answer");
				new SortListTask().execute("answer");
				break;
			}
			

		}
	};

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
			mAdapter.sort(new Comparator<Card>(){
				
				@Override
				public int compare(Card c1, Card c2) {
					final String sb = sortBy;
					if(sb.equals("id")){
						return c1.getId() - c2.getId();
					} else if (sb.equals("question")){
						return c1.getQuestion().compareTo(c2.getQuestion());
					} else {
						return c1.getAnswer().compareTo(c2.getAnswer());
					}
				};
		    });
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
			mAdapter = new CardListAdapter(ListEditScreen.this, initPosition,
					cards);
			ListView listView = (ListView) findViewById(R.id.item_list);
			listView.setAdapter(mAdapter);
			listView.setSelection(initPosition);
			listView.setOnItemClickListener(ListEditScreen.this);
			progressDialog.dismiss();
		}

	}
}
