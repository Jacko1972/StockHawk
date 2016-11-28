package com.jacko1972.stockhawk.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.jacko1972.stockhawk.R;
import com.jacko1972.stockhawk.data.HistoryColumns;
import com.jacko1972.stockhawk.data.QuoteColumns;
import com.jacko1972.stockhawk.data.StockProvider;
import com.jacko1972.stockhawk.model.HistoryQuery;
import com.jacko1972.stockhawk.model.Query;
import com.jacko1972.stockhawk.model.StockHistoryResponseModel;
import com.jacko1972.stockhawk.model.StockResponseModel;
import com.jacko1972.stockhawk.service.StockHawkDbInterface;
import com.jacko1972.stockhawk.service.StockHawkDbService;
import com.jacko1972.stockhawk.utilities.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockHawkSyncAdapter extends AbstractThreadedSyncAdapter {
    private final String TAG = com.jacko1972.stockhawk.sync.StockHawkSyncAdapter.class.getSimpleName();
    private static final int SYNC_INTERVAL = 60 * 60;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final String ACTION_DATA_UPDATED = "com.jacko1972.stockhawk.app.ACTION_DATA_UPDATED";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STOCK_HAWK_STATUS_OK, STOCK_HAWK_STATUS_SERVER_DOWN, STOCK_HAWK_STATUS_SERVER_INVALID, STOCK_HAWK_STATUS_UNKNOWN, STOCK_HAWK_STATUS_EMPTY_LIST, STOCK_HAWK_STATUS_WORKING})
    public @interface StockHawkStatus {
    }

    public static final int STOCK_HAWK_STATUS_OK = 0;
    public static final int STOCK_HAWK_STATUS_SERVER_DOWN = 1;
    public static final int STOCK_HAWK_STATUS_SERVER_INVALID = 2;
    public static final int STOCK_HAWK_STATUS_UNKNOWN = 3;
    public static final int STOCK_HAWK_STATUS_EMPTY_LIST = 4;
    public static final int STOCK_HAWK_STATUS_WORKING = 5;

    public StockHawkSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        //Log.d(TAG, "Starting sync");
        setStockHawkStatus(getContext(), STOCK_HAWK_STATUS_WORKING);
        String[] symbols;
        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append("select Symbol, Name, Bid, Change, PercentChange from yahoo.finance.quotes where symbol in (");
        StringBuilder mStoredSymbols = new StringBuilder();
        Cursor initQueryCursor = getContext().getContentResolver().query(StockProvider.Quotes.CONTENT_URI,
                new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                null, null);
        //DatabaseUtils.dumpCursor(initQueryCursor);
        if (initQueryCursor == null || initQueryCursor.getCount() == 0) {
            urlStringBuilder.append("'YHOO','AAPL','GOOG','MSFT')");
            symbols = new String[]{"YHOO", "AAPL", "GOOG", "MSFT"};
        } else {
            initQueryCursor.moveToFirst();
            symbols = new String[initQueryCursor.getCount()];
            for (int i = 0; i < initQueryCursor.getCount(); i++) {
                mStoredSymbols.append("'").append(initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol"))).append("',");
                symbols[i] = initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol"));
                initQueryCursor.moveToNext();
            }
            mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
            urlStringBuilder.append(mStoredSymbols.toString());
        }

        if (initQueryCursor != null) {
            initQueryCursor.close();
        }

        StockHawkDbInterface dbPopularInterface = StockHawkDbService.getClient().create(StockHawkDbInterface.class);
        final Call<StockResponseModel> makeStockCall = dbPopularInterface.getStocks(urlStringBuilder.toString());
        makeStockCall.enqueue(new Callback<StockResponseModel>() {
                                  @Override
                                  public void onResponse(Call<StockResponseModel> call, Response<StockResponseModel> response) {
                                      if (response.isSuccessful()) {
                                          getContext().getContentResolver().delete(StockProvider.Quotes.CONTENT_URI, null, null);
                                          Query query = response.body().getQuery();
                                          ArrayList<ContentProviderOperation> contentProviderOperations;
                                          contentProviderOperations = Utils.quoteStockJsonToContentVals(query.getResults());
                                          try {
                                              getContext().getContentResolver().applyBatch(StockProvider.AUTHORITY, contentProviderOperations);
                                              setStockHawkStatus(getContext(), STOCK_HAWK_STATUS_OK);
                                              Intent dataUpdated = new Intent(ACTION_DATA_UPDATED);
                                              getContext().sendBroadcast(dataUpdated);
                                          } catch (RemoteException | OperationApplicationException e) {
                                              setStockHawkStatus(getContext(), STOCK_HAWK_STATUS_SERVER_INVALID);
                                              e.printStackTrace();
                                          }
                                      } else {
                                          setStockHawkStatus(getContext(), STOCK_HAWK_STATUS_SERVER_INVALID);
                                      }
                                  }

                                  @Override
                                  public void onFailure(Call<StockResponseModel> call, Throwable t) {
                                      setStockHawkStatus(getContext(), STOCK_HAWK_STATUS_SERVER_DOWN);
                                      Log.d(TAG, "call: " + call);
                                      Log.d(TAG, "throws: " + t.toString());
                                  }
                              }
        );

        for (String symbol : symbols) {
            final String innerSymbol = symbol;
            String query = "select * from yahoo.finance.historicaldata where symbol = '" + symbol;
            int numOfDays = Utils.getHistoryDays(getContext()) * -1;
            query = query + "' and startDate = '" + Utils.daysFromToday(numOfDays) + "' and endDate = '" + Utils.daysFromToday(0) + "'";
            final Call<StockHistoryResponseModel> makeStockHistoryCall = dbPopularInterface.getStocksHistory(query);
            makeStockHistoryCall.enqueue(new Callback<StockHistoryResponseModel>() {
                @Override
                public void onResponse(Call<StockHistoryResponseModel> call, Response<StockHistoryResponseModel> response) {
                    getContext().getContentResolver().delete(StockProvider.History.CONTENT_URI, HistoryColumns.SYMBOL + " = ?", new String[]{innerSymbol});
                    HistoryQuery query = response.body().getQuery();
                    if (query.getCount() > 0) {
                        ArrayList<ContentProviderOperation> contentProviderOperations;
                        contentProviderOperations = Utils.quoteHistoryJsonToContentVals(query.getResults());
                        try {
                            getContext().getContentResolver().applyBatch(StockProvider.AUTHORITY, contentProviderOperations);
                        } catch (RemoteException | OperationApplicationException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<StockHistoryResponseModel> call, Throwable t) {
                    Log.d(TAG, "call: " + call);
                    Log.d(TAG, "throws: " + t.toString());
                }
            });
        }
    }

    private static void configurePeriodicSync(Context context) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(StockHawkSyncAdapter.SYNC_INTERVAL, StockHawkSyncAdapter.SYNC_FLEXTIME).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), StockHawkSyncAdapter.SYNC_INTERVAL);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    private static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        com.jacko1972.stockhawk.sync.StockHawkSyncAdapter.configurePeriodicSync(context);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @SuppressLint("CommitPrefEdits")
    static private void setStockHawkStatus(Context c, @StockHawkStatus int locationStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_stock_hawk_status), locationStatus);
        spe.commit();
    }
}