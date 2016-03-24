package nl.wesleyklop.traveltips.activities;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import nl.wesleyklop.traveltips.ApiHelper;
import nl.wesleyklop.traveltips.JsonRequest;
import nl.wesleyklop.traveltips.R;
import nl.wesleyklop.traveltips.ReqQueue;

public class TipsListActivity extends AppCompatActivity {
    private final static String TAG = "TipsListActivity";
    private final ArrayList<HashMap<String, String>> tipsListData = new ArrayList<>();
    private String id, name, tipsCount;
    private RequestQueue requestQueue;
    private ListAdapter tipsListAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips_list);

        requestQueue = ReqQueue.getInstance(getApplicationContext())
                .getRequestQueue();

        Intent currIntent = getIntent();
        id = currIntent.getStringExtra("id");
        name = currIntent.getStringExtra("name");
        tipsCount = currIntent.getStringExtra("tips");

        ((TextView) findViewById(R.id.tips_header)).setText(name);

        loadCountryTips();
    }

    private void loadCountryTips() {
        JsonRequest countryTipsRequest = new ApiHelper(getApplicationContext())
                .getCountryTips(this.id,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d(TAG, "Response status: " + response.getString("status"));
                                } catch (JSONException e) {
                                    Log.e(TAG, e.toString());
                                }

                                JSONArray data = new JSONArray();
                                try {
                                    data = response.getJSONArray("response");
                                } catch (JSONException e) {
                                    Log.e(TAG, e.toString());
                                }

                                for (int i = 0; i < data.length(); i++) {
                                    String tipTitle = "";
                                    String tipMessage = "";
                                    String tipId = "";
                                    try {
                                        // Get the JSONObject for the current row
                                        JSONObject currRow = data.getJSONObject(i);
                                        //Log.v(TAG, currRow.toString());
                                        // Fetch the country name from the object
                                        tipTitle = currRow.getString("Title");
                                        tipMessage = currRow.getString("Message");
                                        tipId = currRow.getString("TipId");
                                    } catch (JSONException e) {
                                        Log.e(TAG, e.toString());
                                    }

                                    // Create a tmp HashMap that holds the value like "key" => "value"
                                    HashMap<String, String> map = new HashMap<>();
                                    // Add the "key", "value" to the HashMap
                                    map.put("title", tipTitle);
                                    map.put("message", tipMessage);
                                    map.put("id", tipId);
                                    // Add the tmp HashMap to the global list
                                    tipsListData.add(map);
                                }

                                // Create a new listAdapter with the countryList, the layout for the list item and two arrays that hold the key and list item id to bind the value to
                                tipsListAdapter = new SimpleAdapter(TipsListActivity.this,
                                        tipsListData,
                                        android.R.layout.two_line_list_item,
                                        new String[]{"title", "message"},
                                        new int[]{android.R.id.text1, android.R.id.text2}
                                );

                                setAdapter();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });

        requestQueue.add(countryTipsRequest);
    }

    private void setAdapter() {
        Log.v(TAG, "Setting list adapter...");
        ((ListView) findViewById(R.id.tipsListView)).setAdapter(tipsListAdapter);
        Log.v(TAG, "Entries: " + String.valueOf(tipsListAdapter.getCount()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
