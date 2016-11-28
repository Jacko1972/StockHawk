package com.jacko1972.stockhawk.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.jacko1972.stockhawk.model.Quote;
import com.jacko1972.stockhawk.model.Results;

import java.io.IOException;

class StockHawkDbQuoteTypeAdapter extends TypeAdapter<Results> {

    private final Gson gson = new Gson();

    @Override
    public void write(JsonWriter out, Results value) throws IOException {
        gson.toJson(value, Results.class, out);
    }

    @Override
    public Results read(JsonReader in) throws IOException {
        Results results;

        in.beginObject();
        in.nextName();

        if (in.peek() == JsonToken.BEGIN_ARRAY) {
            results = new Results((Quote[]) gson.fromJson(in, Quote[].class));
        } else if (in.peek() == JsonToken.BEGIN_OBJECT) {
            results = new Results((Quote) gson.fromJson(in, Quote.class));
        } else {
            throw new JsonParseException("Unexpected token " + in.peek());
        }
        in.endObject();
        return results;
    }
}
