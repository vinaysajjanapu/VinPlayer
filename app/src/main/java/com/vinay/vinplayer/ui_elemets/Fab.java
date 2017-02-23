package com.vinay.vinplayer.ui_elemets;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import com.gordonwong.materialsheetfab.AnimatedFab;
import com.vinay.vinplayer.R;

/**
 * Created by vinaysajjanapu on 23/2/17.
 */

public class Fab extends FloatingActionButton implements AnimatedFab {
    public Fab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Shows the FAB.
     */
    @Override
    public void show() {
        show(20, 20);
    }

    /**
     * Shows the FAB and sets the FAB's translation.
     *
     * @param translationX translation X value
     * @param translationY translation Y value
     */
    @Override
    public void show(float translationX, float translationY) {
        // NOTE: Using the parameters is only needed if you want
        // to support moving the FAB around the screen.
        // NOTE: This immediately hides the FAB. An animation can
        // be used instead - see the sample app.
        setImageResource(R.drawable.icon_play);
        //setBackgroundColor(Color.GREEN);
        //setColorFilter(Color.GREEN);
        //setRippleColor(Color.RED);
        setVisibility(View.VISIBLE);
    }

    /**
     * Hides the FAB.
     */
    @Override
    public void hide() {
        // NOTE: This immediately hides the FAB. An animation can
        // be used instead - see the sample app.
        setVisibility(View.INVISIBLE);
    }
}