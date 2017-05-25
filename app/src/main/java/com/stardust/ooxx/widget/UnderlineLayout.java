package com.stardust.ooxx.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.stardust.ooxx.R;

/**
 * Created by Stardust on 2017/5/23.
 */

public class UnderlineLayout extends FrameLayout {

    private float mLineLength = -1;
    private float mLineHeight = 6;
    private Paint mLinePaint = new Paint();

    public UnderlineLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public UnderlineLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UnderlineLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        mLinePaint.setColor(getResources().getColor(R.color.colorTitleText));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLineLength == -1) {
            mLineLength = getFullLineLength();
        }
        float left = (getWidth() - mLineLength) / 2;
        float top = getHeight() - mLineHeight;
        float right = getWidth() - left;
        float bottom = getHeight();
        float roundRadius = mLineHeight / 2;
        canvas.drawRoundRect(left, top, right, bottom, roundRadius, roundRadius, mLinePaint);
    }

    public void setLineLength(float lineLength) {
        mLineLength = lineLength;
        invalidate();
    }

    public float getFullLineLength() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    public void setUpWithViewPager(final ViewPager pager, final int position) {
        if (pager.getCurrentItem() == position) {
            setLineLength(getFullLineLength());
        } else {
            setLineLength(0);
        }
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int selectPosition, float positionOffset, int positionOffsetPixels) {
                if (position == selectPosition) {
                    setLineLength(getFullLineLength() * (1 - positionOffset));
                } else if (position == (selectPosition + 1) % pager.getAdapter().getCount()) {
                    setLineLength(getFullLineLength() * positionOffset);
                }
            }

        });
    }
}
