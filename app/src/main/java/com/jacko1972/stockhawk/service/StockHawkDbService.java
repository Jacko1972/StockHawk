package com.jacko1972.stockhawk.service;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jacko1972.stockhawk.model.HistoryResults;
import com.jacko1972.stockhawk.model.Results;

import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StockHawkDbService {

    private static final String STOCK_QUERY_BASE_URL = "https://query.yahooapis.com";
    private static Retrofit retrofit = null;


    public static Retrofit getClient() {
        if (retrofit == null) {

            OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
            //Uncomment three lines and the import above to enable logging for Retrofit API calls
            //HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            //httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            //okHttpClient.addInterceptor(httpLoggingInterceptor);

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Results.class, new StockHawkDbQuoteTypeAdapter())
                    .registerTypeAdapter(HistoryResults.class, new StockHawkDbHistoryQuoteTypeAdapter())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(STOCK_QUERY_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient.build())
                    .build();
        }
        return retrofit;
    }

}