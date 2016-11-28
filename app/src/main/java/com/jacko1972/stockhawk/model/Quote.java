package com.jacko1972.stockhawk.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.jacko1972.stockhawk.data.QuoteColumns;
import com.jacko1972.stockhawk.utilities.Utils;

public class Quote implements Parcelable {

    @SerializedName("id")
    private int id;
    @SerializedName("Symbol")
    private String symbol;
    @SerializedName("Bid")
    private String bid;
    @SerializedName("Change")
    private String change;
    @SerializedName("Name")
    private String name;
    @SerializedName("PercentChange")
    private String percentChange;

    protected Quote(Parcel in) {
        id = in.readInt();
        symbol = in.readString();
        bid = in.readString();
        change = in.readString();
        name = in.readString();
        percentChange = in.readString();
    }

    public static final Creator<Quote> CREATOR = new Creator<Quote>() {
        @Override
        public Quote createFromParcel(Parcel in) {
            return new Quote(in);
        }

        @Override
        public Quote[] newArray(int size) {
            return new Quote[size];
        }
    };

    public Quote() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    private void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getBid() {
        if (bid == null) {
            return "0.0";
        } else {
            return bid;
        }
    }

    private void setBid(String bid) {
        this.bid = bid;
    }

    public String getChange() {
        return change == null ? "+0.0" : change;
    }

    private void setChange(String change) {
        this.change = change;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getPercentChange() {
        return percentChange == null ? "+0.0%" : percentChange;
    }

    private void setPercentChange(String percentChange) {
        this.percentChange = percentChange;
    }

    public static ContentValues quoteToContentValues(Quote quote) {
        ContentValues values = new ContentValues();
        values.put(QuoteColumns._ID, quote.id);
        values.put(QuoteColumns.SYMBOL, quote.symbol);
        values.put(QuoteColumns.BID_PRICE, Utils.truncateBidPrice(quote.bid));
        values.put(QuoteColumns.CHANGE, Utils.truncateChange(quote.change, false));
        values.put(QuoteColumns.PERCENT_CHANGE, Utils.truncateChange(quote.percentChange, true));
        values.put(QuoteColumns.STOCK_NAME, quote.name);
        return values;
    }

    public static Quote quoteFromCursor(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            Quote quote = new Quote();
            quote.setBid(cursor.getString(cursor.getColumnIndex(QuoteColumns.BID_PRICE)));
            quote.setChange(cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE)));
            quote.setName(cursor.getString(cursor.getColumnIndex(QuoteColumns.STOCK_NAME)));
            quote.setPercentChange(cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
            quote.setSymbol(cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));
            return quote;
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(symbol);
        parcel.writeString(bid);
        parcel.writeString(change);
        parcel.writeString(name);
        parcel.writeString(percentChange);
    }
}