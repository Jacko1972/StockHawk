package com.jacko1972.stockhawk.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@SuppressWarnings("WeakerAccess")
@Database(version = StockDatabase.VERSION)
class StockDatabase {
    private StockDatabase() {
    }

    public static final int VERSION = 1;

    @Table(QuoteColumns.class)
    public static final String QUOTES = "quotes";

    @Table(HistoryColumns.class)
    public static final String HISTORY = "history";
}
