package com.rds.mos.calendar.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rds.mos.calendar.resize.ResizeManagerMediator;

/**
 * Title:日历和内容的容器，处理滑动冲突和 Description:
 */
public class ContainerLayout extends LinearLayout {

	/**
	 * 日历控件，viewpager
	 */
	private ThreeGroup headerGroup;

	/**
	 * scrollview内容
	 */
	private ViewGroup contentGroup;
	
	/**
	 * 处理拖动事件中介
	 */
	private ResizeManagerMediator resizeMediator;

	private int lastYIntercept;
	private int contentHeight;
	
	public ContainerLayout(Context context) {
		super(context);
	}

	public ContainerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public ContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus) {
			init();
		}
	}

	private void init() {
		if (headerGroup == null && contentGroup == null) {
			headerGroup = (ThreeGroup) findViewWithTag("vp_calender");
			contentGroup = (ViewGroup) findViewWithTag("view_content");
			resizeMediator.setContentListView(contentGroup);
		}
	}

	public void setMediator(ResizeManagerMediator resizeMediator) {
		this.resizeMediator = resizeMediator;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// return false;
		super.onInterceptTouchEvent(event);
		// 记住本次点击时，日历栏高度，如果在日历下方，拦截并交给Mediator处理；
		// 在日历View上则不拦截,传递给子级GroupView处理
		boolean touch = checkIntercept(event);
		//Log.v("liufuyi", "ContainerLayout2 InterceptTouch=" + touch);
		if (touch)
			return resizeMediator.onInterceptTouchEvent(event);
		else
			return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// return
		super.onTouchEvent(event);

		if (lastYIntercept < contentHeight)
			return false;
		resizeMediator.onTouchEvent(event);
		return false;
	}

	private boolean checkIntercept(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastYIntercept = (int) event.getY();
			// 记住本次点击时，日历栏高度，如果在日历下方，交给Mediator处理,否则不拦截,传递给子级GroupView处理
			contentHeight = (int) contentGroup.getY();
			//Log.v("liufuyi", "ContainerLayout2 ACTION_DOWN");
			break;
		}

		if (lastYIntercept < contentHeight)
			return false;

		return true;
	}
}
