package com.instacart.library.sample;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.instacart.library.truetime.TrueTime;
import com.instacart.library.truetime.TrueTimeRx;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Arrays;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
//        initRxTrueTime();
        initTrueTime();
    }

    /**
     * init the TrueTime using a AsyncTask.
     */
    private void initTrueTime() {
        new InitTrueTimeAsyncTask().execute();
    }

    // a little part of me died, having to use this
    private class InitTrueTimeAsyncTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            try {
                Log.i(TAG, "Use normal truetime");
                TrueTime.build()
                        //.withSharedPreferences(SampleActivity.this)
                        .withNtpHost("time.google.com")
                        .withLoggingEnabled(true)
//                        .withSharedPreferencesCache(App.this)
//                        .withConnectionTimeout(3_1428)
                        .initialize();

                new Timer().scheduleAtFixedRate(new TimerTask(){
                    @Override
                    public void run(){
                        try {
                            //public static final int RESPONSE_INDEX_ORIGINATE_TIME = 0;
                            //public static final int RESPONSE_INDEX_RECEIVE_TIME = 1;
                            //public static final int RESPONSE_INDEX_TRANSMIT_TIME = 2;
                            //public static final int RESPONSE_INDEX_RESPONSE_TIME = 3;
                            //public static final int RESPONSE_INDEX_ROOT_DELAY = 4;
                            //public static final int RESPONSE_INDEX_DISPERSION = 5;
                            //public static final int RESPONSE_INDEX_STRATUM = 6;
                            //public static final int RESPONSE_INDEX_RESPONSE_TICKS = 7;
                            //[1574173506881, 1574173507883, 1574173507883, 1574173506944, 0, 14, 1, 8565795]
                            Log.i(TAG, "Getting new time: ".concat(Arrays.toString(TrueTime.build().requestTime("time.google.com"))));
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Manual call of requestTime errored out.");
                        }
                    }
                },0,5000);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "something went wrong when trying to initialize TrueTime", e);
            }
            return null;
        }
    }

    /**
     * Initialize the TrueTime using RxJava.
     */
    private void initRxTrueTime() {
        DisposableSingleObserver<Date> disposable = TrueTimeRx.build()
                .withConnectionTimeout(31_428)
                .withRetryCount(100)
                .withSharedPreferencesCache(this)
                .withLoggingEnabled(true)
                .initializeRx("time.google.com")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Date>() {
                    @Override
                    public void onSuccess(Date date) {
                        Log.d(TAG, "Success initialized TrueTime :" + date.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", e);
                    }
                });
    }


}
