package nl.wesleyklop.traveltips.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nl.wesleyklop.traveltips.ApiHelper;
import nl.wesleyklop.traveltips.JsonRequest;
import nl.wesleyklop.traveltips.R;
import nl.wesleyklop.traveltips.ReqQueue;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 2428;
    public static ListAdapter countryListAdapter = null;
    private static GoogleApiClient mGoogleApiClient = null;
    private static ArrayList<HashMap<String, String>> countryList = new ArrayList<>();
    private static boolean isUserLoggedIn = false;
    private static GoogleSignInAccount googleAccount = null;
    protected RequestQueue queue;
    private ListView mCountryListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCountryListView = (ListView) findViewById(R.id.countryListView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addTipActivity = new Intent(view.getContext(), AddTipActivity.class);
                addTipActivity.putExtra("countryList", countryList);
                startActivity(addTipActivity);
            }
        });

        queue = ReqQueue.getRequestQueue(getApplicationContext());

        populateListView();

        if (mGoogleApiClient == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken(MainActivity.this.getResources().getString(R.string.server_client_id))
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        startSilentAuthentication();
    }

    private void startSilentAuthentication() {
        if (googleAccount == null) {
            OptionalPendingResult<GoogleSignInResult> pendingSignInResult =
                    Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (pendingSignInResult.isDone()) {
                handleSignInResult(pendingSignInResult.get(), true);
            } else {
                pendingSignInResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult result) {
                        handleSignInResult(result, true);
                    }
                });
            }
        }
    }

/*
    @Override
    public void onStart() {
        super.onStart();

    }
*/

    protected Map<String, String> getSearchParams() {
        Map<String, String> params = new HashMap<>();
        String searchQuery = "";

        // TODO: add search EditText and implement it here

        params.put("search", searchQuery);

        return params;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (isUserLoggedIn) {
            menu.findItem(R.id.action_login).setVisible(false);
            menu.findItem(R.id.action_logout).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.v(TAG, "Settings..");
                return true;
            case R.id.action_login:
                signIn();
                return true;
            case R.id.action_logout:
                if (mGoogleApiClient.isConnected()) {
                    signOut();
                } else {
                    Snackbar.make(findViewById(R.id.MainActivity), "Unable to sign out", Snackbar.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOut() {
        DialogInterface.OnClickListener dialogClickListener = new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (which != DialogInterface.BUTTON_POSITIVE) {
                                    Snackbar.make(findViewById(R.id.MainActivity), "You're now signed out", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                );
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                    Snackbar.make(findViewById(R.id.MainActivity), "You're now fully disconnected", Snackbar.LENGTH_LONG).show();
                                }
                            }
                    );
                }
                isUserLoggedIn = false;
                invalidateOptionsMenu();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
        builder.setMessage("Do you want to completely revoke access your account from the app?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    private View getView() {
        return findViewById(R.id.MainActivity);
    }

    public void populateListView() {
        if (countryListAdapter == null) {
            Log.d(TAG, "Fetching countryies from server...");

            JsonRequest countryListRequest = new ApiHelper(getApplicationContext())
                    .getAllCountries(
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
                                        String countryName = "";
                                        String countryTips = "";
                                        String countryId = "";
                                        try {
                                            // Get the JSONObject for the current row
                                            JSONObject currRow = data.getJSONObject(i);
                                            //Log.v(TAG, currRow.toString());
                                            // Fetch the country name from the object
                                            countryName = currRow.getString("Name");
                                            countryTips = String.valueOf(currRow.getInt("TipsCount"));
                                            countryId = currRow.getString("CountryId");
                                        } catch (JSONException e) {
                                            Log.e(TAG, e.toString());
                                        }

                                        // Create a tmp HashMap that holds the value like "key" => "value"
                                        HashMap<String, String> map = new HashMap<>();
                                        // Add the "key", "value" to the HashMap
                                        map.put("country", countryName);
                                        map.put("tips", countryTips);
                                        map.put("id", countryId);
                                        // Add the tmp HashMap to the global list
                                        countryList.add(map);
                                    }

                                    // Create a new listAdapter with the countryList, the layout for the list item and two arrays that hold the key and list item id to bind the value to
                                    countryListAdapter = new SimpleAdapter(MainActivity.this,
                                            countryList,
                                            R.layout.country_list_item,
                                            new String[]{"country", "tips"},
                                            new int[]{R.id.countryName, R.id.countryTips}
                                    );

                                    setCountryListViewAdapter();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Snackbar.make(findViewById(R.id.MainActivity), "Unable to fetch countries", Snackbar.LENGTH_LONG).show();
                                    Log.e(TAG, error.toString());
                                }
                            });
            queue.add(countryListRequest);
        } else {
            setCountryListViewAdapter();
        }
    }

    private void setCountryListViewAdapter() {
        // Set the adapter on the ListView
        mCountryListView.setAdapter(countryListAdapter);
        // Add an event listener shows you a SnackBar with the value of the ListItem you clicked on
        mCountryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "User clicked: " + countryList.get(+position).toString());
                String clickedCountry = countryList.get(+position).get("country");
                String countryId = countryList.get(+position).get("id");
                String countryTips = countryList.get(+position).get("tips");

                Intent tipsListActivity = new Intent(parent.getContext(), TipsListActivity.class);
                tipsListActivity.putExtra("name", clickedCountry);
                tipsListActivity.putExtra("id", countryId);
                tipsListActivity.putExtra("tips", countryTips);
                startActivity(tipsListActivity);
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "Connection failed? " + connectionResult.toString());
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result van de signin intent
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result, false);
        }
    }

    private void handleSignInResult(GoogleSignInResult result, boolean isAutoSignIn) {
        if (result.isSuccess()) {
            isUserLoggedIn = true;
            invalidateOptionsMenu();
            // Show authenticated UI
            googleAccount = result.getSignInAccount();

            if (googleAccount != null) {
                Log.d(TAG, googleAccount.getDisplayName() + " is now logged in");

                signInUser(googleAccount.getIdToken());

                if (isAutoSignIn) {
                    Log.v(TAG, "Did silent sign in");
                    //Snackbar.make(findViewById(R.id.MainActivity), "Welcome back " + googleAccount.getDisplayName(), Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Oops something went wrong :|");
            }
        } else {
            Log.e(TAG, "GoogleSignInResult is false");
            Log.e(TAG, result.getStatus().toString());
        }
    }

    private void signInUser(String token) {
        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("token", token);

        JsonRequest authRequest = new JsonRequest(Request.Method.POST, "auth.php", tokenParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "SUCCESSFULLY AUTHENTICATED ON THE SERVER");
                        try {
                            Log.v(TAG, response.getJSONObject("response").toString());
                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error in authenticating? " + error.getMessage());
                    }
                });

        queue.add(authRequest);
    }
}