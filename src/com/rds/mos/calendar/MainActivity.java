package com.rds.mos.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.rds.mos.calendar.manager.CalendarManager;
import com.rds.mos.calendar.manager.CalendarManager.State;
import com.rds.mos.calendar.resize.ResizeManagerMediator;
import com.rds.mos.calendar.widget.CollapseCalendarView;
import com.rds.mos.calendar.widget.CollapseCalendarView.OnDateSelectListener;
import com.rds.mos.calendar.widget.CollapseCalendarView.OnExpandedListener;
import com.rds.mos.calendar.widget.ContainerLayout;
import com.rds.mos.calendar.widget.ScrollListener;
import com.rds.mos.calendar.widget.ThreeGroup;

public class MainActivity extends Activity implements OnClickListener, ScrollListener, OnExpandedListener,
		OnDateSelectListener {

	private TextView titleTextView;
	private ImageButton prevImageButton;
	private ImageButton nextImageButtom;
	private TextView selectionTextView;

	private LocalDate curDate;
	private LocalDate maxDate;
	private LocalDate minDate;
	private LocalDate today;

	private ThreeGroup threeGroup;
	private boolean direction;

	private ListView viewContent;

	private ContainerLayout container;
	private ResizeManagerMediator resizeMediator;
	private SimpleDateFormat dateFormat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	private void init() {

		dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
		today = LocalDate.now();
		curDate = today;
		minDate = today.minusYears(1);
		maxDate = today.plusDays(90);

		resizeMediator = new ResizeManagerMediator(this);
		// 设置周月切换监听
		resizeMediator.setOnExpandedListener(this);
		initCalender();
		initListView();

		container.setMediator(resizeMediator);

		onFinishInflate();

		// 初始化加载数据
		loadTask();
	}

	private void initCalender() {

		CollapseCalendarView calendarView0 = new CollapseCalendarView(this);
		CollapseCalendarView calendarView1 = new CollapseCalendarView(this);
		CollapseCalendarView calendarView2 = new CollapseCalendarView(this);
		calendarView0.setDateSelectListener(this);
		calendarView1.setDateSelectListener(this);
		calendarView2.setDateSelectListener(this);

		CalendarManager manager2 = new CalendarManager();
		manager2.setCalendarManager(curDate, CalendarManager.State.MONTH, minDate, maxDate);
		calendarView1.resetInit(manager2);

		LocalDate date = manager2.getPrevMonthSelectedDate();
		CalendarManager manager1 = new CalendarManager();
		manager1.setCalendarManager(date, CalendarManager.State.MONTH, minDate, maxDate);
		calendarView0.resetInit(manager1);

		date = manager2.getNextMonthSelectedDate();
		CalendarManager manager3 = new CalendarManager();
		manager3.setCalendarManager(date, CalendarManager.State.MONTH, minDate, maxDate);
		calendarView2.resetInit(manager3);

		threeGroup = (ThreeGroup) this.findViewById(R.id.calendar);

		threeGroup.addView(calendarView0);
		threeGroup.addView(calendarView1);
		threeGroup.addView(calendarView2);
		threeGroup.setListener(this);

		calendarView1.setMediator(resizeMediator);
	}

	private void initListView() {

		container = (ContainerLayout) findViewById(R.id.container);

		viewContent = (ListView) findViewById(R.id.view_content);
		String[] strs = new String[100];
		for (int i = 0; i < strs.length; i++) {
			if (i % 2 == 0) {
				strs[i] = String.format(Locale.getDefault(), "第%d行,某个日期的一条数据####", i);
			} else {
				strs[i] = String.format(Locale.getDefault(), "第%d行,某个日期的一条数据####", i);
			}
		}
		viewContent.setAdapter(new ArrayAdapter<String>(this, R.layout.listview_item, strs));
	}

	private void onFinishInflate() {

		titleTextView = (TextView) findViewById(R.id.title);
		prevImageButton = (ImageButton) findViewById(R.id.prev);
		nextImageButtom = (ImageButton) findViewById(R.id.next);
		selectionTextView = (TextView) findViewById(R.id.selection_title);

		prevImageButton.setOnClickListener(this);
		nextImageButtom.setOnClickListener(this);

		populate();
	}

	private void populate() {
		Date date = curDate.toDate();
		String showDate = dateFormat.format(date);
		titleTextView.setText(showDate);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.prev && threeGroup.isCanLeft()) {
			threeGroup.prevPage();
		} else if (id == R.id.next && threeGroup.isCanRight()) {
			threeGroup.nexPage();
		}

	}

	@Override
	public void scrollStart(boolean direction) {
		this.direction = direction;
	}

	@Override
	public void scrollEnd(boolean isChanged) {

		if (!isChanged)
			return;

		if (direction) {
			scroll2Right();
		} else {
			scroll2Left();
		}
	}

	private void scroll2Left() {
		CollapseCalendarView view0 = (CollapseCalendarView) threeGroup.getChildAt(0);
		CollapseCalendarView view1 = (CollapseCalendarView) threeGroup.getChildAt(1);
		CollapseCalendarView view2 = (CollapseCalendarView) threeGroup.getChildAt(2);

		// 向左前进滑动
		CalendarManager manager0 = view0.getManager();
		LocalDate date = manager0.getPrevDate();
		State state = manager0.getState();

		CalendarManager manager2 = view2.getManager();
		manager2.setCalendarManager(date, state, minDate, maxDate);
		view2.resetInit(manager2);

		threeGroup.removeViewAt(2);
		threeGroup.addView(view2, 0);
		view0.setMediator(resizeMediator);

		boolean canDragLeft = manager0.canDragLeft();
		threeGroup.setCanLeft(canDragLeft);
		threeGroup.setCanRight(true);

		prevImageButton.setEnabled(canDragLeft);
		nextImageButtom.setEnabled(true);

		CalendarManager manager1 = view1.getManager();
		LocalDate fromDate = manager1.getSelectedDay();
		LocalDate toDate = manager0.getSelectedDay();
		// 刷新数据
		srollPageDayChanged(state, fromDate, toDate);

	}

	private void scroll2Right() {
		CollapseCalendarView view0 = (CollapseCalendarView) threeGroup.getChildAt(0);
		CollapseCalendarView view1 = (CollapseCalendarView) threeGroup.getChildAt(1);
		CollapseCalendarView view2 = (CollapseCalendarView) threeGroup.getChildAt(2);

		// 向右前进滑动
		CalendarManager manager2 = view2.getManager();
		LocalDate date = manager2.getNextDate();
		State state = manager2.getState();

		CalendarManager manager0 = view0.getManager();
		manager0.setCalendarManager(date, state, minDate, maxDate);
		view0.resetInit(manager0);

		threeGroup.removeViewAt(0);
		threeGroup.addView(view0, 2);
		view2.setMediator(resizeMediator);

		boolean canDragRight = manager2.canDragRight();
		threeGroup.setCanLeft(true);
		threeGroup.setCanRight(canDragRight);

		prevImageButton.setEnabled(true);
		nextImageButtom.setEnabled(canDragRight);

		CalendarManager manager1 = view1.getManager();
		LocalDate fromDate = manager1.getSelectedDay();
		LocalDate toDate = manager2.getSelectedDay();
		// 刷新数据
		srollPageDayChanged(state, fromDate, toDate);
	}

	@Override
	public void expanded(boolean expanded) {
		// 周月模式切换监听
		CollapseCalendarView view1 = (CollapseCalendarView) threeGroup.getChildAt(1);
		CalendarManager manager1 = view1.getManager();
		LocalDate prevDate = null;
		LocalDate nextDate = null;
		if (expanded) {
			prevDate = manager1.getPrevMonthSelectedDate();
			nextDate = manager1.getNextMonthSelectedDate();
		} else {
			prevDate = manager1.getPrevWeekSelectedDate();
			nextDate = manager1.getNextWeekSelectedDate();
		}

		State state = expanded ? State.MONTH : State.WEEK;

		CollapseCalendarView view0 = (CollapseCalendarView) threeGroup.getChildAt(0);
		CalendarManager manager0 = view0.getManager();
		manager0.setCalendarManager(prevDate, state, minDate, maxDate);
		view0.populateLayout();

		CollapseCalendarView view2 = (CollapseCalendarView) threeGroup.getChildAt(2);
		CalendarManager manager2 = view2.getManager();
		manager2.setCalendarManager(nextDate, state, minDate, maxDate);
		view2.populateLayout();

		boolean canDragLeft = manager1.canDragLeft();
		boolean canDragRight = manager1.canDragRight();
		// 是否可以左右滑动
		threeGroup.setCanLeft(canDragLeft);
		threeGroup.setCanRight(canDragRight);
		prevImageButton.setEnabled(canDragRight);
		nextImageButtom.setEnabled(canDragRight);
	}

	@Override
	public void onDateSelected(LocalDate date) {
		// 前提：date的值只会在minDate,maxDate内的；因为在这个范围外的DayView是不可以点击的
		// 月模式下：
		// 1.点击的是在本月的数据，获取该天的数据，刷新ListView
		// 2.点击的是上一个月的日期，滚动到上一个月，本月的默认选择的天也变为该天
		// 3.点击的是下一个月的日期，滚动到下一个月，本月默认选择的天也边为该天
		// 周模式：
		// 点击的天都是该周的
		// 点击的的日期如果不是前面选择日期所在月份时，加载月份数据，然后显示该天数据

		CollapseCalendarView view0 = (CollapseCalendarView) threeGroup.getChildAt(0);
		CollapseCalendarView view1 = (CollapseCalendarView) threeGroup.getChildAt(1);
		CollapseCalendarView view2 = (CollapseCalendarView) threeGroup.getChildAt(2);

		CalendarManager manager0 = view0.getManager();
		CalendarManager manager1 = view1.getManager();
		CalendarManager manager2 = view2.getManager();

		LocalDate selectDate = manager1.getSelectedDay();
		State state = manager1.getState();

		int monthIndex = date.getYear() * 100 + date.getMonthOfYear();
		int selectIndex = selectDate.getYear() * 100 + selectDate.getMonthOfYear();

		if (state == State.WEEK) {

			boolean changed = manager1.selectDay(date);
			if (!changed)
				return;

			manager0.selectDay(manager1.getPrevWeekSelectedDate());
			manager2.selectDay(manager1.getNextWeekSelectedDate());
			// 刷新日历
			view0.populateLayout();
			view1.populateLayout();
			view2.populateLayout();

			curDate = date;
			populate();
			// 周模式
			if (monthIndex == selectIndex) {
				// 刷新ListView列表数据
				refreshDayListView();
			} else {
				// 去加载该月的数据，再刷新ListView
				loadTask();
			}
		} else {
			// 月模式
			if (monthIndex == selectIndex) {
				// 同月
				manager1.selectDay(date);
				manager0.selectDay(manager1.getPrevMonthSelectedDate());
				manager2.selectDay(manager1.getNextMonthSelectedDate());
				// 刷新日历
				view0.populateLayout();
				view1.populateLayout();
				view2.populateLayout();
				curDate = date;
				populate();
			} else if (monthIndex > selectIndex) {
				// 这个月的数据也修改为date对应的第几天被选中
				nearMonth2SameIndex(date, selectDate, manager1);
				manager2.selectDay(date);
				view1.populateLayout();
				view2.populateLayout();
				// manager0在滚动后会修改
				// 切换到下个月
				threeGroup.nexPage();
			} else if (monthIndex < selectIndex) {
				// 这个月的数据也修改为date对应的第几天被选中
				nearMonth2SameIndex(date, selectDate, manager1);
				manager0.selectDay(date);
				view0.populateLayout();
				view1.populateLayout();
				// manager1在滚动后会修改
				// 切换到上个月
				threeGroup.prevPage();
			}
		}
	}

	private void nearMonth2SameIndex(LocalDate toDate, LocalDate selectDate, CalendarManager manager1) {
		int index = toDate.getDayOfMonth();
		int maxValue = selectDate.dayOfMonth().getMaximumValue();
		if (index > maxValue) {
			selectDate = selectDate.withDayOfMonth(maxValue);
		} else {
			selectDate = selectDate.withDayOfMonth(index);
		}
		manager1.selectDay(selectDate);
	}

	private void srollPageDayChanged(State state, LocalDate fromDate, LocalDate toDate) {
		// 前提：fromDate,toDate的值只会在minDate,maxDate内的；因为在这个范围外的DayView是不可以点击的
		// 月模式下：
		// 加载数据，刷新：loadTask()
		// 周模式：
		// 同一个月内：直接刷新
		// 不同一个月：加载数据：刷新：loadTask()

		curDate = toDate;
		populate();
		if (state == state.MONTH) {
			// 月模式
			loadTask();
		} else {
			// 周模式
			int fromMonthIndex = fromDate.getYear() * 100 + fromDate.getMonthOfYear();
			int toMonthIndex = toDate.getYear() * 100 + toDate.getMonthOfYear();

			if (fromMonthIndex != toMonthIndex) {
				// 非同一个月
				loadTask();
			} else {
				// 同一个月
				refreshDayListView();
			}
		}

	}

	private void loadTask() {
		// 1.加载缓存或者数据库数据:loadCache()
		// 1.1.数据不为空：
		// 计算日历数据:resetCurCalendarManager()
		// 刷新日历:initCurCalendar()
		// 刷新curDate日期ListView:refreshDayListView()

		// 1.2.本地缓存数据为空
		// 加载网络数据,跟新缓存和内存:loadNetWorkTask()

		// 计算日历数据：resetCurCalendarManager()
		// 刷新日历:initCurCalendar()
		// 刷新curDate日期ListView:refreshDayListView()
	}

	private void loadCacheTask() {
		// 加载缓存或者数据库数据
	}

	private void loadNetWorkTask() {
		// 加载网络数据,跟新缓存和内存
	}

	private void resetCurCalendarManager() {
		// 计算日历数据
	}

	private void initCurCalendar() {
		// 刷新日历
	}

	private void refreshDayListView() {
		// 获取选择curDate日期的数据
		// 刷新ListView
	}

	// ///////////////////////////////////////

	// 跳到某一天
	private void gotoDate(LocalDate date) {

	}

	private void gotoToday() {

	}
}
