package nl.wesleyklop.traveltips.activities;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import nl.wesleyklop.traveltips.JsonRequest;
import nl.wesleyklop.traveltips.R;

public class TipsListActivity extends AppCompatActivity {
    public final static String TAG = "TipsListActivity";

    private String id, name, tipsCount;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips_list);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }

        requestQueue = Volley.newRequestQueue(this);

        Intent currIntent = getIntent();
        id = currIntent.getStringExtra("id");
        name = currIntent.getStringExtra("name");
        tipsCount = currIntent.getStringExtra("tips");

        ((TextView) findViewById(R.id.tips_header)).setText(name);

        loadCountryTips(getCountryIdParams(id));
    }

    private Map<String, String> getCountryIdParams(String id) {
        Map<String, String> params = new HashMap<>();

        params.put("action", "getTips");
        params.put("id", id);

        return params;
    }

    private void loadCountryTips(Map<String, String> params) {
        JsonRequest countryTipsRequest = new JsonRequest(Request.Method.POST, "json.php", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        requestQueue.add(countryTipsRequest);
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
