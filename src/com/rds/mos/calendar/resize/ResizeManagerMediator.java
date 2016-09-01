package com.rds.mos.calendar.resize;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.Scroller;

import com.rds.mos.calendar.manager.CalendarManager;
import com.rds.mos.calendar.widget.CollapseCalendarView;
import com.rds.mos.calendar.widget.CollapseCalendarView.OnExpandedListener;

public class ResizeManagerMediator {

	/** View to resize */
	private CollapseCalendarView mCalendarView;

	private View contentListView;

	/** Distance in px until drag has started */
	private final int mTouchSlop;

	private final int mMinFlingVelocity;

	private final int mMaxFlingVelocity;

	/** Y position on {@link android.view.MotionEvent#ACTION_DOWN} */
	private float mDownY;

	private float mDownX;

	/** Y position when resizing started */
	private float mDragStartY;

	/** If calendar is currently resizing. */
	private State mState = State.IDLE;

	//private VelocityTracker mVelocityTracker;
	private final Scroller mScroller;

	private ProgressManager mProgressManager;
	private OnExpandedListener expandedListener;

	public ResizeManagerMediator(Context context) {

		mScroller = new Scroller(context);

		ViewConfiguration viewConfig = ViewConfiguration.get(context);
		mTouchSlop = viewConfig.getScaledTouchSlop();
		mMinFlingVelocity = viewConfig.getScaledMinimumFlingVelocity();
		mMaxFlingVelocity = viewConfig.getScaledMaximumFlingVelocity();
	}

	public void setCalendarView(CollapseCalendarView calendarView) {
		mCalendarView = calendarView;
	}

	public void setContentListView(View contentListView) {
		this.contentListView = contentListView;
	}

	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = MotionEventCompat.getActionMasked(ev);

		boolean touch = false;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			touch = onDownEvent(ev);
			//Log.v("liufuyi", "ACTION_DOWN:touch="+ touch);
			return touch;
		case MotionEvent.ACTION_MOVE:

//			if(mVelocityTracker!=null)
//				mVelocityTracker.addMovement(ev);
			touch = checkForResizing(ev);
			//Log.v("liufuyi", "ACTION_MOVE:touch="+ touch);
			return touch;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			finishMotionEvent();
			return false;
		}

		return false;
	}

	public boolean onTouchEvent(MotionEvent event) {
		final int action = MotionEventCompat.getActionMasked(event);
		
//		if (action == MotionEvent.ACTION_MOVE && mVelocityTracker!=null) {
//			mVelocityTracker.addMovement(event);
//		}

		if (mState == State.DRAGGING) {
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				int deltaY = calculateDistanceForDrag(event);
				mProgressManager.applyDelta(deltaY);
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				finishMotionEvent();
				break;
			}
			
			return true;

		} else if (action == MotionEvent.ACTION_MOVE) {
			boolean touch = checkForResizing(event);
			return touch;
		}

		return false;
	}

	/**
	 * Triggered
	 * 
	 * @param event
	 *            Down event
	 */
	private boolean onDownEvent(MotionEvent event) {
		if (MotionEventCompat.getActionMasked(event) != MotionEvent.ACTION_DOWN) {
			throw new IllegalStateException("Has to be down event!");
		}

//		if (mVelocityTracker == null) {
//			mVelocityTracker = VelocityTracker.obtain();
//		} else {
//			mVelocityTracker.clear();
//		}
		
		mDownY = event.getY();
		mDownX = event.getX();

		if (mDownY > contentListView.getY() && canChildScrollDown(contentListView)) {
			return false;
		}

		if (!mScroller.isFinished()) {
			mScroller.forceFinished(true);
			if (mScroller.getFinalY() == 0) {
				mDragStartY = mDownY + mScroller.getStartY() - mScroller.getCurrY();
			} else {
				mDragStartY = mDownY - mScroller.getCurrY();
			}
			mState = State.DRAGGING;
			return true;
		} else {
			return false;
		}

	}

	public void recycle() {
//		if (mVelocityTracker != null) {
//			mVelocityTracker.recycle();
//			mVelocityTracker = null;
//		}
	}

	
	public boolean checkForResizing(MotionEvent ev) {
		// FIXME this method should only return true / false. Make another
		// method for starting animation
		CalendarManager manager = mCalendarView.getManager();
		CalendarManager.State state = manager.getState();
		
		final int yDiff = calculateDistance(ev);
		final int xDiff = calculateXDistance(ev);
		// 大于45度角的算上下拖动，算周月模式切换
		boolean angle = Math.abs(yDiff) > Math.abs(xDiff);
		
		//Log.v("liufuyi", "xDiff="+xDiff+"  yDiff="+ yDiff +" mTouchSlop="+mTouchSlop);
		
		if (mDownY > contentListView.getY()) {
			if (canChildScrollDown(contentListView)){
				return false;
			}else{
				if(state == CalendarManager.State.MONTH && yDiff>0){
					//如果已经为月模式，再由上往下拖，不捕获
					return false;
				}else if(state == CalendarManager.State.WEEK && yDiff<0){
					//如果已经为周模式，再有下往上拖，不捕获
					return false;
				}
			}
		}else if(!angle){
			return false;
		}

		if (mState == State.DRAGGING) {
			return true;
		}

		

		if (angle && Math.abs(yDiff) > mTouchSlop) { 
			// FIXME this should happen only if dragging int right direction
			mState = State.DRAGGING;
			mDragStartY = ev.getY();

			if (mProgressManager == null) {

				int weekOfMonth = manager.getWeekOfMonth();

				if (state == CalendarManager.State.WEEK) { 
					// always animate in month view
					manager.toggleView();
					mCalendarView.populateLayout();
				}

				mProgressManager = new ProgressManagerImpl(mCalendarView, weekOfMonth,
						state == CalendarManager.State.MONTH);
			}

			return true;
		}

		return false;
	}

	private void finishMotionEvent() {
		if (mProgressManager != null && mProgressManager.isInitialized()) {
			startScolling();
		}
	}

	private void startScolling() {

		int velocity = 0;
//		if(mVelocityTracker!=null){
//			mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
//			velocity = (int) mVelocityTracker.getYVelocity();
//		}

		if (!mScroller.isFinished()) {
			mScroller.forceFinished(true);
		}

		int progress = mProgressManager.getCurrentHeight();
		int end;
		if (Math.abs(velocity) > mMinFlingVelocity) {

			if (velocity > 0) {
				end = mProgressManager.getEndSize() - progress;
			} else {
				end = -progress;
			}

		} else {

			int endSize = mProgressManager.getEndSize();
			if (endSize / 2 <= progress) {
				end = endSize - progress;
			} else {
				end = -progress;
			}

		}

		mScroller.startScroll(0, progress, 0, end);
		mCalendarView.postInvalidate();

		mState = State.SETTLING;

	}

	private int calculateDistance(MotionEvent event) {
		//Log.v("liufuyi", "y="+event.getY()+ " mDownY="+mDownY);
		return (int) (event.getY() - mDownY);
	}

	private int calculateXDistance(MotionEvent event) {
		//Log.v("liufuyi", "x="+event.getX()+ " mDownX="+mDownX);
		return (int) (event.getX() - mDownX);
	}

	private int calculateDistanceForDrag(MotionEvent event) {
		return (int) (event.getY() - mDragStartY);
	}

	public void onDraw() {
		if (!mScroller.isFinished()) {
			mScroller.computeScrollOffset();

			float position = mScroller.getCurrY() * 1f / mProgressManager.getEndSize();
			mProgressManager.apply(position);
			mCalendarView.postInvalidate();
		} else if (mState == State.SETTLING) {
			mState = State.IDLE;
			float position = mScroller.getCurrY() * 1f / mProgressManager.getEndSize();
			boolean expanded = position > 0;
			mProgressManager.finish(expanded);
			mProgressManager = null;
			if(expandedListener!=null)
				expandedListener.expanded(expanded);
		}

	}

	/**
	 * 判断scrollview / listview是否能滑动
	 * 
	 * @param view
	 * @return
	 */
	public boolean canChildScrollDown(View view) {
		if (Build.VERSION.SDK_INT < 14) {
			if (view instanceof AbsListView) {
				final AbsListView absListView = (AbsListView) view;
				return absListView.getChildCount() > 0
						&& (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView
								.getPaddingTop());
			} else {
				return view.getScrollY() > 0;
			}
		} else {
			boolean canScrollVertically = view.canScrollVertically(-1);
			return canScrollVertically;
		}
	}

	public void setOnExpandedListener(OnExpandedListener listener){
		expandedListener = listener;
	}
	
	private enum State {
		IDLE, DRAGGING, SETTLING
	}
}
