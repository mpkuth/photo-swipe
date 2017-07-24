package com.mpkuth.photoswipe;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

class SwipeDetector implements View.OnTouchListener, GestureDetector.OnGestureListener {

    interface OnSwipeListener {
        boolean onSwipeUp();
        boolean onSwipeDown();
        boolean onSwipeLeft();
        boolean onSwipeRight();
    }

    @NonNull
    private final OnSwipeListener mOnSwipeListener;

    @NonNull
    private final GestureDetector mGestureDetector;

    private static final float SWIPE_DISTANCE_THRESHOLD = Device.getPixlesForDp(100);
    private static final float SWIPE_VELOCITY_THRESHOLD = Device.getPixlesForDp(1000);

    SwipeDetector(@NonNull Context context, @NonNull OnSwipeListener onSwipeListener) {
        mGestureDetector = new GestureDetector(context, this);
        mOnSwipeListener = onSwipeListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();
        return Math.abs(diffX) < Math.abs(diffY) ?
                onVerticalSwipe(diffY, velocityY) :
                onHorizontalSwipe(diffX, velocityX);
    }

    private boolean onVerticalSwipe(float diffY, float velocityY) {
        return Math.abs(diffY) > SWIPE_DISTANCE_THRESHOLD &&
                Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD &&
                (diffY > 0 ? mOnSwipeListener.onSwipeDown() : mOnSwipeListener.onSwipeUp());
    }

    private boolean onHorizontalSwipe(float diffX, float velocityX) {
        return Math.abs(diffX) > SWIPE_DISTANCE_THRESHOLD &&
                Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD &&
                (diffX > 0 ? mOnSwipeListener.onSwipeRight() : mOnSwipeListener.onSwipeLeft());
    }

}
