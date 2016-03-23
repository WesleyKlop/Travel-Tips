package nl.wesleyklop.traveltips;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JsonRequest extends Request<JSONObject> {
    public final static String TAG = "JsonRequest";
    static String mBaseUrl = "http://ap24-28.ict-lab.nl/api/";
    private static String cookies = "";
    private int mMethod;
    private String mUrl;
    private Map<String, String> mParams;
    private Listener<JSONObject> mListener;

    /**
     * Maakt een nieuw login request object
     *
     * @param method          de method voor de request bijvoorbeeld POST of GET
     * @param url             de URL om de post request naar te doen
     * @param params          Een <String,String> Map met parameters
     * @param reponseListener Responselistener die iets doet met de uitkomst
     * @param errorListener   ErrorListener die errors afhandeld
     */
    public JsonRequest(int method, String url, Map<String, String> params, Listener<JSONObject> reponseListener, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mMethod = method;
        if (url.matches("^(https?://).*")) {
            this.mUrl = url;
        } else {
            this.mUrl = mBaseUrl + url;
            Log.d(TAG, "New url  = " + this.mUrl);
        }
        this.mParams = params;
        this.mListener = reponseListener;
    }

    public static String getBaseUrl() {
        return mBaseUrl;
    }

    public static void setBaseUrl(String mBaseUrl) {
        JsonRequest.mBaseUrl = mBaseUrl;
    }

    public static Map<String, String> noParams() {
        return new HashMap<>();
    }

    @Override
    public String getUrl() {
        if (mMethod == Method.GET) {
            StringBuilder stringBuilder = new StringBuilder(mUrl);
            Iterator<Map.Entry<String, String>> iterator = mParams.entrySet().iterator();
            int i = 1;
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (i == 1) {
                    stringBuilder.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                } else {
                    stringBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                }
                iterator.remove(); // avoids a ConcurrentModificationException
                i++;
            }
            mUrl = stringBuilder.toString();
        }
        return mUrl;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();

        if (!cookies.equals(""))
            headers.put("Cookie", cookies);

        return headers;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        Map headers = response.headers;
        try {
            String cookie = headers.get("Set-Cookie").toString();
            saveCookies(cookie);
        } catch (NullPointerException ne) {
            //Log.d(TAG, "No Set-Cookie header is here");
            //Log.v(TAG, "Did get these headers: " + headers.toString());
        }
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    private void saveCookies(String cookie) {
        if (cookie == null) {
            return;
        }
        Log.d(TAG, "Cookie = " + cookie);
        cookies = cookie;
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        mListener.onResponse(response);
    }
}