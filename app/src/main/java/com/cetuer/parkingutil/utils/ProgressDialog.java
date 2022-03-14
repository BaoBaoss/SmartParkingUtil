package com.cetuer.parkingutil.utils;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.cetuer.parkingutil.R;
import com.daimajia.numberprogressbar.NumberProgressBar;

import scut.carson_ho.kawaii_loadingview.Kawaii_LoadingView;

/**
 * Created by Cetuer on 2022/3/12 20:26.
 * 带进度的加载框
 */
public class ProgressDialog extends Dialog {
    private NumberProgressBar numberProgressBar;
    public ProgressDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
        setContentView(R.layout.progress_loading);
        setCancelable(false);
        numberProgressBar = findViewById(R.id.progress);
    }

    /**
     * 设置进度
     * @param value 进度
     */
    public void setProgress(int value) {
        numberProgressBar.setProgress(value);
    }

    /**
     * 初始化，为下一次使用做准备
     */
    public void init() {
        numberProgressBar.setProgress(0);
    }
}
