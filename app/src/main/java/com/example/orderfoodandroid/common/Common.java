package com.example.orderfoodandroid.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import com.example.orderfoodandroid.model.User;
import com.example.orderfoodandroid.remote.APIService;
import com.example.orderfoodandroid.remote.IGoogleService;
import com.example.orderfoodandroid.remote.RetrofitClient;

import java.util.Calendar;
import java.util.Locale;

public class Common {
    public static User currentUser;
    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static APIService getFCMService() {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapAPI() {
        return RetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static String convertCodeToStatus(String status) {
        if (status.equals("0"))
            return "Đã đặt hàng";
        else if (status.equals("1"))
            return "Đang giao";
        else if (status.equals("2"))
            return "Đang Chuyển Hàng";
        else
            return "Đã Giao";
    }

    //check InterNet connection
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    //Delete Cart
    public static final String DELETE = "Xóa";
    //viết function lưu user và pass
    public static final String USER_KEY = "User";
    public static final String PASS_KEY = "Password";

    public static String getDate(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(
                android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", calendar).toString());
        return date.toString();
    }


}

