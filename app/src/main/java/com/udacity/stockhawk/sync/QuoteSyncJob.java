package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.DbHelper;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.data.StockProvider;
import com.udacity.stockhawk.ui.StockWidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

import static android.os.Looper.getMainLooper;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    private QuoteSyncJob() {
    }

    public static ArrayList<String> getQuotesHistoryDates(final Context context,String index){


        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.MONTH, -YEARS_OF_HISTORY);
        ArrayList<String> historyDates = new ArrayList<>();


        try {
            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {

                return historyDates;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();



            while (iterator.hasNext()) {
                String symbol = iterator.next();

                if(symbol.equals(index)){

                    Stock stock = quotes.get(symbol);
                    final StockQuote quote = stock.getQuote();
                    if(quote.getPrice() != null ){

                        // WARNING! Don't request historical data for a stock that doesn't exist!
                        // The request will hang forever X_x
                        List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);


                        for (HistoricalQuote it : history) {
                            historyDates.add(it.getDate().getTime().getYear()+","
                                    +it.getDate().getTime().getMonth());


                        }



                    } else {
                        Handler mHandler = new Handler(getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, R.string.error_Invalid_stocks1 +quote.getSymbol()+R.string.error_Invalid_stocks2, Toast.LENGTH_SHORT).show();
                                PrefUtils.removeStock(context, quote.getSymbol());
                            }
                        });
                    }
                }



            }
            Log.d("size",historyDates.size()+"");
            return historyDates;


        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
        return historyDates;
    }
    public static ArrayList<Float> getQuotesHistory(final Context context,String index){
        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.MONTH, -YEARS_OF_HISTORY);
        ArrayList<Float> historyNumbers = new ArrayList<>();


        try {
            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return historyNumbers;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            if(quotes == null){
                Log.d("This is null","null");
            } else {
                Log.d("This is quotes",quotes.toString());
            }
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();



            while (iterator.hasNext()) {
                String symbol = iterator.next();

                if(symbol.equals(index)){

                    Stock stock = quotes.get(symbol);
                    final StockQuote quote = stock.getQuote();
                    if(quote.getPrice() != null ){

                        // WARNING! Don't request historical data for a stock that doesn't exist!
                        // The request will hang forever X_x
                        List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);


                        for (HistoricalQuote it : history) {
                            historyNumbers.add(Float.parseFloat(it.getClose()+""));

                        }




                    } else {
                        Handler mHandler = new Handler(getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, R.string.error_Invalid_stocks1 +quote.getSymbol()+R.string.error_Invalid_stocks2, Toast.LENGTH_SHORT).show();
                                PrefUtils.removeStock(context, quote.getSymbol());
                            }
                        });
                    }
                }



            }
            return historyNumbers;


        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
        return historyNumbers;
    }

    public static ArrayList<ContentValues> getInfoForWidget(final Context context){
        ArrayList<ContentValues> quoteCVs = new ArrayList<>();

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());


            if (stockArray.length == 0) {
                return quoteCVs;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());


            while (iterator.hasNext()) {
                String symbol = iterator.next();
                Log.d("This is the symbol",symbol);


                Stock stock = quotes.get(symbol);
                final StockQuote quote = stock.getQuote();
                if(quote.getPrice() != null ){
                    float price = quote.getPrice().floatValue();
                    float change = quote.getChange().floatValue();
                    float percentChange = quote.getChangeInPercent().floatValue();



                    ContentValues quoteCV = new ContentValues();
                    quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                    quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                    quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                    quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);

                    quoteCVs.add(quoteCV);
                } else {
                    Handler mHandler = new Handler(getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, R.string.error_Invalid_stocks1 +quote.getSymbol()+R.string.error_Invalid_stocks2, Toast.LENGTH_SHORT).show();
                            PrefUtils.removeStock(context,quote.getSymbol());
                        }
                    });
                }

            }

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }

        return quoteCVs;
    }





    static void getQuotes(final Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.MONTH, -YEARS_OF_HISTORY);

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            if(quotes == null){
                Log.d("This is null","null");
            } else {
                Log.d("This is quotes",quotes.toString());
            }
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();
                Log.d("This is the symbol",symbol);


                Stock stock = quotes.get(symbol);
                final StockQuote quote = stock.getQuote();
                if(quote.getPrice() != null ){
                    float price = quote.getPrice().floatValue();
                    float change = quote.getChange().floatValue();
                    float percentChange = quote.getChangeInPercent().floatValue();

                    // WARNING! Don't request historical data for a stock that doesn't exist!
                    // The request will hang forever X_x
                    List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

                    StringBuilder historyBuilder = new StringBuilder();

                    for (HistoricalQuote it : history) {
                        historyBuilder.append(it.getDate().getTime().getYear()+" "+it.getDate().getTime().getMonth());
                        historyBuilder.append(", ");
                        historyBuilder.append(it.getClose());
                        historyBuilder.append("\n");
                        Log.d("HistoryClose",it.toString());

                    }

                    ContentValues quoteCV = new ContentValues();
                    quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                    quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                    quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                    quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);


                    quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
                    Log.d("History",historyBuilder.toString());

                    quoteCVs.add(quoteCV);


                } else {
                    Handler mHandler = new Handler(getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, R.string.error_Invalid_stocks1 +quote.getSymbol()+R.string.error_Invalid_stocks2, Toast.LENGTH_SHORT).show();
                            PrefUtils.removeStock(context,quote.getSymbol());
                        }
                    });
                }

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));
            Intent intent = new Intent(context,StockWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, StockWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
            context.sendBroadcast(intent);
            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }


}
