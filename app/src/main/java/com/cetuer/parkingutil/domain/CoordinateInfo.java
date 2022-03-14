package com.cetuer.parkingutil.domain;

import android.widget.TextView;

/**
 * Created by Cetuer on 2022/3/13 10:22.
 * 坐标信息
 */
public class CoordinateInfo {
    /**
     * 坐标的TextView
     */
    private TextView textView;

    /**
     * 此坐标是否已收集
     */
    private boolean collect;

    public CoordinateInfo(TextView textView) {
        this.textView = textView;
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public boolean isCollect() {
        return collect;
    }

    public void setCollect(boolean collect) {
        this.collect = collect;
    }
}
