package android.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.widget.AdapterPagedView;
import android.widget.AdapterPagedView.OnDownSlidingBackListener;
import android.widget.AdapterPagedView.OnItemClickListener;
import android.content.Context;
import android.content.Entity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuView extends FrameLayout implements OnItemClickListener,
		OnDownSlidingBackListener {
	private final boolean DEBUG = true;
	private final String TAG = "MenuView";
	private Context mContext;
	private View mMenuView;
	private AdapterPagedView mPagedView;
	private List<MenuEntity> mList = new ArrayList<MenuEntity>();
	private OnClickListener mOnClickListener = null;
	private OnBackListener mOnBackListener = null;
	private View mOnClickView = null;
	private View mOnBackView = null;
	private MenuEntity mEntity;
	private int mScreenWidth;
	private int mScreenHeight;
	private AnimationSet mInAnim = null;
	private AnimationSet mOutAnim = null;
	private MyAdapter mAdapter;

	public MenuView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		mPagedView = new AdapterPagedView(context);
		addView(mPagedView);
		mScreenWidth = getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = getResources().getDisplayMetrics().heightPixels;
		if (DEBUG)Log.d(TAG, "mScreenWidth=" + mScreenWidth+"---------mScreenHeight=" + mScreenHeight);
		loadAnimation();
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.BOTTOM;
		mPagedView.setLayoutParams(params);;
		mPagedView.setOnItemClickListener(this);
		mPagedView.setOnDownSlidingBackListener(this);
		mAdapter = new MyAdapter();
		mPagedView.setAdapter(mAdapter);
	}
	
	// add menuitem 
	public void addMenuItemInfo(String menuTitle) {
		mEntity = new MenuEntity();
		mEntity.setMenuInfo(menuTitle);
		mList.add(mEntity);
		mAdapter.notifyDataSetChanged();
	}
	// remove menuitem
	public void removeMenuItemInfo(String menuTitle) {
		Iterator<MenuEntity> MenuEntity_it = mList.iterator();
		while (MenuEntity_it.hasNext()) {
			MenuEntity checkWork = MenuEntity_it.next();
			if (checkWork.getMenuInfo().equals(menuTitle)) {
				MenuEntity_it.remove();
				mAdapter.notifyDataSetChanged();
			}
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mPagedView.onTouchEvent(event);
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		if (changedView == this) {
			 if (visibility == View.VISIBLE && mInAnim != null) {
			 		startAnimation(mInAnim);
			 } else if (visibility != View.VISIBLE && mOutAnim != null)
			 		startAnimation(mOutAnim);
		}
		super.onVisibilityChanged(changedView, visibility);
	}

	public class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
		    if(mList.size()==1)
			mPagedView.setCanHorizontalOverScroll(false);
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (DEBUG)Log.d(TAG, "getView=");
			ViewHolder holder;		
			if(convertView==null){
				holder=new ViewHolder();
				convertView=holder.view;
				holder.view.setLayoutParams(new LayoutParams(mScreenWidth, mScreenHeight));
				holder.view.setBackgroundColor(0xb0000000);
				holder.view.setTextColor(0xffa0a0a0);
				holder.view.setTextSize(100);
				holder.view.setGravity(Gravity.CENTER);
				convertView.setTag(holder);
			}else{
				holder=(ViewHolder)convertView.getTag();
			}
			String titleResource = mList.get(position).getMenuInfo();		
			holder.view.setText(titleResource);
			return convertView;
		}
	}
       class ViewHolder{
		TextView view=new TextView(mContext);
	
	}
	private void loadAnimation(){
		mInAnim=new AnimationSet(true);
		TranslateAnimation in_translateAnimation=new TranslateAnimation(0, 0, mScreenHeight, 0);
		in_translateAnimation.setDuration(400);
		AlphaAnimation in_alphaAnimation=new AlphaAnimation(0, 1);
		in_alphaAnimation.setDuration(400);
		mInAnim.addAnimation(in_translateAnimation);
		mInAnim.addAnimation(in_alphaAnimation);
		mOutAnim=new AnimationSet(true);
		TranslateAnimation out_translateAnimation=new TranslateAnimation(0, 0, 0, mScreenHeight);
		out_translateAnimation.setDuration(400);
		AlphaAnimation out_alphaAnimation=new AlphaAnimation(1, 0);
		out_alphaAnimation.setDuration(400);
		mOutAnim.addAnimation(out_translateAnimation);
		mOutAnim.addAnimation(out_alphaAnimation);

	}
	@Override
	public void onDownSlidingBack(AdapterPagedView pagedView) {
		// TODO Auto-generated method stub
		mOnBackListener.onBack(mOnBackView);
	}

	@Override
	public void onItemClick(AdapterPagedView pagedView, View view, int position) {
		// TODO Auto-generated method stubd
		if (DEBUG)Log.d(TAG, "onItemClick");
		mOnClickListener.onClick(mOnClickView, position);

	}

	// onItemClick interface
	public interface OnClickListener {
		void onClick(View v, int menuPosition);
	}

	public void setOnClickListener(View v, OnClickListener listener) {
		mOnClickListener = listener;
		mOnClickView = v;
	}

	// onDownBack interface
	public interface OnBackListener {
		void onBack(View v);
	}

	public void setOnBackListener(View v, OnBackListener listener) {
		mOnBackListener = listener;
		mOnBackView = v;
	}

	class MenuEntity {
		private String menuInfo;
		public String getMenuInfo() {
			return menuInfo;
		}
		public void setMenuInfo(String menuInfo) {
			this.menuInfo = menuInfo;
		}

	}

}
