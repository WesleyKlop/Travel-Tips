package nl.wesleyklop.traveltips.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

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
        ArrayList<HashMap<String, String>> countryList = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("countryList");
        this.countryList = countryList;

        AutoCompleteTextView autoComplete = (AutoCompleteTextView) findViewById(R.id.tipCountry);
        autoComplete.setThreshold(1);
        autoComplete.setAdapter(getArrayAdapter());
    }

    private int getIdFromCountryName(String country) {
        for (HashMap<String, String> temp : countryList) {
            if (temp.get("country").equals(country)) {
                try {
                    return Integer.parseInt(temp.get("id"));
                } catch (NumberFormatException nEx) {
                    Log.wtf(TAG, "Country id is not parsable as int!?");
                }
            }
        }
        return -1;
    }


    private ArrayAdapter<String> getArrayAdapter() {
        ArrayList<String> countryNames = new ArrayList<>();
        if (countryList == null) {
            Log.w(TAG, "countryList == null!");
            return null;
        }

        for (int i = 0; i < countryList.size(); i++) {
            HashMap<String, String> temp = countryList.get(i);
            countryNames.add(temp.get("country"));
        }

        return new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                countryNames
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
        int countryId;

        // Validate country
        if ((countryId = getIdFromCountryName(country)) == -1) {
            Toast.makeText(AddTipActivity.this, "Invalid country", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "CountryId of " + country + " is " + String.valueOf(countryId));

        // Create request
        RequestQueue queue = ReqQueue.getRequestQueue(getApplicationContext());
        ApiHelper helper = new ApiHelper(getApplicationContext());
        JsonRequest postRequest = helper.postTip(String.valueOf(countryId), title, message,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        Toast.makeText(AddTipActivity.this, "Successfully added your tip!", Toast.LENGTH_LONG).show();
                        NavUtils.navigateUpFromSameTask(AddTipActivity.this);
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
}
