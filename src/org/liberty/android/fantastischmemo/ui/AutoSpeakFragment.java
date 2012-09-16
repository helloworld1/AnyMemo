package org.liberty.android.fantastischmemo.ui;


import org.liberty.android.fantastischmemo.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class AutoSpeakFragment extends Fragment {
    
    private static final String TAG = "AutoSpeakFragment";
    private ImageButton playButton;
    private boolean isPlaying = false;
    
    private View.OnClickListener buttonListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            isPlaying = !isPlaying;
            
            if(isPlaying) {
                playButton.setSelected(true);
            } else {
                playButton.setSelected(false);
            }
        }
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.auto_speak_layout, container, false);
        playButton = (ImageButton) v.findViewById(R.id.auto_speak_play_button);
        playButton.setOnClickListener(buttonListener);
        
        return v;
    }
    
    
    
    
    public void onClick(View v) {
        
    }
    
}
