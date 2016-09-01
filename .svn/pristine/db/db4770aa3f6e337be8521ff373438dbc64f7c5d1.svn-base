package com.rds.mos.calendar.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 实现三个view无限循环
 */
public class ThreeGroup extends ViewGroup {

	private int screenWidth;
	// private int screenHight;
	/** Distance in px until drag has started */
	private int touchSlop;

	private int oldX;
	private int oldY;
	private Scroller scroller;
	
	private Direction direction;
	private boolean scrolling; // 是否正在滚动
	private boolean isChangedPage = false;
	private ScrollListener scrollListener; // 滑动监听事件
	
	private boolean canRight = true;
	private boolean canLeft = true;

	public ThreeGroup(Context context) {
		super(context);
		init(context);
	}

	public ThreeGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ThreeGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		screenWidth = this.getResources().getDisplayMetrics().widthPixels;
		scroller = new Scroller(context);
		ViewConfiguration viewConfig = ViewConfiguration.get(context);
		touchSlop = viewConfig.getScaledTouchSlop();
	}

	public void setListener(ScrollListener listener) {
		scrollListener = listener;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			final int childWidth = child.getMeasuredWidth();
			getChildAt(i).layout(i * screenWidth, 0, childLeft + childWidth, child.getMeasuredHeight());
			childLeft += childWidth;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int maxHeight = 0;
		int maxWidth = 0;
		int childState = 0;
		maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
		maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

		int widthSize = Math.max(0, getSuggestedMinimumWidth());
		// Reconcile our calculated size with the widthMeasureSpec
		int widthSizeAndState = resolveSizeAndState(widthSize, widthMeasureSpec, 0);

		widthMeasureSpec = MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY);
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View view = getChildAt(i);
			view.measure(widthMeasureSpec, heightMeasureSpec);
			childState = combineMeasuredStates(childState, view.getMeasuredState());
			if (i == 1) {
				// 高度只去当前中间一个ChildView的高度
				final int childHeight = view.getMeasuredHeight();
				maxHeight = Math.max(maxHeight, childHeight);
			}
		}

		setMeasuredDimension(widthSizeAndState | (childState & MEASURED_STATE_MASK),
				resolveSizeAndState(maxHeight, heightMeasureSpec, (childState << MEASURED_HEIGHT_STATE_SHIFT)));

		// 重新计算View后都滚动到第二个View
		scrollTo(screenWidth, 0);// jump to page 1

	}

	@Override
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			invalidate(); // invalidate the View from a non-UI thread
		}
		if (scroller.isFinished() && scrolling) {
			scrolling = false;
			if (scrollListener != null) {
				scrollListener.scrollEnd(isChangedPage);
			}
		}
	}
	
	public void nexPage(){
		isChangedPage = true;//改变了页面
		scroller.startScroll(screenWidth, 0, screenWidth, 0, 1500);// auto first
		scrolling = true;
		invalidate();
		if (scrollListener != null) {
			scrollListener.scrollStart(true);
		}
	}
	
	public void prevPage(){
		isChangedPage = true;//改变了页面
		scroller.startScroll(screenWidth, 0, -screenWidth, 0, 1500);// auto first
		scrolling = true;
		invalidate();
		if (scrollListener != null) {
			scrollListener.scrollStart(false);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		
		super.onInterceptTouchEvent(event);
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			direction = Direction.nil;
			oldX = (int) event.getX();
			oldY = (int) event.getY();
			//Log.v("liufuyi", "ThreeGroup oldX="+oldX+"  oldY="+ oldY);
			break;
		case MotionEvent.ACTION_MOVE:
			int x = (int) event.getX();
			int y = (int) event.getY();
			int deltaY = y - oldY;
			int deltaX = x - oldX;
			//Log.v("liufuyi", "ThreeGroup deltaX="+deltaX+"  deltaY="+ deltaY);
			// 变化至少mTouchSlop时才捕获
			if (Math.abs(deltaX) < touchSlop){
				return false;
			}
			if(!canRight && deltaX<0){
				//不能继续向右前进拖动了
				return false;
			}
			if (!canLeft && deltaX>0) {
				//不能继续向左前进拖动了
				return false;
			}

			if (Math.abs(deltaX) < Math.abs(deltaY)) {
				// X方向变化大于Y方向变化才捕获
				return false;
			} else {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		super.onTouchEvent(event);
		//Log.v("liufuyi", "ThreeGroup onTouchEvent");
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			direction = Direction.nil;
			oldX = (int) event.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			if (!scrolling) {
				int x = (int) event.getX();
				int step = x - oldX;
				if (step >= touchSlop) {// right
					scrollTo(getScrollX() - step, 0);
					oldX = (int) event.getX();
					direction = Direction.left;
					return true;
				} else if (step <= -touchSlop) {// left
					scrollTo(getScrollX() - step, 0);
					oldX = (int) event.getX();
					direction = Direction.right;
					return true;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			autoScroll(direction, getScrollX());
			break;
		}
		return false;
	}

	private void autoScroll(Direction direction, int scrollx) {
		isChangedPage = false;
		if (direction == Direction.left) {
			int dx = screenWidth - scrollx;
			int halfWidth = screenWidth/4;
			
			if(dx>halfWidth){
				isChangedPage = true;//改变了页面
				scroller.startScroll(scrollx, 0, -scrollx, 0, 1500);// auto first
			}else {
				scroller.startScroll(scrollx, 0, dx, 0, 1500);// auto first
			}
			
			scrolling = true;
			invalidate();
			if (scrollListener != null) {
				scrollListener.scrollStart(false);
			}
		} else if (direction == Direction.right) {
			int dx = scrollx - screenWidth;
			int halfWidth = screenWidth/4;
			
			if(dx>halfWidth){
				// auto endpage
				isChangedPage = true;//改变了页面
				scroller.startScroll(scrollx, 0, screenWidth * 2 - scrollx, 0, 1500);
			}else{
				scroller.startScroll(scrollx, 0, -dx, 0, 1500);
			}
			
			scrolling = true;
			invalidate();
			if (scrollListener != null) {
				scrollListener.scrollStart(true);
			}
		}
	}
	

	public void setCanRight(boolean canRight) {
		this.canRight = canRight;
	}

	public void setCanLeft(boolean canLeft) {
		this.canLeft = canLeft;
	}
	
	public boolean isCanRight() {
		return canRight;
	}

	public boolean isCanLeft() {
		return canLeft;
	}




	enum Direction {
		nil, left, right;
	}
}
