package com.example.orderfoodandroid.remote;

import com.example.orderfoodandroid.model.MyResponse;
import com.example.orderfoodandroid.model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAJwzVtAY:APA91bGPlsvSD2p1kW6moOcn4um07FK4hIVB94dIz76DCPNk6yXPPRzMPjSgrZB2vbWmGJigGSFSgT8Cet9-fWRe9n9f8E-jfqTX21RY0DoAC_Vc-Jx-LwjSc0osB7y6jf1MbK9zy8F5"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
