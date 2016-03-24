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
@SuppressWarnings("WeakerAccess")
public class ApiHelper {
    private final static String TAG = "ApiHelper";
    private static final boolean debugMode = true;
    private static String endPoint;
    private JsonRequest lastRequest;

    /**
     * ApiHelper constructor
     *
     * @param context context to get endpoint from
     */
    public ApiHelper(Context context) {
        endPoint = context.getString(R.string.api_endpoint);
    }

    /**
     * ApiHelper constructor
     *
     * @param endPoint endpoint to use
     */
    public ApiHelper(String endPoint) {
        ApiHelper.endPoint = endPoint;
    }

    /**
     * Do a post request against a resource
     *
     * @param params           String Map of parameters
     * @param responseListener ResponseListener to execute on response
     * @param errorListener    to execute on error
     * @return JsonRequest to add to a RequestQueue
     */
    public JsonRequest post(Map<String, String> params, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        lastRequest = new JsonRequest(Request.Method.POST, endPoint, params, responseListener, errorListener);
        return lastRequest;
    }

    /**
     * Do a get request against a web resource
     * @param action action to execute
     * @param params extra parameters
     * @param responseListener to execute on response
     * @param errorListener to execute on error
     * @return JsonRequest to add to a RequestQueue
     */
    public JsonRequest get(String action, Map<String, String> params, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        params.put("action", action);

        lastRequest = new JsonRequest(Request.Method.GET, endPoint, params, responseListener, errorListener);
        return lastRequest;
    }

    /**
     * Do a get request against a web resource
     * @param action action to execute
     * @param responseListener to execute on response
     * @param errorListener to execute on error
     * @return JsonRequest to add to a RequestQueue
     */
    public JsonRequest get(String action, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        Map<String, String> params = getParams(action);

        lastRequest = new JsonRequest(Request.Method.GET, endPoint, params, responseListener, errorListener);
        return lastRequest;
    }

    /**
     * Do a get request to get a JSONObject containing all countries
     * @param responseListener to execute on response
     * @param errorListener to execute on error
     * @return JsonRequest to add to a RequestQueue
     */
    public JsonRequest getAllCountries(Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        lastRequest = get("countries", responseListener, errorListener);
        return lastRequest;
    }

    /**
     * Do a get request to get a JSONObject containing the tips for a country
     * @param countryId id of the country to fetch
     * @param responseListener to execute on response
     * @param errorListener to execute on error
     * @return JsonRequest to add to a RequestQueue
     */
    public JsonRequest getCountryTips(String countryId, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        Log.d(TAG, "Got country id: " + countryId);
        Map<String, String> params = getParams();
        params.put("country", countryId);

        lastRequest = get("tips", params, responseListener, errorListener);
        return lastRequest;
    }

    /**
     * Post a tip for a country
     * @param countryName name of the country
     * @param title tip title
     * @param message tip message
     * @param responseListener to execute on Response
     * @param errorListener to execute on Error
     * @return JsonRequest to add to a RequestQueue
     */
    public JsonRequest postTip(String countryName, String title, String message,
                               Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        // Make params map
        Map<String, String> params = getParams("tips");
        params.put("country", countryName);
        params.put("title", title);
        params.put("message", message);

        lastRequest = post(params, responseListener, errorListener);
        return lastRequest;
    }

    /**
     * Get a HashMap containing parameters
     *
     * @param action parameter
     * @return Map to add parameters to
     */
    public Map<String, String> getParams(String action) {
        Map<String, String> tmp = new HashMap<>();
        tmp.put("action", action);
        if (debugMode) {
            tmp.put("debug", "1");
        }
        return tmp;
    }

    /**
     * Get a HashMap containing parameters
     *
     * @return Map to add parameters to
     */
    public Map<String, String> getParams() {
        Map<String, String> tmp = new HashMap<>();
        if (debugMode) {
            tmp.put("debug", "1");
        }
        return tmp;
    }
}
