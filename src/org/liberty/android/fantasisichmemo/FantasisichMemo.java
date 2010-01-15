package org.liberty.android.fantasisichmemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class FantasisichMemo extends Activity {
	private String dbName;
	private String dbPath;
	private TextView mDBView;
	private int returnValue;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent myIntent = new Intent();
        myIntent.setClass(this, FileBrowser.class);
        startActivityForResult(myIntent, 1);
        /*
        myIntent.setClass(this, MemoScreen.class);
        myIntent.putExtra("mode", "acq");
        startActivity(myIntent);
        */
        
        
        mDBView = (TextView)findViewById(R.id.dbContent);
        //Item resItem;
        //resItem = dbHelper.getItemById(4, 0);
        //mDBView.setText(new StringBuilder().append(resItem.getNote()));
        mDBView.setText(new StringBuilder().append(dbPath).append(dbName));
        
    }
    
    public void onResume(){
    	super.onResume();
    	if(returnValue == 1){
        mDBView = (TextView)findViewById(R.id.dbContent);
        mDBView.setText(new StringBuilder().append(dbPath).append(dbName));
        Item resItem;
        DatabaseHelper dbHelper = new DatabaseHelper(this, dbPath, dbName);
        resItem = dbHelper.getItemById(4, 0);
        mDBView.setText(new StringBuilder().append(resItem.getQuestion()));
    	}
    	
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	switch(requestCode){
    	case (1):
    		if(resultCode == Activity.RESULT_OK){
    			dbName = data.getStringExtra("org.liberty.android.fantasisichmemo.dbName");
    			dbPath = data.getStringExtra("org.liberty.android.fantasisichmemo.dbPath");
    			dbPath += "/";
    			returnValue = 1;
    			
    		}
    		
    		
    	}
    }
}