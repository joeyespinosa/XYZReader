package com.axelia.xyzreader.utils;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class FlexibleHeightNetworkImageView extends AppCompatImageView {
    private float mAspectRatio = 1.5f;

    public FlexibleHeightNetworkImageView(Context context) {
        super(context);
    }

    public FlexibleHeightNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlexibleHeightNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int) (measuredWidth / mAspectRatio));
    }
}
