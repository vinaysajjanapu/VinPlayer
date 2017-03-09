package com.vinay.vinplayer.activities.settings;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import com.vinay.vinplayer.R;

public class MainSettings extends AppCompatActivity implements View.OnClickListener {

    CardView ui_settings,playback_settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ui_settings = (CardView)findViewById(R.id.ui_settings);
        playback_settings = (CardView)findViewById(R.id.playback_settings);
        ui_settings.setOnClickListener(this);
        playback_settings.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ui_settings:
                startActivity(new Intent(this,UI_Settings.class));
                overridePendingTransition(0,0);
                break;
            case R.id.playback_settings:
                startActivity(new Intent(this,PlayBackSettings.class));
                overridePendingTransition(0,0);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }
}
