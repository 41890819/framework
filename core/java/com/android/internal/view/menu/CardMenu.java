package com.android.internal.view.menu;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.PagedView;

import android.graphics.Color;

import android.app.Activity;
import android.view.ViewGroup.LayoutParams;

public class CardMenu implements Menu, PagedView.OnDownSlidingBackListener, PagedView.OnItemClickListener, PagedView.OnPageSelectedListener {
	private PagedView mPageViewGroup = null;
	
	private ArrayList<CardMenuItem> mItems = new ArrayList<CardMenuItem>();

	private Activity mActivity = null;
	private Context mContext = null;
	private Resources mResources = null;

	private CardMenuListener mCardMenuListener = null;

	private ViewGroup mIndicatorGroup = null;
	private ImageView mLastSelectedIndicatorItem = null;
	private ArrayList<ImageView> mIndicatorItems = new ArrayList<ImageView>();
	
	public CardMenu(Activity activity, CardMenuListener listener) {
		mActivity = activity;
		mContext = mActivity.getBaseContext();
		mResources = mContext.getResources();
		mCardMenuListener = listener;
		
		View menuLayout = mActivity.getLayoutInflater().inflate(com.android.internal.R.layout.card_menu, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		ViewGroup container = (ViewGroup)mActivity.findViewById(android.R.id.content);
		container.addView(menuLayout, lp);

		mIndicatorGroup = (ViewGroup)menuLayout.findViewById(com.android.internal.R.id.card_menu_indicator_group);
		
		mPageViewGroup = (PagedView)menuLayout.findViewById(com.android.internal.R.id.card_menu_pagedview);
		// ArrayList<View> views = new ArrayList<View>();
		// mPageViewGroup.setPagedViewList(views);
		mPageViewGroup.setAlpha(0.9f);
		mPageViewGroup.setCircleFlip(false);
		mPageViewGroup.setInOutAnimation(mContext, com.android.internal.R.anim.slide_in_up, com.android.internal.R.anim.slide_out_down);
		mPageViewGroup.setVisibility(View.GONE);
		mPageViewGroup.setOnDownSlidingBackListener(this);
		mPageViewGroup.setOnItemClickListener(this);
		mPageViewGroup.setOnPageSelectedListener(this);
	}
	
	public void show(){
		if(mItems.size() <= 0)
			return;

		for(int i = 0; i < mIndicatorItems.size(); ++i){
			ImageView image = mIndicatorItems.get(i);
			if(i == 0){
				image.setImageResource(com.android.internal.R.drawable.card_menu_indicator_selected);
				mLastSelectedIndicatorItem = image;
			}else{
				image.setImageResource(com.android.internal.R.drawable.card_menu_indicator_non_selected);
			}
		}

		mPageViewGroup.setCurrentScreen(0);
		mPageViewGroup.setVisibility(View.VISIBLE);
		mIndicatorGroup.setVisibility(View.VISIBLE);
	}
	
	public void dismiss(){
		mPageViewGroup.setVisibility(View.GONE);
		mIndicatorGroup.setVisibility(View.GONE);
	}
	
	public void setOnItemClickListener(PagedView.OnItemClickListener listener) {
		if(listener != null)
			mPageViewGroup.setOnItemClickListener(listener);
	}
		
	@Override
	public MenuItem add(CharSequence arg0) {
		return addInternal(0, 0, 0, arg0);
	}

	@Override
	public MenuItem add(int arg0) {
		return addInternal(0, 0, 0, mResources.getString(arg0));
	}

	@Override
	public MenuItem add(int arg0, int arg1, int arg2, CharSequence arg3) {
		return addInternal(arg0, arg1, arg2, arg3);
	}

	@Override
	public MenuItem add(int arg0, int arg1, int arg2, int arg3) {
		return addInternal(arg0, arg1, arg2, mResources.getString(arg3));
	}
	
	private MenuItem addInternal(int group, int id, int categoryOrder, CharSequence title) {
		View itemView = mActivity.getLayoutInflater().inflate(com.android.internal.R.layout.card_menu_item, null);
		int ordering = 0;
		CardMenuItem item = new CardMenuItem(mContext, itemView, group, id, categoryOrder, ordering, title);
		mItems.add(item);

		mPageViewGroup.addItem(item.getLayout());

		addIndicatorItem();
		
		onItemsChanged(true);
		
		return item;		
	}

    private void addIndicatorItem() {
		LayoutParams fp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		ImageView image = new ImageView(mContext);
		image.setBackgroundColor(Color.TRANSPARENT);
		image.setScaleType(ScaleType.CENTER_INSIDE);
		
		mIndicatorItems.add(image);
		mIndicatorGroup.addView(image, fp);
	}

	@Override
	public int addIntentOptions(int arg0, int arg1, int arg2,
			ComponentName arg3, Intent[] arg4, Intent arg5, int arg6,
			MenuItem[] arg7) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public SubMenu addSubMenu(CharSequence arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public SubMenu addSubMenu(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public SubMenu addSubMenu(int arg0, int arg1, int arg2, CharSequence arg3) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public SubMenu addSubMenu(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void clear() {
		// TODO Auto-generated method stub
		mItems.clear();
		mIndicatorGroup.removeAllViews();
		mIndicatorItems.clear();
		mPageViewGroup.clearAllViews();
		onItemsChanged(true);
	}


	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public MenuItem findItem(int arg0) {
		return mItems.get(arg0);
	}


	@Override
	public MenuItem getItem(int arg0) {
		return mItems.get(arg0);
	}


	@Override
	public boolean hasVisibleItems() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean isShortcutKey(int arg0, KeyEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean performIdentifierAction(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean performShortcut(int arg0, KeyEvent arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void removeGroup(int arg0) {
		// TODO Auto-generated method stub
	}


	@Override
	public void removeItem(int arg0) {
		if(arg0 >= mItems.size())
			return;

		View itemView = mItems.get(arg0).getLayout();
		mPageViewGroup.removeItem(itemView);
		mItems.remove(arg0);
		mIndicatorGroup.removeViewAt(arg0);
		mIndicatorItems.remove(arg0);

		onItemsChanged(true);
	}


	@Override
	public void setGroupCheckable(int arg0, boolean arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setGroupEnabled(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setGroupVisible(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setQwertyMode(boolean arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int size() {
		return mItems.size();
	}
	
    private void onItemsChanged(boolean structureChanged) {
		mPageViewGroup.setPageMargin(mItems.size());
		
    }

    @Override
	public void onDownSlidingBack(PagedView pagedView) {
		mCardMenuListener.onDownSlideBack();
	}

    @Override
	public void onItemClick(PagedView pagedView, View view, int position) {
		MenuItem item = mItems.get(position);
		
		if(item != null)
			mCardMenuListener.onItemSelected(item);
	}

	@Override
	public void onPageSelected(PagedView arg0, View arg1, int arg2) {
		mLastSelectedIndicatorItem.setImageResource(com.android.internal.R.drawable.card_menu_indicator_non_selected);
		mLastSelectedIndicatorItem.invalidate();
		ImageView cur = mIndicatorItems.get(arg2);
		cur.setImageResource(com.android.internal.R.drawable.card_menu_indicator_selected);
		cur.invalidate();
		mLastSelectedIndicatorItem = cur;
	}

	public interface CardMenuListener {
		public void onItemSelected(MenuItem item);
		public void onDownSlideBack();
	}
}
