package com.jacko1972.stockhawk.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.jacko1972.stockhawk.model.HistoryQuote;
import com.jacko1972.stockhawk.model.HistoryResults;
import com.jacko1972.stockhawk.model.Results;

import java.io.IOException;

class StockHawkDbHistoryQuoteTypeAdapter extends TypeAdapter<HistoryResults> {

    private final Gson gson = new Gson();

    @Override
    public void write(JsonWriter out, HistoryResults value) throws IOException {
        gson.toJson(value, Results.class, out);
    }

    @Override
    public HistoryResults read(JsonReader in) throws IOException {
        HistoryResults results;

        in.beginObject();
        in.nextName();

        if (in.peek() == JsonToken.BEGIN_ARRAY) {
            results = new HistoryResults((HistoryQuote[]) gson.fromJson(in, HistoryQuote[].class));
        } else if (in.peek() == JsonToken.BEGIN_OBJECT) {
            results = new HistoryResults((HistoryQuote) gson.fromJson(in, HistoryQuote.class));
        } else {
            throw new JsonParseException("Unexpected token " + in.peek());
        }
        in.endObject();
        return results;
    }
}
