package com.jacko1972.stockhawk.utilities;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.jacko1972.stockhawk.R;
import com.jacko1972.stockhawk.data.HistoryColumns;
import com.jacko1972.stockhawk.data.QuoteColumns;
import com.jacko1972.stockhawk.data.StockProvider;
import com.jacko1972.stockhawk.data.StockProvider.History;
import com.jacko1972.stockhawk.data.StockProvider.Quotes;
import com.jacko1972.stockhawk.model.HistoryQuery;
import com.jacko1972.stockhawk.model.HistoryQuote;
import com.jacko1972.stockhawk.model.HistoryResults;
import com.jacko1972.stockhawk.model.Query;
import com.jacko1972.stockhawk.model.Quote;
import com.jacko1972.stockhawk.model.Results;
import com.jacko1972.stockhawk.model.StockHistoryResponseModel;
import com.jacko1972.stockhawk.model.StockResponseModel;
import com.jacko1972.stockhawk.service.StockHawkDbInterface;
import com.jacko1972.stockhawk.service.StockHawkDbService;
import com.jacko1972.stockhawk.sync.StockHawkSyncAdapter;
import com.jacko1972.stockhawk.sync.StockHawkSyncAdapter.StockHawkStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Utils {

    private static final String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList<ContentProviderOperation> quoteStockJsonToContentVals(Results results) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        for (Quote quote : results.getQuote()) {
            String name = quote.getName();
            if (null != name) {
                batchOperations.add(buildStockBatchOperation(quote));
            }
        }
        return batchOperations;
    }

    public static ArrayList<ContentProviderOperation> quoteHistoryJsonToContentVals(HistoryResults results) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        for (HistoryQuote quote : results.getQuote()) {
            batchOperations.add(buildStockHistoryBatchOperation(quote));
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format(Locale.US, "%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format(Locale.US, "%.2f", round);
        StringBuilder changeBuffer = new StringBuilder(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    private static ContentProviderOperation buildStockBatchOperation(Quote quote) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Quotes.CONTENT_URI);

        String change = quote.getChange();
        builder.withValue(QuoteColumns.SYMBOL, quote.getSymbol());
        builder.withValue(QuoteColumns.BID_PRICE, truncateBidPrice(quote.getBid()));
        builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(quote.getPercentChange(), true));
        builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
        builder.withValue(QuoteColumns.STOCK_NAME, quote.getName().replace("!", ""));
        return builder.build();
    }

    private static ContentProviderOperation buildStockHistoryBatchOperation(HistoryQuote history) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(History.CONTENT_URI);
        builder.withValue(HistoryColumns.SYMBOL, history.getSymbol());
        builder.withValue(HistoryColumns.HIGH, history.getHigh());
        builder.withValue(HistoryColumns.LOW, history.getLow());
        builder.withValue(HistoryColumns.OPEN, history.getOpen());
        builder.withValue(HistoryColumns.CLOSE, history.getClose());
        builder.withValue(HistoryColumns.DATE, history.getDate());
        builder.withValue(HistoryColumns.ADJ_CLOSE, history.getAdjClose());
        builder.withValue(HistoryColumns.VOLUME, history.getVolume());
        return builder.build();
    }

    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @SuppressWarnings("ResourceType")
    static public
    @StockHawkStatus
    int getStockHawkStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_stock_hawk_status), StockHawkSyncAdapter.STOCK_HAWK_STATUS_UNKNOWN);
    }

    static public void setStockHawkStatus(Context c, @StockHawkStatus int status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_stock_hawk_status), status);
        spe.apply();
    }

//    static public void resetStockHawkStatus(Context c) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
//        SharedPreferences.Editor spe = sp.edit();
//        spe.putInt(c.getString(R.string.pref_stock_hawk_status), StockHawkSyncAdapter.STOCK_HAWK_STATUS_UNKNOWN);
//        spe.apply();
//    }

    static public String daysFromToday(int days) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        c.add(Calendar.DATE, days);
        return simpleDateFormat.format(c.getTime());
    }

    public static void addStockSymbol(Context context, final String s) {
        final Context context1 = context;

        StockHawkDbInterface dbPopularInterface = StockHawkDbService.getClient().create(StockHawkDbInterface.class);
        final Call<StockResponseModel> makeStockCall = dbPopularInterface.getStocks("select Symbol, Name, Bid, Change, PercentChange from yahoo.finance.quotes where symbol in ('" + s + "')");
        makeStockCall.enqueue(new Callback<StockResponseModel>() {
                                  @Override
                                  public void onResponse(Call<StockResponseModel> call, Response<StockResponseModel> response) {
                                      if (response.isSuccessful()) {
                                          Query query = response.body().getQuery();
                                          ArrayList<ContentProviderOperation> contentProviderOperations;
                                          contentProviderOperations = Utils.quoteStockJsonToContentVals(query.getResults());
                                          if (contentProviderOperations.size() == 0) {
                                              Toast.makeText(context1, context1.getString(R.string.could_not_find_symbol) + s + context1.getString(R.string.please_try_again), Toast.LENGTH_LONG).show();
                                          } else {
                                              try {
                                                  context1.getContentResolver().applyBatch(StockProvider.AUTHORITY, contentProviderOperations);
                                                  addStockHistory(context1, s);
                                              } catch (RemoteException | OperationApplicationException e) {
                                                  e.printStackTrace();
                                              }
                                          }
                                      }
                                  }

                                  @Override
                                  public void onFailure(Call<StockResponseModel> call, Throwable t) {
                                      Log.d(LOG_TAG, "call: " + call);
                                      Log.d(LOG_TAG, "throws: " + t.toString());
                                  }
                              }
        );
    }

    private static void addStockHistory(Context context, final String s) {
        final Context context1 = context;
        String query = "select * from yahoo.finance.historicaldata where symbol = '" + s;
        int numOfDays = Utils.getHistoryDays(context) * -1;
        query = query + "' and startDate = '" + Utils.daysFromToday(numOfDays) + "' and endDate = '" + Utils.daysFromToday(0) + "'";
        StockHawkDbInterface dbPopularInterface = StockHawkDbService.getClient().create(StockHawkDbInterface.class);
        final Call<StockHistoryResponseModel> makeStockHistoryCall = dbPopularInterface.getStocksHistory(query);
        makeStockHistoryCall.enqueue(new Callback<StockHistoryResponseModel>() {
            @Override
            public void onResponse(Call<StockHistoryResponseModel> call, Response<StockHistoryResponseModel> response) {
                context1.getContentResolver().delete(StockProvider.History.CONTENT_URI, HistoryColumns.SYMBOL + " = ?", new String[]{s});
                HistoryQuery query = response.body().getQuery();
                if (query.getCount() > 0) {
                    ArrayList<ContentProviderOperation> contentProviderOperations;
                    contentProviderOperations = Utils.quoteHistoryJsonToContentVals(query.getResults());
                    try {
                        context1.getContentResolver().applyBatch(StockProvider.AUTHORITY, contentProviderOperations);
                    } catch (RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<StockHistoryResponseModel> call, Throwable t) {
                Log.d(LOG_TAG, "call: " + call);
                Log.d(LOG_TAG, "throws: " + t.toString());
            }
        });
    }

    public static int getHistoryDays(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return Integer.parseInt(sp.getString(c.getString(R.string.prefs_days_history), "30"));
    }
}
