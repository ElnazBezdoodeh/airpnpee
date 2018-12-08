package com.appspot.airpeepee.airpeepee;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.appspot.airpeepee.airpeepee.model.CommentAdapter;
import com.appspot.airpeepee.airpeepee.model.GlideApp;
import com.appspot.airpeepee.airpeepee.model.PlaceArrayAdapter;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.appspot.airpeepee.airpeepee.model.DataHolder;
import com.appspot.airpeepee.airpeepee.model.Toilet;
import com.appspot.airpeepee.airpeepee.model.MyLocationListener;
import com.appspot.airpeepee.airpeepee.model.User;
import com.appspot.airpeepee.airpeepee.model.db;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.github.zagum.expandicon.ExpandIconView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import android.content.Intent;


import static com.google.common.base.Strings.isNullOrEmpty;


public class MapsActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks ,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, DirectionCallback ,
EditToiletActivity.NoticeDialogListener , AddReviewActivity.NoticeDialogListener
{

    private boolean mendtrip=true;
    private GoogleMap mMap;
    // User Location
    private Location mlocation;
    //Toilette
    private Marker marker;
    private Marker m_marker;
    private View mBottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private ExpandIconView mExpandIconView;

    private float mSlideOffset = 0;

    //Direction
    private String serverKey = "AIzaSyCwG-ebJNdh97djEIizZFmMw_FlowuaMGs";
    private LatLng origin;
    private LatLng destination;
    private FloatingActionButton btnRequestDirection;
    MyLocationListener myLocationListener;


    //Serach
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private AutoCompleteTextView mAutocompleteTextView;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    private boolean isSearch=false;

    //Comment
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //filter
        FloatingActionButton mfab =(FloatingActionButton) findViewById(R.id.floatingActionButton);
        mfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
         setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // lOGIN
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DataHolder.getInstance().setUser(new User());
            db.findUserbyemail(currentUser.getEmail());
            DataHolder.getInstance().getUser().setFirebaseUser(currentUser);
        }
        // Search
        mGoogleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
        mAutocompleteTextView = (AutoCompleteTextView) findViewById(R.id.search_view);
        mAutocompleteTextView.setThreshold(2);
        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1, BOUNDS_MOUNTAIN_VIEW, null);
        mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);



        btnRequestDirection = findViewById(R.id.direction_btn);
        btnRequestDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // mlocation = myLocationListener.getLastBestLocation();
                mendtrip =false;
                findViewById(R.id.imageView5).setVisibility(View.VISIBLE);
                origin = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
                destination = m_marker.getPosition();
                requestDirection();
            }
        });

        Button edit_toilet =(Button) findViewById(R.id.edit_toilet);
        edit_toilet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoticeDialog();
            }
        });

        ImageView cancel= findViewById(R.id.imageView5);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mendtrip=true;
                findViewById(R.id.bottom_sheet).setVisibility(View.GONE);
                findViewById(R.id.imageView5).setVisibility(View.GONE);
                refreshMarker();
            }
        });

        Button review_toilet = (Button) findViewById(R.id.review_toilet);
        review_toilet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DataHolder.getInstance().getUser() != null){
                    showNoticeDialogReview();

                } else {
                    startActivity(new Intent(MapsActivity.this, LoginActivity.class));

                }
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        myLocationListener = new MyLocationListener(this);
        mlocation = myLocationListener.getLastBestLocation();
        if (mlocation == null)
            mlocation = myLocationListener.currentBestLocation;
        findViews();
        setUpViews();
        findViewById(R.id.bottom_sheet).setVisibility(View.GONE);
        findViewById(R.id.direction_btn).setVisibility(View.GONE);

        findViewById(R.id.ic_euro).setVisibility(View.GONE);
        findViewById(R.id.ic_wheelchair).setVisibility(View.GONE);
        findViewById(R.id.ic_out_of_order).setVisibility(View.GONE);
        findViewById(R.id.imageView5).setVisibility(View.GONE);
    }




    private void findViews() {
        mBottomSheet = findViewById(R.id.bottom_sheet);
        mExpandIconView = (ExpandIconView) mBottomSheet.findViewById(R.id.expandIconView);
    }


    private void setUpViews() {
        mExpandIconView.setState(ExpandIconView.LESS, true);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mExpandIconView.setState(ExpandIconView.LESS, true);
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    mExpandIconView.setState(ExpandIconView.MORE, true);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, final float slideOffset) {
                mSlideOffset = slideOffset;
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        float dis = (mSlideOffset - slideOffset) * 10;
                        if (dis > 1) {
                            dis = 1;
                        } else if (dis < -1) {
                            dis = -1;
                        }
                        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_DRAGGING) {
                            mExpandIconView.setFraction(.5f + dis * .5f, false);
                        }
                    }
                }, 150);
            }
        });
        mBottomSheetBehavior.setPeekHeight((int) convertDpToPixel(140, this));
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public DisplayMetrics getDisplayMetrics(Context context) {
        Resources resources = context.getResources();
        return resources.getDisplayMetrics();
    }

    public float convertDpToPixel(float dp, Context context) {
        return dp * (getDisplayMetrics(context).densityDpi / 160f);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        MarkerOptions markerPOI;


        // Toiltes from data to marker
        for (Toilet t : DataHolder.getInstance().getData()) {
            markerPOI = new MarkerOptions();
            markerPOI.position(new LatLng(t.getLocationLat(), t.getLocationLon()))
                    .title(t.getName());

            if (t.isPrivate())
                markerPOI.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            else
                markerPOI.icon(BitmapDescriptorFactory.defaultMarker());

            mMap.addMarker(markerPOI);
        }

        LatLng sydney = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                sydney, 15);
        mMap.animateCamera(location);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
               if(!mendtrip && !isSearch)
               {
                origin = new LatLng(location.getLatitude(), location.getLongitude());
                if(destination !=null) {
                    Location locationOne = new Location("");
                    locationOne.setLatitude(destination.latitude);
                    locationOne.setLongitude(destination.longitude);
                    float distanceInMetersOne = location.distanceTo(locationOne);
                    if (distanceInMetersOne <30) {
                        //destination = null;
                        mendtrip=true;
                        findViewById(R.id.bottom_sheet).setVisibility(View.GONE);
                        refreshMarker();
                        return;
                    }
                    requestDirection();
                }
               }

            }
        });
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                m_marker=marker;
                findViewById(R.id.bottom_sheet).setVisibility(View.VISIBLE);
                findViewById(R.id.direction_btn).setVisibility(View.VISIBLE);
                putToiletInfo(marker);
                return false;
            }
        });

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void putToiletInfo(Marker marker) {
        TextView name = (TextView) findViewById(R.id.toiletName);
        TextView type = (TextView) findViewById(R.id.toilet_type);
        TextView totalrating = (TextView) findViewById(R.id.reviews);
        TextView cost=findViewById(R.id.view_cost);
        ImageView imageView = (ImageView) findViewById(R.id.imageView2);
        // toilet name zeigen
        if (isNullOrEmpty(marker.getTitle()))
            name.setText("öffentlicher toilette");
        else
            name.setText(marker.getTitle());
        Toilet toilet = DataHolder.getInstance().findToiletbyLatLng(marker.getPosition());
        if (toilet.isPrivate())
            type.setText("Private Toilet");
        else
            type.setText("Public Toilet");
        String total="";

        if(toilet.getCost()==0.0)
        {
            cost.setText("");
        }
        else
        cost.setText("("+toilet.getCost()+" €)");


        if((int)toilet.getTotalRating()==0)
        {
            total = "Reviews : " + "" + " ("+toilet.getRatings().size()+") ";
        }
        if((int)toilet.getTotalRating()==1){
            total = "Reviews : " + "\uD83D\uDCA6" + " ("+toilet.getRatings().size()+") ";
        }
        if((int)toilet.getTotalRating()==2){
            total = "Reviews : " + "\uD83D\uDCA6\uD83D\uDCA6" + " ("+toilet.getRatings().size()+") ";
        }
        if((int)toilet.getTotalRating()==3){
            total = "Reviews : " + "\uD83D\uDCA6\uD83D\uDCA6\uD83D\uDCA6" + " ("+toilet.getRatings().size()+") ";
        }
        if((int)toilet.getTotalRating()==4){
            total = "Reviews : " + "\uD83D\uDCA6\uD83D\uDCA6\uD83D\uDCA6\uD83D\uDCA6" + " ("+toilet.getRatings().size()+") ";
        }
        if((int)toilet.getTotalRating()==5){
            total = "Reviews : " + "\uD83D\uDCA6\uD83D\uDCA6\uD83D\uDCA6\uD83D\uDCA6\uD83D\uDCA6" + " ("+toilet.getRatings().size()+") ";
        }
       totalrating.setText(total);

        // Reference to an image file in Cloud Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(toilet.getPhotoUrl());


        // Download directly from StorageReference using Glide
        // (See MyAppGlideModule for Loader registration)
        GlideApp.with(this /* context */)
                .load(storageReference)
                .into(imageView);

        TextView information=(TextView) findViewById(R.id.information);
        information.setText(toilet.getDescription());

        if(toilet.isOutoforder())
        {
            if( findViewById(R.id.ic_out_of_order).getVisibility()==View.GONE)
                findViewById(R.id.ic_out_of_order).setVisibility(View.VISIBLE);
        }
        else
        {
            if( findViewById(R.id.ic_out_of_order).getVisibility()==View.VISIBLE)
                findViewById(R.id.ic_out_of_order).setVisibility(View.GONE);
        }


        if(toilet.isFee()==null) {
            if (((ImageView) findViewById(R.id.ic_euro)).getVisibility() == View.VISIBLE)
                ((ImageView) findViewById(R.id.ic_euro)).setVisibility(View.GONE);
        }
        else{
        if(toilet.isFee().equals("yes")) {
            if(((ImageView) findViewById(R.id.ic_euro)).getVisibility() == View.GONE)
                 ((ImageView) findViewById(R.id.ic_euro)).setVisibility(View.VISIBLE);
        }
            if (toilet.isFee().equals("no")) {
                if (((ImageView) findViewById(R.id.ic_euro)).getVisibility() == View.VISIBLE)
                    ((ImageView) findViewById(R.id.ic_euro)).setVisibility(View.GONE);
            }


        }

        if(toilet.isWheelchair() == null){
            if(((ImageView) findViewById(R.id.ic_wheelchair)).getVisibility() == View.VISIBLE)
                ((ImageView) findViewById(R.id.ic_wheelchair)).setVisibility(View.GONE);

        }else{
        if(toilet.isWheelchair().equals("yes")) {
            if(((ImageView) findViewById(R.id.ic_wheelchair)).getVisibility() == View.GONE)
                ((ImageView) findViewById(R.id.ic_wheelchair)).setVisibility(View.VISIBLE);

        }
            if (toilet.isWheelchair().equals("no")) {
                if (((ImageView) findViewById(R.id.ic_wheelchair)).getVisibility() == View.VISIBLE)
                    ((ImageView) findViewById(R.id.ic_wheelchair)).setVisibility(View.GONE);
            }

        }

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(MapsActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new CommentAdapter(toilet.getComments());
        mRecyclerView.setAdapter(mAdapter);

    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        mlocation = myLocationListener.getLastBestLocation();
        isSearch =false;
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();

    }

    //--------------------------------------------
    //Direction
    //--------------------------------------------
  /*  @Override
    public void onClick(View v){
        int id = v.getId();
        if(id == R.id.direction_btn){
            requestDirection();
        }
    }
*/
    public void requestDirection() {
        //Snackbar.make(btnRequestDirection, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .alternativeRoute(true)
                .transportMode(TransportMode.WALKING)
                .transitMode(TransportMode.WALKING)
                .execute(this);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        //Snackbar.make(btnRequestDirection, "Success with status: " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        if(direction.isOK()){
            mMap.clear();
          //  refreshMarker();
            Route route = direction.getRouteList().get(0);
            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.BLUE));
            setCameraWithCoordinationBounds(route);

            btnRequestDirection.setVisibility(View.GONE);
        } else {
            Snackbar.make(btnRequestDirection, direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Snackbar.make(btnRequestDirection, t.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    public void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MapsActivity.this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            if(DataHolder.getInstance().getUser() != null) {
                startActivity(new Intent(MapsActivity.this, EditProfileActivity.class));
            }
            else
            {
                startActivity(new Intent(MapsActivity.this, LoginActivity.class));
            }

            // Handle the profile action
        } else if (id == R.id.nav_privToilet) {
            if(DataHolder.getInstance().getUser() != null) {
                if(DataHolder.getInstance().getUser().isAnbieter())
                    startActivity(new Intent(MapsActivity.this, AddActivity.class));
            }
            else
            {
                startActivity(new Intent(MapsActivity.this, LoginActivity.class));
            }

        } else if (id == R.id.nav_addCoupon) {
            if(DataHolder.getInstance().getUser() != null) {
                if(DataHolder.getInstance().getUser().isAnbieter())
                startActivity(new Intent(MapsActivity.this, EditProfileActivity.class));
            }
            else
            {
                startActivity(new Intent(MapsActivity.this, LoginActivity.class));
            }

        } else if (id == R.id.nav_statistic) {
            if(DataHolder.getInstance().getUser() != null) {
                if(DataHolder.getInstance().getUser().isAnbieter())
                startActivity(new Intent(MapsActivity.this, EditProfileActivity.class));
            }
            else
            {
                startActivity(new Intent(MapsActivity.this, LoginActivity.class));
            }

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(MapsActivity.this,SettingsActivity.class));

        } else if (id == R.id.nav_notification) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("Location", "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i("Location", "Fetching details for ID: " + item.placeId);




        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("Location", "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            LatLng sydney = place.getLatLng();
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                    sydney, 15);
            mMap.animateCamera(location);
            mlocation.setLatitude(sydney.latitude);
            mlocation.setLongitude(sydney.longitude);
            isSearch=true;
            Log.i("name", place.getName().toString());
            Log.i("coordinates", place.getLatLng().toString());
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i("Location", "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("Location", "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e("Location", "Google Places API connection suspended.");
    }

    public void showNoticeDialog() {
    // Create an instance of the dialog fragment and show it
    DialogFragment dialog = new EditToiletActivity(m_marker.getPosition());
    dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
}
    public void showNoticeDialogReview() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new AddReviewActivity(m_marker.getPosition());
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }
    public void refreshMarker() {
        mMap.clear();
        MarkerOptions markerPOI;
        for (Toilet t : DataHolder.getInstance().getData()) {
            markerPOI = new MarkerOptions();
            markerPOI.position(new LatLng(t.getLocationLat(), t.getLocationLon()))
                    .title(t.getName());

            if (t.isPrivate())
                markerPOI.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            else
                markerPOI.icon(BitmapDescriptorFactory.defaultMarker());

            mMap.addMarker(markerPOI);
        }
    }
}