package com.example.fallguardian;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

interface Client_ACC {
    @POST("api/")
    Call<Fall> GetPostValue_ACC(@Body List<Data_ACC> post);
}
