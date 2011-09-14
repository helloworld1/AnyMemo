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
package org.liberty.android.fantastischmemo;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.ListView;
import android.util.Log;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class FileBrowser extends AMActivity implements OnItemClickListener, OnItemLongClickListener{
	private enum DISPLAYMODE{ABSOLUTE, RELATIVE;}
	private final DISPLAYMODE displayMode = DISPLAYMODE.RELATIVE;
	private ArrayList<String> directoryEntries = new ArrayList<String>();
	private File currentDirectory = new File("/");
	private String defaultRoot;
	private String[] fileExtensions;
	private Context mContext;
    private ListView fbListView;
    private final static String TAG = "org.liberty.android.fantastischmemo.FileBrowser";
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
        if(extras != null){
            defaultRoot = extras.getString("default_root");
            String ext =  extras.getString("file_extension");
            if (ext != null) {
                fileExtensions = ext.split(",");
            }
            else {
                fileExtensions = new String[]{".db"};
            }
        }
        else{
            fileExtensions = new String[]{".db"};
            defaultRoot = null;
        }
        setContentView(R.layout.file_browser);
		mContext = this;
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if(defaultRoot == null){
            defaultRoot = settings.getString("saved_fb_path", null);
        }

		if(defaultRoot == null || defaultRoot.equals("")){
			File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_dir));
			sdPath.mkdir();
			
			currentDirectory = sdPath;
		}
		else{
			currentDirectory = new File(defaultRoot + "/");
		}
	}

    @Override
    public void onResume(){
        super.onResume();
        browseTo(currentDirectory);
    }
	
	
	
	private void browseTo(final File aDirectory){
		if(aDirectory.isDirectory()){
			this.setTitle(aDirectory.getPath());
			this.currentDirectory = aDirectory;
			fill(aDirectory.listFiles());
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
                else{
                    for(String fileExtension : fileExtensions){
                        if(file.getName().toLowerCase().endsWith(fileExtension.toLowerCase())){
                                this.directoryEntries.add(file.getAbsolutePath().substring(currentPathStringLength));
                        }
                    }
                }
				
			}
		}

        fbListView = (ListView)findViewById(R.id.file_list);
		FileBrowserAdapter directoryList = new FileBrowserAdapter(this, R.layout.filebrowser_item, directoryEntries);
	    fbListView.setAdapter(directoryList);
        fbListView.setOnItemClickListener(this);
        fbListView.setOnItemLongClickListener(this);
	}
	
	
	private void upOneLevel(){
		if(this.currentDirectory.getParent() != null){
			this.browseTo(this.currentDirectory.getParentFile());
		}
	}
	
    @Override
	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id){
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
                        /* Save the current path */
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("saved_fb_path", clickedFile.getParent());
                        editor.commit();
                        fileClickAction(clickedFile.getName(),  clickedFile.getParent());
						
					}
				}
		
				catch(Exception e){
					new AlertDialog.Builder(this).setMessage(e.toString()).show();
					browseTo(new File("/"));
				}
			}
		}

		
		
	}

    protected void fileClickAction(String name, String path){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("org.liberty.android.fantastischmemo.dbName", name);
        resultIntent.putExtra("org.liberty.android.fantastischmemo.dbPath", path);

        this.setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?>  parent, View  view, int position, long id){
		String selectedFileString = this.directoryEntries.get(position);
		if(selectedFileString.equals(getString(R.string.current_dir))){
            /* Do nothing */
		}
		else if(selectedFileString.equals(getString(R.string.up_one_level))){
            /* Do nithing */
		}
		else{
            final int pos = position;
			final File clickedFile;
			switch(this.displayMode){
			case RELATIVE:
				clickedFile = new File(this.currentDirectory.getAbsolutePath() + this.directoryEntries.get(position));
				break;
			case ABSOLUTE:
				clickedFile = new File(this.directoryEntries.get(position));
				break;
            default:
                /* Since clickedFile is final, it have to be initiated */
                clickedFile = new File(this.currentDirectory.getAbsolutePath() + this.directoryEntries.get(position));
			}
			if(clickedFile != null){
				try{
					if(clickedFile.isDirectory()){
						this.browseTo(clickedFile);
					}
					else if(clickedFile.isFile()){
                        new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.fb_edit_dialog_title))
                            .setItems(R.array.fb_dialog_list, new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    if(which == 0){
                                        /* Delete */
                                        new AlertDialog.Builder(FileBrowser.this)
                                            .setTitle(getString(R.string.detail_delete))
                                            .setMessage(getString(R.string.fb_delete_message))
                                            .setPositiveButton(getString(R.string.detail_delete), new DialogInterface.OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int which ){
                                                    clickedFile.delete();
                                                    File dir = new File(clickedFile.getParent());
                                                    Log.v(TAG, "DIR: " + dir.toString());
                                                    browseTo(dir);
                                                }
                                            })
                                            .setNegativeButton(getString(R.string.cancel_text), null)
                                            .create()
                                            .show();

                                    }
                                    else if(which == 1){
                                        /* Clone */
                                        String srcDir = clickedFile.getAbsolutePath();
                                        String destDir = srcDir.replaceAll(".db", ".clone.db");
                                        try{
                                            copyFile(srcDir, destDir);
                                        }
                                        catch(IOException e){
                                            new AlertDialog.Builder(FileBrowser.this)
                                                .setTitle(getString(R.string.fail))
                                                .setMessage(getString(R.string.fb_fail_to_clone) + "\nError: " + e.toString())
                                                .setNeutralButton(getString(R.string.ok_text), null)
                                                .create()
                                                .show();
                                        }

                                        browseTo(new File(clickedFile.getParent()));
                                    }
                                    else if(which == 2){
                                        /* rename card */
                                        final EditText input = new EditText(FileBrowser.this);
                                        input.setText(clickedFile.getAbsolutePath());
                                        new AlertDialog.Builder(FileBrowser.this)
                                            .setTitle(getString(R.string.fb_rename))
                                            .setMessage(getString(R.string.fb_rename_message))
                                            .setView(input)
                                            .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which ){
                                                String value = input.getText().toString();
                                                if(!value.equals(clickedFile.getAbsolutePath())){
                                                    try{
                                                        copyFile(clickedFile.getAbsolutePath(), value);
                                                        clickedFile.delete();
                                                    }
                                                    catch(IOException e){
                                                        new AlertDialog.Builder(FileBrowser.this)
                                                            .setTitle(getString(R.string.fail))
                                                            .setMessage(getString(R.string.fb_rename_fail) + "\nError: " + e.toString())
                                                            .setNeutralButton(getString(R.string.ok_text), null)
                                                            .create()
                                                            .show();
                                                    }

                                                }

                                                browseTo(currentDirectory);
                                                
                                            }
                                        })
                                        .setNegativeButton(getString(R.string.cancel_text), null) 
                                        .create()
                                        .show();
                                    }



                                }
                            })
                            .create()
                            .show();

					}
				}
		
				catch(Exception e){
					new AlertDialog.Builder(this).setMessage(e.toString()).show();
					browseTo(new File("/"));
				}
			}
		}
        return true;

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
                /* Create a new DB */
                final EditText input = new EditText(this);
                new AlertDialog.Builder(this)
                    .setTitle(this.getString(R.string.fb_create_db))
                    .setMessage(this.getString(R.string.fb_create_db_message))
                    .setView(input)
                    .setPositiveButton(this.getString(R.string.ok_text), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which ){
                        String value = input.getText().toString();
                        if(!value.endsWith(".db")){
                            value += ".db";
                        }
                        try{
                            /* create an empty database */
                            DatabaseHelper.createEmptyDatabase(currentDirectory.getAbsolutePath(), value);
                        }
                        catch(Exception e){
                        }
                        browseTo(currentDirectory);
                        
                    }
                })
                .setNegativeButton(this.getString(R.string.cancel_text), null) 
                .create()
                .show();
                return true;
            }
            
            case R.id.file_browser_createdirectory:{
                final EditText input = new EditText(this);
                new AlertDialog.Builder(this)
                    .setTitle(this.getString(R.string.fb_create_dir))
                    .setMessage(this.getString(R.string.fb_create_dir_message))
                    .setView(input)
                    .setPositiveButton(this.getString(R.string.ok_text), new DialogInterface.OnClickListener(){
                    @Override
                        public void onClick(DialogInterface dialog, int which ){
                            String value = input.getText().toString();
                            File newDir = new File(currentDirectory + "/" + value);
                            newDir.mkdir();
                            browseTo(currentDirectory);
                            
                        }
                    })
                    .setNegativeButton(this.getString(R.string.cancel_text), null)
                    .create()
                    .show();
                return true;
                
            }
	    }
	    return false;
	}

    private class FileBrowserAdapter extends ArrayAdapter<String> implements SectionIndexer{
        /* quick index sections */
        private String[] sections;

        HashMap<String, Integer> alphaIndexer = new HashMap<String, Integer>();

        public FileBrowserAdapter(Context context, int textViewResourceId, ArrayList<String> items){
            super(context, textViewResourceId, items);
            sort(new Comparator<String>() {
                @Override
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
                        return (s1.toLowerCase()).compareTo(s2.toLowerCase());
                    }
                }
            });
            List<String> sectionList = new ArrayList<String>();
            String cur = "";
            for(int i = 0; i < getCount(); i++) {
                String item = getItem(i);
                if (item.length() >= 2) {
                    String index;
                    if(item.endsWith("/")) {
                        index = item.substring(0, 2).toLowerCase() + item.substring(item.length() - 1);
                    }
                    else {
                        index = item.substring(0, 2).toLowerCase();
                    }
                    if (index != null && !index.equals(cur)){
                        alphaIndexer.put(index, i);
                        sectionList.add(index);
                        cur = index;
                    }
                }
            }
            sections = new String[sectionList.size()];
            sectionList.toArray(sections);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            if(v == null){
                LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.filebrowser_item, null);
            }
            String name = getItem(position);
            if(name != null){
                TextView tv = (TextView)v.findViewById(R.id.file_name);
                ImageView iv = (ImageView)v.findViewById(R.id.file_icon);
                if(name.endsWith("/")){
                    iv.setImageResource(R.drawable.dir);
                    name = name.substring(0, name.length() - 1);

                }
                else if(name.equals("..")){
                    iv.setImageResource(R.drawable.back);
                }
                else if(name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".tif") || name.endsWith(".bmp")){
                    iv.setImageResource(R.drawable.picture);
                }
                else if(name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".wav")){
                    iv.setImageResource(R.drawable.audio);
                }
                else if(name.endsWith(".txt") || name.endsWith(".csv") || name.endsWith(".xml")){
                    iv.setImageResource(R.drawable.text);
                }
                else{
                    iv.setImageResource(R.drawable.database);
                }

                if(name.charAt(0) == '/'){
                    name = name.substring(1, name.length());
                }

                tv.setText(name);
            }
            return v;
        }

        /* Display the quick index when the user is scrolling */
        
        @Override
        public int getPositionForSection(int section){
            String letters = sections[section];
            return alphaIndexer.get(letters);
        }

        @Override
        public int getSectionForPosition(int position){
            /* Not used */
            return 0;
        }

        @Override
        public Object[] getSections(){
            return sections;
        }
    }

	public static void copyFile(String source, String dest) throws IOException{
        File sourceFile = new File(source);
        File destFile = new File(dest);
		
        destFile.createNewFile();
		InputStream in = new FileInputStream(sourceFile);
		OutputStream out = new FileOutputStream(destFile);
		
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
}

