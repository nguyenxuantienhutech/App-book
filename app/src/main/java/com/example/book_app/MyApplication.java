package com.example.book_app;

import android.app.Application;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;
//application class run before my launcher activity
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //static method to convert
    public  static final String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        //format to dd/MM/yyy
        String date = DateFormat.format("dd/MM/yyyy", cal).toString();
        return date;
    }
}
