package android.widget;

import android.view.MotionEvent;

/*
 * gesture Listener of the glass touch board.
 */
public interface IngGestureDetectListener {
	
	/*
	 * single tap on the touch board.
	 */
	public boolean onTap();
	
	/*
	 * double tap on the touch board.
	 */
	public boolean onDoubleTap();

	/*
	 * long press on the touch board.
	 */
	public boolean onLongPress();
	
	/*
	 * slide up on the touch board. but should never be called on 3rd app. because it has been taken by system notification.
	 */
	public boolean onSlideUp();
	
	/*
	 * slide down on the touch board.
	 */
	public boolean onSlideDown();
	
	/*
	 * quick slide to the left of touch board.
	 */
	public boolean onSlideLeft();
	
	/*
	 * quick slide to the right of touch board.
	 */
	public boolean onSlideRight();
	
	/*
	 * called when touched and moving.
	 */
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
}