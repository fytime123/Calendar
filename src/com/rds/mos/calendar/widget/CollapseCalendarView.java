package com.rds.mos.calendar.widget;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rds.mos.calendar.R;
import com.rds.mos.calendar.manager.CalendarManager;
import com.rds.mos.calendar.manager.Day;
import com.rds.mos.calendar.manager.Formatter;
import com.rds.mos.calendar.manager.Month;
import com.rds.mos.calendar.manager.Week;
import com.rds.mos.calendar.resize.ResizeManagerMediator;

public class CollapseCalendarView extends LinearLayout {

	private CalendarManager calendarManager;

	private LinearLayout weeksView;

	private final LayoutInflater inflater;

	private final RecycleBin recycleBin = new RecycleBin();

	private OnDateSelectListener dateListener;

	private ResizeManagerMediator resizeMediator;

	private boolean initialized;

	public CollapseCalendarView(Context context) {
		this(context, null);
	}

	public CollapseCalendarView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.calendarViewStyle);
	}

	public CollapseCalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		inflater = LayoutInflater.from(context);

		inflate(context, R.layout.calendar_layout, this);
		weeksView = (LinearLayout) findViewById(R.id.weeks);

		setOrientation(VERTICAL);
	}

	public void setMediator(ResizeManagerMediator mediator) {
		mediator.setCalendarView(this);
		resizeMediator = mediator;
	}

//	public void init(CalendarManager manager) {
//		if (manager != null) {
//			calendarManager = manager;
//			populateLayout();
//			if (dateListener != null) {
//				dateListener.onDateSelected(calendarManager.getSelectedDay());
//			}
//		}
//	}
	
	public void resetInit(CalendarManager manager){
		if (manager != null) {
			calendarManager = manager;
			populateLayout();
		}
	}

	public CalendarManager getManager() {
		return calendarManager;
	}

	public ResizeManagerMediator getResizeManager() {
		return resizeMediator;
	}

	@SuppressLint("WrongCall")
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (resizeMediator != null)
			resizeMediator.onDraw();
		super.dispatchDraw(canvas);
	}


	public void setDateSelectListener(OnDateSelectListener listener) {
		dateListener = listener;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(resizeMediator!=null){
			return resizeMediator.onInterceptTouchEvent(ev);
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		if(resizeMediator!=null)
			resizeMediator.onTouchEvent(event);
		return true;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		populateLayout();
	}

	private void populateDays() {

		if (!initialized) {
			CalendarManager manager = getManager();

			if (manager != null) {
				Formatter formatter = manager.getFormatter();

				LinearLayout layout = (LinearLayout) findViewById(R.id.days);

				LocalDate date = LocalDate.now().withDayOfWeek(DateTimeConstants.MONDAY);
				for (int i = 0; i < 7; i++) {
					TextView textView = (TextView) layout.getChildAt(i);
					textView.setText(formatter.getDayName(date));

					date = date.plusDays(1);
				}

				initialized = true;
			}
		}

	}

	public void populateLayout() {

		if (calendarManager != null) {

			populateDays();

			if (calendarManager.getState() == CalendarManager.State.MONTH) {
				populateMonthLayout((Month) calendarManager.getUnits());
			} else {
				populateWeekLayout((Week) calendarManager.getUnits());
			}
		}

	}

	private void populateMonthLayout(Month month) {

		List<Week> weeks = month.getWeeks();
		int cnt = weeks.size();
		for (int i = 0; i < cnt; i++) {
			WeekView weekView = getWeekView(i);
			populateWeekLayout(weeks.get(i), weekView);
		}

		int childCnt = weeksView.getChildCount();
		if (cnt < childCnt) {
			for (int i = cnt; i < childCnt; i++) {
				cacheView(i);
			}
		}

	}

	private void populateWeekLayout(Week week) {
		WeekView weekView = getWeekView(0);
		populateWeekLayout(week, weekView);

		int cnt = weeksView.getChildCount();
		if (cnt > 1) {
			for (int i = cnt - 1; i > 0; i--) {
				cacheView(i);
			}
		}
	}

	private void populateWeekLayout(Week week, WeekView weekView) {

		List<Day> days = week.getDays();
		for (int i = 0; i < 7; i++) {
			final Day day = days.get(i);
			DayView dayView = (DayView) weekView.getChildAt(i);

			dayView.setText(day.getText());
			dayView.setSelected(day.isSelected());
			dayView.setCurrent(day.isCurrent());

			boolean enables = day.isEnabled();
			dayView.setEnabled(enables);

			if (enables) {
				dayView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						LocalDate date = day.getDate();
						if (dateListener != null) {
							dateListener.onDateSelected(date);
						}
					}
				});
			} else {
				dayView.setOnClickListener(null);
			}
		}

	}

	public LinearLayout getWeeksView() {
		return weeksView;
	}

	private WeekView getWeekView(int index) {
		int cnt = weeksView.getChildCount();

		if (cnt < index + 1) {
			for (int i = cnt; i < index + 1; i++) {
				View view = getView();
				weeksView.addView(view);
			}
		}

		return (WeekView) weeksView.getChildAt(index);
	}

	private View getView() {
		View view = recycleBin.recycleView();
		if (view == null) {
			view = inflater.inflate(R.layout.week_layout, this, false);
		} else {
			view.setVisibility(View.VISIBLE);
		}
		return view;
	}

	private void cacheView(int index) {
		View view = weeksView.getChildAt(index);
		if (view != null) {
			weeksView.removeViewAt(index);
			recycleBin.addView(view);
		}
	}

	public LocalDate getSelectedDate() {
		return calendarManager.getSelectedDay();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		if(resizeMediator!=null)
			resizeMediator.recycle();
	}

	private class RecycleBin {

		private final Queue<View> mViews = new LinkedList<View>();

		public View recycleView() {
			return mViews.poll();
		}

		public void addView(View view) {
			mViews.add(view);
		}

	}

	public interface OnDateSelectListener {
		public void onDateSelected(LocalDate date);
	}
	
	public interface OnExpandedListener{
		public void expanded(boolean expanded);
	}

}
