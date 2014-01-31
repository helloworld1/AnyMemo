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

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.service.ConvertIntentService;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

import roboguice.RoboGuice;
import roboguice.util.Ln;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ConverterFragment extends FileBrowserFragment {
    public static final String EXTRA_CONVERTER_CLASS = "converterClass";

    private Class<Converter> converterClass;

    private AMFileUtil amFileUtil;

    @Inject
    public void setAmFileUtil(AMFileUtil amFileUtil) {
        this.amFileUtil = amFileUtil;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        assert args != null : "Null args in ConverterFragment";

        setOnFileClickListener(fileClickListener);
        converterClass = (Class<Converter>) args.getSerializable(EXTRA_CONVERTER_CLASS);
    }

    private FileBrowserFragment.OnFileClickListener fileClickListener
        = new FileBrowserFragment.OnFileClickListener() {
            public void onClick(File file) {
                startConversion(file);
            }
        };

    /**
     * Prepare the conversion and start the conversion.
     * If the destiny file exists, it pops up an option to merge the file into existing db.
     */
    private void startConversion(File file) {
        final String inputPath = file.getAbsolutePath();

        // Get the converter instance so we can get the dest file path
        Converter converter = RoboGuice.getInjector(getActivity()).getInstance(converterClass);

        final String outputPath = FilenameUtils.removeExtension(inputPath) + "." + converter.getDestExtension();

        // If a merge is possible, popup a dialog and ask user to confirm if merge is possible
        // Only do the database merge on .db files, for other files,
        // the dest will be deleted
        if (new File(outputPath).exists() && converter.getDestExtension().equals("db")) {
            new AlertDialog.Builder(getActivity())
                .setTitle(R.string.conversion_merge_text)
                .setMessage(String.format(getString(R.string.conversion_merge_message),
                            outputPath, inputPath, outputPath))
                .setPositiveButton(R.string.yes_text,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            invokeConverterService(inputPath, outputPath);

                        }
                    })
                .setNeutralButton(R.string.no_text,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            try {
                                amFileUtil.deleteFileWithBackup(outputPath);
                                invokeConverterService(inputPath, outputPath);
                            } catch (IOException e) {
                                Ln.e("Faield to deleteWithBackup: " + outputPath, e);
                                Toast.makeText(getActivity(),
                                    getString(R.string.fail) + ": " + e.toString(), Toast.LENGTH_LONG)
                                    .show();
                            }
                        }
                    })
                .setNegativeButton(R.string.cancel_text, null)
                .show();
        } else {
            // If the merging is not possible, do normal conversion
            amFileUtil.deleteDbSafe(outputPath);
            invokeConverterService(inputPath, outputPath);
        }
    }

    private void invokeConverterService(String inputPath, String outputPath) {
        Intent intent =  new Intent(getActivity(), ConvertIntentService.class);
        intent.setAction(ConvertIntentService.ACTION_CONVERT);
        Bundle b = new Bundle();
        b.putSerializable(ConvertIntentService.EXTRA_CONVERTER_CLASS, converterClass);
        b.putString(ConvertIntentService.EXTRA_INPUT_FILE_PATH, inputPath);
        b.putString(ConvertIntentService.EXTRA_OUTPUT_FILE_PATH, outputPath);
        intent.putExtras(b);
        getActivity().startService(intent);
        Toast.makeText(getActivity(), R.string.conversion_started_text, Toast.LENGTH_SHORT)
            .show();

        dismiss();
    }
}
