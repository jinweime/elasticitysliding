package com.jin.mak.elasticitysliding;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * @Author JinWei
 * @Date 2017/10/9
 * @Description
 */

public class ElasticScrollView extends RelativeLayout {
    private VelocityTracker mV;
    private String tag = getClass().getSimpleName();
    private int mTopBoundary;
    private int mBottomBoundary;
    private int mDownReboundOffst = 200;

    public void addObservable(OnObservableListener onObservableListener) {
        this.onObservableListener = onObservableListener;
    }


    private ElasticScrollView.OnObservableListener onObservableListener;

    {
        mScroller = new Scroller(getContext());
        mV = VelocityTracker.obtain();

    }

    public ElasticScrollView(Context context) {
        super(context);
    }

    public ElasticScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void computeScroll() {
        super.computeScroll();
        View viewGroup = (View) getParent();

        Log.i(tag, "### currY =" + mLastY + " ### viewGroup=" + mScroller.getCurrY());
        if (mScroller.computeScrollOffset()) {
            if (!mRebound && !mStartScroller) {
                if (mScroller.getCurrY() < -mTopBoundary) {
                    //up rebound
                    mScroller.startScroll(0, -mTopBoundary, -viewGroup.getScrollX(), mTopBoundary, 500);
                    mRebound = true;
                    Log.i(tag, "### up rebound ###");
                } else if (mScroller.getCurrY() > mBottomBoundary) {
                    //down rebound
                    mScroller.startScroll(0, mBottomBoundary, -viewGroup.getScrollX(), -mDownReboundOffst, 2500);
                    mRebound = true;
                    Log.i(tag, "### down rebound ###");
                } else {
                    mRebound = false;
                }
            } else {
                //normal rolling
                mRebound = false;
            }
            updateOffest(viewGroup.getScrollY(), mScroller.getCurrY()-mLastY);
            ((View) getParent()).scrollTo(0, mScroller.getCurrY());
            invalidate();
            mLastY = mScroller.getCurrY();
        }
    }

    private void updateOffest(int boundaries, float offest) {
        if (onObservableListener != null) {
            onObservableListener.update(boundaries, (int) offest);
        }
    }

    float downRawY;
    boolean mStartScroller;
    private int mLastY;
    private Scroller mScroller;
    boolean mRebound = false;

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        mTopBoundary = getTop();
        mBottomBoundary = getBottom() / 2;
        Log.i(tag, "### top =" + getTop() + " ###  bottom=" + mBottomBoundary);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int mCurrentVelocityUnits = 200;
        int mScrollerTime = 3000;
        int mScrollerRate = 10;
        int action = event.getAction();
        int y = (int) event.getY();
        int offsetY = mLastY - y;
        mV.addMovement(event);
        mV.computeCurrentVelocity(mCurrentVelocityUnits);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                downRawY = event.getRawY();
                mScroller.abortAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                View viewGroup1 = (View) getParent();
                ((View) getParent()).scrollBy(0, offsetY);
                updateOffest(viewGroup1.getScrollY(), offsetY);
                break;
            case MotionEvent.ACTION_UP:
                boolean direction = downRawY < event.getRawY();
                int mVelocityY = (int) mV.getYVelocity();
                View viewGroup = (View) getParent();
                if (viewGroup.getScrollY() < -mTopBoundary) {
                    mStartScroller = true;
                    mScroller.startScroll(0, viewGroup.getScrollY(), -viewGroup.getScrollX(), -viewGroup.getScrollY());
                } else if (viewGroup.getScrollY() > mBottomBoundary) {
                    mStartScroller = true;
                    mScroller.startScroll(0, mBottomBoundary, -viewGroup.getScrollX(), -mDownReboundOffst);
                } else {
                    mStartScroller = false;
                    //~ The operation ensures that the sliding value is consistent with the direction
                    if ((direction && mVelocityY > 0) || (!direction && mVelocityY < 0)) {
                        mVelocityY = ~mVelocityY;
                    }
                    mScroller.startScroll(0, viewGroup.getScrollY(), -viewGroup.getScrollX(), mVelocityY * mScrollerRate, mScrollerTime);
                }
                mLastY = viewGroup.getScrollY();
                Log.i(tag, "### mScroller.getStartY()" + viewGroup.getScrollY());
                invalidate();

                break;
        }
        return true;
    }


    public interface OnObservableListener {
        void update(int boundaries, int offsetY);
    }

}
