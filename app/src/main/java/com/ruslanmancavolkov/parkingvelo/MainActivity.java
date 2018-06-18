package com.ruslanmancavolkov.parkingvelo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanmancavolkov.parkingvelo.models.Parcs;
import com.ruslanmancavolkov.parkingvelo.models.UsersParcs;
import com.ruslanmancavolkov.parkingvelo.services.GoogleMapRoutesBuilder;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

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
    //endregion

    //region Route builder
    private Polyline currentPolyline;
    private LocationManager mLocationManager;
    private LatLng currentUsersPosition;
    private float LOCATION_REFRESH_DISTANCE = 1;
    private long LOCATION_REFRESH_TIME = 100;
    private LocationListener mLocationListener;
    //endregion

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
                            currentUsersPosition = new LatLng(location.getLatitude(), location.getLongitude());
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
                googleMapRoutesBuilder = new GoogleMapRoutesBuilder();
                new RetrieveFeedTask().execute();
                break;
            default:
                break;
        }

        rfabHelper.toggleContent();
    }
    //endregion

    private void configureMarkerClick() {
        onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick ( final Marker marker){
                BuildFloatingButtons(1);
                LatLng position = (LatLng) (marker.getTag());
                clickedMarkerPosition = position;

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
        // Add a new marker to the map
        final Marker marker = this.mMap.addMarker(new MarkerOptions().position(position)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.bike_parc_pin)));
        marker.setTag(position);
        final String parcId = key;
        ref.child("parcs").child(parcId).child("cp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int parcCapacity = dataSnapshot.getValue(Integer.class);
                marker.setTitle(getString(R.string.parc_capacity) + " : " + String.valueOf(parcCapacity));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });

        this.markers.put(key, marker);
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
                Toast.makeText(MainActivity.this, "LatLng : " + point.latitude + point.longitude,
                        Toast.LENGTH_SHORT).show();



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

                final EditText edittext = new EditText(getApplicationContext());
                //alert.setMessage("Ajout d'un parc");
                alert.setTitle("Ajout d'un parc");

                alert.setView(layout);

                alert.setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String parcName = nameBox.getText().toString();
                        String parcCapacity = capacityBox.getText().toString();
                        Toast.makeText(MainActivity.this, "Nom du parc : " + parcName + " - " + parcCapacity,
                                Toast.LENGTH_LONG).show();

                        String uid = auth.getCurrentUser().getUid();
                        DatabaseReference postsRef = ref.child("users_parcs").child(uid);
                        String parcKey = postsRef.push().getKey();
                        ref.child("users_parcs").child(uid).child(parcKey).setValue(new UsersParcs(true));

                        DatabaseReference parcsRef = ref.child("parcs").child(parcKey);
                        parcsRef.setValue(new Parcs(parcName, Integer.parseInt(parcCapacity)));

                        geoFire = new GeoFire(ref.child("parcs_locations"));
                        geoFire.setLocation(parcKey, new GeoLocation(userPoint.latitude, userPoint.longitude));


                        //String key_of_data= ref.child("parcs").push().getKey();
                    }
                });

                alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });
            }
        };
    }

    private void configureMapClick() {
        onMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                BuildFloatingButtons(0);
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
