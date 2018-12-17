package com.zsp.zspwidget.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import static com.zsp.zspwidget.util.DensityUtil.setCustomDensity;

/**
 * Created by Administrator on 2018/9/12.
 *
 */

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomDensity(this,getApplication());
    }
}
