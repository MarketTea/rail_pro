package com.railprosfs.railsapp.utility;

import android.util.Log;

/**
 * Created by Kant on 10/24/2016.
 */

public class LogUtil {

    public static final String TAG = "RailRos";

    public static final boolean IS_DEBUG = false;
    public static final boolean IS_HARD_CODE = false;

    public static final String LOAD_LOCAL_DATA = "LOAD_LOCAL_DATA";
    public static final String LOAD_DWR_ITEM = "LOAD_DWR_ITEM";
    public static final Object LOG_API_REQUEST = "LOG_API_REQUEST";


    public static boolean LOGGING_ENABLED = true;

    private static final int STACK_TRACE_LEVELS_UP = 5;

    public static void verbose(String message) {
        if (LOGGING_ENABLED) {
            Log.v(TAG, getClassNameMethodNameAndLineNumber() + message);
        }
    }

    public static void debug(String message) {
        if (LOGGING_ENABLED) {
            Log.d(TAG, getClassNameMethodNameAndLineNumber() + message);
        }
    }

    public static void info(String message) {
        if (LOGGING_ENABLED) {
            Log.i(TAG, getClassNameMethodNameAndLineNumber() + message);
        }
    }

    public static void error(String message) {
        if (LOGGING_ENABLED) {
            Log.e(TAG, getClassNameMethodNameAndLineNumber() + message);
        }
    }

    public static int getLineNumber() {
        return Thread.currentThread().getStackTrace()[STACK_TRACE_LEVELS_UP].getLineNumber();
    }

    public static String getClassName() {
        String fileName = Thread.currentThread().getStackTrace()[STACK_TRACE_LEVELS_UP].getFileName();
        return fileName.substring(0, fileName.length() - 5);
    }

    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[STACK_TRACE_LEVELS_UP].getMethodName();
    }

    public static String getClassNameMethodNameAndLineNumber() {
        return "[" + getClassName() + "." + getMethodName() + "()-" + getLineNumber() + "]: ";
    }


    public static void debug(String phucTag, String s) {
        if (LOGGING_ENABLED) {
            Log.d(phucTag, getClassNameMethodNameAndLineNumber() + s);
        }
    }


}
