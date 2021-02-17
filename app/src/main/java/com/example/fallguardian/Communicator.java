package com.example.fallguardian;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class Communicator {

    ///communication medium to client server
    private Retrofit retrofit;
    private Client_ACC client_acc;
    private Client_both client_both;


    //Tarik wifi ipV6: "https://192.168.1.103:8000/"
    //"https://app.fakejson.com/"
    //my phone: "http://192.168.43.180:8000/"
    //Tarik wifi ipV4: "http://192.168.1.101:8000/"
    //sss wifi: http://192.168.0.179:8000/
    //"https://fall-guardian.herokuapp.com/"
    private final String BaseUrl ="https://fall-guardian.herokuapp.com/";

    public Communicator() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        client_acc = retrofit.create(Client_ACC.class);
        client_both = retrofit.create(Client_both.class);
    }

    public Client_ACC getClient_acc() {
        return client_acc;
    }


    public Client_both getClient_both() {
        return client_both;
    }


}
