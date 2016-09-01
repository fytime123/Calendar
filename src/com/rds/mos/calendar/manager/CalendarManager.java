package com.rds.mos.calendar.manager;

import org.joda.time.LocalDate;

/**
 * Created by Blaz Solar on 27/02/14.
 */
public class CalendarManager {

	private State mState;
	private RangeUnit mUnit;
	private LocalDate mSelected;
	private LocalDate mToday;
	private LocalDate mMinDate;
	private LocalDate mMaxDate;
	private Formatter formatter;

	private LocalDate mActiveMonth;
	
	
	public CalendarManager(){
	}
	
	public void setCalendarManager(LocalDate selected, State state, LocalDate minDate, LocalDate maxDate) {
		setCalendarManager(selected, state, minDate, maxDate, null);
	}

	public void setCalendarManager(LocalDate selected, State state, LocalDate minDate, LocalDate maxDate, Formatter formatter) {
		mToday = LocalDate.now();
		mState = state;

		if (formatter == null) {
			this.formatter = new DefaultFormatter();
		} else {
			this.formatter = formatter;
		}

		init(selected, minDate, maxDate);
	}

	public boolean selectDay(LocalDate date) {
		if (!mSelected.isEqual(date)) {
			mUnit.deselect(mSelected);
			mSelected = date;
			mUnit.select(mSelected);

			if (mState == State.WEEK) {
				setActiveMonth(date);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 选择某一日期
	 * 
	 * @param date
	 * @return 是否有变化
	 */
	public boolean changeDay(LocalDate date) {
		boolean isChanged = false;
		if(mState == CalendarManager.State.MONTH){
			isChanged = changedMonthDay(date);
		}else{
			isChanged = changedWeekDay(date);
		}
		return isChanged;
	}
	
	private boolean changedMonthDay(LocalDate date){
		if(mState != CalendarManager.State.MONTH) return false;
		LocalDate curSelectedDate = mSelected;
		int selectMonthIndex = curSelectedDate.getMonthOfYear() + curSelectedDate.getYear() * 100;
		int monthIndex = date.getMonthOfYear() + date.getYear() * 100;
		boolean ischanged = false;

		if (selectMonthIndex == monthIndex) {
			// 选择的日期与当前日期在同一个月
			ischanged = selectDay(date);
		} else if (selectMonthIndex < monthIndex) {
			// 选择的日期是当前日期的下一个月
			ischanged = selectDay(date);
			// 在月模式下切换
			ischanged |= next();
		} else if (selectMonthIndex > monthIndex) {
			// 选择的日期是当前日期的上一个月
			ischanged = selectDay(date);
			// 在月模式下切换
			ischanged |= prev();
		}

		return ischanged;
	}
	private boolean changedWeekDay(LocalDate date){
		if(mState != CalendarManager.State.WEEK) return false;
		LocalDate curSelectedDate = mSelected;
		int selectWeekIndex = curSelectedDate.getWeekOfWeekyear() + curSelectedDate.getYear() * 100;
		int weekIndex = date.getWeekOfWeekyear() + date.getYear() * 100;
		boolean ischanged = false;

		if (selectWeekIndex == weekIndex) {
			// 选择的日期与当前日期在同一个月
			ischanged = selectDay(date);
		} else if (selectWeekIndex < weekIndex) {
			// 选择的日期是当前日期的下一个月
			ischanged = selectDay(date);
			// 在月模式下切换
			ischanged |= next();
		} else if (selectWeekIndex > weekIndex) {
			// 选择的日期是当前日期的上一个月
			ischanged = selectDay(date);
			// 在月模式下切换
			ischanged |= prev();
		}

		return ischanged;
	}

	/**
	 * 切换到下一个月，应该被选中的日期
	 */
	public LocalDate getNextMonthSelectedDate() {
		// 当前选中的日期mMaxDate在同一个月时不能切换了
		int curMonthIndex = mSelected.getMonthOfYear() + mSelected.getYear() * 100;
		int maxMonthIndex = mMaxDate.getMonthOfYear() + mMaxDate.getYear() * 100;
		if (curMonthIndex == maxMonthIndex)
			return mSelected;

		LocalDate nextMonthDate = mSelected.plusMonths(1);
		int nextMonthIndex = nextMonthDate.getMonthOfYear() + nextMonthDate.getYear() * 100;
		if (nextMonthIndex == maxMonthIndex) {
			int maxIndex = mMaxDate.getDayOfMonth();
			int nextIndex = nextMonthDate.getDayOfMonth();
			// 如果下月选择的同一天大于mMaxDate,则最大选择mMaxDate
			if (nextIndex > maxIndex)
				return mMaxDate;
		}

		return nextMonthDate;
	}

	/**
	 * 切换到上一个月，对应应该被选中的日期
	 */
	public LocalDate getPrevMonthSelectedDate() {
		// 当前选中的日期mMinDate在同一个月时不能切换了
		int curMonthIndex = mSelected.getMonthOfYear() + mSelected.getYear() * 100;
		int minMonthIndex = mMinDate.getMonthOfYear() + mMinDate.getYear() * 100;
		if (curMonthIndex == minMonthIndex)
			return mSelected;

		LocalDate preMonthDate = mSelected.minusMonths(1);
		int preMonthIndex = preMonthDate.getMonthOfYear() + preMonthDate.getYear()*100;
		if (preMonthIndex == minMonthIndex) {
			int minIndex = mMinDate.getDayOfMonth();
			int preIndex = preMonthDate.getDayOfMonth();
			// 如果下月选择的同一天大于mMaxDate,则最大选择mMaxDate
			if (preIndex < minIndex)
				return mMinDate;
		}

		return preMonthDate;
	}

	public LocalDate getNextWeekSelectedDate() {
		// 当前选中的日期与mMaxDate在同一周不能切换了
		int curWeekIndex = mSelected.getWeekOfWeekyear() + mSelected.getYear()*100;
		int maxWeekIndex = mMaxDate.getWeekOfWeekyear() + mMaxDate.getYear() *100;
		if (curWeekIndex == maxWeekIndex)
			return mSelected;

		LocalDate nextWeekDate = mSelected.plusWeeks(1);
		int nextWeekIndex = nextWeekDate.getWeekOfWeekyear() + nextWeekDate.getYear()*100;
		if (nextWeekIndex == maxWeekIndex) {
			int maxIndex = mMaxDate.getDayOfWeek();
			int nextIndex = nextWeekDate.getDayOfWeek();
			// 如果下月选择的同一天大于mMaxDate,则最大选择mMaxDate
			if (nextIndex > maxIndex)
				return mMaxDate;
		}

		return nextWeekDate;
	}

	public LocalDate getPrevWeekSelectedDate() {
		// 当前选中的日期与mMinDate在同一周不能切换了
		int curWeekIndex = mSelected.getWeekOfWeekyear() + mSelected.getYear() *100;
		int minWeekIndex = mMinDate.getWeekOfWeekyear() + mMinDate.getYear()*100;
		if (curWeekIndex == minWeekIndex)
			return mSelected;

		LocalDate preWeekDate = mSelected.minusWeeks(1);
		int preWeekIndex = preWeekDate.getWeekOfWeekyear() + preWeekDate.getYear()*100;
		if (preWeekIndex == minWeekIndex) {
			int minIndex = mMinDate.getDayOfWeek();
			int preIndex = preWeekDate.getDayOfWeek();
			// 如果下月选择的同一天大于mMaxDate,则最大选择mMaxDate
			if (preIndex < minIndex)
				return mMinDate;
		}

		return preWeekDate;
	}

	public boolean changedNext() {
		LocalDate nextDate = getNextDate();
		boolean isChanged = changeDay(nextDate);
		return isChanged;
	}

	public boolean changedPrev() {
		LocalDate prevDate = getPrevDate();
		boolean isChanged = changeDay(prevDate);
		return isChanged;
	}
	
	public LocalDate getNextDate(){
		LocalDate nextDate = null;
		if (mState == State.MONTH) {
			nextDate = getNextMonthSelectedDate();
		} else {
			nextDate = getNextWeekSelectedDate();
		}
		return nextDate;
	}
	
	public LocalDate getPrevDate(){
		LocalDate prevDate = null;
		if (mState == State.MONTH) {
			prevDate = getPrevMonthSelectedDate();
		} else {
			prevDate = getPrevWeekSelectedDate();
		}
		return prevDate;
	}

	public LocalDate getSelectedDay() {
		return mSelected;
	}

	public String getHeaderText() {
		return formatter.getHeaderText(mUnit.getType(), mUnit.getFrom(), mUnit.getTo());
	}

	public boolean hasNext() {
		return mUnit.hasNext();
	}

	public boolean hasPrev() {
		return mUnit.hasPrev();
	}

	public boolean next() {

		boolean next = mUnit.next();
		mUnit.select(mSelected);

		setActiveMonth(mUnit.getFrom());

		return next;
	}

	public boolean prev() {

		boolean prev = mUnit.prev();
		mUnit.select(mSelected);

		setActiveMonth(mUnit.getTo());

		return prev;
	}

	/**
	 * 
	 * @return index of month to focus to
	 */
	public void toggleView() {

		if (mState == State.MONTH) {
			toggleFromMonth();
		} else {
			toggleFromWeek();
		}

	}

	public State getState() {
		return mState;
	}

	public CalendarUnit getUnits() {
		return mUnit;
	}

	public LocalDate getActiveMonth() {
		return mActiveMonth;
	}

	private void setActiveMonth(LocalDate activeMonth) {
		mActiveMonth = activeMonth.withDayOfMonth(1);
	}

	private void toggleFromMonth() {

		// if same month as selected
		if (mUnit.isInView(mSelected)) {
			toggleFromMonth(mSelected);

			setActiveMonth(mSelected);
		} else {
			setActiveMonth(mUnit.getFrom());
			toggleFromMonth(mUnit.getFirstDateOfCurrentMonth(mActiveMonth));
		}
	}

	public void toggleToWeek(int weekInMonth) {
		LocalDate date = mUnit.getFrom().plusDays(weekInMonth * 7);
		toggleFromMonth(date);
	}

	private void toggleFromMonth(LocalDate date) {
		setUnit(new Week(date, mToday, mMinDate, mMaxDate));
		mUnit.select(mSelected);
		mState = State.WEEK;
	}

	private void toggleFromWeek() {

		setUnit(new Month(mActiveMonth, mToday, mMinDate, mMaxDate));
		mUnit.select(mSelected);

		mState = State.MONTH;
	}

	private void init() {
		if (mState == State.MONTH) {
			setUnit(new Month(mSelected, mToday, mMinDate, mMaxDate));
		} else {
			setUnit(new Week(mSelected, mToday, mMinDate, mMaxDate));
		}
		mUnit.select(mSelected);
	}

	void setUnit(RangeUnit unit) {
		if (unit != null) {
			mUnit = unit;
		}
	}

	public int getWeekOfMonth() {
		if (mUnit.isInView(mSelected)) {
			if (mUnit.isIn(mSelected)) { // TODO not pretty
				return mUnit.getWeekInMonth(mSelected);
			} else if (mUnit.getFrom().isAfter(mSelected)) {
				return mUnit.getWeekInMonth(mUnit.getFrom());
			} else {
				return mUnit.getWeekInMonth(mUnit.getTo());
			}
		} else {
			// if not in this month first week should be selected
			return mUnit.getFirstWeek(mUnit.getFirstDateOfCurrentMonth(mActiveMonth)); 
		}
	}

	public void init(LocalDate date, LocalDate minDate, LocalDate maxDate) {
		mSelected = date;
		setActiveMonth(date);
		mMinDate = minDate;
		mMaxDate = maxDate;

		init();
	}

	public LocalDate getMinDate() {
		return mMinDate;
	}

	public void setMinDate(LocalDate minDate) {
		mMinDate = minDate;
	}

	public LocalDate getMaxDate() {
		return mMaxDate;
	}

	public void setMaxDate(LocalDate maxDate) {
		mMaxDate = maxDate;
	}

	public Formatter getFormatter() {
		return formatter;
	}

	public int getViewPagerCount(){
		int count = 0;
		if(mState == State.MONTH){
			count = getMonthCount();
		}else{
			count = getWeekCount();
		}
		return count;
	}
	
	public int getViewPagerIndex(){
		int index = 0;
		if(mState == State.MONTH){
			index = getMonthIndex(mSelected);
		}else{
			index = getWeekIndex(mSelected);
		}
		return index;
	}
	
	
	private int getMonthCount(){
		//总显示的月数
		LocalDate maxMonthDate = mMaxDate.dayOfMonth().withMaximumValue();
		int count = 12;
		LocalDate date = mToday;
		while(date.isBefore(maxMonthDate)){
			count ++ ;
			date = date.plusMonths(1);
		}
		
		return count;
	}
	
	private int getWeekCount(){
		LocalDate w1 = mMinDate.withDayOfWeek(1);
		LocalDate w2 = mMaxDate.withDayOfWeek(7);
		int diff = w2.compareTo(w1);
		int count = diff/7;
		return count;
	}
	
	private int getMonthIndex(LocalDate date){
		int index = 0;
		LocalDate w1 = mMinDate.dayOfMonth().withMaximumValue();
		while(date.isAfter(w1)){
			index ++ ;
			w1 = w1.plusMonths(1);
		}
		return index;
	}
	
	private int getWeekIndex(LocalDate date){
		LocalDate w1 = mMinDate.withDayOfWeek(1);
		int diff = date.compareTo(w1);
		int count = diff/7;
		if(diff%7>0)count++;
		return count;
	}
	
	/**
	 * 获取当前月或周第一天
	 */
	private LocalDate getFirstDate(){
		LocalDate firstDate = null;
		if(mState == State.MONTH){
			firstDate = mSelected.withDayOfMonth(1);
		}else{
			firstDate = mSelected.withDayOfWeek(1);
		}
		return firstDate;
	}
	
	/**
	 * 获取当前月或者周最后一天
	 */
	private LocalDate getLastDate(){
		LocalDate lastDate = null;
		if(mState == State.MONTH){
			lastDate = mSelected.dayOfMonth().withMaximumValue();
		}else{
			lastDate = mSelected.dayOfWeek().withMaximumValue();
		}
		return lastDate;
	}
	
	public boolean canDragLeft(){
		LocalDate firstDate = getFirstDate();
		return mMinDate.isBefore(firstDate);
	}
	
	public boolean canDragRight(){
		LocalDate lastDate = getLastDate();
		return mMaxDate.isAfter(lastDate);
	}
	
	
	public enum State {
		MONTH, WEEK
	}

}
