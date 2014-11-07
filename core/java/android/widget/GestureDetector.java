package android.widget;

import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/*
 * gesture detector on glass touch board.
 */
public class GestureDetector {

	private GestureDetectListener mGestureListener = null;
	
	private android.view.GestureDetector mSysGestureDetector = null;
	
	private class SysGestureListener extends android.view.GestureDetector.SimpleOnGestureListener {
		
		private static final int MIN_QUICK_SLIDE_VELOCITY_X = 600;
		private static final int MIN_QUICK_SLIDE_VELOCITY_Y = 600;
		private static final int MIN_QUICK_SLIDE_DISTANCE_X = 20;
		private static final int MIN_QUICK_SLIDE_DISTANCE_Y = 7;
		
		// 双击的第二下Touch down时触发
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			mGestureListener.onDoubleTap(false);
			return true;
		}

		// 双击的第二下Touch down和up都会触发，可用e.getAction()区分
		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			return true;
		}

		// Touch down时触发
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		// Touch了滑动一点距离后，up时触发
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if((e1.getX() - e2.getX()) > MIN_QUICK_SLIDE_DISTANCE_X &&
					Math.abs(velocityX) > MIN_QUICK_SLIDE_VELOCITY_X) {
				mGestureListener.onSlideLeft(false);
			}else if((e2.getX() - e1.getX()) > MIN_QUICK_SLIDE_DISTANCE_X &&
					Math.abs(velocityX) > MIN_QUICK_SLIDE_VELOCITY_X) {
				mGestureListener.onSlideRight(false);
			}else if((e1.getY() - e2.getY()) > MIN_QUICK_SLIDE_DISTANCE_Y &&
					Math.abs(velocityY) > MIN_QUICK_SLIDE_VELOCITY_Y){
				//mGestureListener.onSlideUp(false);
			}else if((e2.getY() - e1.getY()) > MIN_QUICK_SLIDE_DISTANCE_Y &&
					Math.abs(velocityY) > MIN_QUICK_SLIDE_VELOCITY_Y){
				mGestureListener.onSlideDown(false);
			}
			
			return true;
		}

		// Touch了不移动一直Touch down时触发
		@Override
		public void onLongPress(MotionEvent e) {
			mGestureListener.onLongPress(false);
		}

		// Touch了滑动时触发
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
		        mGestureListener.onScroll(e1, e2, distanceX, distanceY, false);
			
			return true;
		}

		/**
		 * Touch了还没有滑动时触发 (1)onDown只要Touch Down一定立刻触发 (2)Touch
		 * Down后过一会没有滑动先触发onShowPress再触发onLongPress So: Touch Down后一直不滑动，onDown
		 * -> onShowPress -> onLongPress这个顺序触发。
		 **/
		@Override
		public void onShowPress(MotionEvent e) {
			
		}

		/**
		 * 两个函数都是在Touch Down后又没有滑动(onScroll)，又没有长按(onLongPress)，然后Touch Up时触发
		 * 点击一下非常快的(不滑动)Touch Up: onDown->onSingleTapUp->onSingleTapConfirmed
		 * 点击一下稍微慢点的(不滑动)Touch Up:
		 * onDown->onShowPress->onSingleTapUp->onSingleTapConfirmed
		 **/
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			mGestureListener.onTap(false);
			
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return true;
		}
	}

        public GestureDetector(Context context, GestureDetectListener listener) {
		mGestureListener = listener;
		mSysGestureDetector = new android.view.GestureDetector(context, new SysGestureListener());
		mSysGestureDetector.setIsLongpressEnabled(true);
	}
	
        public void setIsLongpressEnabled(boolean isLongpressEnabled) {
	        mSysGestureDetector.setIsLongpressEnabled(isLongpressEnabled);
	}

        private boolean handleSCGestureEvent(MotionEvent event) {
	        if (mGestureListener == null)
		    return false;
		// the gesture save as event.x
		int gesture = (int) event.getX();
		switch(gesture) {
		case 0:
		    return mGestureListener.onTap(true);
		case 1:
		    return mGestureListener.onDoubleTap(true);
		case 2:
		    return mGestureListener.onLongPress(true);
		case 3:
		    return mGestureListener.onSlideUp(true);
		case 4:
		    return mGestureListener.onSlideDown(true);
		case 5:
		    return mGestureListener.onSlideLeft(true);
		case 6:
		    return mGestureListener.onSlideRight(true);
		default:
		    return false;
		}
	}

	/*
	 * shall be called at activity::onTouchEvent.
	 */
	public boolean onTouchEvent(MotionEvent event) {
	        // event from phone
	        if (event.getAction() == 20)
		    return handleSCGestureEvent(event);
		else
		    return mSysGestureDetector.onTouchEvent(event);
	}

        public static class SimpleOnGestureListener implements android.widget.GestureDetectListener{
	        /*
		 * single tap on the touch board.
		 */
	        public boolean onTap(boolean fromPhone){
		    return false;
		}
	
	        /*
		 * double tap on the touch board.
		 */
	        public boolean onDoubleTap(boolean fromPhone){
		    return false;
		}

	        /*
		 * long press on the touch board.
		 */
	        public boolean onLongPress(boolean fromPhone){
		    return false;
		}
	
	        /*
		 * slide up on the touch board. but should never be called on 3rd app. because it has been taken by system notification.
		 */
	        public boolean onSlideUp(boolean fromPhone){
		    return false;
		}
	
	        /*
		 * slide down on the touch board.
		 */
	        public boolean onSlideDown(boolean fromPhone){
		    return false;
		}
	    
	        /*
		 * quick slide to the left of touch board.
		 */
	        public boolean onSlideLeft(boolean fromPhone){
		    return false;
		}
	
	        /*
		 * quick slide to the right of touch board.
		 */
	        public boolean onSlideRight(boolean fromPhone){
		    return false;
		}
	
	        /*
		 * called when touched and moving.
		 */
	        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, boolean fromPhone){
		    return false;
		}
	    
	}

}
