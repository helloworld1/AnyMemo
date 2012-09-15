package org.liberty.android.fantastischmemo.ui;


import org.liberty.android.fantastischmemo.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class AutoSpeakFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.auto_speak_layout, container, false);
		
		
		return v;
	}
	
}
