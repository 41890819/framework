/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.widget;
import android.util.Log;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.InputEventConsistencyVerifier;
import android.view.ViewConfiguration;

public class GestureDetector {

    private static String TAG="GestureDetector";
    //事件监听接口
    public interface OnGestureListener {

        boolean onDown(boolean fromPhone);

        boolean onUp(MotionEvent ev,boolean fromPhone) ;

        boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY , boolean fromPhone);

        boolean onLongPress(boolean fromPhone);

        boolean onSlideUp( boolean fromPhone );
	
        boolean onSlideDown( boolean fromPhone);
	
        boolean onSlideLeft( boolean fromPhone );
       
        boolean onSlideRight( boolean fromPhone );

	boolean onTap( boolean fromPhone);

        boolean onDoubleTap(boolean fromPhone) ;
	
    }
    //事件监听接口实现类
    public static class SimpleOnGestureListener implements OnGestureListener {

        public boolean onLongPress(boolean fromPhone) {
	    return false;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY , boolean fromPhone) {
	    return false;
        }

        public boolean onDown(boolean fromPhone) {
	    return false;

        }

        public boolean onUp(MotionEvent ev,boolean fromPhone) {
	    return false;
        }    
        public boolean onDoubleTap(boolean fromPhone) {
	    return false;
        }

	public boolean onTap( boolean fromPhone ) {
	    return false;
	}

	public boolean onSlideUp( boolean fromPhone ){
	    return false;
	}

	public boolean onSlideDown( boolean fromPhone ){
	    return false;
	}

	public boolean onSlideLeft( boolean fromPhone ){
	    return false;
	}
	
	public boolean onSlideRight( boolean fromPhone ){
	    return false;
	}
    }

    private int mTouchSlopSquare;
    private int mDoubleTapTouchSlopSquare;
    private int mDoubleTapSlopSquare;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;
    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout() + ViewConfiguration.getTapTimeout();
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();

    // constants for Message.what used by GestureHandler below
    private static final int LONG_PRESS = 1;
    private static final int TAP = 2;

    private final Handler mHandler;
    private final OnGestureListener mListener;
    private boolean mStillDown;
    private boolean mDeferConfirmSingleTap;
    private boolean mInLongPress;
    private boolean mUsedLongPress; //if app
    private boolean mAlwaysInTapRegion;
    private boolean mAlwaysInBiggerTapRegion;
    private boolean fromPhone;
    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;

    private float mLastFocusX;
    private float mLastFocusY;
    private float mDownFocusX;
    private float mDownFocusY;
	
    private static final int MIN_QUICK_SLIDE_VELOCITY_X = 600;
    private static final int MIN_QUICK_SLIDE_VELOCITY_Y = 600;
    private static final int MIN_QUICK_SLIDE_DISTANCE_X = 20;
    private static final int MIN_QUICK_SLIDE_DISTANCE_Y = 20;
		
    private boolean mIsLongpressEnabled;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;


    private final InputEventConsistencyVerifier mInputEventConsistencyVerifier =
            InputEventConsistencyVerifier.isInstrumentationEnabled() ?
                    new InputEventConsistencyVerifier(this, 0) : null;

    private class GestureHandler extends Handler {
        GestureHandler() {
            super();
        }

        GestureHandler(Handler handler) {
            super(handler.getLooper());
        }

        @Override
	    public void handleMessage(Message msg) {
            switch (msg.what) {
            case LONG_PRESS:
                dispatchLongPress();
                break;
                
            case TAP:
                // If the user's finger is still down, do not count it as a tap             
		if (!mStillDown) {
		    mListener.onTap(false);
		} else {
		    mDeferConfirmSingleTap = true;
		}               
                break;

            default:
                throw new RuntimeException("Unknown message " + msg); //never
            }
        }
    }
   
    public GestureDetector(Context context, OnGestureListener listener) {
	mListener = listener;
        init(context);
	mHandler = new GestureHandler();
    }
    
    private void init(Context context) {
        if (mListener == null) {
            throw new NullPointerException("OnGestureListener must not be null");
        }
        mIsLongpressEnabled = true;

        // Fallback to support pre-donuts releases
        int touchSlop, doubleTapSlop, doubleTapTouchSlop;
        if (context == null) {
            //noinspection deprecation
            touchSlop = ViewConfiguration.getTouchSlop();
            doubleTapTouchSlop = touchSlop; // Hack rather than adding a hiden method for this
            doubleTapSlop = ViewConfiguration.getDoubleTapSlop();
            //noinspection deprecation
            mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
            mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
        } else {
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            touchSlop = configuration.getScaledTouchSlop();
            doubleTapTouchSlop = configuration.getScaledDoubleTapTouchSlop();
            doubleTapSlop = configuration.getScaledDoubleTapSlop();
            mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
            mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        }
        mTouchSlopSquare = touchSlop * touchSlop;
        mDoubleTapTouchSlopSquare = doubleTapTouchSlop * doubleTapTouchSlop;
        mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop;
    }

    public void setIsLongpressEnabled(boolean isLongpressEnabled) {
        mIsLongpressEnabled = isLongpressEnabled;
    }

    public boolean isLongpressEnabled() {
        return mIsLongpressEnabled;
    }

    public boolean onTouchEvent(MotionEvent ev) {
	// event from phone
	if (ev.getAction() == 20){
	    return handleSCGestureEvent(ev);
	}
        if (mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onTouchEvent(ev, 0);
        }

        final int action = ev.getAction();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final boolean pointerUp =
                (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? ev.getActionIndex() : -1;

        // Determine focal point
        float sumX = 0, sumY = 0;
        final int count = ev.getPointerCount();
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;
            sumX += ev.getX(i);
            sumY += ev.getY(i);
        }
        final int div = pointerUp ? count - 1 : count;
        final float focusX = sumX / div;
        final float focusY = sumY / div;

        boolean handled = false;

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_POINTER_DOWN:
            mDownFocusX = mLastFocusX = focusX;
            mDownFocusY = mLastFocusY = focusY;
            // Cancel long press and taps
            cancelTaps();
            break;

        case MotionEvent.ACTION_POINTER_UP:
            mDownFocusX = mLastFocusX = focusX;
            mDownFocusY = mLastFocusY = focusY;
            // Check the dot product of current velocities.
            // If the pointer that left was opposing another velocity vector, clear.
            mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
            final int upIndex = ev.getActionIndex();
            final int id1 = ev.getPointerId(upIndex);
            final float x1 = mVelocityTracker.getXVelocity(id1);
            final float y1 = mVelocityTracker.getYVelocity(id1);
            for (int i = 0; i < count; i++) {
                if (i == upIndex) continue;

                final int id2 = ev.getPointerId(i);
                final float x = x1 * mVelocityTracker.getXVelocity(id2);
                final float y = y1 * mVelocityTracker.getYVelocity(id2);
                final float dot = x + y;
                if (dot < 0) {
                    mVelocityTracker.clear();
                    break;
                }
            }
            break;

        case MotionEvent.ACTION_DOWN:
		Log.d(TAG,"--ACTION_DOWN");

                boolean hadTapMessage = mHandler.hasMessages(TAP);
                if (hadTapMessage) mHandler.removeMessages(TAP);
                if ((mCurrentDownEvent != null) && (mPreviousUpEvent != null) && hadTapMessage &&
                        isConsideredDoubleTap(mCurrentDownEvent, mPreviousUpEvent, ev)) {
                    // Give a callback with the first tap of the double-tap
                    handled |= mListener.onDoubleTap(false);
                } else {
                    // This is a first tap
		    Log.d(TAG,"send TAP DOUBLE_TAP_TIMEOUT"+DOUBLE_TAP_TIMEOUT);
                    mHandler.sendEmptyMessageDelayed(TAP, DOUBLE_TAP_TIMEOUT);
                }
            

            mDownFocusX = mLastFocusX = focusX;
            mDownFocusY = mLastFocusY = focusY;
            if (mCurrentDownEvent != null) {
                mCurrentDownEvent.recycle();
            }
            mCurrentDownEvent = MotionEvent.obtain(ev);
            mAlwaysInTapRegion = true;
            mAlwaysInBiggerTapRegion = true;
            mStillDown = true;
            mInLongPress = false;
            mUsedLongPress = false;
            mDeferConfirmSingleTap = false;
            if (mIsLongpressEnabled) {
		Log.d(TAG,"send long press...."+mCurrentDownEvent.getDownTime() +"  "+ LONGPRESS_TIMEOUT);
	    mHandler.sendEmptyMessageAtTime(LONG_PRESS,mCurrentDownEvent.getDownTime() + LONGPRESS_TIMEOUT);
	    }
            handled |= mListener.onDown(false);
            break;

        case MotionEvent.ACTION_MOVE:
	    Log.d(TAG,"--ACTION_MOVE x="+ ev.getX()+" --y="+ev.getY());
            if (mUsedLongPress) {
                break;
            }
	    
            final float scrollX = mLastFocusX - focusX;
            final float scrollY = mLastFocusY - focusY;

	    if (mAlwaysInTapRegion) {
                final int deltaX = (int) (focusX - mDownFocusX);
                final int deltaY = (int) (focusY - mDownFocusY);
                int distance = (deltaX * deltaX) + (deltaY * deltaY);
                if (distance > mTouchSlopSquare ) {
		    Log.d(TAG,"remove longpress..."+mTouchSlopSquare );
                    handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX, scrollY,false);
                    mLastFocusX = focusX;
                    mLastFocusY = focusY;
		    mHandler.removeMessages(LONG_PRESS);
                    mAlwaysInTapRegion = false;
                    mHandler.removeMessages(TAP);
                  
                }
                if (distance > mDoubleTapTouchSlopSquare) {
                    mAlwaysInBiggerTapRegion = false;
                }
                } else if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {
                  handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX, scrollY,false);
                  mLastFocusX = focusX;
                  mLastFocusY = focusY;
 }
            break;

        case MotionEvent.ACTION_UP:
	    Log.d(TAG,"ACTION_UP");
            mStillDown = false;
            MotionEvent currentUpEvent = MotionEvent.obtain(ev);
	    mListener.onUp(ev,false);
	    if (mInLongPress) {
                mHandler.removeMessages(TAP);
                mInLongPress = false;
            } else if (mAlwaysInTapRegion) {
                if ( mDeferConfirmSingleTap ) {
                    mListener.onTap(false);
                }
            } else {

                // A fling must travel the minimum tap distance
                final VelocityTracker velocityTracker = mVelocityTracker;
                final int pointerId = ev.getPointerId(0);
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final float velocityY = velocityTracker.getYVelocity(pointerId);
                final float velocityX = velocityTracker.getXVelocity(pointerId);
                if ((Math.abs(velocityY) > mMinimumFlingVelocity)
		    || (Math.abs(velocityX) > mMinimumFlingVelocity)){
		    Log.d(TAG,"mMinimumFlingVelocity"+mMinimumFlingVelocity);
		    onFling(mCurrentDownEvent,ev,velocityX,velocityY);
		    //mListener.onScroll(mCurrentDownEvent, ev, velocityX,velocityY,false);
                }
            }
            if (mPreviousUpEvent != null) {
                mPreviousUpEvent.recycle();
            }
            // Hold the event we obtained above - listeners may have changed the original.
            mPreviousUpEvent = currentUpEvent;
            if (mVelocityTracker != null) {
                // This may have been cleared when we called out to the
                // application above.
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
	    mUsedLongPress = false;
            mDeferConfirmSingleTap = false;
	    Log.d(TAG,"action up remove longpress...");
	    mHandler.removeMessages(LONG_PRESS);
            break;

        case MotionEvent.ACTION_CANCEL:
            cancel();
            break;
        }

        if (!handled && mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onUnhandledEvent(ev, 0);
        }
        return handled;
    }

    private void cancel() {
	mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mStillDown = false;
        mAlwaysInTapRegion = false;
        mAlwaysInBiggerTapRegion = false;
        mDeferConfirmSingleTap = false;
        if (mInLongPress) {
            mInLongPress = false;
        }
    }

    private void cancelTaps() {
	mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
        mAlwaysInTapRegion = false;
        mAlwaysInBiggerTapRegion = false;
        mDeferConfirmSingleTap = false;
        if (mInLongPress) {
            mInLongPress = false;
        }
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp,
            MotionEvent secondDown) {
        if (!mAlwaysInBiggerTapRegion) {
            return false;
        }

        if (secondDown.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
            return false;
        }

        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        return (deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare);
    }

    private void dispatchLongPress() {
	Log.d(TAG,"--dispatchLongPress"+mTouchSlopSquare);
        mHandler.removeMessages(TAP);
        mDeferConfirmSingleTap = false;
        mInLongPress = true;
	if(mListener.onLongPress(false) == true){
	    mUsedLongPress = true;
	}
    }
    private void onFling(MotionEvent mCurrentDownEvent, MotionEvent ev,float velocityX,float velocityY){
	//计算滑动事件
	Log.d(TAG,"onFling"+"  mCurrentDownEvent.getY()="+mCurrentDownEvent.getY()+" ev.getY()="+ev.getY()+"   mCurrentDownEvent.getX()="+mCurrentDownEvent.getX()+" ev.getX()="+ev.getX());
	Log.d(TAG,"onFling"+"  velocityX="+velocityX+"  velocityY= "+velocityY);
	if( Math.abs(mCurrentDownEvent.getY() - ev.getY()) <  Math.abs(mCurrentDownEvent.getX() - ev.getX())){
	    Log.d("test","left or right");
	    if((mCurrentDownEvent.getX() - ev.getX()) > MIN_QUICK_SLIDE_DISTANCE_X &&
	       Math.abs(velocityX) > MIN_QUICK_SLIDE_VELOCITY_X) {
		Log.d(TAG,"slide left");
		mListener.onSlideLeft(false);

	    }else if((ev.getX() - mCurrentDownEvent.getX()) > MIN_QUICK_SLIDE_DISTANCE_X &&
		     Math.abs(velocityX) > MIN_QUICK_SLIDE_VELOCITY_X) {
		Log.d(TAG,"slide right");
		mListener.onSlideRight(false);
	    }
	}else if ((mCurrentDownEvent.getY() - ev.getY()) > MIN_QUICK_SLIDE_DISTANCE_Y &&
		  Math.abs(velocityY) > MIN_QUICK_SLIDE_VELOCITY_Y){          
	    Log.d(TAG,"slide up");
	    //mListener.onSlideUp(false);

	}else if((ev.getY() - mCurrentDownEvent.getY()) > MIN_QUICK_SLIDE_DISTANCE_Y &&
		 Math.abs(velocityY) > MIN_QUICK_SLIDE_VELOCITY_Y){
	    Log.d(TAG,"slide down");
	    mListener.onSlideDown(false);
	}
    }
    private boolean handleSCGestureEvent(MotionEvent event) {
	if (mListener == null)
	    return false;
	// the gesture save as event.x
	int gesture = (int) event.getX();
	switch(gesture) {
	case 0:
	    return mListener.onTap(true);
	case 1:
	    return mListener.onDoubleTap(true);
	case 2:
	    return mListener.onLongPress(true);
	case 3:
	    return mListener.onSlideUp(true);
	case 4:
	    return mListener.onSlideDown(true);
	case 5:
	    return mListener.onSlideLeft(true);
	case 6:
	    return mListener.onSlideRight(true);
	default:
	    return false;
	}
    }

}
