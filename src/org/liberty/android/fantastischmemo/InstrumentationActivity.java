package org.liberty.android.fantastischmemo;

import android.os.Bundle;

import android.widget.TextView;

public class InstrumentationActivity extends AMActivity {
    private TextView output;
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        output = new TextView(this);
        output.setText("This activity is for instrumentation only!");
        setContentView(output);
    }
}
