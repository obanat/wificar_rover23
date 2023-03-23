package com.obana.carproxy.utils;


import android.util.Log;

public class AppLog {
  private static boolean showLog = true;
  
  public static void d(String paramString1, String paramString2) {
    if (showLog)
      Log.e(paramString1, paramString2);
  }
  
  public static void e(String paramString) {
    if (showLog)
      Log.e("test", paramString); 
  }
  
  public static void e(String paramString1, String paramString2) {
    if (showLog)
      Log.e(paramString1, paramString2); 
  }
  
  public static void e(String paramString1, String paramString2, Throwable paramThrowable) {
    if (showLog)
      Log.e(paramString1, paramString2, paramThrowable); 
  }
  
  public static void enableLogging(boolean paramBoolean) {
    showLog = paramBoolean;
  }
  
  public static void i(String paramString1, String paramString2) {
    if (showLog)
      Log.e(paramString1, paramString2);
  }
  
  public static void v(String paramString1, String paramString2) {
    if (showLog)
      Log.v(paramString1, paramString2); 
  }
  
  public static void w(String paramString1, String paramString2) {
    if (showLog)
      Log.w(paramString1, paramString2); 
  }
  
  public static void w(String paramString1, String paramString2, Throwable paramThrowable) {
    if (showLog)
      Log.w(paramString1, paramString2, paramThrowable); 
  }
}
