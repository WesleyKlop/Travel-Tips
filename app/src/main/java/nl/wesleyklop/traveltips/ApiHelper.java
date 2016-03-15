package nl.wesleyklop.traveltips;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wesley on 11-3-16.
 * Class to help access the TravelTips API
 */
public class ApiHelper {
    public final static String TAG = "ApiHelper";
    private static String endPoint;
    private JsonRequest lastRequest;

    public ApiHelper(Context context) {
        endPoint = context.getString(R.string.api_endpoint);
    }

    public ApiHelper(String endPoint) {
        ApiHelper.endPoint = endPoint;
    }

    public JsonRequest post(String action, Map<String, String> params, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        params.put("action", action);

        lastRequest = new JsonRequest(Request.Method.POST, endPoint, params, responseListener, errorListener);
        return lastRequest;
    }

    public JsonRequest post(Map<String, String> params, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        lastRequest = new JsonRequest(Request.Method.POST, endPoint, params, responseListener, errorListener);
        return lastRequest;
    }

    public JsonRequest get(String action, Map<String, String> params, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        params.put("action", action);

        lastRequest = new JsonRequest(Request.Method.GET, endPoint, params, responseListener, errorListener);
        return lastRequest;
    }

    public JsonRequest get(String action, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("action", action);

        lastRequest = new JsonRequest(Request.Method.GET, endPoint, params, responseListener, errorListener);
        return lastRequest;
    }

    public JsonRequest getAllCountries(Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        lastRequest = get("countries", responseListener, errorListener);
        return lastRequest;
    }

    public JsonRequest getCountryTips(String countryId, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        Log.d(TAG, "Got country id: " + countryId);
        Map<String, String> params = new HashMap<>();
        params.put("country", countryId);

        lastRequest = get("tips", params, responseListener, errorListener);
        return lastRequest;
    }

    public JsonRequest postTip(String countryName, String title, String message,
                               Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        // Make params map
        Map<String, String> params = new HashMap<>();
        params.put("action", "tips");
        params.put("country", countryName);
        params.put("title", title);
        params.put("message", message);

        lastRequest = post(params, responseListener, errorListener);
        return lastRequest;
    }
}
