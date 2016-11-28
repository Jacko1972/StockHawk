package com.jacko1972.stockhawk.model;

import com.google.gson.annotations.SerializedName;

public class HistoryQuote {

    @SerializedName("Symbol")
    private String symbol;
    @SerializedName("Date")
    private String date;
    @SerializedName("Open")
    private String open;
    @SerializedName("High")
    private String high;
    @SerializedName("Low")
    private String low;
    @SerializedName("Close")
    private String close;
    @SerializedName("Volume")
    private String volume;
    @SerializedName("Adj_Close")
    private String adjClose;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getAdjClose() {
        return adjClose;
    }

    public void setAdjClose(String adjClose) {
        this.adjClose = adjClose;
    }
}
