package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.GestureDetector.OnGestureListener;

public class GlassFrameLayout extends FrameLayout {
    private final String TAG = "GlassFrameLayout";
    
    public final static boolean DEBUG = true;
    public final static int PAUSE = 0;
    public final static int RESUME = 1;
    public int mLifeState;
    private OnGestureListener mSimpleGesture ;
    
    public GlassFrameLayout(Context context) {
	this(context, null);
    }
    
    public GlassFrameLayout(Context context, AttributeSet attrs) {
	super(context, attrs);
	mLifeState = RESUME;
    }
    public GlassFrameLayout(Context context, AttributeSet attrs,
			       int defStyleAttr) {
	super(context, attrs, defStyleAttr);
	mLifeState = RESUME;
    }

    @Override
    protected void onAttachedToWindow() {
	super.onAttachedToWindow();
	requestGfFocus();
	if(DEBUG)Log.e(TAG,"onAttachedToWindow0 focused = "+isFocused());
    }
    
    /**
     *通过setDescendantFocusability(...)管理是否子view获得焦点 
     *
     * params:
     * FOCUS_BLOCK_DESCENDANTS 本身进行处理，不管是否处理成功，都不会分发给ChildView进行处理
     *
     * FOCUS_BEFORE_DESCENDANTS 本身先对焦点进行处理，如果没有处理则分发给child View进行处理
     *
     * FOCUS_AFTER_DESCENDANTS 先分发给Child View进行处理，如果所有的Child View都没有处理，则自己再处理
     */
    public void requestGfFocus(){
	//for get key event
	this.setFocusable(true);
	this.setFocusableInTouchMode(true);
	this.requestFocus();
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
	if(DEBUG)
	    Log.e(TAG, "GlassFrameLayout action="+event.getAction()+"keyCode= "+event.getKeyCode()
		  +"state = "+mLifeState);
	if (event.getAction() == KeyEvent.ACTION_DOWN && mLifeState == RESUME
	    && mSimpleGesture != null) {
	    if(DEBUG) Log.e(TAG, "------dispatchKeyEvent");
	    switch (event.getKeyCode()) {
	    case KeyEvent.KEYCODE_DPAD_LEFT:
		if(mSimpleGesture.onSlideLeft(true))
		    return true;
		else
		    break;
	    case KeyEvent.KEYCODE_DPAD_RIGHT:
		if(mSimpleGesture.onSlideRight(true))
		    return true;
		else
		    break;
	    case KeyEvent.KEYCODE_DPAD_UP:
		if(mSimpleGesture.onSlideUp(true))
		    return true;
		else
		    break;
	    case KeyEvent.KEYCODE_DPAD_DOWN:
		if(mSimpleGesture.onSlideDown(true))
		    return true;
		else 
		    break;
	    case KeyEvent.KEYCODE_DPAD_CENTER:
		if(mSimpleGesture.onTap(true))
		    return true;
		else
		    break;
	    }	    
	}
	return super.dispatchKeyEvent(event);
    }

    /**
     * 此view不可见时，应该进入pause状态
     */
    public void setLifeState(int lifeState){
	mLifeState = lifeState;
    }
    
    public void setSimpleGesture(OnGestureListener sgl){
	mSimpleGesture = sgl;
    }
}