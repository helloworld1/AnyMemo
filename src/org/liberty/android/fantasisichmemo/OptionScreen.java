package org.liberty.android.fantasisichmemo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class OptionScreen extends Activity implements OnClickListener{
	public static final String PREFS_NAME = "FantasisichMemoPrefs";
	private boolean autoaudioSetting;
	CheckBox autoaudioCheckbox;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.option_screen);
        readSettings();
        autoaudioCheckbox = (CheckBox)findViewById(R.id.option_autoaudio);
        autoaudioCheckbox.setChecked(autoaudioSetting);
        autoaudioCheckbox.setOnClickListener(this);
        
    }
    private void readSettings(){
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	autoaudioSetting = settings.getBoolean("autoaudio", true);
    }
    public void onClick(View v){
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	if(v == autoaudioCheckbox){
    		if(autoaudioCheckbox.isChecked()){
    			editor.putBoolean("autoaudio", true);
    		}
    		else{
    			editor.putBoolean("autoaudio", false);
    		}
    		editor.commit();
    	}
    }
}
