package org.liberty.android.fantastischmemo.ui.widgets;

import java.util.Arrays;
import java.util.List;

import org.liberty.android.fantastischmemo.R;


import android.content.Context;

import android.content.res.TypedArray;

import android.util.AttributeSet;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class AMSpinner extends Spinner {
    private ArrayAdapter<CharSequence> mAdapter;
    private List<CharSequence> valueList;

    public AMSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.AMSpinner);
        CharSequence[] displayArray = arr.getTextArray(R.styleable.AMSpinner_display_array);
        CharSequence[] valueArray = arr.getTextArray (R.styleable.AMSpinner_value_array);

        assert displayArray.length != 0;
        assert valueArray.length != 0;

        mAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, displayArray);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        valueList = (List<CharSequence>) Arrays.asList(valueArray);
        this.setAdapter(mAdapter);
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

}
