package org.liberty.android.fantastischmemo.widgets;

import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

abstract class AMSpinner<T> {
    protected Spinner mSpinner;

    public AMSpinner(Spinner spinner) {
        mSpinner = spinner;
        
    }

    abstract public void setSelectedItem(final T value);
    abstract public T getSelectedItem();
    
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mSpinner.setOnItemSelectedListener(listener);
    }
    
    public int getSelectedItemPosition() {
        return mSpinner.getSelectedItemPosition();
    }
}
