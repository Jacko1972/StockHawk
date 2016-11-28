package com.jacko1972.stockhawk.model;

import com.google.gson.annotations.SerializedName;

public class StockResponseModel {
    @SerializedName("query")
    private Query query;


    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
}


