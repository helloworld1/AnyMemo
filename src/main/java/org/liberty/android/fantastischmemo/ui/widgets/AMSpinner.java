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

import java.util.Arrays;
import java.util.List;

import org.liberty.android.fantastischmemo.R;


import android.content.Context;

import android.content.res.TypedArray;

import android.util.AttributeSet;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

/*
 * This is a spinner used in AnyMemo.
 * It accept two more attributes in XML.
 * anymemo:display_array="@array/align_list"
 * anymemo:value_array="@array/align_list_values"
 * the first one is the array displayed on screen.
 * The second one is the real value of the items.
 * See more res/values/attrs.xml
 */
public class AMSpinner extends Spinner {
    private ArrayAdapter<CharSequence> mAdapter;
    private List<CharSequence> valueList;

    /*
     * This constructor should not be called directly.
     * It is called when initializing the class using XML.
     */
    public AMSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.AMSpinner);
        CharSequence[] displayArray = arr.getTextArray(R.styleable.AMSpinner_display_array);
        CharSequence[] valueArray = arr.getTextArray (R.styleable.AMSpinner_value_array);

        assert displayArray != null && displayArray.length != 0;
        assert valueArray != null && valueArray.length != 0;

        mAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, displayArray);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        valueList = (List<CharSequence>) Arrays.asList(valueArray);
        this.setAdapter(mAdapter);
        arr.recycle();
    }

    public String getSelectedItemValue() {
        int pos = getSelectedItemPosition();
        return (String)valueList.get(pos);
    }

    public void selectItemFromValue(CharSequence value, int defaultPosition) {
        int index = valueList.indexOf(value);
        if (index == -1) {
            index = defaultPosition;
        }
        this.setSelection(index);
    }

    public String getItemValueForPosition(int position) {
        return valueList.get(position).toString();
    }

}
