package com.jacko1972.stockhawk.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryResults {

    @SerializedName("quote")
    private List<HistoryQuote> quote = new ArrayList<>();

    public HistoryResults(HistoryQuote ... historyQuotes) {
        quote = Arrays.asList(historyQuotes);
    }

    public List<HistoryQuote> getQuote() {
        return quote;
    }

    public void setQuote(List<HistoryQuote> quote) {
        this.quote = quote;
    }
}
