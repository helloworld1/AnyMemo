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
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.service.ConvertIntentService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ConverterFragment extends FileBrowserFragment {
    public static final String EXTRA_CONVERTER_CLASS = "converterClass";

    private Class<Converter> converterClass;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setOnFileClickListener(fileClickListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        assert args != null : "Null args in ConverterFragment";

        converterClass = (Class<Converter>) args.getSerializable(EXTRA_CONVERTER_CLASS);
    }

    private FileBrowserFragment.OnFileClickListener fileClickListener
        = new FileBrowserFragment.OnFileClickListener() {
            public void onClick(File file) {
                String fullpath = file.getAbsolutePath();

                Intent intent =  new Intent(getActivity(), ConvertIntentService.class);
                intent.setAction(ConvertIntentService.ACTION_CONVERT);
                Bundle b = new Bundle();
                b.putSerializable(ConvertIntentService.EXTRA_CONVERTER_CLASS, converterClass);
                b.putString(ConvertIntentService.EXTRA_INPUT_FILE_PATH, fullpath);
                intent.putExtras(b);
                getActivity().startService(intent);
                Toast.makeText(getActivity(), R.string.conversion_started_text, Toast.LENGTH_SHORT)
                    .show();
                    
                dismiss();

            }
        };
}
