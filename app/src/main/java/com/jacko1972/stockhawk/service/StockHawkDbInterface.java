package com.jacko1972.stockhawk.service;

import com.jacko1972.stockhawk.model.StockHistoryResponseModel;
import com.jacko1972.stockhawk.model.StockResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface StockHawkDbInterface {

    @GET("/v1/public/yql?format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=")
    Call<StockResponseModel> getStocks(@Query("q") String query);

    @GET("/v1/public/yql?format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=")
    Call<StockHistoryResponseModel> getStocksHistory(@Query("q") String query);
}
