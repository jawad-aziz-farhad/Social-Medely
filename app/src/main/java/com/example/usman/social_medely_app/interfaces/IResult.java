package com.example.usman.social_medely_app.interfaces;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Created by Farhad on 11/12/2018.
 */

public interface IResult {

    void onSuccess(String result);
    void onError(String error);

}
