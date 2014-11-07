package android.widget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.widget.GestureDetector;
import android.widget.GestureDetector.SimpleOnGestureListener;
import android.view.SoundEffectConstants;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Scroller;
import com.android.internal.msgcenter.IMessageCenterService;
/**
 * A common case of using PagedView as follows:
 * 
 * PagedView pagedView = new PagedView(context); // or findViewById(R.id.xxx)...
 * pagedView.setAdapter(adapter); // must do this first
 * pagedView.setPageCountInScreen(1); // or setCircleFlip ...
 * pagedView.setOnItemClickListener(listener); // or setOnPageSelectedListener
 * 
 * if change data:
 * list.remove(position);
 * adapter.notifyDataSetChanged();
 */

public class AdapterPagedView extends AdapterView<BaseAdapter> {
	private static final String TAG = "AdapterPagedView";
	private static final boolean DEBUG = false;
	private static final boolean DEVELOPER_MODE = false;

	protected Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	/**
	 * current left screen
	 */
	protected int mCurScreen;

	/**
	 * default screen
	 */
	private int mDefaultScreen = 0;

	private static final int INVALID_SCREEN = -1;
	/**
	 * no valid touch motion
	 */
	protected static final int TOUCH_STATE_REST = 0;
	/**
	 * pages are scrolling
	 */
	protected static final int TOUCH_STATE_SCROLLING = 1;
	/**
	 * pages are flying
	 */
	protected static final int TOUCH_STATE_FLYING = 2;
	/**
	 * rest state when pages are flying
	 */
	protected static final int TOUCH_STATE_FLYING_REST = 3;
	/**
	 * scroll when pages are flying
	 */
	protected static final int TOUCH_STATE_FLYING_SCROLLING = 4;
	/**
	 * just move
	 */
	protected static final int TOUCH_STATE_MOVING = 5;
	/**
	 * min velocity to detect snap
	 */
	private static final int SNAP_VELOCITY = 600;
	/**
	 * fast fly snap x velocity
	 */
	private static final int FAST_FLY_SNAP_X_VELOCITY = 2000;
	/**
	 * fast fly snap x distance
	 */
	private static final int FAST_FLY_SNAP_X_DISTANCE = 300;
	/**
	 * fast snap velocity
	 */
	private static final int FAST_SNAP_VELOCITY = 1000;
	/**
	 * screens be snapped fast
	 */
	private static final int FAST_SNAP_SCREENS = 5;
	/**
	 * size scale when page is flying
	 */
	private static final float FLY_PAGE_SIZE_SCALE = 0.5f;
	/**
	 * current touch state
	 */
	protected int mTouchState = TOUCH_STATE_REST;

	private static final float OVERSCROLL_DAMP_FACTOR = 0.14f;

	/**
	 * fly pages restore after 500ms without touching
	 */
	private static final int TIMEOUT_DELAY = 500;

	final static float START_DAMPING_TOUCH_SLOP_ANGLE = (float) Math.PI / 6;
	final static float MAX_SWIPE_ANGLE = (float) Math.PI / 3;
	final static float TOUCH_SLOP_DAMPING_FACTOR = 4;

	protected int mTouchSlop;
	private int mPagingTouchSlop;

	protected float mLastMotionX;
	protected float mLastMotionY;
	protected float mDownMotionX;
	protected float mDownMotionY;
        private boolean mIsLongPress=false;

	// mOverScrollX is equal to getScrollX() when we're within the normal scroll
	// range. Otherwise
	// it is equal to the scaled overscroll position. We use a separate value so
	// as to prevent
	// the screens from continuing to translate beyond the normal bounds.
	protected int mOverScrollX;

	protected int mMaxScrollX;
	protected int mMinScrollX;

	protected float mLayoutScale = 1.0f;
	/**
	 * It true, use a different slop parameter (pagingTouchSlop = 2 * touchSlop)
	 * for deciding to switch to a new page
	 */
	protected boolean mUsePagingTouchSlop = true;
	protected int[] mTempVisiblePagesRange = new int[2];
//	protected ArrayList<View> mPagedViewList = new ArrayList<View>();
	private GestureDetector mGestureDetector = null;
	protected OnItemClickListener mOnItemClickListener = null;
	protected OnTouchListener mOnTouchListener = null;
	protected OnItemLongPressListener mOnItemLongPressListener = null;
	protected OnItemDoubleClickListener mOnItemDoubleClickListener = null;
	protected OnDownSlidingBackListener mOnDownSlidingBackListener = null;
	protected OnPageFlyingListener mOnPageFlyingListener = null;
	protected OnPageSelectedListener mOnPageSelectedListener = null;

	// Scrolling indicator
	private ValueAnimator mScrollIndicatorAnimator;
	private View mScrollIndicator;
	private boolean mShouldShowScrollIndicator = false;
	private boolean mShouldShowScrollIndicatorImmediately = false;
	protected static final int sScrollIndicatorFadeInDuration = 150;
	protected static final int sScrollIndicatorFadeOutDuration = 650;
	protected static final int sScrollIndicatorFlashDuration = 650;
	private boolean mScrollingPaused = false;
	protected static final int PAGE_SNAP_ANIMATION_DURATION = 550;
	private final Object mLock = new Object();
	private boolean mIsDataReady = false;
	private Animation mInAnim = null;
	private Animation mOutAnim = null;

	private int mNextScreen = INVALID_SCREEN;
	// 保存子View的顺序信息，并处理子view移动位置的工作
	private ScreenQueue mScreenQueue = new ScreenQueue();
	private boolean mCanCycleFlip = false;
	private boolean mCycleFlipByUser = false;
	private boolean mCanFlyFlip = false;
	private boolean mFlyFlipByUser = false;
	private int mPageMargin = 1;
	private int mPageCountInScreen = 1;
	private int mPageWidth = 0;
	private int mSpacePageCount = 0;
	private float mFlyPageSizeScale = FLY_PAGE_SIZE_SCALE;
	protected boolean mIsDownWhenFlaying = false;
        protected boolean mIsDownWhenHasNotifications = false;
	private boolean mCanHorizontalOverScroll = true;
	private boolean mCanVerticalOverScroll = true;
        protected IMessageCenterService mMsgCenterService;

	private boolean mUseSoundEffect = false;

	protected BaseAdapter mAdapter;
	private boolean mDataChanged = false;
	private Queue<View> mRemovedViewQueue = new LinkedList<View>();
	protected int[] mVisiblePagesRange = new int[2];
	protected int[] mVisiblePagesIDRange = new int[2];
	private int mShouldToScreen = -1;
	
	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			synchronized (AdapterPagedView.this) {
				mDataChanged = true;
			}
			invalidate();
			requestLayout();
		}

		@Override
		public void onInvalidated() {
			reset();
			invalidate();
			requestLayout();
		}

	};
	
	public AdapterPagedView(Context context) {
		this(context, null);
	}

	public AdapterPagedView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AdapterPagedView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializeView(context);
		if(DEVELOPER_MODE){
		    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()       
		       .detectAll()
		       .penaltyLog()
		       .penaltyDialog() ////打印logcat
		       .build());
		    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		   .detectAll()
		   .penaltyLog()
		   .build());
		}
	}

	private void initializeView(Context context) {
		mScroller = new Scroller(context);
		mCurScreen = mDefaultScreen;
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mPagingTouchSlop = ViewConfiguration.get(getContext())
				.getScaledPagingTouchSlop();
		mGestureDetector = new GestureDetector(context, new MySimpleGesture());
		mMsgCenterService = IMessageCenterService.Stub.asInterface(ServiceManager.getService(Context.STATUS_MSGCENTER_SERVICE));
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		if (changedView == this) {
			if (visibility == View.VISIBLE && mInAnim != null)
				startAnimation(mInAnim);
			else if (visibility != View.VISIBLE && mOutAnim != null)
				startAnimation(mOutAnim);
		}
		super.onVisibilityChanged(changedView, visibility);
	}

	protected void screenScrolled(int screenCenter) {
		updateScrollingIndicator();
	}

	protected boolean shouldDrawChild(View child) {
		return child.getAlpha() > 0;
	}

	protected void getVisiblePages(int[] range) {
		final int pageCount = mScreenQueue.getChildCount();

		if (pageCount > 0) {
			final int screenWidth = getMeasuredWidth();
			int leftScreen = 0;
			int rightScreen = 0;
			ScreenInfo curSi = mScreenQueue.getScreenAt(leftScreen);
			int pageWidth = curSi.width + mPageMargin;
			leftScreen = Math.max(0, Math.min((getScrollX() - curSi.left)
					/ pageWidth - 1, pageCount - 1));
			while (leftScreen < pageCount - 1
					&& curSi.getRight() < getScrollX()) {
				// Log.e("sn",leftScreen+"/"+curSi.childId+" "+curSi.childView.getX()+" curSi.left="+curSi.left+" curSi.getRight()="+curSi.getRight());
				leftScreen++;
				curSi = mScreenQueue.getScreenAt(leftScreen);
			}
			rightScreen = leftScreen;
			curSi = mScreenQueue.getScreenAt(rightScreen + 1);
			while (rightScreen < pageCount - 1
					&& curSi.left < getScrollX() + screenWidth) {
				// Log.e("sn",rightScreen+"/"+curSi.childId+" "+curSi.childView.getX()+" curSi.left="+curSi.left+" curSi.getRight()="+curSi.getRight());
				rightScreen++;
				curSi = mScreenQueue.getScreenAt(rightScreen + 1);
			}
			range[0] = leftScreen;
			range[1] = rightScreen;
		} else {
			range[0] = -1;
			range[1] = -1;
		}
	}

	private void makeAndAddVisibleViews() {
		synchronized (AdapterPagedView.this) {
			int lastVisiblePagesID[] = { mVisiblePagesIDRange[0],
					mVisiblePagesIDRange[1] };

			getVisiblePages(mVisiblePagesRange);

			mVisiblePagesIDRange[0] = mVisiblePagesRange[0] == -1 ? -1
					: mScreenQueue.getScreenAt(mVisiblePagesRange[0]).childId;
			mVisiblePagesIDRange[1] = mVisiblePagesRange[1] == -1 ? -1
					: mScreenQueue.getScreenAt(mVisiblePagesRange[1]).childId;
//			if (mVisiblePagesRange[0] != -1 && mVisiblePagesRange[1] != -1)
//				Log.e("sn",
//						mDataChanged
//								+ " ooo "
//								+ mScreenQueue
//										.getScreenAt(mVisiblePagesRange[0]).childId
//								+ " "
//								+ mScreenQueue
//										.getScreenAt(mVisiblePagesRange[1]).childId
//								+ " : " + lastVisiblePagesID[0] + " "
//								+ lastVisiblePagesID[1]);
//			else
//				Log.e("sn", mDataChanged
//						+ " **oooooooooooooooooooooooooooooooooooooooooo "
//						+ mVisiblePagesRange[0] + " " + mVisiblePagesRange[1]
//						+ " : " + lastVisiblePagesID[0] + " "
//						+ lastVisiblePagesID[1]);

			if (lastVisiblePagesID[0] != mVisiblePagesIDRange[0]
					|| lastVisiblePagesID[1] != mVisiblePagesIDRange[1]) {
				// removeAllViewsInLayout();
				int left = mScreenQueue.getScreenAt(mVisiblePagesRange[0]).left;
				if (isFlying())
					left += (mPageWidth + mPageMargin) * mSpacePageCount;
				else
					left += (mPageWidth * mFlyPageSizeScale + mPageMargin)
							* mSpacePageCount;
				for (int i = 0; i < mScreenQueue.getChildCount(); i++) {
					View child = null;
					if (i >= mVisiblePagesRange[0]
							&& i <= mVisiblePagesRange[1]) {
						ScreenInfo childSi = mScreenQueue.getScreenAt(i);
						if (lastVisiblePagesID[0] > lastVisiblePagesID[1]) {
							if ((childSi.childId >= lastVisiblePagesID[0] && childSi.childId <= mAdapter
									.getCount())
									|| (childSi.childId >= 0 && childSi.childId <= lastVisiblePagesID[1]))
								continue;
						} else if (childSi.childId >= lastVisiblePagesID[0]
								&& childSi.childId <= lastVisiblePagesID[1])
							continue;
						View convertView = childSi.childView;
						if (convertView != null) {
							removeViewInLayout(convertView);
							mRemovedViewQueue.offer(convertView);
						}
						child = mAdapter.getView(childSi.childId,
								mRemovedViewQueue.poll(), this);
						LayoutParams params = child.getLayoutParams();
						// if(params == null) {
						params = new LayoutParams(LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT);
						// }
						addViewInLayout(child, -1, params, true);
						child.measure(MeasureSpec.makeMeasureSpec(mPageWidth,
								MeasureSpec.EXACTLY), MeasureSpec
								.makeMeasureSpec(getHeight(),
										MeasureSpec.EXACTLY));
						child.layout(left, 0, left + child.getMeasuredWidth(),
								child.getMeasuredHeight());

						if (isFlying()) {
							child.setScaleX(mFlyPageSizeScale);
							child.setScaleY(mFlyPageSizeScale);
							child.setX(childSi.left
									- (mPageWidth - childSi.width) / 2);
						} else {
							child.setScaleX(1.0f);
							child.setScaleY(1.0f);
							child.setX(childSi.left);
						}

						// Log.e(TAG,i+" x:"+child.getX()+" left:"+childSi.left+" w:"+child.getWidth()+" cX:"+getScrollX());
						childSi.childView = child;
						left += child.getMeasuredWidth()
								+ child.getPaddingRight() + mPageMargin;
					} else {
						child = mScreenQueue.getScreenAt(i).childView;
						if (child != null) {
							mScreenQueue.getScreenAt(i).childView = null;
							mRemovedViewQueue.offer(child);
							removeViewInLayout(child);
							// Log.e(TAG,"remove child "+i+" mRemovedViewQueue.size="+mRemovedViewQueue.size());
						}
					}
				}
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (!mIsDataReady || mAdapter == null || mAdapter.getCount() == 0) {
			return;
		}

		int halfScreenSize = getMeasuredWidth() / 2;
		// mOverScrollX is equal to getScrollX() when we're within the normal
		// scroll range.
		// Otherwise it is equal to the scaled overscroll position.
		int screenCenter = mOverScrollX + halfScreenSize;
		screenScrolled(screenCenter);

		int lastVisiblePagesID[] = { mVisiblePagesIDRange[0],
				mVisiblePagesIDRange[1] };
		
		makeAndAddVisibleViews();

		if (mUseSoundEffect
				&& mVisiblePagesIDRange[0] != -1 && lastVisiblePagesID[0] != -1
				&& lastVisiblePagesID[0] != mVisiblePagesIDRange[0])
			playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);

		final long drawingTime = getDrawingTime();
		for (int i = 0; i < getChildCount(); i++) {
			drawChild(canvas, getChildAt(i), drawingTime);
		}

		if (DEBUG)
			Log.i(TAG,
					"dispatchDraw mCurrentPage=" + mCurScreen + " mTouchState="
							+ mTouchState + " mNextPage=" + mNextScreen
							+ " chW=" + getChildAt(mCurScreen).getWidth()
							+ " chH=" + getChildAt(mCurScreen).getHeight()
							+ " chX=" + getChildAt(mCurScreen).getX() + " chV="
							+ getChildAt(mCurScreen).getVisibility() + " chA="
							+ getChildAt(mCurScreen).getAlpha());
	}

	private void resetScreenQueue(int pageWidth) {
		removeAllViewsInLayout();
		mVisiblePagesRange[0] = mVisiblePagesRange[1] = -1;
		mVisiblePagesIDRange[0] = mVisiblePagesIDRange[1] = -1;
		for (int i = 0; i < mScreenQueue.getChildCount(); i++) {
			if (mScreenQueue.getScreenAt(i).childView != null)
				mRemovedViewQueue.offer(mScreenQueue.getScreenAt(i).childView);
		}
		mScreenQueue.clear();
		final int height = getMeasuredHeight();
		int childLeft = (int) (getX() + ((pageWidth + mPageMargin) * mSpacePageCount));
		for (int i = 0; i < mAdapter.getCount(); ++i) {
			// 这里的screen们必须是按顺序添加的，否则就会出现混乱的view显示特征。
			// 而且，应该注意到，view的出现顺序i，被用作了相应screen的ID。
			mScreenQueue.addScreen(new ScreenInfo(i, childLeft, 0, pageWidth,
					height, null));
			childLeft += pageWidth + mPageMargin;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (!mIsDataReady || mAdapter == null || mAdapter.getCount() == 0) {
			return;
		}
		// removeAllViewsInLayout();
		if (mDataChanged) {
			mDataChanged = false;
			resetScreenQueue(mPageWidth);
			detectFlyAndCycle();
			if (mAdapter.getCount() == 1)
				hideScrollingIndicator(true);
			Log.e(TAG,
					"mCurScreen=" + mCurScreen + " count="
							+ mAdapter.getCount());
			if (mCurScreen >= mAdapter.getCount())
				mCurScreen = mAdapter.getCount() - 1;
			scrollTo((int) mScreenQueue.getChildById(mCurScreen).left
					- (mPageWidth + mPageMargin) * mSpacePageCount, 0);
			makeAndAddVisibleViews();
		}

		if (mCanCycleFlip) {
			for (int i = mCurScreen; i < mCurScreen + mPageCountInScreen; i++) {
				mScreenQueue.backPageFault(i % mAdapter.getCount());
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (!mIsDataReady || mAdapter == null || mAdapter.getCount() == 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException(
					"ScrollLayout only can run at EXACTLY mode!");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException(
					"ScrollLayout only can run at EXACTLY mode!");
		}

		/*
		 * final int count = getChildCount(); for (int i = 0; i < count; i++) {
		 * getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec); }
		 */

		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		// Return early if we aren't given a proper dimension
		if (widthSize <= 0 || heightSize <= 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		mPageWidth = (widthSize - mPageMargin * (mPageCountInScreen - 1))
				/ mPageCountInScreen;
		// Log.e("sn","uuu "+mPageWidth+" "+heightSize);
		/*
		 * Allow the height to be set as WRAP_CONTENT. This allows the
		 * particular case of the All apps view on XLarge displays to not take
		 * up more space then it needs. Width is still not allowed to be set as
		 * WRAP_CONTENT since many parts of the code expect each page to have
		 * the same width.
		 */
		int maxChildHeight = 0;

		final int verticalPadding = getPaddingTop() + getPaddingBottom();
		final int horizontalPadding = getPaddingLeft() + getPaddingRight();
		// The children are given the same width and height as the workspace
		// unless they were set to WRAP_CONTENT
		final int childCount = getChildCount();
		// Log.e("sn","childCount="+childCount);
		for (int i = 0; i < childCount; i++) {
			// disallowing padding in paged view (just pass 0)
			final View child = getChildAt(i);
			final LayoutParams lp = child.getLayoutParams();

			int childWidthMode;
			if (lp.width == LayoutParams.WRAP_CONTENT) {
				childWidthMode = MeasureSpec.AT_MOST;
			} else {
				childWidthMode = MeasureSpec.EXACTLY;
			}

			int childHeightMode;
			if (lp.height == LayoutParams.WRAP_CONTENT) {
				childHeightMode = MeasureSpec.AT_MOST;
			} else {
				childHeightMode = MeasureSpec.EXACTLY;
			}

			final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
					mPageWidth - horizontalPadding, childWidthMode);
			final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
					heightSize - verticalPadding, childHeightMode);

			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			maxChildHeight = Math
					.max(maxChildHeight, child.getMeasuredHeight());
		}

		if (heightMode == MeasureSpec.AT_MOST) {
			heightSize = maxChildHeight + verticalPadding;
		}

		setMeasuredDimension(widthSize, heightSize);

		updateScrollingIndicatorPosition();

	        if (mScreenQueue.getChildCount() > 0){
		        scrollTo((int) mScreenQueue.getChildById(mCurScreen).left
					- (mPageWidth + mPageMargin) * mSpacePageCount, 0);
		}
		else
		scrollTo(mCurScreen * (mPageWidth + mPageMargin), 0);

		if (childCount > 0) {
			// mMaxScrollX = (mPagedViewList.size() - 1) * (mPageWidth +
			// mPageMargin);
			mMaxScrollX = (mAdapter.getCount() - 1)
					* (mPageWidth + mPageMargin);
		} else {
			mMaxScrollX = 0;
		}
	}

	protected void snapFast(int whichScreen) {
		Log.e(TAG, "snapFast " + whichScreen);

		whichScreen = Math.max(0,
				Math.min(whichScreen, mAdapter.getCount() - 1));
		int curScreen = getCurScreen();
		// Log.e("sn","mCurScreen="+mCurScreen+" whichScreen="+whichScreen+" curScreen="+curScreen);
		if (whichScreen != curScreen) {
			mTouchState = TOUCH_STATE_FLYING;
			mNextScreen = whichScreen;
			// int deltaScreen = whichScreen - mCurScreen;
			int pageWidth = mPageWidth;
			int width = (int) (pageWidth * mFlyPageSizeScale);
			int height = (int) (getHeight() * mFlyPageSizeScale);
			resetScreenQueue(width);
			mMinScrollX = -(getMeasuredWidth() / 2 - mSpacePageCount
					* (width + mPageMargin) - width / 2);
			scrollTo(curScreen * (width + mPageMargin), 0);

			postInvalidate();
			final int delta = (int) (mScreenQueue.getScreenAt(mNextScreen).left - getScrollX());
			mScroller.startScroll(getScrollX(), 0, delta, 0,
					PAGE_SNAP_ANIMATION_DURATION);
			pageBeginMoving();
			invalidate();

			if (mOnPageFlyingListener != null)
				mOnPageFlyingListener.onPageFlying(AdapterPagedView.this, true);
		} else {
			if (mCanCycleFlip)
				snapToDestination(mCurScreen);
			else
				snapToDestinationNoCircle(mCurScreen, mPageWidth);
			mTouchState = TOUCH_STATE_REST;
			mMinScrollX = 0;
		}
	}

	/**
	 * Snap to destination in circle mode.
	 */
	protected void snapToDestination(int tmpScreen) {
		// Log.e("sn","snapToDestination tmpScreen="+tmpScreen+" mCurScreen="+mCurScreen);
		final int scrollX = getScrollX();
		ScreenInfo cur = mScreenQueue.getChildById(tmpScreen);
		int newX = cur.left; // newX就是curLeft
		mNextScreen = tmpScreen;
		// mNextScreen将可能在左侧，因为curLeft在对齐点scrollX的右侧
		if (newX >= scrollX) {
			if (newX - scrollX > (cur.width >> 4)) {
				if (--mNextScreen < 0)
					mNextScreen = mAdapter.getCount() - 1;
				// 但是子view的width都一样，左侧view的left就是当前view的left减去其宽度
				newX -= cur.width;
			}
		} else {
			if (scrollX - newX > (cur.width >> 4)) {
				if (++mNextScreen >= mAdapter.getCount())
					mNextScreen = 0;
				newX += cur.width;
			}
		}
		final int delta = newX - scrollX;
		mCurScreen = mNextScreen;
		mScroller.startScroll(scrollX, getScrollY(), delta, 0,
				PAGE_SNAP_ANIMATION_DURATION);
		invalidate();
		pageBeginMoving();
		if (DEBUG) {
			Log.i(TAG, "=====1 to page " + mNextScreen + " move " + delta);
			String s = "";
			for (int i = 0; i < getChildCount(); i++) {
				s += mScreenQueue.screens.get(i).childId + " ";
			}
			Log.i(TAG, s);
			Log.i(TAG, "=====2");
		}
	}

	/**
	 * Snap to screen according to x velocity in circle mode.
	 */
	protected void snapToScreen(int currentScreen, int velocityX) {
		int offsetScreen = velocityX / FAST_SNAP_VELOCITY;
		int maxScrollScreens = mAdapter.getCount() - mPageCountInScreen > 1
				&& mPageCountInScreen > 1 ? mAdapter.getCount()
				- mPageCountInScreen : (currentScreen == mCurScreen ? 1 : 0);
		offsetScreen = Math.min(Math.abs(offsetScreen), maxScrollScreens)
				* (velocityX / Math.abs(velocityX));
		mNextScreen = currentScreen - offsetScreen;
		// Log.e("sn","snapToScreen offsetScreen="+offsetScreen+" maxScrollScreens="+maxScrollScreens+" currentScreen="+currentScreen+" mCurScreen="+mCurScreen);
		// mNextScreen = (currentScreen + (velocityX < 0 ? 1
		// : (getChildCount() - 1))) % getChildCount();
		// 如果向左滑动，则右侧可能会产生缺页（左为负，右为正）
		if (mNextScreen > currentScreen) {
			for (int i = currentScreen; i <= mNextScreen; i++)
				mScreenQueue.backPageFault(i % mAdapter.getCount());
			int rightScreen = mNextScreen + mPageCountInScreen - 1;
			// Log.e("sn","rightScreen="+rightScreen);
			for (int i = mNextScreen; i < rightScreen; i++)
				mScreenQueue.backPageFault(i % mAdapter.getCount());
		}
		// 同理，如果向右滑动，则左侧可能会产生缺页
		else {
			// Log.e("sn","---whichScreen="+mNextScreen+" mCurScreen="+currentScreen);
			for (int i = currentScreen; i > mNextScreen; i--)
				mScreenQueue.frontPageFault((i + mAdapter.getCount())
						% mAdapter.getCount());
		}
		mNextScreen = (mNextScreen + (velocityX < 0 ? 0 : mAdapter.getCount()))
				% mAdapter.getCount();
		mCurScreen = mNextScreen;
		// Log.e("sn","---mNextScreen="+mNextScreen+" mCurScreen="+currentScreen);
		// 到此处，mNextScreen一定在子view范围之内，不会越界。
		// deQueue.getChildById(mNextScreen).left为目标位置
		final int delta = mScreenQueue.getChildById(mNextScreen).left
				- getScrollX();
		// 每2 millisecond移动一个像素
		mScroller.startScroll(getScrollX(), 0, delta, 0,
				PAGE_SNAP_ANIMATION_DURATION);
		pageBeginMoving();
		invalidate();
		if (DEBUG) {
			Log.i(TAG, "=====1 to page " + mNextScreen + " move " + delta);
			String s = "";
			for (int i = 0; i < getChildCount(); i++) {
				s += mScreenQueue.screens.get(i).childId + " ";
			}
			Log.i(TAG, s);
			Log.i(TAG, "=====2");
		}
	}

	/**
	 * Snap to destination in nocircle mode.
	 */
	protected void snapToDestinationNoCircle(int tmpScreen, int pageWidth) {
		tmpScreen = Math.max(0, Math.min(tmpScreen, mAdapter.getCount() - 1));
		final int scrollX = getScrollX();
		int newX = (int) mScreenQueue.getScreenAt(tmpScreen).left
				- mSpacePageCount * (pageWidth + mPageMargin);
		int destScreen = tmpScreen;
		Log.e(TAG, "newX=" + newX + " scrollX=" + scrollX);
		if (newX >= scrollX) {
			if (newX - scrollX > (pageWidth >> 4))
				destScreen--;
		} else {
			if (scrollX - newX > (pageWidth >> 4))
				destScreen++;
		}
		// Log.e("sn","flying destScreen="+destScreen+" tmpScreen="+tmpScreen+" pageWidth="+pageWidth);
		// final int destScreen = (getScrollX() + screenWidth / 2) /
		// screenWidth;
		snapToScreenNoCircle(destScreen, pageWidth);
		pageBeginMoving();
	}

	/**
	 * Snap to screen according to x velocity in nocircle mode.
	 */
	protected void snapToScreenNoCircle(int whichScreen, int pageWidth) {
		whichScreen = Math.max(0,
				Math.min(whichScreen, mAdapter.getCount() - 1));
		pageWidth += mPageMargin;
		Log.e("sn", "snapToScreenNoCircle whichScreen=" + whichScreen
				+ " mCurScreen=" + mCurScreen);
		if (getScrollX() != (whichScreen * pageWidth)) {
//			 Log.e("sn", "snapToScreen !!!!!!!!!!!!!!!!!!!!!!!!");
			final int delta = whichScreen * pageWidth - getScrollX();
//			 Log.e("sn","flying delta="+delta+" whichScreen="+whichScreen+" getScrollX()="+getScrollX()+" pageWidth="+pageWidth+" left="+mScreenQueue.getChildById(whichScreen).left);
			mScroller.startScroll(getScrollX(), 0, delta, 0,
					PAGE_SNAP_ANIMATION_DURATION);
			mNextScreen = whichScreen;
			pageBeginMoving();
			invalidate();
		} else {
			if (isFlying()) {
			        mHandler.removeMessages(0);
				mHandler.sendEmptyMessageDelayed(0, TIMEOUT_DELAY);
				return;
			} else
				mCurScreen = whichScreen;
			mTouchState = TOUCH_STATE_REST;
			mNextScreen = INVALID_SCREEN;
			mMinScrollX = 0;
			pageEndMoving();
			Log.e("sn", "call onPageSelected --5");
			notifyPageSelected();
		}
	}

	/**
	 * Set screen to destination without animation.
	 */
	protected void setToScreen(int whichScreen) {
		// Log.e("sn","setToScreen "+whichScreen);
		if (mAdapter == null)
			return;
		whichScreen = Math.max(0,
				Math.min(whichScreen, mAdapter.getCount() - 1));
		int pageWidth = mPageWidth + mPageMargin;// getWidth();
		scrollTo(whichScreen * pageWidth, 0);
		updateScrollingIndicator();
		if (mCurScreen != whichScreen) {
			mCurScreen = whichScreen;
			Log.e("sn", "call onPageSelected --4");
			notifyPageSelected();
		}
	}

	// This curve determines how the effect of scrolling over the limits of the
	// page dimishes
	// as the user pulls further and further from the bounds
	private float overScrollInfluenceCurve(float f) {
		f -= 1.0f;
		return f * f * f + 1.0f;
	}

	protected void dampedOverScroll(float amount) {
		int screenSize = getMeasuredWidth();

		float f = (amount / screenSize);

		if (f == 0)
			return;
		f = f / (Math.abs(f)) * (overScrollInfluenceCurve(Math.abs(f)));

		// Clamp this factor, f, to -1 < f < 1
		if (Math.abs(f) >= 1) {
			f /= Math.abs(f);
		}

		int overScrollAmount = Math.round(OVERSCROLL_DAMP_FACTOR * f
				* screenSize);
		if (amount < mMinScrollX) {
			mOverScrollX = overScrollAmount;
			super.scrollTo(mMinScrollX, getScrollY());
		} else {
			mOverScrollX = mMaxScrollX + overScrollAmount;
			super.scrollTo(mMaxScrollX, getScrollY());
		}
		invalidate();
	}

	@Override
	public void scrollTo(int x, int y) {
		if (mCanCycleFlip || mCanHorizontalOverScroll)
			super.scrollTo(x, y);
		else {
			if (x < mMinScrollX) {
				super.scrollTo(mMinScrollX, y);
				dampedOverScroll(x);
			} else if (x > mMaxScrollX) {
				super.scrollTo(mMaxScrollX, y);
				dampedOverScroll(x - mMaxScrollX);
			} else {
				mOverScrollX = x;
				super.scrollTo(x, y);
			}
		}
	}

	/**
	 * Get the page nearest to center of screen.
	 */
	protected int getCenterPage() {
		final int pageCount = mScreenQueue.getChildCount();

		if (pageCount > 0) {
			final int screenWidth = getMeasuredWidth();
			int centerScreen = 0;
			int centerX = getScrollX() + screenWidth / 2;
			ScreenInfo curSi = mScreenQueue.getScreenAt(centerScreen);
			while (centerScreen < pageCount - 1 && curSi.getRight() < centerX) {
				centerScreen++;
				curSi = mScreenQueue.getScreenAt(centerScreen);
			}
			return centerScreen;
		} else {
			return -1;
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		} else if (mNextScreen != INVALID_SCREEN) {
			if (mTouchState == TOUCH_STATE_FLYING) {
				// mTouchState = TOUCH_STATE_REST;
				// mNextScreen = restorePages(mNextScreen);
				//mHandler.removeMessages(0);
				mHandler.sendEmptyMessageDelayed(0, TIMEOUT_DELAY);
				mCurScreen = mNextScreen;
				return;
			}
			mCurScreen = mNextScreen;
			mNextScreen = INVALID_SCREEN;

			if (mTouchState == TOUCH_STATE_REST) {
				pageEndMoving();
				Log.e("sn", "call onPageSelected --1");
				notifyPageSelected();
			}
		}
	}

	private void notifyPageSelected() {
		if (mScreenQueue.getChildById(getCurScreen()).childView == null)
			makeAndAddVisibleViews();
		if (mOnPageSelectedListener != null)
			mOnPageSelectedListener.onPageSelected(AdapterPagedView.this,
					mScreenQueue.getChildById(getCurScreen()).childView,
					getCurScreen());
	}
	
	/**
	 * Restore page to normal mode from flying mode.
	 */
	private int restorePages(int nextPage) {
		int scalePage = mScreenQueue.getScreenAt(getCenterPage()).childId;// getCenterPage();
		Log.e(TAG, "scalePage=" + scalePage);
		if (mCanCycleFlip) {
			mNextScreen = (scalePage - (mPageCountInScreen / 2) + mAdapter
					.getCount()) % mAdapter.getCount();
			for (int i = 0; i <= mPageCountInScreen; i++) {
				mScreenQueue.backPageFault((mNextScreen + i)
						% mAdapter.getCount());
			}
		} else
			mNextScreen = Math.max(0,
					Math.min(scalePage, mAdapter.getCount() - 1));
		mNextScreen = Math.max(0, Math.min(scalePage, mAdapter.getCount() - 1));
		// Log.e("sn","** mNextScreen="+mNextScreen+" getCenterPage()="+scalePage);

		int pageWidth = mPageWidth;// getWidth();
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).setScaleX(1.0f);
			getChildAt(i).setScaleY(1.0f);
			// getChildAt(i).measure(newWidthSpec, newHeightSpec);
		}

		resetScreenQueue(mPageWidth);

		Log.e(TAG, "--------------------mNextScreen = " + mNextScreen);
		mVisiblePagesRange[0] = mVisiblePagesRange[1] = -1;
		mVisiblePagesIDRange[0] = mVisiblePagesIDRange[1] = -1;
		scrollTo(mNextScreen * (mPageWidth + mPageMargin), 0);

		// ScaleAnimation sa = new ScaleAnimation(mFlyPageSizeScale, 1.0f,
		// mFlyPageSizeScale, 1.0f,
		// Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		// sa.setDuration(300);
		// mScreenQueue.getChildById(scalePage).childView.setAnimation(sa);

		mMinScrollX = 0;

		requestLayout();
		postInvalidate();
		if (mOnPageFlyingListener != null)
			mOnPageFlyingListener.onPageFlying(AdapterPagedView.this, false);
		return mNextScreen;
	}

	private synchronized void reset() {
		mVisiblePagesRange[0] = mVisiblePagesRange[1] = -1;
		mVisiblePagesIDRange[0] = mVisiblePagesIDRange[1] = -1;
		removeAllViewsInLayout();
		requestLayout();
		mScreenQueue.clear();
	}

	protected void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);
	}

	protected void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mCurScreen = restorePages(mCurScreen);
			mTouchState = TOUCH_STATE_REST;
			mNextScreen = INVALID_SCREEN;
			mMinScrollX = 0;
			pageEndMoving();
			Log.e("sn", "call onPageSelected --2");
			notifyPageSelected();
			mHandler.removeMessages(0);
		}

	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if(mIsLongPress && mOnTouchListener != null){		
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN)
		    mIsLongPress = false;
		else {
		    mOnTouchListener.onTouch(AdapterPagedView.this,
					 mScreenQueue.getChildById(getCurScreen()).childView,getCurScreen(),event);
		    return false;
		}
	    }
		// Skip touch handling if there are no pages to swipe
		if (!mIsDataReady || getChildCount() <= 0)
			return super.onTouchEvent(event);
		// Skip touch handling if there are no pages to swipe
		acquireVelocityTrackerAndAddMovement(event);
		mHandler.removeMessages(0);

		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			mDownMotionX = x;
			mDownMotionY = y;
			if (isFlying())
				mIsDownWhenFlaying = true;
			else
				mIsDownWhenFlaying = false;
			try {
			    if (mMsgCenterService != null && mMsgCenterService.hasNotifications())
				mIsDownWhenHasNotifications = true;
			    else
				mIsDownWhenHasNotifications = false;
			} catch (RemoteException ex) {
			}
			final int xDist = Math.abs(mScroller.getFinalX()
					- mScroller.getCurrX());
			final boolean finishedScrolling = (mScroller.isFinished() || xDist < mTouchSlop);
			mScroller.abortAnimation();
			if (mTouchState == TOUCH_STATE_FLYING) {
				mScroller.abortAnimation();
				mTouchState = TOUCH_STATE_FLYING_REST;
			} else if (isFlying()) {

			} else if (finishedScrolling) {
				mTouchState = TOUCH_STATE_REST;
			} else {
				pageBeginMoving();
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			break;

		case MotionEvent.ACTION_MOVE:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;
				// Log.e("sn","cur = "+mCurScreen+" next = "+mNextScreen);
				// Log.e("sn", "deltaX=" + deltaX + " getScrollX=" +
				// getScrollX());
				if (mCanCycleFlip) {
					int dX = (int) (mDownMotionX - x);
					// deltaX > 0 标识scroll指针要向右移动，表明此时手指在向左滑。
					if (mDownMotionX > x) {
						int rightScreen = (mCurScreen + mPageCountInScreen - 1)
								% mAdapter.getCount();
						int tmpScreen = rightScreen
								+ (Math.abs(dX) / mPageWidth);
						for (int i = rightScreen; i <= tmpScreen; i++) {
							mScreenQueue.backPageFault(i % mAdapter.getCount());
						}
						// 表明在持续向左滑动（或则向右又向左滑了），要判断右侧是否会有缺页
					} else {
						int tmpScreen = mCurScreen
								- (Math.abs(dX) / mPageWidth);
						for (int i = mCurScreen; i >= tmpScreen; i--) {
							mScreenQueue.frontPageFault((i + mAdapter
									.getCount()) % mAdapter.getCount());
						}
						// 表明持续向右滑动（或向左后，又反复向右了）。
					}
				}
				if (Math.abs(deltaX) >= 1.0f) {
					// scrollBy(deltaX, 0);
				} else {
					awakenScrollBars();
				}
				scrollBy(deltaX, 0);
			} else if (mTouchState == TOUCH_STATE_FLYING_SCROLLING) {
				int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;
				if (Math.abs(deltaX) >= 1.0f) {
					// scrollBy(deltaX, 0);
				} else {
					awakenScrollBars();
				}
				scrollBy(deltaX, 0);
			} else {
				determineScrollingStart(event);
				if (mCanVerticalOverScroll && !mIsDownWhenHasNotifications) {
					if (mTouchState == TOUCH_STATE_MOVING) {
						float yDiff = y - mDownMotionY;
						if (yDiff < -mTouchSlop) {
							scrollBy(0, (int) (mLastMotionY - y));
							mLastMotionY = y;
						}
					} else if (getScrollY() != 0)
						scrollTo((int) getScrollX(), 0);
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			Log.e("sn", "UP mTouchState=" + mTouchState);
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				int velocityX = (int) velocityTracker.getXVelocity();
				int velocityY = (int) velocityTracker.getYVelocity();
				final int deltaX = (int) (mDownMotionX - x);
				if (mCanCycleFlip) {
					if (velocityX > SNAP_VELOCITY /* && mCurScreen >= 0 */) {
						// 鍚戝乏绉诲姩
						if (mCanFlyFlip && velocityX > FAST_FLY_SNAP_X_VELOCITY
						// && mCurScreen >= mPageCountInScreen
								&& deltaX < -FAST_FLY_SNAP_X_DISTANCE) {
							// getCurScreen() is not same with mCurScreen always
							snapFast(getCurScreen() - FAST_SNAP_SCREENS);
							releaseVelocityTracker();
							break;
						} else {
							// fling
							int tmpScreen = (mCurScreen
									- (Math.abs(deltaX) / mPageWidth) + mAdapter
										.getCount()) % mAdapter.getCount();
							snapToScreen(tmpScreen, velocityX);
						}
					} else if (velocityX < -SNAP_VELOCITY
					/* && mCurScreen < getChildCount() - 1 */) {
						if (mCanFlyFlip
								&& velocityX < -FAST_FLY_SNAP_X_VELOCITY
								// && mCurScreen <= (getChildCount() -
								// mPageCountInScreen - 1)
								&& deltaX > FAST_FLY_SNAP_X_DISTANCE) {
							snapFast(getCurScreen() + FAST_SNAP_SCREENS);
							releaseVelocityTracker();
							break;
						} else {
							// fling
							int tmpScreen = (mCurScreen + (Math.abs(deltaX) / mPageWidth))
									% mAdapter.getCount();
							snapToScreen(tmpScreen, velocityX);
						}
					} else {
						int tmpScreen = mCurScreen;
						if (deltaX > 0)
							tmpScreen = (mCurScreen + (Math.abs(deltaX) / mPageWidth))
									% mAdapter.getCount();// getChildCount();
						else
							tmpScreen = (mCurScreen
									- (Math.abs(deltaX) / mPageWidth) + mAdapter
										.getCount()) % mAdapter.getCount();
						snapToDestination(tmpScreen);
					}
				} else {
					if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
						if (mCanFlyFlip && velocityX > FAST_FLY_SNAP_X_VELOCITY
						// && mCurScreen >= mPageCountInScreen
								&& deltaX < -FAST_FLY_SNAP_X_DISTANCE) {
							snapFast(getCurScreen() - FAST_SNAP_SCREENS);
							releaseVelocityTracker();
							break;
						} else {
							int tmpScreen = mCurScreen
									- (Math.abs(deltaX) / mPageWidth);
							if (mPageCountInScreen > 1
									&& velocityX > FAST_SNAP_VELOCITY)
								tmpScreen -= velocityX / FAST_SNAP_VELOCITY;
							else
								tmpScreen--;
							snapToScreenNoCircle(tmpScreen, mPageWidth);
						}
					} else if (velocityX < -SNAP_VELOCITY
							&& mCurScreen < mAdapter.getCount() - 1) {
						Log.e(TAG, "mCanFlyFlip=" + mCanFlyFlip + " velocityX="
								+ velocityX + " mCurScreen=" + mCurScreen);
						if (mCanFlyFlip
								&& velocityX < -FAST_FLY_SNAP_X_VELOCITY
								// && mCurScreen <= (getChildCount() -
								// mPageCountInScreen - 1)
								&& deltaX > FAST_FLY_SNAP_X_DISTANCE) {
							snapFast(getCurScreen() + FAST_SNAP_SCREENS);
							releaseVelocityTracker();
							break;
						} else {
							int tmpScreen = mCurScreen
									+ (Math.abs(deltaX) / mPageWidth);
							if (mPageCountInScreen > 1
									&& velocityX < -FAST_SNAP_VELOCITY)
								tmpScreen += (-velocityX) / FAST_SNAP_VELOCITY;
							else
								tmpScreen++;
							snapToScreenNoCircle(tmpScreen, mPageWidth);
						}
					} else {
						int tmpScreen = mCurScreen;
						if (deltaX > 0)
							tmpScreen = mCurScreen
									+ (Math.abs(deltaX) / mPageWidth);
						else
							tmpScreen = mCurScreen
									- (Math.abs(deltaX) / mPageWidth);
						Log.e("sn", "............................. tmpScreen="
								+ tmpScreen);
						snapToDestinationNoCircle(tmpScreen, mPageWidth);
					}
				}
			} else if (mTouchState == TOUCH_STATE_FLYING_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				int velocityX = (int) velocityTracker.getXVelocity();
				getVisiblePages(mTempVisiblePagesRange);
				int tmpScreen = mCurScreen = mTempVisiblePagesRange[0];
				int width = (int) (mPageWidth * mFlyPageSizeScale);
				if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
					Log.e(TAG,"right slide velocityX="+velocityX+" tmpScreen="+tmpScreen);
					if (velocityX > FAST_SNAP_VELOCITY)
						tmpScreen -= velocityX / FAST_SNAP_VELOCITY;
					snapToScreenNoCircle(tmpScreen, width);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurScreen < mAdapter.getCount() - 1) {
					Log.e(TAG,"left slide velocityX="+velocityX+" tmpScreen="+tmpScreen);
					if (velocityX < -FAST_SNAP_VELOCITY)
						tmpScreen += (-velocityX) / FAST_SNAP_VELOCITY;
					snapToScreenNoCircle(tmpScreen, width);
				} else {
					Log.e(TAG, "tmpScreen=" + tmpScreen
							+ " mTempVisiblePagesRange[0]="
							+ mTempVisiblePagesRange[0]);
					if (tmpScreen == mAdapter.getCount() - 1)
						snapToScreenNoCircle(tmpScreen, width);
					else {
						tmpScreen = mScreenQueue
								.getScreenAt(getCenterPage()).childId;// mTempVisiblePagesRange[0];
						if (tmpScreen == 0) {
							mScroller.startScroll(getScrollX(), 0, mMinScrollX
									- getScrollX(), 0);
							invalidate();
						}
						mHandler.sendEmptyMessageDelayed(0, TIMEOUT_DELAY);
						releaseVelocityTracker();
						break;
					}
				}
			} else if (mTouchState == TOUCH_STATE_FLYING_REST) {
				getVisiblePages(mTempVisiblePagesRange);
				int tmpScreen = mTempVisiblePagesRange[0];
				if (tmpScreen == mAdapter.getCount() - 1) {
					mTouchState = TOUCH_STATE_FLYING;
					snapToScreenNoCircle(tmpScreen,
							(int) (mPageWidth * mFlyPageSizeScale));
				} else {
					if (tmpScreen == 0) {
						mScroller.startScroll(getScrollX(), 0, mMinScrollX
								- getScrollX(), 0);
						invalidate();
					}
					mHandler.sendEmptyMessageDelayed(0, TIMEOUT_DELAY);
				}
				releaseVelocityTracker();
				break;
			} else if (mCanVerticalOverScroll
					&& mTouchState == TOUCH_STATE_MOVING && getScrollY() != 0) {
				mScroller.startScroll(getScrollX(), getScrollY(), 0,
						-getScrollY(), PAGE_SNAP_ANIMATION_DURATION);
				invalidate();
			}
			releaseVelocityTracker();
			if (mTouchState == TOUCH_STATE_FLYING_SCROLLING)
				mTouchState = TOUCH_STATE_FLYING;
			else
				mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			Log.e(TAG,"CANCLE....");
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				int deltaX = (int) (mDownMotionX - x);
				int tmpScreen = mCurScreen;
				if (mCanCycleFlip) {
					if (deltaX > 0)
						tmpScreen = (mCurScreen + (Math.abs(deltaX) / mPageWidth))
								% mAdapter.getCount();
					else
						tmpScreen = (mCurScreen
								- (Math.abs(deltaX) / mPageWidth) + mAdapter
									.getCount()) % mAdapter.getCount();
					snapToDestination(tmpScreen);
				} else {
					if (deltaX > 0)
						tmpScreen = mCurScreen
								+ (Math.abs(deltaX) / mPageWidth);
					else
						tmpScreen = mCurScreen
								- (Math.abs(deltaX) / mPageWidth);
					snapToDestinationNoCircle(tmpScreen, mPageWidth);
				}
			} else if (mTouchState == TOUCH_STATE_FLYING_SCROLLING) {
				getVisiblePages(mTempVisiblePagesRange);
				int tmpScreen = mTempVisiblePagesRange[0];
				if (tmpScreen == mAdapter.getCount() - 1)
					snapToScreenNoCircle(tmpScreen,
							(int) (mPageWidth * mFlyPageSizeScale));
				else {
					if (tmpScreen == 0) {
						mScroller.startScroll(getScrollX(), 0, mMinScrollX
								- getScrollX(), 0);
						invalidate();
					}
					mHandler.sendEmptyMessageDelayed(0, TIMEOUT_DELAY);
					releaseVelocityTracker();
					break;
				}
			} else if (mTouchState == TOUCH_STATE_FLYING_REST) {
				getVisiblePages(mTempVisiblePagesRange);
				int tmpScreen = mTempVisiblePagesRange[0];
				if (tmpScreen == mAdapter.getCount() - 1) {
					mTouchState = TOUCH_STATE_FLYING;
					snapToScreenNoCircle(tmpScreen,
							(int) (mPageWidth * mFlyPageSizeScale));
				} else {
					if (tmpScreen == 0) {
						mScroller.startScroll(getScrollX(), 0, mMinScrollX
								- getScrollX(), 0);
						invalidate();
					}
					mHandler.sendEmptyMessageDelayed(0, TIMEOUT_DELAY);
				}
				releaseVelocityTracker();
				break;
			} else if (mCanVerticalOverScroll
					&& mTouchState == TOUCH_STATE_MOVING && getScrollY() != 0) {
				mScroller.startScroll(getScrollX(), getScrollY(), 0,
						-getScrollY(), PAGE_SNAP_ANIMATION_DURATION);
				invalidate();
			}
			releaseVelocityTracker();
			if (mTouchState == TOUCH_STATE_FLYING_SCROLLING)
				mTouchState = TOUCH_STATE_FLYING;
			else
				mTouchState = TOUCH_STATE_REST;
			break;
		}		
		mGestureDetector.onTouchEvent(event);
		return true;

	}

	private class MySimpleGesture extends SimpleOnGestureListener {
		@Override
		public boolean onDoubleTap(boolean fromPhone) {
		        if (fromPhone)
			        mIsLongPress = false;
			if (!mIsDownWhenFlaying && mTouchState == TOUCH_STATE_REST
					&& mOnItemDoubleClickListener != null)
				mOnItemDoubleClickListener.onItemDoubleClick(AdapterPagedView.this,
						mScreenQueue.getChildById(getCurScreen()).childView,
						getCurScreen());
			return true;
		}

		@Override
		public boolean onSlideDown(boolean fromPhone) {
		        if (fromPhone)
			        mIsLongPress = false;
			  // 鍚戜笅绉诲姩
			if (!mIsDownWhenFlaying && mTouchState == TOUCH_STATE_REST
			    && mOnDownSlidingBackListener != null) {
			        Log.e("sn", "onSlideDown");
				mOnDownSlidingBackListener
				    .onDownSlidingBack(AdapterPagedView.this);
				if (mUseSoundEffect)
				        playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
			}
			return true;
		}

		@Override
		public boolean onLongPress(boolean fromPhone) {
		        if (fromPhone)
			        mIsLongPress = false;
			if (!mIsDownWhenFlaying && mTouchState == TOUCH_STATE_REST
			    && mOnItemLongPressListener != null){
			    Log.e("sn", "onLongPress " + getCurScreen());
			    mOnItemLongPressListener.onItemLongPress(AdapterPagedView.this,
								     mScreenQueue.getChildById(getCurScreen()).childView,getCurScreen());
			    mIsLongPress = true;								
			}
			return true;
		}

		@Override
		public boolean onTap(boolean fromPhone){
		        if (fromPhone)
			        mIsLongPress = false;
			if (!mIsDownWhenFlaying && mTouchState == TOUCH_STATE_REST
					&& mOnItemClickListener != null) {
				Log.e("sn", "onTap " + getCurScreen());
				if (mUseSoundEffect)
					playSoundEffect(SoundEffectConstants.CLICK);
				if (DEBUG)
					dumpScreenQueue();
				mOnItemClickListener.onItemClick(AdapterPagedView.this,
						mScreenQueue.getChildById(getCurScreen()).childView,
						getCurScreen());
			}
			return true;
		}

		@Override
	        public boolean onSlideLeft(boolean fromPhone){
		    if (fromPhone) {
			mIsLongPress = false;
			scrollRight();
		    }
		    return true;
		}

		@Override
	        public boolean onSlideRight(boolean fromPhone){
		    if (fromPhone) {
			mIsLongPress = false;
			scrollLeft();
		    }
		    return true;
		}
	}

	protected void dumpScreenQueue() {
		String dump = "";
		for (int i = 0; i < mScreenQueue.getChildCount(); i++) {
			dump += mScreenQueue.getScreenAt(i).childId + " ";
		}
		if (!dump.equals(""))
			Log.e(TAG, dump);
	}
	
	protected boolean isFlying() {
		return mTouchState == TOUCH_STATE_FLYING
				|| mTouchState == TOUCH_STATE_FLYING_REST
				|| mTouchState == TOUCH_STATE_FLYING_SCROLLING;
	}

	protected void determineScrollingStart(MotionEvent e) {
		float deltaX = Math.abs(e.getX() - mDownMotionX);
		float deltaY = Math.abs(e.getY() - mDownMotionY);

		if (Float.compare(deltaX, 0f) == 0)
			return;

		float slope = deltaY / deltaX;
		float theta = (float) Math.atan(slope);
		if (!isFlying() && theta > MAX_SWIPE_ANGLE) {
			if (deltaX > mTouchSlop || deltaY > mTouchSlop)
				mTouchState = TOUCH_STATE_MOVING;
			// Above MAX_SWIPE_ANGLE, we don't want to ever start scrolling the
			// workspace
			return;
		} else if (theta > START_DAMPING_TOUCH_SLOP_ANGLE) {
			// Above START_DAMPING_TOUCH_SLOP_ANGLE and below MAX_SWIPE_ANGLE,
			// we want to
			// increase the touch slop to make it harder to begin scrolling the
			// workspace. This
			// results in vertically scrolling widgets to more easily. The
			// higher the angle, the
			// more we increase touch slop.
			theta -= START_DAMPING_TOUCH_SLOP_ANGLE;
			float extraRatio = (float) Math
					.sqrt((theta / (MAX_SWIPE_ANGLE - START_DAMPING_TOUCH_SLOP_ANGLE)));
			determineScrollingStart(e, 1 + TOUCH_SLOP_DAMPING_FACTOR
					* extraRatio);
		} else {
			// Below START_DAMPING_TOUCH_SLOP_ANGLE, we don't do anything
			// special
			determineScrollingStart(e, 1.0f);
		}
	}

	/**
	 * Determines if we should change the touch state to start scrolling after
	 * the user moves their touch point too far.
	 */
	protected void determineScrollingStart(MotionEvent e, float touchSlopScale) {
		/**
		 * Locally do absolute value. mLastMotionX is set to the y value of the
		 * down event.
		 */
		final float x = e.getX();
		// final float y = e.getY();
		final int xDiff = (int) Math.abs(x - mDownMotionX);
		// final int yDiff = (int) Math.abs(y - mDownMotionY);

		final int touchSlop = Math.round(touchSlopScale * mTouchSlop);
		boolean xPaged = xDiff > mPagingTouchSlop;
		boolean xMoved = xDiff > touchSlop;
		// boolean yMoved = yDiff > touchSlop;
		// Log.e("sn", "xDiff=" + xDiff + " yDiff=" + yDiff +
		// " mPagingTouchSlop="
		// + mPagingTouchSlop + " touchSlop=" + touchSlop);
		if (xMoved || xPaged) {
			if (mUsePagingTouchSlop ? xPaged : xMoved) {
				// Scroll if the user moved far enough along the X axis
				if (mTouchState == TOUCH_STATE_FLYING_REST)
					mTouchState = TOUCH_STATE_FLYING_SCROLLING;
				else
					mTouchState = TOUCH_STATE_SCROLLING;
				pageBeginMoving();
			}
		}
	}

	Runnable hideScrollingIndicatorRunnable = new Runnable() {
		@Override
		public void run() {
			hideScrollingIndicator(false);
		}
	};

	protected void pageBeginMoving() {
		showScrollingIndicator(false);
	}

	protected void pageEndMoving() {
		hideScrollingIndicator(false);
	}

	public void flashScrollingIndicator(boolean animated) {
		removeCallbacks(hideScrollingIndicatorRunnable);
		showScrollingIndicator(!animated);
		postDelayed(hideScrollingIndicatorRunnable,
				sScrollIndicatorFlashDuration);
	}

	protected void showScrollingIndicator(boolean immediately) {
		mShouldShowScrollIndicator = true;
		mShouldShowScrollIndicatorImmediately = true;
		if (mPageCountInScreen > 1 || getChildCount() <= 1)
			return;

		synchronized (mLock) {
			mShouldShowScrollIndicator = false;
			if (mScrollIndicator != null) {
				// Fade the indicator in
				updateScrollingIndicatorPosition();
				mScrollIndicator.setVisibility(View.VISIBLE);
				cancelScrollingIndicatorAnimations();
				if (immediately || mScrollingPaused) {
					mScrollIndicator.setAlpha(1f);
				} else {
					mScrollIndicatorAnimator = ObjectAnimator.ofFloat(
							mScrollIndicator, "alpha", 1f);
					mScrollIndicatorAnimator
							.setDuration(sScrollIndicatorFadeInDuration);
					mScrollIndicatorAnimator.start();
				}
			}
		}
	}

	protected void cancelScrollingIndicatorAnimations() {
		if (mScrollIndicatorAnimator != null) {
			mScrollIndicatorAnimator.cancel();
		}
	}

	protected void hideScrollingIndicator(boolean immediately) {
		if (mAdapter.getCount() <= 1)
			return;

		synchronized (mLock) {
			if (mScrollIndicator != null) {
				// Fade the indicator out
				updateScrollingIndicatorPosition();
				cancelScrollingIndicatorAnimations();
				if (immediately || mScrollingPaused) {
					mScrollIndicator.setVisibility(View.INVISIBLE);
					mScrollIndicator.setAlpha(0f);
				} else {
					mScrollIndicatorAnimator = ObjectAnimator.ofFloat(
							mScrollIndicator, "alpha", 0f);
					mScrollIndicatorAnimator
							.setDuration(sScrollIndicatorFadeOutDuration);
					mScrollIndicatorAnimator
							.addListener(new AnimatorListenerAdapter() {
								private boolean cancelled = false;

								@Override
								public void onAnimationCancel(
										android.animation.Animator animation) {
									cancelled = true;
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									if (!cancelled) {
										mScrollIndicator
												.setVisibility(View.INVISIBLE);
									}
								}
							});
					mScrollIndicatorAnimator.start();
				}
			}
		}
	}

	private void updateScrollingIndicator() {
		if (mAdapter.getCount() <= 1)
			return;

		synchronized (mLock) {
			if (mScrollIndicator != null) {
				updateScrollingIndicatorPosition();
			}
			if (mShouldShowScrollIndicator) {
				showScrollingIndicator(mShouldShowScrollIndicatorImmediately);
			}
		}
	}

	protected void updateScrollingIndicatorPosition() {
		if (mPageCountInScreen > 1 || mScrollIndicator == null)
			return;
		int numPages = mAdapter.getCount();
		int pageWidth = getMeasuredWidth();
		int indicatorSpace = pageWidth / numPages;
		int maxScroll = (numPages - 1) * pageWidth;
		float offset = (float) getScrollX() / (float) maxScroll;
		if (mScreenQueue.getChildCount() > 1)
			offset = (float) (mCurScreen * pageWidth + getScrollX() - mScreenQueue
					.getChildById(mCurScreen).left) / (float) maxScroll;
		int indicatorPos = (int) (offset * (pageWidth - indicatorSpace));
		// int indicatorPos = mCurScreen * indicatorSpace;
		if (mScrollIndicator.getMeasuredWidth() != indicatorSpace) {
			mScrollIndicator.getLayoutParams().width = indicatorSpace;
			mScrollIndicator.requestLayout();
			Log.i(TAG, "2 numPages=" + numPages + " pageWidth=" + pageWidth
					+ " indicatorSpace=" + indicatorSpace
					+ " mScrollIndicator.getMeasuredWidth()="
					+ mScrollIndicator.getMeasuredWidth()
					+ " mScrollIndicator.getMeasuredHeight()="
					+ mScrollIndicator.getMeasuredHeight());
		}
		mScrollIndicator.setTranslationX(indicatorPos);
	}

	protected class ScreenInfo {
		public int childId;
		public int left;
		public int top;
		public int width;
		public int height;
		public View childView;

		public ScreenInfo(int childId, int left, int top, int width,
				int height, View childView) {
			this.childId = childId;
			this.left = left;
			this.top = top;
			this.width = width;
			this.height = height;
			this.childView = childView;
		}

		public final int getRight() {
			return (this.left + this.width);
		}

		public final int getBottom() {
			return (this.top + this.height);
		}

		@Override
		public String toString() {
			return "ScreenInfo(" + childId + ", " + left + ", " + top + ", "
					+ width + ", " + height + ")";
		}
	}

	protected class ScreenQueue {
		public static final int ModeHorizontal = 0;
		public static final int ModeVertical = 1;

		private List<ScreenInfo> screens = new ArrayList<ScreenInfo>();
		// 因为使用次数比较多，所以就不每次调用screens.size()了，直接维护一个mScreenCount。
		private int mScreenCount = 0;

		/**
		 * 输入时，应该以Horizontal的方式，逐个添加
		 */
		public final void addScreen(ScreenInfo screen) {
			screens.add(screen);
			++mScreenCount;
		}

		public final void clear() {
			screens.clear();
			mScreenCount = 0;
		}

		public final int getChildCount() {
			return mScreenCount;
		}

		/**
		 * 保证传入的childId在子view的ID中，不能越界，如果越界的话 出现何种情况，不予预测（实际会返回处于右侧边缘的一个screen）。
		 * 
		 * @param childId
		 * @return childId对应的子view信息
		 */
		public ScreenInfo getChildById(int childId) {
			ScreenInfo child = null;
			for (int i = 0; i < mScreenCount; ++i) {
				child = screens.get(i);
				if (child.childId == childId)
					return child;
			}
			return child;
		}

		public final boolean isAtFront(int childId) {
			return childId == screens.get(0).childId;
		}

		public final boolean isAtBack(int childId) {
			return childId == screens.get(mScreenCount - 1).childId;
		}

		public ScreenInfo getScreenAt(int index) {
			if (index < mScreenCount)
				return screens.get(index);
			else
				return null;
		}

		/**
		 * 判断左(上)侧是否缺页（该位置没有子view存在），若是，则将最后一个子view移动到此处。 参考了内存缺页的管理模式。
		 * 
		 * @param curChildId
		 *            当前正在显示的页面（子view）ID
		 */
		public final void frontPageFault(int curChildId) {
			if (isAtFront(curChildId)) {
				if (DEBUG)
					Log.i("sn", "frontPageFault isAtFront curChildId="
							+ curChildId);
				Log.i("sn", "11111111111111111111111111111111111 frontPageFault isAtFront curChildId=" + curChildId);
				moveLastToFront();
			}
		}

		/**
		 * 判断右(下)侧是否缺页（该位置没有子view存在），若是，则将第一个子view移动到此处。 参考了内存缺页的管理模式。
		 * 
		 * @param curChildId
		 *            当前正在显示的页面（子view）ID
		 */
		public final void backPageFault(int curChildId) {
			if (isAtBack(curChildId)) {
				if (DEBUG)
					Log.i("sn", "backPageFault isAtBack curChildId="
							+ curChildId);
				Log.i("sn", "backPageFault isAtBack curChildId=" + curChildId);
				moveFirstToBack();
			}
		}

		private void moveLastToFront() {
			ScreenInfo referChild = screens.get(0);
			ScreenInfo moveChild = screens.remove(mScreenCount - 1);

			moveChild.left = referChild.left - moveChild.width - mPageMargin;
			if (DEBUG)
				Log.i(TAG, "moveLastToFront referId=" + referChild.childId
						+ " moveId=" + moveChild.childId);
			Log.i(TAG, "moveLastToFront referId=" + referChild.childId
					+ " moveId=" + moveChild.childId);
			// 在layout中移动相应的Child
			// getChildAt(moveChild.childId).layout(moveChild.left,
			// moveChild.top,
			// moveChild.getRight(), moveChild.getBottom());
			postInvalidate();
			// 在双端队列中移动相应的Child的信息
			screens.add(0, moveChild);

		}

		private void moveFirstToBack() {
			ScreenInfo referChild = screens.get(mScreenCount - 1);
			ScreenInfo moveChild = screens.remove(0);

			moveChild.left = referChild.getRight() + mPageMargin;
			if (DEBUG)
				Log.i(TAG, "moveFirstToBack referId=" + referChild.childId
						+ " moveId=" + moveChild.childId);
			Log.i(TAG, "moveFirstToBack referId=" + referChild.childId
					+ " moveId=" + moveChild.childId);
			// getChildAt(moveChild.childId).layout(moveChild.left,
			// moveChild.top,
			// moveChild.getRight(), moveChild.getBottom());
			postInvalidate();
			screens.add(mScreenCount - 1, moveChild);
		}

	}

	/**
	 * Detect if flyflip and cycleflip can be allowed.
	 * 
	 * Fly can only works when: 1. mPageCountInScreen > 1 &&
	 * mPagedViewList.size() >= (mPageCountInScreen * 2) 2. mPageCountInScreen
	 * == 1 && mPagedViewList.size() >= FAST_SNAP_SCREENS Cycle only works when:
	 * 1. mPageCountInScreen == 1 && mPagedViewList.size() > 2 2.
	 * mPageCountInScreen > 1 && mPagedViewList.size() > mPageCountInScreen
	 * 
	 * If cyclefilp is forbidden, add space page at front of pagedView to make
	 * sure all page can be chosen in center.
	 */
	private void detectFlyAndCycle() {
		if (mAdapter == null)
			return;
		if (mCycleFlipByUser
				&& ((mPageCountInScreen == 1 && mAdapter.getCount() > 2) || (mPageCountInScreen > 1 && mAdapter
						.getCount() > mPageCountInScreen))) {
			mCanCycleFlip = true;
			mSpacePageCount = 0;
		} else {
			mCanCycleFlip = false;
			mSpacePageCount = mPageCountInScreen / 2;
		}
		if (mFlyFlipByUser
				&& ((mPageCountInScreen > 1 && mAdapter.getCount() >= (mPageCountInScreen * 2)) || (mPageCountInScreen == 1 && mAdapter
						.getCount() >= FAST_SNAP_SCREENS)))
			mCanFlyFlip = true;
		else
			mCanFlyFlip = false;
	}

	// /////////////////////////////////////////////
	public void setScrollIndicator(View scrollIndicator) {
		mScrollIndicator = scrollIndicator;
	}

	public void setInOutAnimation(Context context, int inResourceID,
			int outResourceID) {
		mInAnim = AnimationUtils.loadAnimation(context, inResourceID);
		mOutAnim = AnimationUtils.loadAnimation(context, outResourceID);
	}

	/**
	 * Clear all pages in pagedview and clear the list.
	 */
	public void clearAllViews() {
		removeAllViewsInLayout();
		mScreenQueue.clear();
		mCanCycleFlip = false;
		mCanFlyFlip = false;
		mIsDataReady = false;
		mCurScreen = mDefaultScreen;
		mTouchState = TOUCH_STATE_REST;
	}

	/**
	 * Set page count displayed in one screen.
	 * 
	 * @param count : must be more than 0 and odd
	 */
	public void setPageCountInScreen(int count) {
		String err = null;
		if (count <= 0)
			err = "setPageCountInScreen(count) doesn't support (count <= 0)";
		else if (count % 2 == 0)
			err = "setPageCountInScreen(count) doesn't support (count % 2 == 0)";
		if (err != null)
			throw new IllegalArgumentException(err);

		mPageCountInScreen = count;
		mSpacePageCount = (count / 2);
		detectFlyAndCycle();
		requestLayout();
	}

	/**
	 * Set page margin.
	 */
	public void setPageMargin(int margin) {
		mPageMargin = margin;
	}

	/**
	 * Set whether page can fly. Only works when: 1. mPageCountInScreen > 1 &&
	 * mPagedViewList.size() >= (mPageCountInScreen * 2) 2. mPageCountInScreen
	 * == 1 && mPagedViewList.size() >= FAST_SNAP_SCREENS
	 */
	public void setFlyFlip(boolean enable) {
		mCanFlyFlip = mFlyFlipByUser = enable;
		detectFlyAndCycle();
	}

	/**
	 * Set whether page can cycle. Only works when: 1. mPageCountInScreen == 1
	 * && mPagedViewList.size() > 2 2. mPageCountInScreen > 1 &&
	 * mPagedViewList.size() > mPageCountInScreen
	 */
	public void setCircleFlip(boolean enable) {
		mCanCycleFlip = mCycleFlipByUser = enable;
		detectFlyAndCycle();
		setToScreen(mCurScreen);
	}

	/**
	 * Set page size scale when flying.
	 */
	public void setFlyPageSizeScale(float scale) {
		if (scale > 0)
			mFlyPageSizeScale = scale;
	}

	/**
	 * Set whether page can horizontal over scroll. Default false.
	 * 
	 * @param enable
	 *            true: page will continue to scroll even if hits horizontal
	 *            edge and then back to edge after hand off. false: page can not
	 *            scroll horizontally over edge.
	 */
	public void setCanHorizontalOverScroll(boolean enable) {
		mCanHorizontalOverScroll = enable;
	}

	/**
	 * Set whether page can vertical over scroll. Default false.
	 * 
	 * @param enable
	 *            true: page will continue to scroll even if hits vertical edge
	 *            and then back to edge after hand off. false: page can not
	 *            scroll vertically over edge.
	 */
	public void setCanVerticalOverScroll(boolean enable) {
		mCanVerticalOverScroll = enable;
	}

	/**
	 * Scroll to left page.
	 */
	public void scrollLeft() {
	        if (mCanCycleFlip) {
		        if (mScroller.isFinished())
			        snapToScreen(mCurScreen, FAST_SNAP_VELOCITY);
			else
			        snapToScreen(mNextScreen, FAST_SNAP_VELOCITY);
		} else {
		        if (mScroller.isFinished()) {
			        if (mCurScreen > 0)
				        snapToScreenNoCircle(mCurScreen - 1, mPageWidth);
			} else {
			        if (mNextScreen > 0)
				        snapToScreenNoCircle(mNextScreen - 1, mPageWidth);
			}
		}
	}

	/**
	 * Scroll to right page.
	 */
	public void scrollRight() {
	        if (mCanCycleFlip) {
		        if (mScroller.isFinished())
			        snapToScreen(mCurScreen, -FAST_SNAP_VELOCITY);
			else
			        snapToScreen(mNextScreen, -FAST_SNAP_VELOCITY);
		} else {
		        if (mScroller.isFinished()) {
			        if (mCurScreen < mAdapter.getCount() - 1)
				        snapToScreenNoCircle(mCurScreen + 1, mPageWidth);
			} else {
			        if (mNextScreen < mAdapter.getCount() - 1)
				        snapToScreenNoCircle(mNextScreen + 1, mPageWidth);
			}
		}
	}

	/**
	 * get the current page.
	 */
	public int getCurScreen() {
		if (mCanCycleFlip)
			return (mCurScreen + (mPageCountInScreen / 2))
					% mAdapter.getCount();
		else
			return mCurScreen;
	}

	/**
	 * Set the current page.
	 */
	public void setCurrentScreen(int currentScreen) {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		// Log.e("sn","setCurrentScreen currentScreen="+currentScreen+" getChildCount()="+getChildCount()+" mSpacePageList.size()="+mSpacePageList.size());
		// don't introduce any checks like mCurrentPage == currentPage here-- if
		// we change the
		// the default
		if (mAdapter == null) {
			mShouldToScreen = currentScreen;
			return;
		}
		
		mShouldToScreen = -1;
		
		if (mCanCycleFlip)
			mCurScreen = (currentScreen - (mPageCountInScreen / 2) + mAdapter
					.getCount()) % mAdapter.getCount();
		else
			mCurScreen = Math.max(0,
					Math.min(currentScreen, mAdapter.getCount() - 1));

		if (mScreenQueue.getChildCount() == 0) {
			return;
		}

		scrollTo((int) mScreenQueue.getChildById(mCurScreen).left
				- (mPageWidth + mPageMargin) * mSpacePageCount, 0);
		updateScrollingIndicator();
		invalidate();		
		Log.e("sn",
				"call onPageSelected mCurScreen=" + mCurScreen
						+ " mNextScreen=" + mNextScreen + " mScreenQueue.size="
						+ mScreenQueue.getChildCount() + " "
						+ mScreenQueue.getChildById(mCurScreen).left + " --3");
		notifyPageSelected();
	}

	/**
	 * Set whether use sound effect when page has motions.
	 */
	public void setUseSoundEffect(boolean enable) {
		mUseSoundEffect = enable;
	}

	//-------------------------- interface of adatper ---------------------//
	@Override
	public BaseAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void setAdapter(BaseAdapter adapter) {
		if (mAdapter != null && mDataObserver != null) {
			mAdapter.unregisterDataSetObserver(mDataObserver);
		}
		mAdapter = adapter;
		if (mAdapter != null) {
			mAdapter.registerDataSetObserver(mDataObserver);
			mDataChanged = true;
			mIsDataReady = true;
			reset();
		}
		detectFlyAndCycle();
		requestLayout();	
		if (mShouldToScreen != -1) {
			setCurrentScreen(mShouldToScreen);
		}
	}

	@Override
	public View getSelectedView() {
		ScreenInfo si = mScreenQueue.getChildById(getCurScreen());
		if (si != null)
			return si.childView;
		else
			return null;
	}

	@Override
	public void setSelection(int selection) {
		setCurrentScreen(selection);
	}

    // -------------------- lisenters -------------------- //
	// single tap
	public interface OnItemClickListener {
		void onItemClick(AdapterPagedView pagedView, View view, int position);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}
	// after longpress,receive touch event
	public interface OnTouchListener {
	    void onTouch(AdapterPagedView pagedView, View view, int position, MotionEvent event);
	}

	public void setOnTouchListener(OnTouchListener listener) {
		mOnTouchListener = listener;
	}

	// long press
	public interface OnItemLongPressListener {
		void onItemLongPress(AdapterPagedView pagedView, View view, int position);
	}

	public void setOnItemLongPressListener(OnItemLongPressListener listener) {
		mOnItemLongPressListener = listener;
	}

	// double tap
	public interface OnItemDoubleClickListener {
		void onItemDoubleClick(AdapterPagedView pagedView, View view, int position);
	}

	public void setOnItemDoubleClickListener(OnItemDoubleClickListener listener) {
		mOnItemDoubleClickListener = listener;
	}

	// back
	public interface OnDownSlidingBackListener {
		void onDownSlidingBack(AdapterPagedView pagedView);
	}

	public void setOnDownSlidingBackListener(OnDownSlidingBackListener listener) {
		mOnDownSlidingBackListener = listener;
	}

	// fly
	public interface OnPageFlyingListener {
		void onPageFlying(AdapterPagedView pagedView, boolean isFlying);
	}

	public void setOnPageFlyingListener(OnPageFlyingListener listener) {
		mOnPageFlyingListener = listener;
	}

	// select
	public interface OnPageSelectedListener {
		void onPageSelected(AdapterPagedView pagedView, View view, int position);
	}

	public void setOnPageSelectedListener(OnPageSelectedListener listener) {
		mOnPageSelectedListener = listener;
	}
}
