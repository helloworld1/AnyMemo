/*
Copyright (C) 2012 Haowen Ning

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import roboguice.fragment.RoboDialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.google.common.base.Strings;

public class FileBrowserFragment extends RoboDialogFragment implements OnItemClickListener, OnItemLongClickListener {
    public final static String EXTRA_DEFAULT_ROOT = "default_root";
    public final static String EXTRA_FILE_EXTENSIONS = "file_extension";
    public final static String EXTRA_DISMISS_ON_SELECT = "dismiss_on_select";

    private enum DISPLAYMODE{ABSOLUTE, RELATIVE;}
    private final DISPLAYMODE displayMode = DISPLAYMODE.RELATIVE;
    private ArrayList<String> directoryEntries = new ArrayList<String>();
    private File currentDirectory = new File("/");
    private String defaultRoot;
    private String[] fileExtensions;
    private AMActivity mActivity;
    private ListView fbListView;
    private TextView titleTextView;
    private boolean dismissOnSelect = false;

    /* Used when the file is clicked. */
    private OnFileClickListener onFileClickListener;

    private final static String TAG = "AbstractFileBrowserFragment";
    private final static String UP_ONE_LEVEL_DIR = "..";
    private final static String CURRENT_DIR = ".";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    public void setOnFileClickListener(OnFileClickListener listener) {
        this.onFileClickListener = listener;
    }

    private AMFileUtil amFileUtil;

    private RecentListUtil recentListUtil;

    @Inject
    public void setAmFileUtil(AMFileUtil amFileUtil) {
        this.amFileUtil = amFileUtil;
    }

    @Inject
    public void setRecentListUtil(RecentListUtil recentListUtil) {
        this.recentListUtil = recentListUtil;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity)activity;
        settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        editor = settings.edit();
    }


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle args = this.getArguments();
        if(args != null) {
            defaultRoot = args.getString(EXTRA_DEFAULT_ROOT);
            String ext =  args.getString(EXTRA_FILE_EXTENSIONS);
            // Default do not dismiss the dialog
            dismissOnSelect = args.getBoolean(EXTRA_DISMISS_ON_SELECT, false);

            if (ext != null) {
                fileExtensions = ext.split(",");
            }
            else {
                fileExtensions = new String[]{".db"};
            }
        }
        else {
            fileExtensions = new String[]{".db"};
            defaultRoot = null;
        }
        if(defaultRoot == null){
            defaultRoot = settings.getString(AMPrefKeys.SAVED_FILEBROWSER_PATH_KEY, null);

            // Make sure the path exists.
            if (!Strings.isNullOrEmpty(defaultRoot) && !new File(defaultRoot).exists()) {
                defaultRoot = null;
            }
        }

        if (Strings.isNullOrEmpty(defaultRoot)) {
            File sdPath = new File(AMEnv.DEFAULT_ROOT_PATH);
            sdPath.mkdir();

            currentDirectory = sdPath;
        } else {
            currentDirectory = new File(defaultRoot + "/");
        }

        // Should use this to enable menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.file_browser, container, false);
        fbListView = (ListView)v.findViewById(R.id.file_list);
        titleTextView = (TextView) v.findViewById(R.id.file_path_title);
        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        browseTo(currentDirectory);
    }



    private void browseTo(final File aDirectory){
        if(aDirectory.isDirectory()){
            titleTextView.setText(aDirectory.getPath());
            this.currentDirectory = aDirectory;
            fill(aDirectory.listFiles());
        }
    }

    private void fill(File[] files){
        this.directoryEntries.clear();

        if(this.currentDirectory.getParent() != null){
            this.directoryEntries.add(UP_ONE_LEVEL_DIR);
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

        FileBrowserAdapter directoryList = new FileBrowserAdapter(mActivity, R.layout.filebrowser_item, directoryEntries);
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
        if(selectedFileString.equals(CURRENT_DIR)){
            this.browseTo(this.currentDirectory);
        }
        else if(selectedFileString.equals(UP_ONE_LEVEL_DIR)) {
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
                        editor.putString(AMPrefKeys.SAVED_FILEBROWSER_PATH_KEY, clickedFile.getParent());
                        editor.commit();
                        if (onFileClickListener != null) {
                            onFileClickListener.onClick(clickedFile);
                            // dismiss on demand
                            if (dismissOnSelect) {
                                dismiss();
                            }
                        }
                    }
                }

                catch(Exception e){
                    new AlertDialog.Builder(mActivity).setMessage(e.toString()).show();
                    Log.e(TAG, "Error handling click", e);
                    browseTo(new File("/"));
                }
            }
        }



    }


    @Override
    public boolean onItemLongClick(AdapterView<?>  parent, View  view, int position, long id){
        String selectedFileString = this.directoryEntries.get(position);
        if(selectedFileString.equals(CURRENT_DIR)){
            /* Do nothing */
        }
        else if(selectedFileString.equals(UP_ONE_LEVEL_DIR)){
            /* Do nithing */
        }
        else{
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
                        new AlertDialog.Builder(mActivity)
                            .setTitle(getString(R.string.fb_edit_dialog_title))
                            .setItems(R.array.fb_dialog_list, new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    if(which == 0){
                                        /* Delete */
                                        new AlertDialog.Builder(mActivity)
                                            .setTitle(getString(R.string.delete_text))
                                            .setMessage(getString(R.string.fb_delete_message))
                                            .setPositiveButton(getString(R.string.delete_text), new DialogInterface.OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int which ){
                                                    amFileUtil.deleteDbSafe(clickedFile.getAbsolutePath());
                                                    File dir = new File(clickedFile.getParent());
                                                    Log.v(TAG, "DIR: " + dir.toString());
                                                    /* Refresh the list */
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
                                        try {
                                            FileUtils.copyFile(new File(srcDir), new File(destDir));
                                        }
                                        catch(IOException e){
                                            new AlertDialog.Builder(mActivity)
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
                                        final EditText input = new EditText(mActivity);
                                        input.setText(clickedFile.getAbsolutePath());
                                        new AlertDialog.Builder(mActivity)
                                            .setTitle(getString(R.string.fb_rename))
                                            .setMessage(getString(R.string.fb_rename_message))
                                            .setView(input)
                                            .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which ){
                                                String value = input.getText().toString();
                                                if(!value.equals(clickedFile.getAbsolutePath())){
                                                    try {
                                                        FileUtils.copyFile(clickedFile, new File(value));
                                                        amFileUtil.deleteDbSafe(clickedFile.getAbsolutePath());
                                                        recentListUtil.deleteFromRecentList(clickedFile.getAbsolutePath());

                                                    } catch (IOException e) {
                                                        new AlertDialog.Builder(mActivity)
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
                    new AlertDialog.Builder(mActivity).setMessage(e.toString()).show();
                    browseTo(new File("/"));
                }
            }
        }
        return true;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.file_browser_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.file_browser_createdb:{
                /* Create a new DB */
                final EditText input = new EditText(mActivity);
                new AlertDialog.Builder(mActivity)
                    .setTitle(mActivity.getString(R.string.fb_create_db))
                    .setMessage(mActivity.getString(R.string.fb_create_db_message))
                    .setView(input)
                    .setPositiveButton(mActivity.getString(R.string.ok_text), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which ){
                        String value = input.getText().toString();
                        if(!value.endsWith(".db")){
                            value += ".db";
                        }
                        try {
                            String emptyDbPath = mActivity.getApplicationContext().getFilesDir().getAbsolutePath() + "/" + AMEnv.EMPTY_DB_NAME;
                            FileUtils.copyFile(new File(emptyDbPath), new File(currentDirectory.getAbsolutePath() + "/" + value));
                        } catch(IOException e){
                            Log.e(TAG, "Fail to create file", e);
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
                final EditText input = new EditText(mActivity);
                new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.fb_create_dir)
                    .setMessage(R.string.fb_create_dir_message)
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
                LayoutInflater li = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.filebrowser_item, null);
            }
            String name = getItem(position);
            if (name != null) {
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
                else if(name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".amr")){
                    iv.setImageResource(R.drawable.audio);
                }
                else if(name.endsWith(".txt") || name.endsWith(".csv") || name.endsWith(".xml")){
                    iv.setImageResource(R.drawable.text);
                }
                else if(name.endsWith(".zip")){
                    iv.setImageResource(R.drawable.zip);
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

    public static interface OnFileClickListener {
        void onClick(File file);
    }
}

