package com.stardust.ooxx.widget;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Stardust on 2017/5/25.
 */

public class ResizeAnimation extends Animation {

    private View mView;
    private int mInitialWidth;
    private int mWidth;


    public ResizeAnimation(View view, int width) {
        mView = view;
        mWidth = width;
        mInitialWidth = mView.getWidth();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int width = (int) (mInitialWidth + interpolatedTime * (mWidth - mInitialWidth));
        ViewGroup.LayoutParams params = mView.getLayoutParams();
        params.width = width;
        mView.setLayoutParams(params);
        mView.requestLayout();
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
