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

package org.liberty.android.fantastischmemo.converter;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.Context;
import java.lang.reflect.Constructor;
import org.liberty.android.fantastischmemo.*;

/* 
 * This class requires the strategies passed using intent extras with
 * the key "converter" 
 */
public class ConvertScreen extends FileBrowser{
    AbstractConverter converter = null;
    private final String TAG = "org.liberty.android.fantastischmemo.ConvertScreen";

    @Override
    public void fileClickAction(final String name, final String path){
		Bundle extras = getIntent().getExtras();
        if(extras == null){
            finish();
        }
        final String successTitle = getString(R.string.success);
        final String successText = getString(R.string.convert_success) + " " + path;
        try{
            Class<AbstractConverter> ac = (Class<AbstractConverter>)extras.getSerializable("converter");
            Constructor<AbstractConverter> acc = ac.getConstructor(Context.class);
            converter = acc.newInstance(this);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        AMGUIUtility.doProgressTask(this, R.string.loading_please_wait, R.string.convert_wait, 
            new AMGUIUtility.ProgressTask(){
                public void doHeavyTask() throws Exception{
                    converter.convert(path, name);
                }
                public void doUITask(){
                    new AlertDialog.Builder(ConvertScreen.this)
                        .setTitle(successTitle)
                        .setMessage(successText)
                        .setPositiveButton(R.string.ok_text, AMGUIUtility.getDialogFinishListener(ConvertScreen.this))
                        .show();
                }
            });

    }
}

