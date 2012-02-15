package org.liberty.android.fantastischmemo.widgets;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Spinner corresponding to a String column in database 
 * @author sean
 *
 */
public class AMStrSpinner extends AMSpinner<String>{
    public AMStrSpinner(Spinner spinner) {
        super(spinner);
    }
    
    @Override
    public String getSelectedItem() {
        @SuppressWarnings("unchecked")
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) mSpinner.getAdapter();
        return adapter.getItem(mSpinner.getSelectedItemPosition()).toString();        
    }
    
    @Override
    public void setSelectedItem(final String value) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) mSpinner.getAdapter();
        mSpinner.setSelection(adapter.getPosition(value));
    }
}
