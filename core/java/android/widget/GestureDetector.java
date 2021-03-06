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
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.os.SystemProperties;
public class GestureDetector {

    private static String TAG="GestureDetector";
    private boolean DEBUG = false;
    private static final int MINIMUM_FLING_VELOCITY = 100;
    private static final int TOUCH_SLOP1 = 7;
    private static final int TOUCH_SLOP2 = 20;
    private static final int MAXIMUM_FLING_VELOCITY = 8000;
    private OnDoubleTapListener mDoubleTapListener = null;
    private Context mContext;
    private String mTouchBoardId;
    public static final int GESTURE_SINGLE_TAP = 0;
    public static final int GESTURE_DOUBLE_TAP = 1;
    public static final int GESTURE_LONG_PRESS = 2;
    public static final int GESTURE_SLIDE_UP = 3;
    public static final int GESTURE_SLIDE_DOWN = 4;
    public static final int GESTURE_SLIDE_LEFT = 5;
    public static final int GESTURE_SLIDE_RIGHT = 6;

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
    }

    public interface OnDoubleTapListener {

        boolean onDoubleTap(boolean fromPhone);
    }

    //SimpleOnGestureListener实现了<除了>双击在内的所有手势，若用户不需要双击手势，推荐使用SimpleOnGestureListener，因为这个点击手势响应时间短
    public static class SimpleOnGestureListener implements OnGestureListener{

        public boolean onLongPress(boolean fromPhone) {
	    return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY , boolean fromPhone) {
	    return true;
        }

        public boolean onDown(boolean fromPhone) {
	    return true;

        }

        public boolean onUp(MotionEvent ev,boolean fromPhone) {
	    return true;
        }

	public boolean onTap( boolean fromPhone ) {
	    return true;
	}

	public boolean onSlideUp( boolean fromPhone ){
	    return true;
	}

	public boolean onSlideDown( boolean fromPhone ){
	    return true;
	}

	public boolean onSlideLeft( boolean fromPhone ){
	    return true;
	}
	
	public boolean onSlideRight( boolean fromPhone ){
	    return true;
	}
    }
    //CompleteOnGestureListener实现了包括双击在内的左右手势，若用户需要双击手势，使用CompleteOnGestureListener
    public static class CompleteOnGestureListener extends SimpleOnGestureListener implements OnDoubleTapListener{

        public boolean onDoubleTap(boolean fromPhone){
    	    return true;
    	}
    }

    public  void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
        mDoubleTapListener = onDoubleTapListener;
    }
    private int mTouchSlopSquare;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;
    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout() + ViewConfiguration.getTapTimeout();
    private static final int DOUBLE_TAP_TIMEOUT = 300;

    // constants for Message.what used by GestureHandler below
    private static final int LONG_PRESS = 1;
    private static final int TAP = 2;

    private final Handler mHandler;
    private final OnGestureListener mListener;
    private boolean mInLongPress;
    private boolean mUsedLongPress; //if app
    private boolean mAlwaysInTapRegion;
    private boolean fromPhone;
    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;

    private float mLastFocusX;
    private float mLastFocusY;
    private float mDownFocusX;
    private float mDownFocusY;

    private static final int MIN_QUICK_SLIDE_DISTANCE_X = 20;
    private static final int MIN_QUICK_SLIDE_DISTANCE_Y = 8;
		
    private boolean mIsLongpressEnabled;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

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
		if(DEBUG)Log.d(TAG,"----------recive tap message");      
		mListener.onTap(false);
                break;

            default:
                throw new RuntimeException("Unknown message " + msg); //never
            }
        }
    }
   
    public GestureDetector(Context context, OnGestureListener listener) {
	mListener = listener;
	mContext = context;
        init(context);
	mHandler = new GestureHandler();
        if (listener instanceof OnDoubleTapListener) {
            setOnDoubleTapListener((OnDoubleTapListener) listener);
        }
    }
    private void init(Context context) {
        if (mListener == null) {
            throw new NullPointerException("OnGestureListener must not be null");
        }
        mIsLongpressEnabled = true;

        // Fallback to support pre-donuts releases
        if (context == null) {
            mMinimumFlingVelocity = MINIMUM_FLING_VELOCITY;//wConfiguration.getMinimumFlingVelocity();
            mMaximumFlingVelocity = MAXIMUM_FLING_VELOCITY;
        } else {
	    final ViewConfiguration configuration = ViewConfiguration.get(context);
            mMinimumFlingVelocity = MINIMUM_FLING_VELOCITY;
            mMaximumFlingVelocity = MAXIMUM_FLING_VELOCITY;//configuration.getScaledMaximumFlingVelocity()
        }
	mTouchBoardId = SystemProperties.get("ro.touchboard.id","");
	if(DEBUG)Log.d(TAG,"TOUCHBOARD_ID="+mTouchBoardId);
	mTouchSlopSquare = mTouchBoardId.equals("ITE7236")?TOUCH_SLOP2 * TOUCH_SLOP2 : TOUCH_SLOP1 * TOUCH_SLOP1;
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

        switch (action) {
        case MotionEvent.ACTION_DOWN:
	    if(DEBUG)Log.d(TAG,"--ACTION_DOWN x="+ ev.getX()+" --y="+ev.getY());
            mDownFocusX = mLastFocusX = focusX;
            mDownFocusY = mLastFocusY = focusY;
            if (mCurrentDownEvent != null) {
                mCurrentDownEvent.recycle();
            }
            mCurrentDownEvent = MotionEvent.obtain(ev);
            mAlwaysInTapRegion = true;
            mInLongPress = false;
            mUsedLongPress = false;
            if (mIsLongpressEnabled) {
		if (DEBUG)Log.d(TAG,"send long press...."+mCurrentDownEvent.getDownTime() +"  "+ LONGPRESS_TIMEOUT);
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
		if(DEBUG)Log.d(TAG,"distance="+distance+"  mTouchSlopSquare="+mTouchSlopSquare);
                if (distance > mTouchSlopSquare ) {
		    if(DEBUG)Log.d(TAG,"remove longpress and TAP");
                    handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX, scrollY,false);
                    mLastFocusX = focusX;
                    mLastFocusY = focusY;
		    mHandler.removeMessages(TAP);
		    mHandler.removeMessages(LONG_PRESS);
                    mAlwaysInTapRegion = false;
                  
                }
	    } else if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {
		handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX, scrollY,false);
		mLastFocusX = focusX;
		mLastFocusY = focusY;
	    }
            break;

        case MotionEvent.ACTION_UP:
	    if(DEBUG)Log.d(TAG,"----ACTION_UP x="+ ev.getX()+" --y="+ev.getY());
            MotionEvent currentUpEvent = MotionEvent.obtain(ev);
	    handled = mListener.onUp(ev,false);
	    if (mInLongPress) {
		mHandler.removeMessages(TAP);
                mInLongPress = false;
            } else {
                if (mAlwaysInTapRegion) {
		    isConsiderDoubleTapOrTap();
		} else {
		    // A fling must travel the minimum tap distance
		    final VelocityTracker velocityTracker = mVelocityTracker;
		    final int pointerId = ev.getPointerId(0);
		    velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
		    final float velocityY = velocityTracker.getYVelocity(pointerId);
		    final float velocityX = velocityTracker.getXVelocity(pointerId);
		    if(DEBUG)Log.d(TAG,"(Math.abs(velocityY)"+(Math.abs(velocityY))+"  (Math.abs(velocityX)"+(Math.abs(velocityX)));          
		    if ((Math.abs(velocityY) > mMinimumFlingVelocity)
			|| (Math.abs(velocityX) > mMinimumFlingVelocity)){
			handled |= onFling(mCurrentDownEvent,ev,velocityX,velocityY);
		    }
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
	   if(mHandler.hasMessages(LONG_PRESS))mHandler.removeMessages(LONG_PRESS);
            break;

        case MotionEvent.ACTION_CANCEL:
            cancel();
            break;
        }

        return handled;
    }

    private void cancel() {
	mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mAlwaysInTapRegion = false;
        if (mInLongPress) {
            mInLongPress = false;
        }
    }

    private void cancelTaps() {
	mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
        mAlwaysInTapRegion = false;
        if (mInLongPress) {
            mInLongPress = false;
        }
    }

    private boolean isConsiderDoubleTapOrTap(){
	if(mDoubleTapListener == null){
	    if(DEBUG)Log.d(TAG,"user do'nt need doubleTap,so onTap() immediately");
	    return  mListener.onTap(false);
	}else{
	    if(mHandler.hasMessages(TAP) == false){
		//first tap
		if(DEBUG)Log.d(TAG,"first tap of doubleTap");
		mHandler.sendEmptyMessageDelayed(TAP, DOUBLE_TAP_TIMEOUT);
		return true;
	    }else{
		//seconed tap
		if(DEBUG)Log.d(TAG,"doubleTap event");
		mHandler.removeMessages(TAP);
		return mDoubleTapListener.onDoubleTap(false);
	    }
	}
	
    }

    private void dispatchLongPress() {
	if (DEBUG)Log.d(TAG,"--dispatchLongPress");
        mInLongPress = true;
	if(mListener.onLongPress(false) == true){
	    mUsedLongPress = true;
	}
    }
    private boolean onFling(MotionEvent mCurrentDownEvent, MotionEvent ev,float velocityX,float velocityY){
	//计算滑动事件
	if (DEBUG)Log.d(TAG,"onFling"+"  mCurrentDownEvent.getY()="+mCurrentDownEvent.getY()+" ev.getY()="+ev.getY()+"   mCurrentDownEvent.getX()="+mCurrentDownEvent.getX()+" ev.getX()="+ev.getX());
	if (DEBUG)Log.d(TAG,"onFling"+"  velocityX="+velocityX+"  velocityY= "+velocityY);
	if(DEBUG)Log.d(TAG,"distance Y"+(Math.abs(mCurrentDownEvent.getY() - ev.getY())));
	if(DEBUG)Log.d(TAG,"distances X"+(Math.abs(mCurrentDownEvent.getX() - ev.getX())));
	if(Math.abs(mCurrentDownEvent.getY() - ev.getY()) <  Math.abs(mCurrentDownEvent.getX() - ev.getX())){
	    if((mCurrentDownEvent.getX() - ev.getX()) > MIN_QUICK_SLIDE_DISTANCE_X) {
		if (DEBUG)Log.d(TAG,"slide left");
		return mListener.onSlideLeft(false);

	    }else if((ev.getX() - mCurrentDownEvent.getX()) > MIN_QUICK_SLIDE_DISTANCE_X) {
		if (DEBUG)Log.d(TAG,"slide right");
		return mListener.onSlideRight(false);
	    }
	}else if ((ev.getY() - mCurrentDownEvent.getY()) > MIN_QUICK_SLIDE_DISTANCE_Y){          
	    if (DEBUG)Log.d(TAG,"slide down");
	    return mListener.onSlideDown(false);

	}else if((mCurrentDownEvent.getY() - ev.getY()) > MIN_QUICK_SLIDE_DISTANCE_Y){
	    if (DEBUG)Log.d(TAG,"slide up");
	    return mListener.onSlideUp(false);	    
	}
	return true;
    }
    private boolean handleSCGestureEvent(MotionEvent event) {
	if (mListener == null)
	    return false;
	// the gesture save as event.x
	 int x =  (int) event.getX();
	 int y = (int)event.getY();
	 int halfScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels/2;
	 int gesture = x - halfScreenWidth;
	if(gesture == GESTURE_SINGLE_TAP){
	    return mListener.onTap(true);
	}else if(gesture == GESTURE_DOUBLE_TAP){
	    if(mDoubleTapListener == null){
		return false;
	    }else{
		return mDoubleTapListener.onDoubleTap(true);
	    }
	}else if(gesture == GESTURE_LONG_PRESS){
	    return mListener.onLongPress(true);
	}else if(gesture == GESTURE_SLIDE_UP){
	    return mListener.onSlideUp(true);
	}else if(gesture == GESTURE_SLIDE_DOWN){
	    return mListener.onSlideDown(true);
	}else if(gesture == GESTURE_SLIDE_LEFT){
	    return mListener.onSlideLeft(true);
	}else if(gesture == GESTURE_SLIDE_RIGHT){
	    return mListener.onSlideRight(true);
	}else{
	    return false;
	}
    }

}
