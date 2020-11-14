package com.mutual.emove.entidades;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class EmoveSingleton {

    private static EmoveSingleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private String url = "https://gestion.emoveapp.com";
//    private String url = "http://192.168.0.12:8000";

    private EmoveSingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized EmoveSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new EmoveSingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public  String getUrl() {
        return url;
    }


}
