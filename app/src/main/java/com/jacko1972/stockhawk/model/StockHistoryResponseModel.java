package com.jacko1972.stockhawk.model;

import com.google.gson.annotations.SerializedName;

public class StockHistoryResponseModel {

    @SerializedName("query")
    private HistoryQuery query;

    public HistoryQuery getQuery() {
        return query;
    }

    public void setQuery(HistoryQuery query) {
        this.query = query;
    }
}
