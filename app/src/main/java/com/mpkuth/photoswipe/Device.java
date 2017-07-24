package com.mpkuth.photoswipe;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

class Device {

    static float getPixlesForDp(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

}
