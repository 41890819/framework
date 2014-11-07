package android.widget;

import android.view.MotionEvent;

/*
 * gesture Listener of the glass touch board.
 */
public interface GestureDetectListener {
	
	/*
	 * single tap on the touch board.
	 */
	public boolean onTap(boolean fromPhone);
	
	/*
	 * double tap on the touch board.
	 */
	public boolean onDoubleTap(boolean fromPhone);

	/*
	 * long press on the touch board.
	 */
	public boolean onLongPress(boolean fromPhone);
	
	/*
	 * slide up on the touch board. but should never be called on 3rd app. because it has been taken by system notification.
	 */
	public boolean onSlideUp(boolean fromPhone);
	
	/*
	 * slide down on the touch board.
	 */
	public boolean onSlideDown(boolean fromPhone);
	
	/*
	 * quick slide to the left of touch board.
	 */
	public boolean onSlideLeft(boolean fromPhone);
	
	/*
	 * quick slide to the right of touch board.
	 */
	public boolean onSlideRight(boolean fromPhone);
	
	/*
	 * called when touched and moving.
	 */
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, boolean fromPhone);
}