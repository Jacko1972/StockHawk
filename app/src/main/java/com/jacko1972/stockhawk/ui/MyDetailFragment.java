package com.jacko1972.stockhawk.ui;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.jacko1972.stockhawk.R;
import com.jacko1972.stockhawk.data.HistoryColumns;
import com.jacko1972.stockhawk.data.StockProvider;
import com.jacko1972.stockhawk.model.Quote;

import java.util.ArrayList;
import java.util.Collections;


public class MyDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //private static final String TAG = "MyDetailFragment";
    private static final int HISTORY_CURSOR_LOADER_ID = 2;
    private Quote quote;
    private boolean mTwoPane;
    private CandleStickChart candleStickChart;
    private Activity mActivity;
    private ArrayList<String> labels;

    public static MyDetailFragment newInstance(Quote quote, boolean mTwoPane) {
        MyDetailFragment fragment = new MyDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("quote", quote);
        args.putBoolean("mTwoPane", mTwoPane);
        fragment.setArguments(args);
        return fragment;
    }


    public MyDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (quote != null) {
            getLoaderManager().initLoader(HISTORY_CURSOR_LOADER_ID, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_detail, container, false);
        candleStickChart = (CandleStickChart) rootView.findViewById(R.id.chart);
        candleStickChart.getLegend().setEnabled(false);
        candleStickChart.setMaxVisibleValueCount(10);
        candleStickChart.setPinchZoom(false);
        candleStickChart.setDrawGridBackground(false);
        XAxis xAxis = candleStickChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(iAxisValueFormatter);
        YAxis leftAxis = candleStickChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        if (getArguments() != null) {
            quote = getArguments().getParcelable("quote");
            mTwoPane = getArguments().getBoolean("mTwoPane");
        }
        if (savedInstanceState != null) {
            quote = savedInstanceState.getParcelable("quote");
            mTwoPane = savedInstanceState.getBoolean("mTwoPane");
        }

        if (quote != null) {
            if (quote.getName() != null) {
                Description description = new Description();
                description.setText(quote.getName());
                candleStickChart.setDescription(description);
            }
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getLoaderManager().getLoader(HISTORY_CURSOR_LOADER_ID) == null && quote != null) {
            getLoaderManager().restartLoader(HISTORY_CURSOR_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mActivity, StockProvider.History.withSymbol(quote.getSymbol()),
                new String[]{HistoryColumns.DATE, HistoryColumns.CLOSE, HistoryColumns.HIGH, HistoryColumns.LOW, HistoryColumns.OPEN, HistoryColumns.SYMBOL},
                null,
                null,
                null);
    }

    private final IAxisValueFormatter iAxisValueFormatter = new IAxisValueFormatter() {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return labels.get((int) value);
        }

        @Override
        public int getDecimalDigits() {
            return 0;
        }
    };

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == HISTORY_CURSOR_LOADER_ID) {
            if (data.getCount() > 0) {
                data.moveToFirst();
                ArrayList<CandleEntry> list = new ArrayList<>();
                labels = new ArrayList<>();
                int a = 0;
                do {
                    String historyDate = data.getString(data.getColumnIndex(HistoryColumns.DATE));
                    String[] splitHistoryDate = historyDate.split("-");
                    labels.add(splitHistoryDate[2] + "/" + splitHistoryDate[1]);
                    CandleEntry entry = new CandleEntry(a,
                            Float.parseFloat(data.getString(data.getColumnIndex(HistoryColumns.HIGH))),
                            Float.parseFloat(data.getString(data.getColumnIndex(HistoryColumns.LOW))),
                            Float.parseFloat(data.getString(data.getColumnIndex(HistoryColumns.OPEN))),
                            Float.parseFloat(data.getString(data.getColumnIndex(HistoryColumns.CLOSE))));
                    list.add(entry);
                    a++;
                } while (data.moveToNext());
                Collections.sort(list, new EntryXComparator());
                CandleDataSet candleDataSet = new CandleDataSet(list, "History");
                candleDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                candleDataSet.setShadowColor(Color.DKGRAY);
                candleDataSet.setShadowWidth(0.7f);
                candleDataSet.setDecreasingColor(Color.RED);
                candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);
                candleDataSet.setIncreasingColor(Color.GREEN);
                candleDataSet.setIncreasingPaintStyle(Paint.Style.STROKE);
                candleDataSet.setNeutralColor(Color.BLUE);
                candleDataSet.setHighlightEnabled(false);
                CandleData candleData = new CandleData(candleDataSet);
                candleStickChart.setData(candleData);
            } else {
                candleStickChart.setData(null);
            }
            candleStickChart.notifyDataSetChanged();
            candleStickChart.invalidate();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        candleStickChart.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (quote != null) {
            outState.putParcelable("quote", quote);
            outState.putBoolean("mTwoPane", mTwoPane);
        }
        super.onSaveInstanceState(outState);
    }
}
