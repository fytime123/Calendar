package com.rds.mos.calendar.manager;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

/**
 * Created by Blaz Solar on 24/02/14.
 */
public class Month extends RangeUnit {

	private final List<Week> mWeeks = new ArrayList<Week>();
	private int mSelectedIndex = -1;

	protected Month(LocalDate date, LocalDate today, LocalDate minDate, LocalDate maxDate) {
		super(date.withDayOfMonth(1), date.withDayOfMonth(date.dayOfMonth().getMaximumValue()), today, minDate, maxDate);

		build();
	}

	@Override
	public boolean hasNext() {

		LocalDate maxDate = getMaxDate();
		if (maxDate == null) {
			return true;
		} else {

			LocalDate to = getTo();
			int year = maxDate.getYear();
			int yearTo = to.getYear();

			int month = maxDate.getMonthOfYear();
			int monthTo = to.getMonthOfYear();

			return year > yearTo || (year == yearTo && month > monthTo);

		}
	}

	@Override
	public boolean hasPrev() {

		LocalDate minDate = getMinDate();
		if (minDate == null) {
			return true;
		} else {

			LocalDate from = getFrom();
			int year = minDate.getYear();
			int yearFrom = from.getYear();

			int month = minDate.getMonthOfYear();
			int monthFrom = from.getMonthOfYear();

			return year < yearFrom || (year == yearFrom && month < monthFrom);

		}
	}

	@Override
	public boolean next() {

		if (hasNext()) {
			LocalDate date = getFrom().withDayOfMonth(1);
			date = date.plusMonths(1);
			//月第一天
			setFrom(date);
			
			//月最后一天
			int max = date.dayOfMonth().getMaximumValue();
			date = date.withDayOfMonth(max);
			setTo(date);

			build();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean prev() {

		if (hasPrev()) {
			LocalDate date = getFrom().minusMonths(1);
			date = date.withDayOfMonth(1);
			
			setFrom(date);
			
			int max = date.dayOfMonth().getMaximumValue();
			date = date.withDayOfMonth(max);
			
			setTo(date);

			build();
			return true;
		} else {
			return false;
		}

	}

	@Override
	public int getType() {
		return TYPE_MONTH;
	}

	@Override
	public void deselect(LocalDate date) {
		if (date != null && isSelected() && isInView(date)) {
			for (Week week : mWeeks) {
				if (week.isSelected() && week.isIn(date)) {
					mSelectedIndex = -1;
					setSelected(false);
					week.deselect(date);
				}
			}
		}
	}

	@Override
	public boolean select(LocalDate date) {
		int cnt = mWeeks.size();
		for (int i = 0; i < cnt; i++) {
			Week week = mWeeks.get(i);
			if (week.select(date)) {
				mSelectedIndex = i;
				setSelected(true);
				return true;
			}
		}
		return false;
	}

	public List<Week> getWeeks() {
		return mWeeks;
	}

	public int getSelectedIndex() {
		return mSelectedIndex;
	}

	@Override
	public void build() {

		setSelected(false);
		mWeeks.clear();

		LocalDate date = getFrom().withDayOfMonth(1).withDayOfWeek(1);
		for (int i = 0; i == 0 || getTo().compareTo(date) >= 0 || i<6; i++) {
			mWeeks.add(new Week(date, getToday(), getMinDate(), getMaxDate()));
			date = date.plusWeeks(1);
		}

	}

	@Override
	LocalDate getFirstDateOfCurrentMonth(LocalDate currentMonth) {

		if (currentMonth != null) {
			int year = currentMonth.getYear();
			int month = currentMonth.getMonthOfYear();

			LocalDate from = getFirstEnabled();
			int fromYear = from.getYear();
			int fromMonth = from.getMonthOfYear();

			if (year == fromYear && month == fromMonth) {
				return from;
			}
		}

		return null;

	}
}
