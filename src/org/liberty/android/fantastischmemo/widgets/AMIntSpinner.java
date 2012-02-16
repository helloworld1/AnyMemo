package org.liberty.android.fantastischmemo.widgets;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class AMIntSpinner extends AMSpinner<Integer> {
    public AMIntSpinner(Spinner spinner) {
        super(spinner);
    }

    @Override
    public void setSelectedItem(final Integer value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) mSpinner.getAdapter();
        mSpinner.setSelection(adapter.getPosition(String.valueOf(value)));
    }

    @Override
    public Integer getSelectedItem() {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) mSpinner.getAdapter();
        String strValue = adapter.getItem(mSpinner.getSelectedItemPosition()).toString();
        return Integer.valueOf(strValue);
    }
}

