package com.gearback.zt.persiancalendar.core.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import com.gearback.zt.calendarcore.core.Constants;
import com.gearback.zt.calendarcore.core.models.Day;
import com.gearback.zt.calendarcore.core.models.PersianDate;
import com.gearback.zt.persiancalendar.R;
import com.gearback.zt.persiancalendar.core.PersianCalendarHandler;
import com.gearback.zt.persiancalendar.core.adapters.MonthAdapter;

public class MonthFragment extends Fragment {
    private PersianCalendarHandler mPersianCalendarHandler;
    private CalendarFragment mCalendarFragment;
    private PersianDate mPersianDate;
    private int mOffset;
    private MonthAdapter mMonthAdapter;
    private RecyclerView recyclerView;

    private BroadcastReceiver updateEventsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateMonth();
        }
    };

    private BroadcastReceiver setCurrentMonthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int value = intent.getExtras().getInt(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT);
            if (value == mOffset) {
                if(mPersianCalendarHandler.getOnMonthChangedListener() != null)
                    mPersianCalendarHandler.getOnMonthChangedListener().onPersianChanged(mPersianDate);
                int day = intent.getExtras().getInt(Constants.BROADCAST_FIELD_SELECT_DAY);
                if (day != -1) {
                    mMonthAdapter.selectDay(day);
                }
            } else if (value == Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY) {
                mMonthAdapter.clearSelectedDay();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPersianCalendarHandler = PersianCalendarHandler.getInstance(getContext());
        View view = inflater.inflate(R.layout.fragment_month, container, false);
        recyclerView = view.findViewById(R.id.month_recycler);
        mOffset = getArguments().getInt(Constants.OFFSET_ARGUMENT);

        UpdateMonth();

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(setCurrentMonthReceiver, new IntentFilter(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(updateEventsReceiver, new IntentFilter(Constants.BROADCAST_UPDATE_EVENTS));

        return view;
    }

    private void UpdateMonth() {
        List<Day> days = mPersianCalendarHandler.getDays(mOffset);

        mPersianDate = mPersianCalendarHandler.getToday();
        int month = mPersianDate.getMonth() - mOffset;
        month -= 1;
        int year = mPersianDate.getYear();

        year = year + (month / 12);
        month = month % 12;
        if (month < 0) {
            year -= 1;
            month += 12;
        }

        month += 1;
        mPersianDate.setMonth(month);
        mPersianDate.setYear(year);
        mPersianDate.setDayOfMonth(1);

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        recyclerView.setLayoutManager(layoutManager);
        mMonthAdapter = new MonthAdapter(getContext(), this, days);
        recyclerView.setAdapter(mMonthAdapter);

        mCalendarFragment = (CalendarFragment) getActivity().getSupportFragmentManager().findFragmentByTag(CalendarFragment.class.getName());

        if (mOffset == 0 && mCalendarFragment.getViewPagerPosition() == mOffset) {
            // mCalendarFragment.selectDay(mPersianCalendarHandler.getToday());
            // updateTitle();
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(setCurrentMonthReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(updateEventsReceiver);
        super.onDestroy();
    }

    public void onClickItem(PersianDate day) {
        //mCalendarFragment.selectDay(day);
        if (mPersianCalendarHandler.getOnDayClickedListener() != null)
            mPersianCalendarHandler.getOnDayClickedListener().onPersianClick(day);
    }

    public void onLongClickItem(PersianDate day) {
        if (mPersianCalendarHandler.getOnDayLongClickedListener() != null)
            mPersianCalendarHandler.getOnDayLongClickedListener().onPersianLongClick(day);

        //mCalendarFragment.addEventOnCalendar(day);
    }
}
