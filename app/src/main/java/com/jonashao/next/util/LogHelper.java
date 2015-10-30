package com.jonashao.next.util;

import android.util.Log;

/**
 * Created by Junnan on 2015/10/28.
 */
public class LogHelper {

    private static final int VERBOSE = 2;

    private static final int DEBUG = 3;

    private static final int INFO = 4;

    private static final int WARN = 5;

    private static final int ERROR = 6;

    private static final int ASSERT = 7;

    private static final int logLevel = 7;

    public static void v(String tag, String message){
        if(logLevel>VERBOSE){
            Log.v(tag, message);
        }
    }

    public static void i(String tag, String message){
        if(logLevel>INFO){
            Log.i(tag, message);
        }
    }

    public static void e(String tag, String message){
        if(logLevel>ERROR){
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message,Throwable tr ){
        if(logLevel>ERROR){
            Log.e(tag, message,tr);
        }
    }

    public static void e(String tag, Throwable tr){
        if(logLevel>ERROR){
            Log.e(tag,tr.toString());
        }
    }

    //TODO:补充LogHelper


}
