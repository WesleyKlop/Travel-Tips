package nl.wesleyklop.traveltips.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

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
        GoogleApiClient.OnConnectionFailedListener,
        SearchView.OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener,
        Drawer.OnDrawerItemClickListener {

    private static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 2428;
    private static ListAdapter countryListAdapter = null;
    private static GoogleApiClient mGoogleApiClient = null;
    private static ArrayList<HashMap<String, String>> countryList = new ArrayList<>();
    private static boolean isUserLoggedIn = false;
    private static GoogleSignInAccount googleAccount = null;
    private RequestQueue queue;
    private ListView mCountryListView;
    private boolean doStartAddTipActivityOnResult = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AccountHeader accountHeader;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCountryListView = (ListView) findViewById(R.id.countryListView);
        mCountryListView.addHeaderView(getLayoutInflater().inflate(R.layout.header_country_list, null));
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.countryListLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isUserLoggedIn) {
                    Intent addTipActivity = new Intent(view.getContext(), AddTipActivity.class);
                    addTipActivity.putExtra("countryList", countryList);
                    startActivity(addTipActivity);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_logged_in_warning), Toast.LENGTH_LONG).show();
                    doStartAddTipActivityOnResult = true;
                    startSignIn();
                }
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

        createDrawer();
        startSilentAuthentication();
    }

    private void createDrawer() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });


        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(new ImageHolder(getDrawable(R.drawable.img_header_background)))
                        //.withTextColorRes(R.color.text_primary)
                .build();


        Drawer navigationDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar((Toolbar) findViewById(R.id.toolbar))
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("HOLY FUCKING SHIT THAT WAS EASY"),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.action_settings).withIcon(GoogleMaterial.Icon.gmd_settings)
                )
                .withOnDrawerItemClickListener(this)
                .withAccountHeader(accountHeader, true)
                .withSelectedItem(-1)
                .build();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            ((SearchView) searchItem.getActionView()).setOnQueryTextListener(this);
        }

        if (isUserLoggedIn) {
            menu.findItem(R.id.action_login).setVisible(false);
            menu.findItem(R.id.action_logout).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        Log.d(TAG, String.valueOf(position));
        switch (position) {
            case 0:
                Log.d(TAG, "HOLY SHIT!?");
                return true;
            case 1:
                // Divider
                return true;
            case 2:
                Log.d(TAG, "SETTINGS?");
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.v(TAG, "Settings..");
                return true;
            case R.id.action_login:
                startSignIn();
                return true;
            case R.id.action_logout:
                if (mGoogleApiClient.isConnected()) {
                    signOut();
                } else {
                    Snackbar.make(findViewById(R.id.MainActivity), "Unable to sign out", Snackbar.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_refresh:
                swipeRefreshLayout.setRefreshing(true);
                updateCountryListView();
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

    private void populateListView() {
        if (countryListAdapter == null) {
            Log.d(TAG, "Fetching countries from server...");

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
        if (swipeRefreshLayout.isRefreshing()) {
            ((SimpleAdapter) countryListAdapter).notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "Connection failed? " + connectionResult.toString());
    }

    private void startSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result van de SignIn intent
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

                accountHeader.addProfiles(
                        new ProfileDrawerItem()
                                .withEmail(googleAccount.getEmail())
                                .withName(googleAccount.getDisplayName())
                                .withIcon(googleAccount.getPhotoUrl())
                );

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
                            if (response.getString("status").equals("success")) {
                                if (doStartAddTipActivityOnResult) {
                                    doStartAddTipActivityOnResult = false;
                                    Intent addTipActivity = new Intent(getApplicationContext(), AddTipActivity.class);
                                    addTipActivity.putExtra("countryList", countryList);
                                    startActivity(addTipActivity);
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        ((SimpleAdapter) countryListAdapter).getFilter().filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ((SimpleAdapter) countryListAdapter).getFilter().filter(newText);
        return true;
    }

    @Override
    public void onRefresh() {
        updateCountryListView();
    }

    private void updateCountryListView() {
        countryList = new ArrayList<>();
        countryListAdapter = null;
        populateListView();
    }
}