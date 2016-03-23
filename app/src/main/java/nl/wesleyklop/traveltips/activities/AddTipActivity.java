package nl.wesleyklop.traveltips.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleAdapter;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import nl.wesleyklop.traveltips.ApiHelper;
import nl.wesleyklop.traveltips.JsonRequest;
import nl.wesleyklop.traveltips.R;
import nl.wesleyklop.traveltips.ReqQueue;

/*
 * TODO: button hover/onclick styling
 * TODO: implement button disabled/enabled
 * TODO: verify country name using countryAdapter
 * TODO: convert country name -> id OR server side?
 */
public class AddTipActivity extends AppCompatActivity {
    public final static String TAG = "AddTipActivity";
    private ArrayList<HashMap<String, String>> countryList = null;
    private SimpleAdapter countryAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.action_add_tip_title));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        @SuppressWarnings("unchecked")
        ArrayList<HashMap<String, String>> countryList = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("countrylist");
        this.countryList = countryList;

        countryAdapter = getCountryListAsAdapter();
    }

    private SimpleAdapter getCountryListAsAdapter() {
        return new SimpleAdapter(AddTipActivity.this,
                countryList,
                R.layout.country_list_item,
                new String[]{"country", "tips"},
                new int[]{R.id.countryName, R.id.countryTips}
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_tip, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_send:
                submitTip();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void submitTip() {
        // Get variables
        String country = ((EditText) findViewById(R.id.tipCountry)).getText().toString(),
                title = ((EditText) findViewById(R.id.tipTitle)).getText().toString(),
                message = ((EditText) findViewById(R.id.tipMessage)).getText().toString();

        // Create request
        RequestQueue queue = ReqQueue.getRequestQueue(getApplicationContext());
        ApiHelper helper = new ApiHelper(getApplicationContext());
        JsonRequest postRequest = helper.postTip(country, title, message,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Submit request
        queue.add(postRequest);
    }

    /*private void filterSearchView(String query) {
        SearchView countrySearchView = (SearchView) findViewById(R.id.searchCountryView);
        Log.v(TAG, "Searchquery: " + query);
    }*/
}
