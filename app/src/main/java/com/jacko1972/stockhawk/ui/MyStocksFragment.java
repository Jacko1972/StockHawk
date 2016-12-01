package com.jacko1972.stockhawk.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jacko1972.stockhawk.R;
import com.jacko1972.stockhawk.data.QuoteColumns;
import com.jacko1972.stockhawk.data.StockProvider;
import com.jacko1972.stockhawk.model.Quote;
import com.jacko1972.stockhawk.rest.QuoteCursorAdapter;
import com.jacko1972.stockhawk.rest.RecyclerViewItemClickListener;
import com.jacko1972.stockhawk.sync.StockHawkSyncAdapter;
import com.jacko1972.stockhawk.touch_helper.SimpleItemTouchHelperCallback;
import com.jacko1972.stockhawk.utilities.Utils;

public class MyStocksFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor> {

    //private static final String TAG = "MyStocksFragment";
    private static final int MSG_RELOAD_STOCK_CHART = 999;

    private Activity mActivity;
    private QuoteCursorAdapter mCursorAdapter;
    private static final int CURSOR_LOADER_ID = 0;
    private boolean mTwoPane;
    private TextView emptyView;
    private Quote initialQuote;

    public MyStocksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    public static MyStocksFragment newInstance(boolean mTwoPane) {
        MyStocksFragment frag = new MyStocksFragment();
        Bundle args = new Bundle();
        args.putBoolean("mTwoPane", mTwoPane);
        frag.setArguments(args);
        return frag;
    }

    public interface CallBack {
        void onItemSelected(Quote quote);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
        sp.registerOnSharedPreferenceChangeListener(this);
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(mActivity, StockProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.STOCK_NAME, QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BID_PRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE},
                null,
                null,
                QuoteColumns._ID + mActivity.getString(R.string.query_ascending));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //DatabaseUtils.dumpCursor(data);
        if (!Utils.isNetworkAvailable(mActivity)) {
            Utils.setStockHawkStatus(mActivity, StockHawkSyncAdapter.STOCK_HAWK_STATUS_UNKNOWN);
        } else if (data.getCount() == 0) {
            Utils.setStockHawkStatus(mActivity, StockHawkSyncAdapter.STOCK_HAWK_STATUS_EMPTY_LIST);
        } else if (mTwoPane) {
            data.moveToFirst();
            initialQuote = Quote.quoteFromCursor(data);
            handler.sendEmptyMessage(MSG_RELOAD_STOCK_CHART);
            //((CallBack) mActivity).onItemSelected(initialQuote);
        }
        mCursorAdapter.swapCursor(data);
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detail_my_stocks, container, false);

        if (getArguments() != null) {
            mTwoPane = getArguments().getBoolean("mTwoPane");
        }

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        emptyView = (TextView) rootView.findViewById(R.id.recyclerview_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mCursorAdapter = new QuoteCursorAdapter(mActivity, null, emptyView);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(mActivity,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        Cursor cursor = mCursorAdapter.getCursor();
                        cursor.moveToPosition(position);
                        Quote quote = Quote.quoteFromCursor(cursor);
                        ((CallBack) mActivity).onItemSelected(quote);
                    }
                }));
        recyclerView.setAdapter(mCursorAdapter);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        return rootView;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_stock_hawk_status))) {
            updateEmptyView();
        }
    }

    private void updateEmptyView() {
        if (mCursorAdapter.getItemCount() == 0) {
            if (null != emptyView) {
                // if cursor is empty, why? do we have an invalid status
                int message = R.string.empty_stock_hawk_list;
                @StockHawkSyncAdapter.StockHawkStatus int status = Utils.getStockHawkStatus(getActivity());
                switch (status) {
                    case StockHawkSyncAdapter.STOCK_HAWK_STATUS_SERVER_DOWN:
                        message = R.string.empty_stock_hawk_list_server_down;
                        break;
                    case StockHawkSyncAdapter.STOCK_HAWK_STATUS_SERVER_INVALID:
                        message = R.string.empty_stock_hawk_list_server_error;
                        break;
                    case StockHawkSyncAdapter.STOCK_HAWK_STATUS_UNKNOWN:
                        message = R.string.empty_stock_hawk_list_no_network;
                        break;
                    case StockHawkSyncAdapter.STOCK_HAWK_STATUS_EMPTY_LIST:
                        message = R.string.empty_stock_hawk_list_no_data;
                        break;
                    case StockHawkSyncAdapter.STOCK_HAWK_STATUS_OK:
                        break;
                    case StockHawkSyncAdapter.STOCK_HAWK_STATUS_WORKING:
                        message = R.string.sync_adapter_is_working;
                        break;
                    default:
                        if (!Utils.isNetworkAvailable(getActivity())) {
                            message = R.string.empty_stock_hawk_list_no_network;
                        }


                }
                emptyView.setText(message);
            }
        }
    }
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_RELOAD_STOCK_CHART) {
                if (initialQuote != null) {
                    ((CallBack) mActivity).onItemSelected(initialQuote);
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
