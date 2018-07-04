package com.ruslanmancavolkov.parkingvelo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruslanmancavolkov.parkingvelo.models.ParcWithPosition;
import com.ruslanmancavolkov.parkingvelo.models.Parcs;
import com.ruslanmancavolkov.parkingvelo.models.ParcsLocations;
import com.ruslanmancavolkov.parkingvelo.models.UsersPreferences;
import com.ruslanmancavolkov.parkingvelo.services.GoogleMapRoutesBuilder;
import com.ruslanmancavolkov.parkingvelo.utils.DateBuilder;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import org.florescu.android.rangeseekbar.RangeSeekBar;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@AILayout(R.layout.activity_main)
//AIActionBarActivity
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GeoQueryEventListener, RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {

    //region Floating Buttons
    //@AIView(R.id.activity_main_rfal)
    private RapidFloatingActionLayout rfaLayout;
    //@AIView(R.id.activity_main_rfab)
    private RapidFloatingActionButton rfaBtn;
    private RapidFloatingActionHelper rfabHelper;
    private RapidFloatingActionHelper rfabHelperBis;
//endregion

    //region Google Maps
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private PlaceDetectionClient mPlaceDetectionClient;
    private GoogleMapRoutesBuilder googleMapRoutesBuilder;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private GoogleMap.OnMapLongClickListener onMapLongClickListener;
    private GoogleMap.OnMapClickListener onMapClickListener;
    private GoogleMap.OnMarkerClickListener onMarkerClickListener;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    //endregion

    //region Firebase
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference ref;
    private DatabaseReference refParcsLocations;
    //endregion

    //region GeoFire
    private static final GeoLocation INITIAL_CENTER = new GeoLocation(37.7789, -122.4017);
    private static final int INITIAL_ZOOM_LEVEL = 14;
    private static final String GEO_FIRE_DB = "https://parking-velo.firebaseio.com/";
    private static final String GEO_FIRE_REF = GEO_FIRE_DB;
    private static final String GEO_FIRE_PARCS_REF = GEO_FIRE_DB + "/parcs_locations";

    //private Circle searchCircle;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    //endregion

    //region Markers
    private Map<String,Marker> markers;
    //endregion

    //region Marker Click
    private LatLng clickedMarkerPosition;
    private Parcs clickedMarkerParc;
    //endregion

    //region Route builder
    private Polyline currentPolyline;
    private LocationManager mLocationManager;
    private LatLng currentUsersPosition;
    private float LOCATION_REFRESH_DISTANCE = 1;
    private long LOCATION_REFRESH_TIME = 100;
    private LocationListener mLocationListener;
    //endregion

    LinearLayout likeDislikeLayout;
    ImageButton btnLike, btnDislike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        googleMapRoutesBuilder = new GoogleMapRoutesBuilder();

        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                //your code here
                currentUsersPosition = new LatLng(location.getLatitude(), location.getLongitude());
            }
            @Override
            public void onStatusChanged(String str, int in, Bundle bundle){

            }
            @Override
            public void onProviderEnabled(String str){

            }
            @Override
            public void onProviderDisabled(String str){

            }
        };

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPSEnabled = mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        Log.v("isGPSEnabled", "=" + isGPSEnabled);

        // getting network status
        boolean isNetworkEnabled = mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        try {
            if (isGPSEnabled == false && isNetworkEnabled == false) {
                // no network provider is enabled
            } else {
                if (isNetworkEnabled) {
                    currentUsersPosition = null;
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            LOCATION_REFRESH_TIME,
                            LOCATION_REFRESH_DISTANCE, mLocationListener);
                    Log.d("Network", "Network");
                    if (mLocationManager != null) {
                        Location location = mLocationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        currentUsersPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    currentUsersPosition = null;
                    if (currentUsersPosition == null) {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                LOCATION_REFRESH_TIME,
                                LOCATION_REFRESH_DISTANCE, mLocationListener);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (mLocationManager != null) {
                            Location location = mLocationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                currentUsersPosition = new LatLng(location.getLatitude(), location.getLongitude());
                            }
                        }
                    }
                }
            }
        }
        catch (SecurityException ex){

        }

        /*mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);*/

        // setup GeoFire
        ref = FirebaseDatabase.getInstance(FirebaseApp.getInstance()).getReferenceFromUrl(GEO_FIRE_REF);
        refParcsLocations = FirebaseDatabase.getInstance(FirebaseApp.getInstance()).getReferenceFromUrl(GEO_FIRE_PARCS_REF);
        this.geoFire = new GeoFire(refParcsLocations);
        // radius in km
        this.geoQuery = this.geoFire.queryAtLocation(INITIAL_CENTER, 1);

        // setup markers
        this.markers = new HashMap<String, Marker>();

        BuildFloatingButtons(0);

        configureCameraIdle();
        configureMapLongClick();
        configureMapClick();
        configureMarkerClick();

        likeDislikeLayout = findViewById(R.id.like_dislike_layout);
        btnLike = findViewById(R.id.btn_like);
        btnDislike = findViewById(R.id.btn_dislike);

        btnLike.setPressed(true);
        btnDislike.setPressed(true);

        btnLike.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    //likeDislikeLayout.setVisibility(View.GONE);
                    DatabaseReference notationsRef = ref.child("parcs_notations");
                    String uid = auth.getCurrentUser().getUid();
                    if (btnLike.isPressed()) {
                        notationsRef.child(clickedMarkerParc.getId()).child("likes").child(uid).child("d").setValue(DateBuilder.GetCurrentDate());
                    } else {
                        notationsRef.child(clickedMarkerParc.getId()).child("likes").child(uid).removeValue();
                    }

                    if (!btnDislike.isPressed()) {
                        notationsRef.child(clickedMarkerParc.getId()).child("dislikes").child(uid).removeValue();
                    }

                    boolean test = btnLike.isPressed();
                    btnLike.setPressed(!test);

                    if (!btnDislike.isPressed()) {
                        btnDislike.setPressed(true);
                    }
                }

                return true;
            }
        });

        btnDislike.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    //likeDislikeLayout.setVisibility(View.GONE);
                    DatabaseReference notationsRef = ref.child("parcs_notations");
                    String uid = auth.getCurrentUser().getUid();
                    if (btnDislike.isPressed()) {
                        notationsRef.child(clickedMarkerParc.getId()).child("dislikes").child(uid).child("d").setValue(DateBuilder.GetCurrentDate());
                    } else {
                        notationsRef.child(clickedMarkerParc.getId()).child("dislikes").child(uid).removeValue();
                    }

                    if (!btnLike.isPressed()) {
                        notationsRef.child(clickedMarkerParc.getId()).child("likes").child(uid).removeValue();
                    }

                    boolean test = btnDislike.isPressed();
                    btnDislike.setPressed(!test);

                    if (!btnLike.isPressed()) {
                        btnLike.setPressed(true);
                    }
                }

                return true;
            }
        });
    }

    //region Floating Buttons
    public void BuildFloatingButtons(int state){
        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(MainActivity.this);
        rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
        List<RFACLabelItem> items = new ArrayList<>();
        items.add(new RFACLabelItem<Integer>()
                .setLabel(getString(R.string.btn_account))
                .setResId(R.mipmap.icon_profil)
                .setIconNormalColor(0xffffffff)
                .setIconPressedColor(0xffffffff)
                .setWrapper(0)
                .setLabelSizeSp(14)
        );

        items.add(new RFACLabelItem<Integer>()
                .setLabel(getString(R.string.btn_parcs))
                .setResId(R.mipmap.icon_parcs)
                .setIconNormalColor(0xffffffff)
                .setIconPressedColor(0xffffffff)
                .setWrapper(0)
                .setLabelSizeSp(14)
        );

        items.add(new RFACLabelItem<Integer>()
                .setLabel(getString(R.string.btn_filters))
                .setResId(R.mipmap.filter_none)
                .setIconNormalColor(0xffffffff)
                .setIconPressedColor(0xffffffff)
                .setWrapper(0)
                .setLabelSizeSp(14)
        );

        // If the state is 1, add the button which builds the route to the parc
        if (state == 1){
            items.add(new RFACLabelItem<Integer>()
                    .setLabel(getString(R.string.btn_show_route))
                    .setResId(R.mipmap.logo_route)
                    .setIconNormalColor(0xffffffff)
                    .setIconPressedColor(0xffffffff)
                    .setWrapper(0)
                    //.setLabelSizeSp(14)
            );
        }

        rfaContent
                .setItems(items)
                .setIconShadowColor(0xff888888)
        //.setIconShadowRadius(5)
        //.setIconShadowDy(5)
        //.setIconShadowRadius(ABTextUtil.dip2px(MainActivity.this, 5))
        //.setIconShadowDy(ABTextUtil.dip2px(MainActivity.this, 5))
        ;

        RapidFloatingActionButton rfaBtn = findViewById(R.id.activity_main_rfab);
        RapidFloatingActionLayout rfaLayout = findViewById(R.id.activity_main_rfal);

        rfabHelper = new RapidFloatingActionHelper(
                MainActivity.this,
                rfaLayout,
                rfaBtn,
                rfaContent
        ).build();

        /*if (state == 1){
            rfaBtn.performClick();
        }*/
    }

    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem item) {
        switch (position) {
            case 0:
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                break;
            case 1:
                startActivity(new Intent(MainActivity.this, ParcsActivity.class));
                break;
            case 2:
                SetupPreferencesUi();
                break;
            case 3:
                googleMapRoutesBuilder = new GoogleMapRoutesBuilder();
                new RetrieveFeedTask().execute();
                break;
            default:
                break;
        }

        rfabHelper.toggleContent();
    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem item) {
        switch (position) {
            case 0:
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                break;
            case 1:
                startActivity(new Intent(MainActivity.this, ParcsActivity.class));
                break;
            case 2:
                SetupPreferencesUi();
                break;
            case 3:
                googleMapRoutesBuilder = new GoogleMapRoutesBuilder();
                new RetrieveFeedTask().execute();
                break;
            default:
                break;
        }

        rfabHelper.toggleContent();
    }

    public void SetupPreferencesUi(){

        //region Retrieving User Preferences
        String uid = auth.getCurrentUser().getUid();
        ref.child("users_preferences").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final UsersPreferences usersPreferences = dataSnapshot.getValue(UsersPreferences.class);
                SetupPreferencesAlert(usersPreferences);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });
        //region
    }

    public void SetupPreferencesAlert(UsersPreferences usersPreferences){
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        Context context = getApplicationContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final Switch switchButton = new Switch(getApplicationContext());
        switchButton.setText(getString(R.string.switch_shared));
        switchButton.setGravity(Gravity.RIGHT);
        switchButton.setChecked(usersPreferences != null ? usersPreferences.getSs() : true);
        layout.addView(switchButton);

        //region Seekbar Capacity
        LinearLayout layoutSeekbarCapacity = new LinearLayout(context);

        TextView tvSeekbarCapacity = new TextView(context);
        layoutSeekbarCapacity.setOrientation(LinearLayout.HORIZONTAL);
        layoutSeekbarCapacity.setWeightSum(2);

        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lParams.weight = 1.6f;
        tvSeekbarCapacity.setLayoutParams(lParams);
        tvSeekbarCapacity.setText(R.string.seekbar_capacity);
        tvSeekbarCapacity.setPadding(0,100,0,0);
        layoutSeekbarCapacity.addView(tvSeekbarCapacity);

        final RangeSeekBar<Integer> seekbarCapacity = new RangeSeekBar<>(this);
        seekbarCapacity.setRangeValues(0, 500);
        seekbarCapacity.setSelectedMinValue(usersPreferences != null ? usersPreferences.getCmi() : 0);
        seekbarCapacity.setSelectedMaxValue(usersPreferences != null ? usersPreferences.getCma() : 500);
        seekbarCapacity.setTextAboveThumbsColorResource(R.color.colorAccent);
        LinearLayout.LayoutParams lParamsSeekBar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lParamsSeekBar.weight = 0.4f;
        seekbarCapacity.setLayoutParams(lParamsSeekBar);

        layoutSeekbarCapacity.addView(seekbarCapacity);
        layout.addView(layoutSeekbarCapacity);
        //endregion

        //region Seekbar Note
        LinearLayout layoutSeekbarNote = new LinearLayout(context);

        TextView tvSeekbarNote = new TextView(context);
        layoutSeekbarNote.setOrientation(LinearLayout.HORIZONTAL);
        layoutSeekbarNote.setWeightSum(2);

        LinearLayout.LayoutParams lParamsNote = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lParamsNote.weight = 1.6f;
        tvSeekbarNote.setLayoutParams(lParamsNote);
        tvSeekbarNote.setText(R.string.seekbar_note);
        tvSeekbarNote.setPadding(0,100,0,0);
        layoutSeekbarNote.addView(tvSeekbarNote);

        final RangeSeekBar<Integer> seekbarNote = new RangeSeekBar<>(this);
        seekbarNote.setRangeValues(-100, 100);
        seekbarNote.setSelectedMinValue(usersPreferences != null ? usersPreferences.getNmi() : -100);
        seekbarNote.setSelectedMaxValue(usersPreferences != null ? usersPreferences.getNma() : 100);
        seekbarNote.setTextAboveThumbsColorResource(R.color.colorAccent);
        LinearLayout.LayoutParams lParamsSeekBarNote = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lParamsSeekBarNote.weight = 0.4f;
        seekbarNote.setLayoutParams(lParamsSeekBarNote);

        layoutSeekbarNote.addView(seekbarNote);
        layout.addView(layoutSeekbarNote);
        //endregion

        layout.setPadding(25,50,25,25);

        //final TextView seekBarValue = (TextView)findViewById(R.id.seekbarvalue);

        alert.setTitle("Préférences");
        alert.setView(layout);

        alert.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String uid = auth.getCurrentUser().getUid();
                DatabaseReference parcsRef = ref.child("users_preferences");
                parcsRef.child(uid).child("ss").setValue(switchButton.isChecked());
                parcsRef.child(uid).child("cmi").setValue(seekbarCapacity.getSelectedMinValue());
                parcsRef.child(uid).child("cma").setValue(seekbarCapacity.getSelectedMaxValue());
                parcsRef.child(uid).child("nmi").setValue(seekbarNote.getSelectedMinValue());
                parcsRef.child(uid).child("nma").setValue(seekbarNote.getSelectedMaxValue());
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });

        alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();
    }

    //endregion

    private void configureMarkerClick() {
        onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick ( final Marker marker){
                BuildFloatingButtons(1);
                ParcWithPosition parcWithPosition = (ParcWithPosition) (marker.getTag());
                clickedMarkerPosition = parcWithPosition.getPosition();
                clickedMarkerParc = parcWithPosition.getParc();

                DatabaseReference notationsRef = ref.child("parcs_notations");
                String uid = auth.getCurrentUser().getUid();

                //btnLike.setPressed(true);
                //btnDislike.setPressed(true);

                notationsRef.child(clickedMarkerParc.getId()).child("likes").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Object obj = dataSnapshot.getValue();
                        if (obj != null){
                            btnLike.setPressed(false);
                            btnDislike.setPressed(true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // ...
                    }
                });

                notationsRef.child(clickedMarkerParc.getId()).child("dislikes").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Object obj = dataSnapshot.getValue();
                        if (obj != null){
                            btnDislike.setPressed(false);
                            btnLike.setPressed(true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // ...
                    }
                });

                likeDislikeLayout.setVisibility(View.VISIBLE);

                return false;
            }
        };
    }

    //region Route building
    class RetrieveFeedTask extends AsyncTask<String, Void, Document> {

        private Exception exception;

        protected Document doInBackground(String... urls) {
            try {
                    /*Document doc = googleMapRoutesBuilder.getDocument(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), clickedMarkerPosition,
                        GoogleMapRoutesBuilder.MODE_DRIVING);*/
                Document doc = googleMapRoutesBuilder.getDocument(new LatLng(currentUsersPosition.latitude, currentUsersPosition.longitude), clickedMarkerPosition,
                        GoogleMapRoutesBuilder.MODE_CYCLING);

                return doc;
            } catch (Exception e) {
                this.exception = e;

                return null;
            }
        }

        protected void onPostExecute(Document doc) {
            if (doc != null) {
                if (currentPolyline != null){
                    currentPolyline.remove();
                }

                BuildGoogleMapRoutes(doc);
            }
            else {
                Toast.makeText(MainActivity.this, getString(R.string.routes_fail), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void BuildGoogleMapRoutes(Document doc){
        ArrayList<LatLng> directionPoint = googleMapRoutesBuilder.getDirection(doc);
        PolylineOptions rectLine = new PolylineOptions().width(8).color(
                Color.BLUE);

        for (int i = 0; i < directionPoint.size(); i++) {
            rectLine.add(directionPoint.get(i));
        }

        Polyline polylin = mMap.addPolyline(rectLine);
        currentPolyline = polylin;
    }
    //endregion

    //region GeoFire
    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        final LatLng position = new LatLng(location.latitude, location.longitude);
        // Add a new marker to the map*

        final String parcId = key;
        ref.child("parcs").child(parcId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Parcs parc = dataSnapshot.getValue(Parcs.class);
                parc.setId(parcId);
                final ParcWithPosition tag = new ParcWithPosition(parc, position);
                final String uid = auth.getCurrentUser().getUid();

                ref.child("users_preferences").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final UsersPreferences usersPreferences = dataSnapshot.getValue(UsersPreferences.class);
                        Marker marker = null;
                        Boolean preferencesOk = true;
                        if (usersPreferences != null){
                            if (usersPreferences.getSs() != parc.getS()){
                                preferencesOk = false;
                            }

                            if (parc.getCp() < usersPreferences.getCmi() || parc.getCp() > usersPreferences.getCma()){
                                preferencesOk = false;
                            }
                        }

                        // Si le parc est partagé
                        if ((parc.getS() || uid.equals(parc.getU())) && preferencesOk) {
                            marker = mMap.addMarker(new MarkerOptions().position(position)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.bike_parc_pin)));
                            marker.setTag(tag);
                            marker.setTitle(getString(R.string.parc_capacity) + " : " + String.valueOf(parc.getCp()));
                            markers.put(parcId, marker);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // ...
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });

        //this.markers.put(key, marker);
    }

    @Override
    public void onKeyExited(String key) {
        // Remove any old marker
        Marker marker = this.markers.get(key);
        if (marker != null) {
            marker.remove();
            this.markers.remove(key);
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        // Move the marker
        Marker marker = this.markers.get(key);
        if (marker != null) {
            this.animateMarkerTo(marker, location.latitude, location.longitude);
        }
    }

    @Override
    public void onGeoQueryReady() {
    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    //endregion

    //region Google Maps
    // Animation handler for old APIs without animation support
    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 3000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final LatLng startPosition = marker.getPosition();
        handler.post(new Runnable() {
            @Override
            public void run() {
                float elapsed = SystemClock.uptimeMillis() - start;
                float t = elapsed/DURATION_MS;
                float v = interpolator.getInterpolation(t);

                double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                marker.setPosition(new LatLng(currentLat, currentLng));

                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private double zoomLevelToRadius(double zoomLevel) {
        // Approximation to fit circle into view
        return 26384000/Math.pow(2, zoomLevel);
    }

    private void configureCameraIdle() {
        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                LatLng center = mMap.getCameraPosition().target;
                double radius = zoomLevelToRadius(mMap.getCameraPosition().zoom);
                //searchCircle.setCenter(center);
                //searchCircle.setRadius(radius);
                geoQuery.setCenter(new GeoLocation(center.latitude, center.longitude));
                // radius in km
                geoQuery.setRadius(radius / 1000);
            }
        };
    }

    private void configureMapLongClick() {
        onMapLongClickListener = new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                final LatLng userPoint = point;

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                Context context = getApplicationContext();
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                // Add a TextView here for the "Title" label, as noted in the comments
                final EditText nameBox = new EditText(context);
                nameBox.setHint("Nom");
                layout.addView(nameBox); // Notice this is an add method

                // Add another TextView here for the "Description" label
                final EditText capacityBox = new EditText(context);
                capacityBox.setHint("Capacité");
                capacityBox.setInputType(InputType.TYPE_CLASS_NUMBER);
                layout.addView(capacityBox); // Another add method

                final Switch switchButton = new Switch(getApplicationContext());
                switchButton.setText(getString(R.string.publishing_switch));
                switchButton.setGravity(Gravity.RIGHT);
                layout.addView(switchButton); // Another add method*/

                alert.setTitle("Ajout d'un parc");
                alert.setView(layout);

                alert.setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String parcName = nameBox.getText().toString();
                        String parcCapacity = capacityBox.getText().toString();
                        boolean published = switchButton.isChecked();

                        String uid = auth.getCurrentUser().getUid();
                        DatabaseReference parcsRef = ref.child("parcs");
                        String parcKey = parcsRef.push().getKey();
                        parcsRef.child(parcKey).setValue(new Parcs(parcName, Integer.parseInt(parcCapacity), published, uid));

                        geoFire = new GeoFire(ref.child("parcs_locations"));
                        geoFire.setLocation(parcKey, new GeoLocation(userPoint.latitude, userPoint.longitude));
                    }
                });

                alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
            }
        };
    }

    private void configureMapClick() {
        onMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                BuildFloatingButtons(0);
                likeDislikeLayout.setVisibility(View.INVISIBLE);
                btnLike.setPressed(true);
                btnDislike.setPressed(true);
            }
        };
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }
        return true;
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        LatLng latLngCenter = new LatLng(INITIAL_CENTER.latitude, INITIAL_CENTER.longitude);
        //this.searchCircle = this.mMap.addCircle(new CircleOptions().center(latLngCenter).radius(1000));
        //this.searchCircle.setFillColor(Color.argb(66, 137, 180, 56));
        //this.searchCircle.setStrokeColor(Color.argb(66, 137, 180, 56));
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngCenter, INITIAL_ZOOM_LEVEL));

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });


        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        mMap.setOnCameraIdleListener(onCameraIdleListener);
        mMap.setOnMapLongClickListener(onMapLongClickListener);
        mMap.setOnMapClickListener(onMapClickListener);
        mMap.setOnMarkerClickListener(onMarkerClickListener);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d("TAG", "Current location is null. Using defaults.");
                            Log.e("TAG", "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    //endregion

    //region Permissions
    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
    //endregion

    //region Google Maps others
    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();

                            } else {
                                Log.e("TAG", "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i("TAG", "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    //endregion

    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
        this.geoQuery.addGeoQueryEventListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }

        this.geoQuery.removeAllListeners();
        for (Marker marker: this.markers.values()) {
            marker.remove();
        }
        this.markers.clear();
    }
}
