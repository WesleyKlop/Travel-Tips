package nl.wesleyklop.traveltips.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import nl.wesleyklop.traveltips.R;

/*
 * TODO: implement functionality
 * TODO: button hover/onclick styling
 * TODO: implement button disabled/enabled
 * TODO: verify country name using countryAdapter
 */
public class AddTipActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String TAG = "AddTipActivity";
    private ArrayList<HashMap<String, String>> countryList = null;
    private SimpleAdapter countryAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tip);

        Intent intent = getIntent();

        @SuppressWarnings("unchecked")
        ArrayList<HashMap<String, String>> countryList = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("countrylist");
        this.countryList = countryList;

        countryAdapter = getCountryListAsAdapter();

        /*if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            filterSearchView(query);
        }*/
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
    public void onClick(View v) {

    }

    /*private void filterSearchView(String query) {
        SearchView countrySearchView = (SearchView) findViewById(R.id.searchCountryView);
        Log.v(TAG, "Searchquery: " + query);
    }*/
}
