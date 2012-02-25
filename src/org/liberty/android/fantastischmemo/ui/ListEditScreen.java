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
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ListEditScreen extends AMActivity implements OnItemClickListener {
	private String dbPath;
	private static String TAG = "org.liberty.android.fantastischmemo.ListEditScreen";
	private CardListAdapter mAdapter;
	
	/* Initial position in the list */
	private int initPosition = 1;
	private List<Card> cards;

	public static String EXTRA_DBPATH = "dbpath";
	private static String curPreviewOn = "id";
	
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

		final Button byID = (Button) findViewById(R.id.by_id);
		byID.setOnClickListener(sortButtonOnClickListener);
		
		final Button byQuestion = (Button) findViewById(R.id.by_question);
		byQuestion.setOnClickListener(sortButtonOnClickListener);

		final Button byAnswer = (Button) findViewById(R.id.by_answer);
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

	private class SectionObject {
		Card card;
		
		public SectionObject(Card card){
			this.card = card;
		}
		
		public void setPreviewLetter(String previewOn){
			curPreviewOn = previewOn;
		}
		
		public String toString(){
			Log.v(TAG, "curPreviewon "+ curPreviewOn);
			if(curPreviewOn == "id"){
				return String.valueOf((card.getId()-1)*100);
			} else if (curPreviewOn == "question"){
				return card.getQuestion().substring(0,2);
			} else {
				return card.getAnswer().substring(0,2);
			}
			
		}
		
		
		public int compareTo(SectionObject anotherSectionObjec, String sortBy){
			if(sortBy == "id"){
				return card.getId() - anotherSectionObjec.card.getId(); 
			} else if (sortBy == "question"){
				return card.getQuestion().compareTo(anotherSectionObjec.card.getQuestion());
			} else {
				return card.getAnswer().compareTo(anotherSectionObjec.card.getAnswer());
			}
			
		}
		
	}
	
	private class CardListAdapter extends ArrayAdapter<Card> implements
			SectionIndexer {
		/* quick index sections */
		private String[] sections;
		private String[] sectionsQuestion;
		private String[] sectionsAnswer;
		private String curOrderedBy = "id";
		private SectionObject[] sectionObjects;

        HashMap<String, Integer> questionAlphaIndexer = new HashMap<String, Integer>();
        HashMap<String, Integer> answerAlphaIndexer = new HashMap<String, Integer>();
		
		public CardListAdapter(Context context, int textViewResourceId,
				List<Card> cards) {
			super(context, textViewResourceId, cards);
			int sectionSize = getCount() / 100;
			sections = new String[sectionSize];
			sectionObjects = new SectionObject[sectionSize];
			for (int i = 0; i < sectionSize; i++) {
				sections[i] = "" + (i/100 * 100);
				sectionObjects[i] = new SectionObject(cards.get(i));
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
			
			section = section<0?0:section;
			
			
			if(curOrderedBy == "id"){
				Log.v(TAG, "id getPositionForSection");
				return section * 100;
			} else if(curOrderedBy == "question"){
				Log.v(TAG, "question getPositionForSection");
				String qLetter = sectionsQuestion[section];
				return questionAlphaIndexer.get(qLetter);
			} else { //answer
				Log.v(TAG, "answer getPositionForSection");
				String aLetter = sectionsAnswer[section];
				return answerAlphaIndexer.get(aLetter);
			}
		}

		@Override
		public int getSectionForPosition(int position) {
			Log.v(TAG, "xinixn getSectionForPosition");
			return position/100;
/*			
			position = position<0?0:position;
			
			if(curOrderedBy == "id"){
				return position/100;
			} else if(curOrderedBy == "question"){
				String qLetter = sectionsQuestion[position];
				return questionAlphaIndexer.get(qLetter);
			} else { 
				String aLetter = sectionsQuestion[position];
				return questionAlphaIndexer.get(aLetter);
			}
*/
			
		}

		@Override
		public Object[] getSections() {
			return sectionObjects;
//			
//			if(curOrderedBy == "id"){
//				Log.v(TAG, "return section xinxin");
//				return sections;
//			} else if(curOrderedBy == "question"){
//				Log.v(TAG, "return question section xinxin");
//				return sectionsQuestion;
//			} else {
//				Log.v(TAG, "return answer section xinxin");
//				return sectionsAnswer;
//			}
		}
		
		public void updateSection(String orderBy){
			List<String> sectionList = new ArrayList<String>();
			
			curOrderedBy = orderBy;
			curPreviewOn = orderBy;
			Log.v(TAG, curOrderedBy + "updateSection xinxin");
			
			if(orderBy == "question"){
				if(sectionsQuestion == null){
		            String cur = "";
		            for(int i = 0; i < getCount(); i++) {
		                Card c = getItem(i);
		                if (c.getQuestion().length() >= 2) {
		                    String index = c.getQuestion().substring(0, 2).toLowerCase();
		                        
		                    if (index != null && !index.equals(cur)){
		                        questionAlphaIndexer.put(index, i);
		                        sectionList.add(index);
		                        cur = index;
		                    }
		                }
		            }
		            sectionsQuestion = new String[sectionList.size()];
		            sectionList.toArray(sectionsQuestion);
				}
				
			} else if(orderBy == "answer"){
				if(sectionsAnswer == null){
		            String cur = "";
		            for(int i = 0; i < getCount(); i++) {
		                Card c = getItem(i);
		                if (c.getAnswer().length() >= 2) {
		                    String index = c.getAnswer().substring(0, 2).toLowerCase();
		                        
		                    if (index != null && !index.equals(cur)){
		                        answerAlphaIndexer.put(index, i);
		                        sectionList.add(index);
		                        cur = index;
		                    }
		                }
		            }
		            sectionsAnswer = new String[sectionList.size()];
		            sectionList.toArray(sectionsAnswer);
				}
				
			}
			
		}
		
		public void sortSectionObject(final String sortBy){
			Arrays.sort(sectionObjects, new Comparator<SectionObject>(){
				public int compare(SectionObject so1, SectionObject so2) {
					return so1.compareTo(so2, sortBy);
				}
			});
		}
	}

	private View.OnClickListener sortButtonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.by_id:
				Log.v(TAG, "sort by id");
				new SortListTask().execute("id");
				break;
			case R.id.by_question:
				Log.v(TAG, "sort by question");
				new SortListTask().execute("question");
				break;
			case R.id.by_answer:
				Log.v(TAG, "sort by answer");
				new SortListTask().execute("answer");
				break;
			}
			

		}
	};

	
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_edit_screen_sort_menu, menu);
        menu.setHeaderTitle("sort by"); //R.string.menu_text);
        
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
            	Log.v(TAG, "clicking on sort from menu xinxin");
                mAdapter.updateSection("id");
            	new SortListTask().execute("id");
                return true;
            case R.id.by_question:
                mAdapter.updateSection("question");
            	new SortListTask().execute("question");
                return true;
            case R.id.by_answer:
                mAdapter.updateSection("answer");
            	new SortListTask().execute("answer");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
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
			
			mAdapter.sortSectionObject(sortBy);
			
			Log.v(TAG, "on post execute");
			
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
