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

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.converter.AbstractConverter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class ConverterFragment extends FileBrowserFragment {
    public static final String EXTRA_CONVERTER = "converterObject";
    private Activity mActivity;
    private AbstractConverter mConverter;

    private final static String TAG = "ConverterFragment";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        setOnFileClickListener(fileClickListener);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        assert args != null : "Null args in ConverterFragment";

        mConverter = (AbstractConverter) args.getSerializable(EXTRA_CONVERTER);
    }

    /*
     * input: paths[0]:src, path[1]:dest
     * result: Error message
     */
    private class ConvertTask extends AsyncTask<String, Void, String> {
        private ProgressDialog progressDialog;
        private String src;
        private String dest;

        @Override
        public void onPreExecute(){
            super.onPreExecute();
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        
        /*
         * The paths should be passed as 2 argument.
         * paths[0] is the src, path[1] is the dest
         */
        @Override
        public String doInBackground(String... paths){
            try {
                src = paths[0];
                dest = paths[1];
                mConverter.convert(src, dest);
            } catch (Exception e) {
                Log.e(TAG, "Error converting", e);
                return e.toString();
            }
            return null;
        }

        @Override
        public void onCancelled(){
            return;
        }

        @Override
        public void onPostExecute(String error){
            super.onPostExecute(error);
            progressDialog.dismiss();
            dismiss();
            if (error != null) {
                new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.fail_import)
                    .setMessage(mActivity.getString(R.string.exception_text) +": " + error)
                    .setPositiveButton(mActivity.getString(R.string.ok_text), null)
                    .show();

            } else {
                new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.success)
                    .setMessage(mActivity.getString(R.string.convert_success) +": " + dest)
                    .setPositiveButton(mActivity.getString(R.string.ok_text), null)
                    .show();
            }
        }
    }

    private FileBrowserFragment.OnFileClickListener fileClickListener
        = new FileBrowserFragment.OnFileClickListener() {
            public void onClick(File file) {
                String fullpath = file.getAbsolutePath();
                ConvertTask task = new ConvertTask();
                task.execute(fullpath, fullpath + "." +  mConverter.getDestExtension());
            }
        };
}
