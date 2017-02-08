package com.vinay.vinplayer.anim;

/**
 * Created by Dell on 6/7/2016.
 */
import android.view.View;

public class AccordionTransformer extends ABaseTransformer {

    @Override
    protected void onTransform(View view, float position) {
        view.setPivotX(position < 0 ? 0 : view.getWidth());
        view.setScaleX(position < 0 ? 1f + position : 1f - position);
    }

}
