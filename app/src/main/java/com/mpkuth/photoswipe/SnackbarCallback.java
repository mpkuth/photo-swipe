package com.mpkuth.photoswipe;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

class SnackbarCallback extends Snackbar.Callback {

    interface OnDismissedListener {
        void onCommit(@NonNull Image image, int index);
        void onRollback(@NonNull Image image, int index);
    }

    private final int mIndex;

    @NonNull
    private final Image mImage;

    @NonNull
    private final OnDismissedListener mOnDismissedListener;

    SnackbarCallback(@NonNull OnDismissedListener onDismissedListener, @NonNull Image image, int index) {
        mOnDismissedListener = onDismissedListener;
        mImage = image;
        mIndex = index;
    }

    @Override
    public void onDismissed(Snackbar snackbar, int event) {
        if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
            mOnDismissedListener.onRollback(mImage, mIndex);
        } else {
            mOnDismissedListener.onCommit(mImage, mIndex);
        }
    }

}
