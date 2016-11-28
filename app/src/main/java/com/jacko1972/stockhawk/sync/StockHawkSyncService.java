package com.jacko1972.stockhawk.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class StockHawkSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static StockHawkSyncAdapter stockHawkSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("SyncService", "onCreate - StockHawkSyncService");
        synchronized (sSyncAdapterLock) {
            if (stockHawkSyncAdapter == null) {
                stockHawkSyncAdapter = new StockHawkSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stockHawkSyncAdapter.getSyncAdapterBinder();
    }
}