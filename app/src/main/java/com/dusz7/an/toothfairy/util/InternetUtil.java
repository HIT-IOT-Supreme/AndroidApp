package com.dusz7.an.toothfairy.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by dusz2 on 2016/12/28 0028.
 */

public class InternetUtil {


    public static String urlToUTF8(String value){

        String result = "";
        try{
            result = URLEncoder.encode(value,"UTF-8");

        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }

        return result;
    }
    public static boolean isNetworkConnected(Context context){
        if(context != null){
            ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo != null){
                return networkInfo.isAvailable();
            }
        }
        return false;
    }
}
