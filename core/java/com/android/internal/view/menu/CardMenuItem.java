package com.android.internal.view.menu;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;


public class CardMenuItem implements MenuItem {
	
	private View mLayout = null;
	
    private int mId = 0;
    private int mGroup = 0;
    private int mCategoryOrder = 0;
    private int mOrdering = 0;
    
    private CharSequence mTitle = null;
    
    private Drawable mIconDrawable = null;
    private int mIconResId = 0;
    
    private Context mContext = null;
    
    private MenuItem.OnMenuItemClickListener mClickListener = null;
        
    public CardMenuItem(Context context, View layout, int group, int id, int categoryOrder, int ordering,
            CharSequence title) {
        mContext = context;
		mLayout = layout;
        mId = id;
        mGroup = group;
        mCategoryOrder = categoryOrder;
        mOrdering = ordering;
        mTitle = title;

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mLayout.setLayoutParams(lp);

		setTitle(title);
    }
        
    public View getLayout(){
    	return mLayout;
    }
    
	@Override
	public boolean collapseActionView() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean expandActionView() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ActionProvider getActionProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getActionView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char getAlphabeticShortcut() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getGroupId() {
		return mGroup;
	}

	@Override
	public Drawable getIcon() {
		return mIconDrawable;
	}

	@Override
	public Intent getIntent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getItemId() {
		return mId;
	}

	@Override
	public ContextMenuInfo getMenuInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char getNumericShortcut() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getOrder() {
		return mOrdering;
	}

	@Override
	public SubMenu getSubMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence getTitle() {
		return mTitle;
	}

	@Override
	public CharSequence getTitleCondensed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSubMenu() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isActionViewExpanded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCheckable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChecked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MenuItem setActionProvider(ActionProvider arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setActionView(View arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setActionView(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setAlphabeticShortcut(char arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setCheckable(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setChecked(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setEnabled(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setIcon(Drawable arg0) {		
		mIconDrawable = arg0;

		if(mIconDrawable != null){
			ImageView image = (ImageView)mLayout.findViewById(com.android.internal.R.id.card_menu_item_icon);
			image.setImageDrawable(mIconDrawable);
		}
		
		return this;
	}

	@Override
	public MenuItem setIcon(int arg0) {
		mIconDrawable = mContext.getResources().getDrawable(arg0);
		return setIcon(mIconDrawable);
	}

	@Override
	public MenuItem setIntent(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setNumericShortcut(char arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setOnActionExpandListener(OnActionExpandListener arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener arg0) {
        mClickListener = arg0;
        return this;
	}

	@Override
	public MenuItem setShortcut(char arg0, char arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setShowAsAction(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MenuItem setShowAsActionFlags(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setTitle(CharSequence arg0) {
		mTitle = arg0;
		
		if(mTitle != null){
			TextView text = (TextView)mLayout.findViewById(com.android.internal.R.id.card_menu_item_title);
			text.setText(mTitle);
		}

		return this;
	}

	@Override
	public MenuItem setTitle(int arg0) {
		mTitle = mContext.getResources().getString(arg0);
		return setTitle(mTitle);
	}

	@Override
	public MenuItem setTitleCondensed(CharSequence arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setVisible(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
