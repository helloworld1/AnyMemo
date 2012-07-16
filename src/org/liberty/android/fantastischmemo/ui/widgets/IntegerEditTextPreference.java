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

package org.liberty.android.fantastischmemo.ui.widgets;

import org.liberty.android.fantastischmemo.R;

import android.content.Context;
import android.content.res.TypedArray;

import android.preference.EditTextPreference;

import android.util.AttributeSet;


/*
 * This class can be used in PreferenceActivity to persist
 * the Integer preferences.
 */
public class IntegerEditTextPreference extends EditTextPreference {
	private int maxIntValue;
	private int minIntValue;
	
    public IntegerEditTextPreference(Context context) {
        super(context);
    }

    public IntegerEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.IntegerEditTextPreference);
        CharSequence maxCharValue = arr.getText(R.styleable.IntegerEditTextPreference_maxIntValue);
        CharSequence minCharValue = arr.getText(R.styleable.IntegerEditTextPreference_minIntValue);
        
        assert maxCharValue != null && minCharValue != null;
        
        this.maxIntValue = Integer.parseInt(maxCharValue.toString());
        this.minIntValue = Integer.parseInt(minCharValue.toString());
    }

    public IntegerEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(-1));
    }

    @Override
    protected boolean persistString(String value) {
    	int valueInt = Integer.parseInt(value);
    	// default to the max and min int value it value exceeds the limit.
    	valueInt = (valueInt > maxIntValue) ? maxIntValue : ((valueInt < minIntValue) ? minIntValue: valueInt);
    	
        return persistInt(valueInt);
    }
    
    
}

