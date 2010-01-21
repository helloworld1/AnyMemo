package org.liberty.android.fantastischmemo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.liberty.android.fantastischmemo.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileBrowser extends ListActivity {
	private enum DISPLAYMODE{ABSOLUTE, RELATIVE;}
	private final DISPLAYMODE displayMode = DISPLAYMODE.RELATIVE;
	private List<String> directoryEntries = new ArrayList<String>();
	private File currentDirectory = new File("/");
	private String defaultRoot;
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		defaultRoot = extras.getString("default_root");
		
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
				this.directoryEntries.add(file.getAbsolutePath().substring(currentPathStringLength));
				
			}
		}
		ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, R.layout.file_browser, this.directoryEntries);
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

}
