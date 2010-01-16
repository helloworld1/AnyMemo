package org.liberty.android.fantasisichmemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FantasisichMemo extends Activity implements OnClickListener{
	private String dbName;
	private String dbPath;
	private TextView mDBView;
	private int returnValue;
	private Button btnNew;
	private Button btnRecent;
	private Button btnOption;
	private Button btnAbout;
	private Button btnExit;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btnNew = (Button)this.findViewById(R.id.main_open_database_button);
        btnRecent = (Button)this.findViewById(R.id.main_open_recent_button);
        btnOption = (Button)this.findViewById(R.id.main_option_button);
        btnAbout = (Button)this.findViewById(R.id.main_about_button);
        btnExit = (Button)this.findViewById(R.id.main_exit_button);
        btnNew.setOnClickListener(this);
        btnRecent.setOnClickListener(this);
        btnOption.setOnClickListener(this);
        btnAbout.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        //Intent myIntent = new Intent();
        //myIntent.setClass(this, FileBrowser.class);
        //startActivityForResult(myIntent, 1);
        /*
        myIntent.setClass(this, MemoScreen.class);
        myIntent.putExtra("mode", "acq");
        startActivity(myIntent);
        */
        
        
        //Item resItem;
        //resItem = dbHelper.getItemById(4, 0);
        //mDBView.setText(new StringBuilder().append(resItem.getNote()));
        
    }
    
    public void onClick(View v){
    	if(v == btnNew){
           Intent myIntent = new Intent();
           myIntent.setClass(this, FileBrowser.class);
           startActivityForResult(myIntent, 1);
            
    	}
    	if(v == btnExit){
    		finish();
    	}
    	
    }
    
    public void onResume(){
    	super.onResume();
    	if(returnValue == 1){
        Intent myIntent = new Intent();
        myIntent.setClass(this, MemoScreen.class);
        myIntent.putExtra("dbname", dbName);
        myIntent.putExtra("dbpath", dbPath);
        startActivity(myIntent);
        returnValue = 0;
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