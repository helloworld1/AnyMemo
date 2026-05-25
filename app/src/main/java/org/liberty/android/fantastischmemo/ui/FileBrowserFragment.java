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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.common.AMPrefKeys;
import org.liberty.android.fantastischmemo.common.BaseDialogFragment;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.DatabaseOperationDialogUtil;
import org.liberty.android.fantastischmemo.utils.DatabaseOperationDialogUtil;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import android.content.Intent;
import android.net.Uri;

public class FileBrowserFragment extends BaseDialogFragment {
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
    private Activity mActivity;

    private RecyclerView filesListRecyclerView;
    private FileBrowserAdapter fileListAdapter;
    private TextView filesListEmptyView;

    private TextView titleTextView;
    private FloatingActionButton addDbFab;


    private boolean dismissOnSelect = false;
    private boolean showCreateDbButton = false;

    /* Used when the file is clicked. */
    private OnFileClickListener onFileClickListener;

    private CompositeDisposable disposables;

    private final static String TAG = FileBrowserFragment.class.getSimpleName();
    private final static String UP_ONE_LEVEL_DIR = "..";
    private final static String CURRENT_DIR = ".";
    private static final int REQUEST_CODE_IMPORT_DB = 1001;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    public void setOnFileClickListener(OnFileClickListener listener) {
        this.onFileClickListener = listener;
    }

    @Inject AMFileUtil amFileUtil;

    @Inject RecentListUtil recentListUtil;

    @Inject DatabaseOperationDialogUtil databaseOperationDialogUtil;

    public FileBrowserFragment() { }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mActivity = (Activity) activity;
        settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        editor = settings.edit();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        fragmentComponents().inject(this);

        disposables = new CompositeDisposable();
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
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.file_browser, container, false);
        filesListRecyclerView = (RecyclerView)v.findViewById(R.id.file_list);
        filesListRecyclerView.setLayoutManager(new LinearLayoutManager(filesListRecyclerView.getContext()));
        titleTextView = (TextView) v.findViewById(R.id.file_path_title);

        filesListEmptyView = (TextView) v.findViewById(R.id.empty_text_view);
        filesListEmptyView.setText(R.string.directory_empty_text);

        addDbFab = v.findViewById(R.id.add_db_fab);

        if (showCreateDbButton) {
            initCreateDbFab();
        } else {
            addDbFab.setVisibility(View.GONE);
        }

        fileListAdapter = new FileBrowserAdapter(this);
        filesListRecyclerView.setAdapter(fileListAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        browseTo(currentDirectory);
    }

    private void refreshFileList(@Nullable File folderToRefresh) {
        if (folderToRefresh != null) {
            browseTo(folderToRefresh);
        } else if (currentDirectory != null){
            browseTo(currentDirectory);
        }
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

        if (files.length > 0) {
            this.filesListEmptyView.setVisibility(View.INVISIBLE);
        } else {
            this.filesListEmptyView.setVisibility(View.VISIBLE);
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
        int itemId = item.getItemId();
        if (itemId == R.id.file_browser_createdb) {
            disposables.add(databaseOperationDialogUtil.showCreateDbDialog(currentDirectory.getAbsolutePath())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<File>() {
                        @Override
                        public void accept(File file) throws Exception {
                            browseTo(file.getParentFile());
                        }
                    }));
            return true;
        } else if (itemId == R.id.file_browser_createdirectory) {
            disposables.add(databaseOperationDialogUtil.showCreateFolderDialog(currentDirectory)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<File>() {
                        @Override
                        public void accept(File file) throws Exception {
                            browseTo(file);
                        }
                    }));
            return true;
        } else if (itemId == R.id.file_browser_importdb) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CODE_IMPORT_DB);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_IMPORT_DB && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            final Uri uri = data.getData();
            String newFileName = amFileUtil.getFileNameFromUri(getContext(), uri);
            if (newFileName == null) {
                newFileName = "imported_db.db";
            }
            // Clean up query parameters or encoded chars if necessary, but a simple append works for now
            if (!newFileName.endsWith(".db")) {
                newFileName += ".db";
            }
            final File newFile = new File(currentDirectory.getAbsolutePath() + "/" + newFileName);

            if (newFile.exists()) {
                new AlertDialog.Builder(getContext())
                    .setTitle(R.string.overwrite_db_title)
                    .setMessage(getString(R.string.overwrite_db_message, newFileName))
                    .setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            importDb(uri, newFile);
                        }
                    })
                    .setNegativeButton(R.string.no_text, null)
                    .show();
            } else {
                importDb(uri, newFile);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void importDb(final Uri uri, final File newFile) {
        disposables.add(io.reactivex.Observable.fromCallable(new java.util.concurrent.Callable<File>() {
            @Override
            public File call() throws Exception {
                InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                FileUtils.copyInputStreamToFile(inputStream, newFile);
                return newFile;
            }
        })
        .subscribeOn(io.reactivex.schedulers.Schedulers.io())
        .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
        .subscribe(new io.reactivex.functions.Consumer<File>() {
            @Override
            public void accept(File file) {
                // If the user navigates away, we don't need to refresh anymore
                if (getActivity() != null && isAdded()) {
                    refreshFileList(currentDirectory);
                }
                Toast.makeText(getContext(), R.string.success, Toast.LENGTH_SHORT).show();
            }
        }, new io.reactivex.functions.Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Log.e("FileBrowserFragment", "Error importing DB", throwable);
                Toast.makeText(getContext(), "Error importing DB", Toast.LENGTH_SHORT).show();
            }
        }));
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
                            onFileClickListener.onFileBrowserFileClick(clickedFile);
                        } else {
                            notifyFileClick(clickedFile);
                        }

                        // dismiss on demand
                        if (dismissOnSelect) {
                            dismiss();
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

    private void notifyFileClick(File clickedFile) {
        if (getParentFragment() instanceof OnFileClickListener) {
            ((OnFileClickListener) getParentFragment()).onFileBrowserFileClick(clickedFile);
        } else if (getActivity() instanceof OnFileClickListener) {
            ((OnFileClickListener) getActivity()).onFileBrowserFileClick(clickedFile);
        }
    }

    /**
     * We create the FAB only at Activity level to make sure the FAB is not scrolling up and down
     * with appbar_scrolling_view_behavior. The behavior will make the FAB half visible if the FAB is
     * inside a ViewPager fragment.
     */
    private void initCreateDbFab() {
        addDbFab.setOnClickListener(new CardFragment.OnClickListener() {
            @Override
            public void onClick(View v) {
                disposables.add(databaseOperationDialogUtil.showCreateDbDialog(AMEnv.DEFAULT_ROOT_PATH)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<File>() {
                            @Override
                            public void accept(File file) {
                                refreshFileList(file.getAbsoluteFile().getParentFile());
                            }
                        }));
            }
        });
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
                                                fragment.disposables.add(fragment.databaseOperationDialogUtil.showDeleteDbDialog(clickedFile)
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Consumer<File>() {
                                                            @Override
                                                            public void accept(File file) throws Exception {
                                                                fragment.browseTo(file.getParentFile());
                                                            }
                                                        }));
                                            } else if (which == 1) {
                                                fragment.disposables.add(fragment.databaseOperationDialogUtil.showCloneDbDialog(clickedFile)
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Consumer<File>() {
                                                            @Override
                                                            public void accept(File file) throws Exception {
                                                                fragment.browseTo(file.getParentFile());
                                                            }
                                                        }));
                                            } else if (which == 2) {
                                                fragment.disposables.add(fragment.databaseOperationDialogUtil.showRenameDbDialog(clickedFile)
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Consumer<File>() {
                                                            @Override
                                                            public void accept(File file) throws Exception {
                                                                fragment.browseTo(file.getParentFile());
                                                            }
                                                        }));
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
        void onFileBrowserFileClick(File file);
    }
}

