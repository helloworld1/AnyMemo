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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;
import roboguice.fragment.RoboDialogFragment;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileBrowserFragment extends RoboDialogFragment {
    public final static String EXTRA_DEFAULT_ROOT = "default_root";
    public final static String EXTRA_FILE_EXTENSIONS = "file_extension";
    public final static String EXTRA_DISMISS_ON_SELECT = "dismiss_on_select";
    public final static String EXTRA_SHOW_CREATE_DB_BUTTON = "show_create_db_button";

    private enum DISPLAYMODE{ABSOLUTE, RELATIVE;}
    private final DISPLAYMODE displayMode = DISPLAYMODE.RELATIVE;
    private ArrayList<String> directoryEntries = new ArrayList<String>();
    private File currentDirectory = new File("/");
    private String defaultRoot;
    private String[] fileExtensions;
    private AMActivity mActivity;

    private RecyclerView filesListRecyclerView;
    private FileBrowserAdapter fileListAdapter;
    private FloatingActionButton addDbButton;

    private TextView titleTextView;
    private boolean dismissOnSelect = false;
    private boolean showCreateDbButton = false;

    /* Used when the file is clicked. */
    private OnFileClickListener onFileClickListener;

    private final static String TAG = FileBrowserFragment.class.getSimpleName();
    private final static String UP_ONE_LEVEL_DIR = "..";
    private final static String CURRENT_DIR = ".";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    public void setOnFileClickListener(OnFileClickListener listener) {
        this.onFileClickListener = listener;
    }

    private AMFileUtil amFileUtil;

    private RecentListUtil recentListUtil;

    public FileBrowserFragment() { }

    @Inject
    public void setAmFileUtil(AMFileUtil amFileUtil) {
        this.amFileUtil = amFileUtil;
    }

    @Inject
    public void setRecentListUtil(RecentListUtil recentListUtil) {
        this.recentListUtil = recentListUtil;
    }

    @Override
    public void onAttach(Context activity) {
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

            showCreateDbButton = args.getBoolean(EXTRA_SHOW_CREATE_DB_BUTTON, false);

            if (ext != null) {
                fileExtensions = ext.split(",");
            }
            else {
                fileExtensions = new String[]{".db"};
            }
        } else {
            fileExtensions = new String[]{".db"};
            defaultRoot = null;
        } if(defaultRoot == null){
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
        filesListRecyclerView = (RecyclerView)v.findViewById(R.id.file_list);
        filesListRecyclerView.setLayoutManager(new LinearLayoutManager(filesListRecyclerView.getContext()));
        titleTextView = (TextView) v.findViewById(R.id.file_path_title);

        fileListAdapter = new FileBrowserAdapter(this);
        filesListRecyclerView.setAdapter(fileListAdapter);

        addDbButton = (FloatingActionButton) v.findViewById(R.id.add_db_fab);
        addDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateDbDialog();
            }
        });

        if (!showCreateDbButton) {
            addDbButton.hide();
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        browseTo(currentDirectory);
    }

    private void browseTo(final File aDirectory){
        if(aDirectory.isDirectory()){
            File[] listedFiles = aDirectory.listFiles();
            if (listedFiles != null) {
                titleTextView.setText(aDirectory.getPath());
                this.currentDirectory = aDirectory;
                fill(listedFiles);
            } else {
                Toast.makeText(filesListRecyclerView.getContext(),
                               R.string.change_directory_permission_denied_message,
                               Toast.LENGTH_SHORT).show();
            }
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

        this.fileListAdapter.setItems(directoryEntries);
    }


    private void upOneLevel(){
        if(this.currentDirectory.getParent() != null){
            this.browseTo(this.currentDirectory.getParentFile());
        }
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
                showCreateDbDialog();
                return true;
            }

            case R.id.file_browser_createdirectory:{
                showCreateFolderDialog();
                return true;

            }
        }
        return false;
    }

    private void openFile(final String selectedFileString) {
        if(selectedFileString.equals(CURRENT_DIR)){
            this.browseTo(this.currentDirectory);
        } else if(selectedFileString.equals(UP_ONE_LEVEL_DIR)) {
            this.upOneLevel();
        } else {
            File clickedFile = null;
            switch(this.displayMode){
                case RELATIVE:
                    clickedFile = new File(this.currentDirectory.getAbsolutePath() + selectedFileString);
                    break;
                case ABSOLUTE:
                    clickedFile = new File(selectedFileString);
                    break;
            }
            if(clickedFile != null) {
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

    private void showCreateDbDialog() {
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
                    File newDbFile = new File(currentDirectory.getAbsolutePath() + "/" + value);
                    try {
                        if (newDbFile.exists()) {
                            amFileUtil.deleteFileWithBackup(newDbFile.getAbsolutePath());
                        }

                        amFileUtil.createDbFileWithDefaultSettings(newDbFile);
                    } catch(IOException e){
                        Log.e(TAG, "Fail to create file", e);
                    }
                    browseTo(currentDirectory);

                }
            })
            .setNegativeButton(this.getString(R.string.cancel_text), null)
            .create()
            .show();
    }

    private void showCreateFolderDialog() {
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
    }

    private void showCloneDbDialog(final File clickedFile) {
        /* Clone */
        String srcDir = clickedFile.getAbsolutePath();
        String destDir = srcDir.replaceAll(".db", ".clone.db");
        try {
            FileUtils.copyFile(new File(srcDir), new File(destDir));
        } catch(IOException e){
            new AlertDialog.Builder(mActivity)
                    .setTitle(getString(R.string.fail))
                    .setMessage(getString(R.string.fb_fail_to_clone) + "\nError: " + e.toString())
                    .setNeutralButton(getString(R.string.ok_text), null)
                    .create()
                    .show();
        }

        browseTo(new File(clickedFile.getParent()));

    }

    private void showDeleteDbDialog(final File clickedFile) {
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

    private void showRenameDbDialog(final File clickedFile) {
        final EditText input = new EditText(mActivity);
        input.setText(clickedFile.getAbsolutePath());
        new AlertDialog.Builder(mActivity)
            .setTitle(getString(R.string.fb_rename))
            .setMessage(getString(R.string.fb_rename_message))
            .setView(input)
            .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String value = input.getText().toString();
                    if (!value.equals(clickedFile.getAbsolutePath())) {
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
            .show();
    }

    private static class FileBrowserAdapter extends RecyclerView.Adapter<FileBrowserAdapter.ViewHolder> {
        private final FileBrowserFragment fragment;

        private final List<String> items = new ArrayList<String>();

        public FileBrowserAdapter(FileBrowserFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater li = (LayoutInflater)fragment.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = li.inflate(R.layout.filebrowser_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final String selectedFileName = items.get(position);

            if (Strings.isNullOrEmpty(selectedFileName)) {
                return;
            }

            String displayFileName = selectedFileName;

            if(selectedFileName.endsWith("/")){
                holder.setImage(R.drawable.dir);
                displayFileName = displayFileName.substring(0, selectedFileName.length() - 1);

            } else if(selectedFileName.equals("..")){
                holder.setImage(R.drawable.back);
            } else if(selectedFileName.endsWith(".png") || selectedFileName.endsWith(".jpg") || selectedFileName.endsWith(".tif") || selectedFileName.endsWith(".bmp")){
                holder.setImage(R.drawable.picture);
            } else if(selectedFileName.endsWith(".ogg") || selectedFileName.endsWith(".mp3") || selectedFileName.endsWith(".wav") || selectedFileName.endsWith(".amr")){
                holder.setImage(R.drawable.audio);
            } else if(selectedFileName.endsWith(".txt") || selectedFileName.endsWith(".csv") || selectedFileName.endsWith(".xml")){
                holder.setImage(R.drawable.text);
            } else if(selectedFileName.endsWith(".zip")){
                holder.setImage(R.drawable.zip);
            } else{
                holder.setImage(R.drawable.database);
            }

            if (displayFileName.charAt(0) == '/'){
                displayFileName = displayFileName.substring(1, displayFileName.length());
            }

            holder.setText(displayFileName);

            // Set click listeners
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.openFile(selectedFileName);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(selectedFileName.equals(CURRENT_DIR)){
                        /* Do nothing */
                    } else if(selectedFileName.equals(UP_ONE_LEVEL_DIR)){
                        /* Do nothing */
                    } else {
                        final File clickedFile;
                        switch(fragment.displayMode){
                            case RELATIVE:
                                clickedFile = new File(fragment.currentDirectory.getAbsolutePath() + selectedFileName);
                                break;
                            case ABSOLUTE:
                                clickedFile = new File(selectedFileName);
                                break;
                            default:
                                clickedFile = new File(fragment.currentDirectory.getAbsolutePath() + selectedFileName);
                        }
                        if (clickedFile != null){
                            if(clickedFile.isDirectory()) {
                                fragment.browseTo(clickedFile);
                            } else if (clickedFile.isFile()){
                                new AlertDialog.Builder(fragment.getContext())
                                    .setTitle(fragment.getContext().getString(R.string.fb_edit_dialog_title))
                                    .setItems(R.array.fb_dialog_list, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                fragment.showDeleteDbDialog(clickedFile);
                                            } else if (which == 1) {
                                                fragment.showCloneDbDialog(clickedFile);
                                            } else if (which == 2) {
                                                fragment.showRenameDbDialog(clickedFile);
                                            }
                                        }
                                    })
                                    .create()
                                    .show();
                            }
                        }
                    }
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void setItems(List<String> items) {
            this.items.clear();
            this.items.addAll(items);

            Collections.sort(this.items, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2){
                    if (s1.equals("..")) {
                        return -1;
                    } else if (s2.equals("..")){
                        return 1;
                    } else if (s1.endsWith("/") && !s2.endsWith("/")){
                        return -1;
                    } else if (s2.endsWith("/") && !s1.endsWith("/")){
                        return 1;
                    } else {
                        return (s1.toLowerCase()).compareTo(s2.toLowerCase());
                    }
                }
            });
            this.notifyDataSetChanged();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;

            private final ImageView imageView;

            public ViewHolder(View view) {
                super(view);
                textView  = (TextView)view.findViewById(R.id.file_name);
                imageView = (ImageView)view.findViewById(R.id.file_icon);

            }

            public void setText(String text) {
                textView.setText(text);
            }

            public void setImage(int imageResource) {
                imageView.setImageResource(imageResource);
            }
        }
    }

    public interface OnFileClickListener {
        void onClick(File file);
    }
}

