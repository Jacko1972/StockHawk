package com.jacko1972.stockhawk.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.jacko1972.stockhawk.R;
import com.jacko1972.stockhawk.data.QuoteColumns;
import com.jacko1972.stockhawk.data.StockProvider;
import com.jacko1972.stockhawk.model.Quote;

class DetailWidgetRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private Cursor data = null;
    private final Context mContext;
    private final int mWidgetId;

    public DetailWidgetRemoteViewFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        if (data != null) {
            data.close();
        }
        // This method is called by the app hosting the widget (e.g., the launcher)
        // However, our ContentProvider is not exported so it doesn't have access to the
        // data. Therefore we need to clear (and finally restore) the calling identity so
        // that calls use our process and permission
        final long identityToken = Binder.clearCallingIdentity();
        data = mContext.getContentResolver().query(StockProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.STOCK_NAME, QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BID_PRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE},
                null,
                null,
                QuoteColumns._ID + " ASC");
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        if (i == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(i)) {
            return null;
        }
        int widgetWidth = getWidgetWidth(mContext, AppWidgetManager.getInstance(mContext), mWidgetId);
        int defaultWidth = mContext.getResources().getDimensionPixelSize(R.dimen.widget_default_detail_width);
        int layoutId;
        if (widgetWidth < defaultWidth) {
            layoutId = R.layout.widget_detail_small_list_item;
        } else {
            layoutId = R.layout.widget_detail_list_item;
        }
        RemoteViews views = new RemoteViews(mContext.getPackageName(), layoutId);
        Quote quote = Quote.quoteFromCursor(data);
        if (layoutId == R.layout.widget_detail_list_item) {
            views.setTextViewText(R.id.widget_change, quote.getChange());
        } else {
            if (quote.getChange().charAt(0) == '-') {
                views.setImageViewResource(R.id.widget_trend_image, R.drawable.ic_trending_down);
                views.setContentDescription(R.id.widget_trend_image, mContext.getString(R.string.trending_down));
            } else if (quote.getChange().charAt(0) == '+') {
                views.setImageViewResource(R.id.widget_trend_image, R.drawable.ic_trending_up);
                views.setContentDescription(R.id.widget_trend_image, mContext.getString(R.string.trending_up));
            } else {
                views.setImageViewResource(R.id.widget_trend_image, R.drawable.ic_trending_flat);
                views.setContentDescription(R.id.widget_trend_image, mContext.getString(R.string.no_change));
            }
        }
        views.setTextViewText(R.id.widget_stock_symbol, quote.getSymbol());
        views.setTextViewText(R.id.widget_bid_price, quote.getBid());
        views.setContentDescription(R.id.widget_stock_symbol, mContext.getString(R.string.stock) + quote.getName());
        views.setContentDescription(R.id.widget_bid_price, mContext.getString(R.string.bid_price) + quote.getBid());
        views.setContentDescription(R.id.widget_change, mContext.getString(R.string.change) + quote.getChange());

        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra("quote", quote);
        views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

        return views;

    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.widget_detail_small_list_item);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int i) {
        if (data.moveToPosition(i)) {
            return data.getLong(data.getColumnIndex(QuoteColumns._ID));
        }
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private int getWidgetWidth(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return context.getResources().getDimensionPixelSize(R.dimen.widget_default_detail_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(context, appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return context.getResources().getDimensionPixelSize(R.dimen.widget_default_detail_width);
    }

}
