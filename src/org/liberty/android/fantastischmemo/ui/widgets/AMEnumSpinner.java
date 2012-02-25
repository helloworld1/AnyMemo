package org.liberty.android.fantastischmemo.ui.widgets;

import java.util.Arrays;
import java.util.List;

import android.widget.Spinner;

/**
 * The valid values are given in the Enum T
 * 
 * @author sean
 * 
 * @param <T>
 */
public class AMEnumSpinner<T extends Enum<T>> extends AMSpinner<T> {
    List<T> validValues;

    public AMEnumSpinner(Spinner spinner, T[] validValues) {
        super(spinner);
        this.validValues = Arrays.asList(validValues);
    }

    public void setSelectedItem(final T value) {
        for (int position = 0; position < validValues.size(); position++) {
            if (value == validValues.get(position)) {
                mSpinner.setSelection(position);
                return;
            }
        }
        assert (false);
    }

    public T getSelectedItem() {
        return validValues.get(mSpinner.getSelectedItemPosition());
    }
}

