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

import java.util.LinkedHashSet;
import java.util.Set;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.domain.Option;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;

public class DictionaryUtil {

    private static final String TAG = "DictionaryUtil";

    private Activity mActivity;

    private Option option;

    private AMStringUtil amStringUtil;

    public DictionaryUtil(Activity activity) {
        mActivity = activity;
        option = new Option(mActivity);
        amStringUtil = new AMStringUtil(mActivity);
    }

    /*
     * Show a dialog to look up a list of words in the text
     */
    public void showLookupListDialog(String text, String... texts) {

        assert text != null : "showLookupListDialog shoul not get null input";

        // For the null input, do nothing.
        if (text == null) {
            Log.e(TAG, "showLookupListDialog get input null");
            return;
        }

        // Maintina the order of words in the original text
        Set<String> wordSet = new LinkedHashSet<String>();
        wordSet.add(amStringUtil.stripHTML(text));
        for (String t : texts) {
            wordSet.add(amStringUtil.stripHTML(t));
        }

        String[] splittedText = amStringUtil.stripHTML(text).split(" ");

        for (String word : splittedText) {
            wordSet.add(word);
        }

        for (String t : texts) {
            String[] splitted = amStringUtil.stripHTML(t).split(" ");

            for (String word : splitted) {
                wordSet.add(word);
            }
        }

        final String[] wordsToDisplay = new String[wordSet.size()];
        wordSet.toArray(wordsToDisplay);

        new AlertDialog.Builder(mActivity)
            .setTitle(R.string.look_up_text)
            .setItems(wordsToDisplay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    lookupDictionary(wordsToDisplay[which]);
                }

            })
            .show();
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

        if(option.getDictApp() == Option.DictApp.BLUEDICT) {
            try {
                Intent intent = new Intent();
                intent.setAction("bluedict.intent.action.FLOATSEARCH");
                intent.putExtra("EXTRA_QUERY", lookupWord);

                intent.putExtra("EXTRA_FULLSCREEN", false);
                intent.putExtra("EXTRA_GRAVITY",Gravity.BOTTOM);
                // intent.putExtra("EXTRA_HEIGHT", 500);
                intent.putExtra("EXTRA_MARGIN_LEFT",4);
                intent.putExtra("EXTRA_MARGIN_RIGHT",4);
                intent.putExtra("EXTRA_MARGIN_TOP",4);
                intent.putExtra("EXTRA_MARGIN_BOTTOM",4);
                mActivity.startActivity(intent);
            } catch(Exception e) {
                AMGUIUtility.displayException(mActivity, mActivity.getString(R.string.error_text), mActivity.getString(R.string.dict_fora) + " " + mActivity.getString(R.string.error_no_dict), e);
            }

        }

    }
}
