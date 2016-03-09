package nl.wesleyklop.traveltips;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ReqQueue {
    private static ReqQueue mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;

    private ReqQueue(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized ReqQueue getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ReqQueue(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void add(Request<T> req) {
        getRequestQueue().add(req);
    }
}