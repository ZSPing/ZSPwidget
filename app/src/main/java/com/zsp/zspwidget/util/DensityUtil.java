package com.zsp.zspwidget.util;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

/**
 * Created by Administrator on 2018/9/12.
 * 头条屏幕适配
 */

public class DensityUtil {

    public static float SCREEN_RATIO;
    private static float sNoncompatDensity;
    private static float sNoncompatScaledDensity;
    public static void setCustomDensity(@NonNull Activity activity, @NonNull final Application application){
        final DisplayMetrics appDisplayMetrics=application.getResources().getDisplayMetrics();
        if(sNoncompatDensity==0){
            sNoncompatDensity=appDisplayMetrics.density;
            sNoncompatScaledDensity=appDisplayMetrics.scaledDensity;
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    if(newConfig!=null&&newConfig.fontScale>0){
                        sNoncompatScaledDensity=application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
        }
        final float targetDensity=appDisplayMetrics.widthPixels/360f;
        SCREEN_RATIO=targetDensity;
        final float targetScaledDensity=targetDensity*(sNoncompatScaledDensity/sNoncompatDensity);
        final int targetDensityDpi=(int)(160*targetDensity);
        final DisplayMetrics activityDisplayMetrics=activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density=targetDensity;
        activityDisplayMetrics.scaledDensity=targetScaledDensity;
        activityDisplayMetrics.densityDpi=targetDensityDpi;
    }

    public static int getSize(int size){
       return (int)SCREEN_RATIO*size;
    }
}
