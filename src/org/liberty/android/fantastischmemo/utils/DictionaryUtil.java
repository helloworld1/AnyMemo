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
package org.liberty.android.fantastischmemo.utils;

import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.domain.Option;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.util.Log;

import android.view.Gravity;

public class DictionaryUtil {
    private Activity mActivity;

    private Option option;

    public DictionaryUtil(Activity activity) {
        mActivity = activity;
        option = new Option(mActivity);
    }

    public void lookupDictionary(String lookupWord) {
        if(option.getDictApp() == Option.DictApp.COLORDICT){
            Intent intent = new Intent("colordict.intent.action.SEARCH");
            intent.putExtra("EXTRA_QUERY", lookupWord);
            intent.putExtra("EXTRA_FULLSCREEN", false);
            //intent.putExtra(EXTRA_HEIGHT, 400); //400pixel, if you don't specify, fill_parent"
            intent.putExtra("EXTRA_GRAVITY", Gravity.BOTTOM);
            //intent.putExtra(EXTRA_MARGIN_LEFT, 100);
            try {
                mActivity.startActivity(intent);
            } catch(Exception e) {
                AMGUIUtility.displayException(mActivity, mActivity.getString(R.string.error_text), mActivity.getString(R.string.dict_colordict) + " " + mActivity.getString(R.string.error_no_dict), e);
            }
        }
        if(option.getDictApp() == Option.DictApp.FORA) {
            Intent intent = new Intent("com.ngc.fora.action.LOOKUP");
            intent.putExtra("HEADWORD", lookupWord);
            try {
                mActivity.startActivity(intent);
            } catch(Exception e) {
                AMGUIUtility.displayException(mActivity, mActivity.getString(R.string.error_text), mActivity.getString(R.string.dict_fora) + " " + mActivity.getString(R.string.error_no_dict), e);
            }
        }
    }
}
