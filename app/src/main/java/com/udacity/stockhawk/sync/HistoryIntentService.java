package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by DELL on 2/24/2017.
 */

public class HistoryIntentService extends IntentService {
    public HistoryIntentService() {
        super(QuoteIntentService.class.getSimpleName());
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        QuoteSyncJob.getQuotesHistory(getApplicationContext(),intent.getStringExtra("stock"));
    }
}
