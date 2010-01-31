package org.liberty.android.fantastischmemo;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddItemScreen extends Activity implements OnClickListener{
	private EditText entryId;
	private EditText entryQuestion;
	private EditText entryAnswer;
	private Button btnNew;
	private Button btnEdit;
	private Button btnGotoid;
	private Button btnBack;
	private Button btnNext;
	private Button btnPrevious;
	private String dbName;
	private String dbPath;
	private int openId = -1;
	private DatabaseHelper dbHelper;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_screen);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dbPath = extras.getString("dbpath");
			dbName = extras.getString("dbname");
			openId = extras.getInt("openid");
		}
		dbHelper = new DatabaseHelper(this, dbPath, dbName);
		
		
        entryId = (EditText)findViewById(R.id.add_item_id_entry);
        entryQuestion = (EditText)findViewById(R.id.add_item_question_entry);
        entryAnswer = (EditText)findViewById(R.id.add_item_answer_entry);
        btnNew = (Button)findViewById(R.id.add_item_but_new);
        btnEdit = (Button)findViewById(R.id.add_item_but_edit);
        btnGotoid = (Button)findViewById(R.id.add_item_but_gotoid);
        btnBack = (Button)findViewById(R.id.add_item_but_back);
        btnNext = (Button)findViewById(R.id.add_item_but_next);
        btnPrevious = (Button)findViewById(R.id.add_item_but_previous);
        btnNew.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        btnGotoid.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        if(openId != -1 && dbHelper.getNewId() > openId){
        	entryId.setText("" + openId);
        	Item item = dbHelper.getItemById(openId, 0);
        	entryQuestion.setText(item.getQuestion());
        	entryAnswer.setText(item.getAnswer());
        }
        
    }
    
    private void setEntryById(int id){
    	int maxId = dbHelper.getNewId();
    	if(id == 0){
    		id = 1;
    	}
    	if(maxId == id){
    		entryQuestion.setText("");
    		entryAnswer.setText("");
    		entryId.setText("" + id);
    	}
    	else if(maxId < id){
    		entryId.setText("" + maxId);
    		entryQuestion.setText("");
    		entryAnswer.setText("");
    	}
    	else if(maxId > id && id > 0){
    		Item item = dbHelper.getItemById(id, 0);
    		entryQuestion.setText(item.getQuestion());
    		entryAnswer.setText(item.getAnswer());
    		entryId.setText("" + id);
    	}
    
    	
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	dbHelper.close();
    }

	@Override
	public void onClick(View v) {
		if(v == btnNew){
			entryQuestion.setText("");
			entryAnswer.setText("");
			entryId.setText("" + dbHelper.getNewId());
			
			
		}
		
		if(v == btnEdit){
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put("_id", entryId.getText().toString());
			hm.put("question", entryQuestion.getText().toString());
			hm.put("answer", entryAnswer.getText().toString());
			Item item = new Item();
			item.setData(hm);
			dbHelper.addOrReplaceItem(item);
		}
		if(v == btnGotoid){
			int id = -1;
			try{
				id = Integer.parseInt(entryId.getText().toString());
			}
			catch(Exception e){
				return;
			}
			setEntryById(id);
		}
		
		if(v == btnBack){
			finish();
		}
		
		if(v == btnNext){
			int id = -1;
			try{
				id = Integer.parseInt(entryId.getText().toString());
			}
			catch(Exception e){
				return;
			}
			setEntryById(id + 1);
			
		}
		if(v == btnPrevious){
			int id = -1;
			try{
				id = Integer.parseInt(entryId.getText().toString());
			}
			catch(Exception e){
				return;
			}
			setEntryById(id - 1);
			
		}
		
	}

}
