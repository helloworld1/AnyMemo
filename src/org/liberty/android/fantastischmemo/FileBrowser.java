package org.liberty.android.fantastischmemo;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class FileBrowser extends ListActivity {
	private enum DISPLAYMODE{ABSOLUTE, RELATIVE;}
	private final DISPLAYMODE displayMode = DISPLAYMODE.RELATIVE;
	private List<String> directoryEntries = new ArrayList<String>();
	private File currentDirectory = new File("/");
	private String defaultRoot;
	private String fileExtension = ".db";
	private Context mContext;
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		defaultRoot = extras.getString("default_root");
		fileExtension = extras.getString("file_extension");
		mContext = this;
		
		browseToRoot();
	}
	
	private void browseToRoot(){
		if(defaultRoot == null){
			File sdPath = new File(getString(R.string.default_sd_path));
			sdPath.mkdir();
			browseTo(sdPath);
		}
		else{
			browseTo(new File(defaultRoot + "/"));
		}
	}
	
	
	private void browseTo(final File aDirectory){
		if(aDirectory.isDirectory()){
			this.setTitle(aDirectory.getPath());
			this.currentDirectory = aDirectory;
			fill(aDirectory.listFiles());
		}
		else{
			OnClickListener okButtonListener = new OnClickListener(){
				public void onClick(DialogInterface arg0, int arg1){
					openFile(aDirectory);
				}
			};
			OnClickListener cancelButtonListener = new OnClickListener(){
				public void onClick(DialogInterface arg0, int arg1){
					//
				}
			};
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Question");
			alertDialog.setMessage("Do you want to open this file?\n" + aDirectory.getName());
			alertDialog.setButton("OK", okButtonListener);
			alertDialog.setButton("Cancel", cancelButtonListener);
			
		}
	}
	
	private void fill(File[] files){
		this.directoryEntries.clear();
		
		if(this.currentDirectory.getParent() != null){
			this.directoryEntries.add(getString(R.string.up_one_level));
		}
		switch(this.displayMode){
		case ABSOLUTE:
			for(File file : files){
				this.directoryEntries.add(file.getPath());
			}
			break;
		case RELATIVE:
			int currentPathStringLength = this.currentDirectory.getAbsolutePath().length();
			for(File file: files){
				if(file.isDirectory()){
						this.directoryEntries.add(file.getAbsolutePath().substring(currentPathStringLength) + "/");
				}
				if(file.getName().endsWith(fileExtension)){
						this.directoryEntries.add(file.getAbsolutePath().substring(currentPathStringLength));
				}
				
			}
		}
		ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, R.layout.file_browser, this.directoryEntries);
		directoryList.sort(new Comparator<String>() {
			public int compare(String s1, String s2){
				if(s1.equals("..")){
					return -1;
				}
				else if(s2.equals("..")){
					return 1;
				}
				else if(s1.endsWith("/") && !s2.endsWith("/")){
					return -1;
				}
				else if(s2.endsWith("/") && !s1.endsWith("/")){
					return 1;
				}
				else{
					return s1.compareTo(s2);
				}
			}
			public boolean equals(String s1, String s2){
				return s1.equals(s2);
			}
		});
		this.setListAdapter(directoryList);
	}
	
	private void openFile(File aDirectory){
		Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("file://" + aDirectory));
		this.startActivity(myIntent);
		
		
	}
	
	private void upOneLevel(){
		if(this.currentDirectory.getParent() != null){
			this.browseTo(this.currentDirectory.getParentFile());
		}
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id){
		super.onListItemClick(l, v, position, id);
		String selectedFileString = this.directoryEntries.get(position);
		if(selectedFileString.equals(getString(R.string.current_dir))){
			this.browseTo(this.currentDirectory);
		}
		else if(selectedFileString.equals(getString(R.string.up_one_level))){
			this.upOneLevel();
		}
		else{
			File clickedFile = null;
			switch(this.displayMode){
			case RELATIVE:
				clickedFile = new File(this.currentDirectory.getAbsolutePath() + this.directoryEntries.get(position));
				break;
			case ABSOLUTE:
				clickedFile = new File(this.directoryEntries.get(position));
				break;
			}
			if(clickedFile != null){
				try{
					if(clickedFile.isDirectory()){
						this.browseTo(clickedFile);
					}
					else if(clickedFile.isFile()){
						Intent resultIntent = new Intent();
		
						resultIntent.putExtra("org.liberty.android.fantastischmemo.dbName", clickedFile.getName());
						resultIntent.putExtra("org.liberty.android.fantastischmemo.dbPath", clickedFile.getParent());
						this.setResult(Activity.RESULT_OK, resultIntent);
						finish();
						
					}
				}
		
				catch(Exception e){
					new AlertDialog.Builder(this).setMessage(e.toString()).show();
					browseTo(new File("/"));
				}
			}
		}
		
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.file_browser_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.file_browser_createdb:{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(this.getString(R.string.fb_create_db));
			alert.setMessage(this.getString(R.string.fb_create_db_message));
			final EditText input = new EditText(this);
			alert.setView(input);
			alert.setPositiveButton(this.getString(R.string.ok_text), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which ){
					String value = input.getText().toString();
					if(!value.endsWith(".db")){
						value += ".db";
					}
					DatabaseHelper dbHelper = new DatabaseHelper(mContext, currentDirectory.getAbsolutePath(), value, 1);
					try{
						dbHelper.createEmptyDatabase();
					}
					catch(Exception e){
					}
					dbHelper.close();
					browseTo(currentDirectory);
					
				}
			});
			alert.setNegativeButton(this.getString(R.string.cancel_text), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which ){
					
				}
			});
			alert.show();
			return true;
		}
		
		case R.id.file_browser_createdirectory:{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(this.getString(R.string.fb_create_dir));
			alert.setMessage(this.getString(R.string.fb_create_dir_message));
			final EditText input = new EditText(this);
			alert.setView(input);
			alert.setPositiveButton(this.getString(R.string.ok_text), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which ){
					String value = input.getText().toString();
					File newDir = new File(currentDirectory + "/" + value);
					newDir.mkdir();
					browseTo(currentDirectory);
					
				}
			});
			alert.setNegativeButton(this.getString(R.string.cancel_text), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which ){
					
					
				}
			});
			alert.show();
			return true;
		}
				
	    }
	    return false;
	}

}
