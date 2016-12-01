package com.jacko1972.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jacko1972.stockhawk.R;
import com.jacko1972.stockhawk.data.QuoteColumns;
import com.jacko1972.stockhawk.data.StockProvider;
import com.jacko1972.stockhawk.model.Quote;
import com.jacko1972.stockhawk.sync.StockHawkSyncAdapter;
import com.jacko1972.stockhawk.utilities.Utils;

public class MyStocksActivity extends AppCompatActivity implements MyStocksFragment.CallBack {

    //private static final String TAG = "MyStocksActivity";
    private static final int SETTINGS_REQUEST_CODE = 1;
    private Context mContext;
    private boolean isConnected;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        isConnected = Utils.isNetworkAvailable(this);
        setContentView(R.layout.activity_my_stocks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.stock_detail_container) != null) {
            mTwoPane = true;
            if (getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_stock_detail_tag)) == null) {
                MyDetailFragment fragment = MyDetailFragment.newInstance(null, true);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.stock_detail_container, fragment, getString(R.string.fragment_stock_detail_tag))
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        StockHawkSyncAdapter.initializeSyncAdapter(this);

        MyStocksFragment myStocksFragment = MyStocksFragment.newInstance(mTwoPane);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_my_stocks, myStocksFragment).commit();
        setTitle(R.string.app_name);
    }

    private void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            return true;
        }

        if (id == R.id.refresh_list) {
            if (isConnected) {
                StockHawkSyncAdapter.syncImmediately(this);
            } else networkToast();
        }

        if (id == R.id.action_change_units) {
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(StockProvider.Quotes.CONTENT_URI, null);
        }

        if (id == R.id.add_new_symbol) {
            if (isConnected) {
                new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                        .content(R.string.content_test)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(R.string.input_hint, R.string.input_pre_fill, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                // On click, receive user input. Make sure the stock doesn't already exist
                                // in the DB and proceed accordingly
                                Cursor c = getContentResolver().query(StockProvider.Quotes.CONTENT_URI,
                                        new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                        new String[]{input.toString()}, null);
                                if (c != null && c.getCount() != 0) {
                                    Toast toast = Toast.makeText(MyStocksActivity.this, R.string.this_stock_is_already_saved, Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                    toast.show();
                                } else {
                                    addSymbol(input.toString());
                                }
                                if (c != null) {
                                    c.close();
                                }
                            }
                        })
                        .show();
            } else {
                networkToast();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_REQUEST_CODE) {
            StockHawkSyncAdapter.syncImmediately(this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addSymbol(String s) {
        Utils.addStockSymbol(this, s);
        Toast toast = Toast.makeText(MyStocksActivity.this, R.string.adding_stock_symbol_please_wait, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
        toast.show();
    }

    @Override
    public void onItemSelected(Quote quote) {
        if (mTwoPane) {
            MyDetailFragment fragment = MyDetailFragment.newInstance(quote, true);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.stock_detail_container, fragment, getString(R.string.fragment_stock_detail_tag))
                    .commit();
        } else {
            Intent intent = new Intent(this, MyDetailActivity.class);
            intent.putExtra("quote", quote);
            startActivity(intent);
        }
    }
}


