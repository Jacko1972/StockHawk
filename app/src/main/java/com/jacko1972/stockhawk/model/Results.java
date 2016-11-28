package com.jacko1972.stockhawk.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Results {

    @SerializedName("quote")
    private List<Quote> quote = new ArrayList<>();

    public List<Quote> getQuote() {
        return quote;
    }

    public Results(Quote ... qs) {
        quote = Arrays.asList(qs);
    }

    public void setQuote(List<Quote> quote) {
        this.quote = quote;
    }
}
