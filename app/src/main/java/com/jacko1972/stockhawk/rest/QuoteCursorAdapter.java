package com.jacko1972.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jacko1972.stockhawk.R;
import com.jacko1972.stockhawk.data.QuoteColumns;
import com.jacko1972.stockhawk.data.StockProvider;
import com.jacko1972.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.jacko1972.stockhawk.touch_helper.ItemTouchHelperViewHolder;
import com.jacko1972.stockhawk.utilities.Utils;

/**
 * Created by sam_chordas on 10/6/15.
 * Credit to skyfishjy gist:
 * https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private final Context mContext;
    private static Typeface robotoLight;

    public QuoteCursorAdapter(Context context, Cursor cursor, View emptyView) {
        super(context, cursor, emptyView);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_quote, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        viewHolder.symbol.setText(cursor.getString(cursor.getColumnIndex("symbol")));
        viewHolder.symbol.setContentDescription(mContext.getString(R.string.stock) + cursor.getString(cursor.getColumnIndex("name")));
        viewHolder.bidPrice.setText(cursor.getString(cursor.getColumnIndex("bid_price")));
        viewHolder.bidPrice.setContentDescription(mContext.getString(R.string.bid_price) + cursor.getString(cursor.getColumnIndex("bid_price")));
        if (cursor.getString(cursor.getColumnIndex("change")).charAt(0) == '-') {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                //noinspection deprecation
                viewHolder.change.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            } else {
                //noinspection deprecation
                viewHolder.change.setBackground(mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                //noinspection deprecation
                viewHolder.change.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            } else {
                //noinspection deprecation
                viewHolder.change.setBackground(mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            }
        }
        if (Utils.showPercent) {
            viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("percent_change")));
            viewHolder.change.setContentDescription(mContext.getString(R.string.change) + cursor.getString(cursor.getColumnIndex("percent_change")));
        } else {
            viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("change")));
            viewHolder.change.setContentDescription(mContext.getString(R.string.change) + cursor.getString(cursor.getColumnIndex("change")));
        }
    }

    @Override
    public void onItemDismiss(int position) {
        Cursor c = getCursor();
        c.moveToPosition(position);
        String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
        mContext.getContentResolver().delete(StockProvider.Quotes.withSymbol(symbol), null, null);
        mContext.getContentResolver().delete(StockProvider.History.withSymbol(symbol), null, null);
        notifyItemRemoved(position);
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder, View.OnClickListener {
        public final TextView symbol;
        public final TextView bidPrice;
        public final TextView change;

        public ViewHolder(View itemView) {
            super(itemView);
            symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
            symbol.setTypeface(robotoLight);
            bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
            change = (TextView) itemView.findViewById(R.id.change);
        }

        @Override
        public void onItemSelected() {
            //itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            //itemView.setBackgroundColor(0);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
