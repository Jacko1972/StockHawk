package com.jacko1972.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class DetailWidgetRemoteViewsService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new DetailWidgetRemoteViewFactory(getApplicationContext(), intent);
    }
}
