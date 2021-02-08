package com.example.fallguardian;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

interface Client_both {
    @POST("api/")
    Call<Fall> GetPostValue_both(@Body List<Data> post);
}