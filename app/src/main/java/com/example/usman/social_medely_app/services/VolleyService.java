package com.example.usman.social_medely_app.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.usman.social_medely_app.interfaces.IResult;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Farhad on 11/12/2018.
 */

public class VolleyService {

    private IResult mResultCallBack = null;
    private Context context;
    private static final String TAG = VolleyService.class.getSimpleName();

    public VolleyService(IResult mResultCallBack, Context context){
        this.mResultCallBack = mResultCallBack;
        this.context = context;
    }

    public void postRequest(String url , final String requestType, JSONObject data){

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Success Callback
                        Log.w(TAG, "RESPONSE: "+ response.toString());
                        mResultCallBack.onSuccess(requestType, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "ERROR  "+ error.toString());
                        mResultCallBack.onError(requestType,error);
                    }
                })

        {

            /** Passing some request headers* */
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        // add it to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjReq);
    }

}
