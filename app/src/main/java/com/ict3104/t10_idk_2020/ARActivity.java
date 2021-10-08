package com.ict3104.t10_idk_2020;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;
import com.ict3104.t10_idk_2020.model.Weather;
import com.ict3104.t10_idk_2020.repository.AsyncResponse;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.google.ar.sceneform.math.Quaternion.lookRotation;

public class ARActivity extends AppCompatActivity  implements SensorEventListener, LocationListener{
    //db
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ARFragment arFragment;
    private SupportMapFragment mapFragment;
    private ImageView mdirectionPic;

    //Map
    private static final int DEFAULT_ZOOM = 18;
    GoogleMap map = null;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    //Token Object
    AnchorNode anchorNode;
    List<PlaceToken> places = new ArrayList<PlaceToken>();

    //for current location
    Location mCurrentLocation;
    String mLastUpdateTime;
    private GoogleApiClient googleApiClient;
    private Marker mCurrLocationMarker;
    private LocationRequest mLocationRequest;
    private ArrayList<LatLng> routePoints;
    private Polyline line;
    private LatLng c;
    private int nearestPointIndex;
    private LatLng origin;
    private LatLng dest;
    private Location location;

    //3d rotation
    private float centx;
    private float centy;

    //sensor
    private SensorManager manager;
    private Sensor orientationSensor;

    //store data for accelerometer
    private float[] accelerometerReading = new float[3];
    //store date for magnetometer 
    private float[] magnetometerReading = new float[3];
    //data for the rotation matrix, store the combined magnetomer and accelerometer
    private final float[] rotationMatrix = new float[9];
    //data for orientation angles, mainly used this
    private final float[] orientationAngles = new float[3];

    private float fromDegrees;
    private double angleRotating;
    private TextView routeWeather;
    private int k;
    private int degreesx;
    private int degreesy;
    private int degreesz;

    //for route data
    List<HashMap<String, String>> listResult = null;
    ArrayList points2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        //get list data
        listResult = (List<HashMap<String, String>>) getIntent().getSerializableExtra("ResultList");
        //get start and end location
        origin = new LatLng(Double.parseDouble(listResult.get(0).get("startlocationlat")),Double.parseDouble(listResult.get(0).get("startlocationlng")));
        dest = new LatLng(Double.parseDouble(listResult.get(0).get("endlocationlat")),Double.parseDouble(listResult.get(0).get("endlocationlng")));

        arFragment = (ARFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps_fragment);
        mdirectionPic = (ImageView) findViewById(R.id.direction_pic);
        // Construct a FusedLocationProviderClient.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getApplicationContext());

        setUpPlaces();
        setUpMaps();
        setUpAr();


        // android 6.0 or above auto granted, or set targetSdkVersion vto 23 or below
        if (ActivityCompat.checkSelfPermission(ARActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ARActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
        }

        //get LocationManager object from system
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //get latest current location from GPS
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        c = new LatLng(location.getLatitude(), location.getLongitude());

        //get nearest dot
        points2 = new ArrayList();
        for (int j = 1; j < listResult.size(); j++) {
            HashMap<String, String> point = listResult.get(j);

            double lat = Double.parseDouble(point.get("lat"));
            double lng = Double.parseDouble(point.get("lng"));
            LatLng position = new LatLng(lat, lng);
            if ((j % 2) == 0) {
                points2.add(position);
            }
        }
        nearestPointIndex = findNearestPoint(c,points2);
        if(nearestPointIndex>0){
            ArrayList p3 = new ArrayList();
            for (int j = nearestPointIndex; j < points2.size(); j++) {
                LatLng point = (LatLng) points2.get(j);
                p3.add(point);
            }
            points2 = p3;
            //points2 = (ArrayList) points2.subList(nearestPointIndex, 4);
        }


        angleRotating = (float) angleFromCoordinate(c, (LatLng) points2.get(1));
        //set get location interval to 1 second
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,      //GPS定位提供者
                2000,       //更新数据时间为2秒
                1,      //位置间隔为1米
                //位置监听器
                new LocationListener() {  //active when GPS data changed, used to update location info

                    @Override
                    public void onLocationChanged(Location location) {
                        //update current lacation when the GPS changed
                        c = new LatLng(location.getLatitude(), location.getLongitude());
                        updateCameraBearing(map, location.getBearing());
                        nearestPointIndex = findNearestPoint(c,points2);
                        if(nearestPointIndex>0){
                            ArrayList p3 = new ArrayList();
                            for (int j = nearestPointIndex; j < points2.size(); j++) {
                                LatLng point = (LatLng) points2.get(j);
                                p3.add(point);
                            }
                            points2 = p3;
                            DrawDots(listResult,points2);
                        }
                    }

                    @Override
                    //trigger when the location status changed
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    //triggered when the provider enabled
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    //triggered when the provider disabled
                    public void onProviderDisabled(String provider) {
                    }
                });

        // arrow rotation
        centx = mdirectionPic.getX() + mdirectionPic.getWidth() / 2;
        centy = mdirectionPic.getY() + mdirectionPic.getHeight() / 2;

        Matrix mMatrix = new Matrix();
        Bitmap bmp = ((BitmapDrawable) getResources().getDrawable(
                R.drawable.myup2)).getBitmap();
        Camera camera = new Camera();
        camera.save();
        //camera.rotateY(50f);
        camera.rotateX(80);
        camera.rotateZ((float) angleRotating);
        camera.getMatrix(mMatrix);
        camera.restore();
        mMatrix.preTranslate(-centx, -centy);
        mMatrix.postTranslate(centx, centy);
        Bitmap bm = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                bmp.getHeight(), mMatrix, true);

        mdirectionPic.setImageBitmap(bm);

        //for sensor
        manager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        //for wheather
        /* Retrieve weather from API */
        ImageView wheatherIconimageView = (ImageView) findViewById(R.id.wheatherIconimageView);
        routeWeather = (TextView)findViewById(R.id.routeWeather);
        Picasso.with(this).load("http://openweathermap.org/img/wn/10d@2x.png").into(wheatherIconimageView);
        FetchWeather fetchWeather = new FetchWeather(new AsyncResponse() {

            @Override
            public void processFinish(Weather weather) {
                routeWeather.setText(weather.getWeather() + " " + weather.getTemp() + "°C");
            }
        });

        fetchWeather.execute(String.valueOf(origin.latitude), String.valueOf(origin.longitude)); // Singapore Lat & Long



    }

    private void setUpPlaces(){
        //loop through database for places.
        db.collection("Tokens").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String name = document.get("name").toString();
                                String desc = document.get("description").toString();
                                String[] latLng = document.get("latlng").toString().split(",");
                                double latitude = Double.parseDouble(latLng[0]);
                                double longitude = Double.parseDouble(latLng[1]);
                                places.add(new PlaceToken(id, name, desc, new LatLng(latitude, longitude)));
                            }
                        }
                    }
                });
    }

    private void setUpAr() {
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                Anchor anchor = hitResult.createAnchor();
                anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());
                addPlaces(anchorNode);
            }
        });
    }

    private void setUpMaps() {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                getLocationPermission();
                getCurrentLocation();
                updateLocationUI();

                //zoom map
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(c, DEFAULT_ZOOM));

                //LatLng origin = new LatLng(1.37, 103.84);
                //LatLng dest = new LatLng(1.35, 103.94);
                //String bywhat = "walking";
                //String[] origintemp = getIntent().getStringExtra("ORIGIN").split(",");
                //final LatLng origin = new LatLng(Double.parseDouble(origintemp[0]), Double.parseDouble(origintemp[1]));
                //String[] desttemp = getIntent().getStringExtra("DEST").split(",");
                //final LatLng dest = new LatLng(Double.parseDouble(desttemp[0]), Double.parseDouble(desttemp[1]));
                MarkerOptions options = new MarkerOptions();

                // this is the start location marker in green
                options.position(origin);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                // the is the end location marker in red
                map.addMarker(options);
                options.position(dest);

                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                map.addMarker(options);

                //-------------------------------here is where we called google api---------------------
                // Getting URL to the Google Directions API
                /*HashMap<String, String> tempv1 = new HashMap<String, String>() {{
                    put("lng", "103.84658");
                    put("lat", "1.37723");
                }};
                HashMap<String, String> tempv2 = new HashMap<String, String>() {{
                    put("lng", "103.84691");
                    put("lat", "1.37716");
                }};
                HashMap<String, String> tempv3 = new HashMap<String, String>() {{
                    put("lng", "103.84705");
                    put("lat", "1.37713");
                }};
                HashMap<String, String> tempv4 = new HashMap<String, String>() {{
                    put("lng", "103.84725");
                    put("lat", "1.37708");
                }};
                List<HashMap<String, String>> listResult = new ArrayList<HashMap<String, String>>() {{
                    add(tempv1);
                    add(tempv2);
                    add(tempv3);
                    add(tempv4);
                }};
                */
                DrawDots(listResult,points2);

                // ------------------here is the end of calling api as well as drawing lines----

            }
        });
    }
    
    private int findNearestPoint(LatLng test, List<LatLng> target) {
        float distance = distanceTo(test, target.get(0));
        int pointIndex = -1;
        //LatLng minimumDistancePoint = test;

        if (test == null || target == null) {
            return -1;
        }

        for (int i = 1; i < target.size(); i++) {
            LatLng point = target.get(i);

            int segmentPoint = i + 1;
            if (segmentPoint >= target.size()) {
                segmentPoint = 0;
            }

            float currentDistance = distanceTo(test, target.get(segmentPoint));
            if (distance == -1 || currentDistance < distance) {
                distance = currentDistance;
                pointIndex = i;
            }
        }

        return pointIndex;
    }
    public float distanceTo (LatLng pa, LatLng pb)
    {
        float lat_a = (float) pa.latitude;
        float lng_a = (float) pa.longitude;
        float lat_b = (float) pb.latitude;
        float lng_b = (float) pb.longitude;
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if ( googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .bearing(bearing)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }
    //for google map route
    private void DrawDots(List<HashMap<String, String>> result,ArrayList point2) {
        map.clear();
        MarkerOptions options = new MarkerOptions();

        // this is the start location marker in green
        options.position(origin);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        // the is the end location marker in red
        map.addMarker(options);
        options.position(dest);

        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        map.addMarker(options);
        ArrayList points = null;

        PolylineOptions lineOptions = null;

        MarkerOptions markerOptions = new MarkerOptions();
        points = new ArrayList();


        lineOptions = new PolylineOptions();
        List<HashMap<String, String>> path = result;

        for (int j = 1; j < path.size(); j++) {
            HashMap<String, String> point = path.get(j);

            double lat = Double.parseDouble(point.get("lat"));
            double lng = Double.parseDouble(point.get("lng"));
            LatLng position = new LatLng(lat, lng);
            points.add(position);

        }

        lineOptions.addAll(points);
        lineOptions.width(12);

        lineOptions.color(Color.RED);

        lineOptions.geodesic(true);
        map.addPolyline(lineOptions);

        for (Object k : points2) {
            map.addCircle(new CircleOptions()
                    .center((LatLng) k)
                    .radius(3)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.BLUE));
        }
    }

    //for 3D tags
    private void addPlaces(AnchorNode anchorNode) {
        if (lastKnownLocation == null) {
            return;
        }

        if (places == null) {
            return;
        }

        for (int i = 0; i < places.size(); i++) {
            PlaceToken place = places.get(i);

            double placeLat = place.latlng.latitude;
            double placeLng = place.latlng.longitude;

            LatLng startLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            LatLng endLatLng = new LatLng(placeLat, placeLng);
            Double distance = SphericalUtil.computeDistanceBetween(startLatLng, endLatLng);

            //Only display tokens within 3KM away from current location
            if (distance < 3000){
                PlaceNode placeNode = new PlaceNode(this, place);
                ObjNode objNode = new ObjNode(this, place);
                placeNode.setParent(anchorNode);
                objNode.setParent(anchorNode);

                Location tLocation = new Location("temp");
                tLocation.setLatitude(place.latlng.latitude);
                tLocation.setLongitude(place.latlng.longitude);

                double latDiff = lastKnownLocation.getLatitude() - tLocation.getLatitude();
                double lngDiff = lastKnownLocation.getLongitude() - tLocation.getLongitude();

                int conversionFactor = 111000;
                float x = (float) latDiff * conversionFactor;
                float z = (float) lngDiff * conversionFactor;
                float y = 0.0F;

                placeNode.setWorldPosition(new Vector3(x, y, z));
                objNode.setWorldPosition(new Vector3(x, 1, z));

                Vector3 cameraPosition = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
                Vector3 direction = Vector3.subtract(cameraPosition, placeNode.getWorldPosition());
                Vector3 direction2 = Vector3.subtract(cameraPosition, placeNode.getWorldPosition());
                Quaternion lookRotation = lookRotation(direction, Vector3.up());
                Quaternion lookRotation2 = lookRotation(direction2, Vector3.up());
                placeNode.setWorldRotation(lookRotation);
                objNode.setWorldRotation(lookRotation2);

                //placeNode.setLocalPosition(place.getPositionVector(orientationAngles[0], new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())));
                placeNode.setOnTapListener((hitTestResult, motionEvent) -> {
                    showInfoWindow(place);
                });
                //Add the place in maps
                MarkerOptions options = new MarkerOptions();
                options.title(place.name);
                options.position(place.latlng);
                map.addMarker(options);
            }
        }
    }

    private void showInfoWindow(PlaceToken place){
        if(anchorNode == null){
            return;
        }

        for(int i = 0; i < anchorNode.getChildren().size(); i++){
            //Check if the node is not the Sphere Obj.
            if(anchorNode.getChildren().get(i).getClass() == ObjNode.class){
                continue;
            }
            PlaceNode matchingPlaceNode = (PlaceNode)anchorNode.getChildren().get(i);

            if(matchingPlaceNode.place == place){
                matchingPlaceNode.showInfoWindow();
            }
        }
    }

    private void getCurrentLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                updateCameraBearing(map, location.getBearing());
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

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
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //for sensor
    @Override
    protected void onResume() {
        super.onResume();

        if (orientationSensor != null) {
            manager.registerListener(this, orientationSensor, 600000);
        }


        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            manager.registerListener(this, accelerometer,
                    600000, 600000);
        }
        Sensor magneticField = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            manager.registerListener(this, magneticField,
                    600000, 600000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (manager != null) {
            manager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        float degree = event.values[0];
        StringBuilder sb = new StringBuilder();
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            System.arraycopy(event.values, 0, accelerometerReading,
//                    0, accelerometerReading.length);
            accelerometerReading = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            System.arraycopy(event.values, 0, magnetometerReading,
//                    0, magnetometerReading.length);
            magnetometerReading = event.values.clone();
        }
        updateOrientationAngles();
    }
    private double angleFromCoordinate(LatLng tempStart, LatLng tempEnd) {
        double long1 = tempStart.longitude;
        double long2 = tempEnd.longitude;
        double lat1 = tempStart.latitude;
        double lat2 = tempEnd.latitude;
        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        brng = 360 - brng; // count degrees counter-clockwise - remove to make clockwise

        return brng;
    }

    @SuppressLint("WrongConstant")
    public void updateOrientationAngles() {
        // update rotation matrix.
        // var1：oritation 
        // var2 ：default is null, mag to gravrity
        //  var3：accel
        // var4：magn
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        //calculate the device oritation based on the rotation matrix
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        if (orientationAngles.length >= 3) {
            if(k == 50){

                //converts an angle measured in radians to an approximately equivalent angle measured in degrees.

                degreesx = degreesx/50;
                degreesy = degreesy/50;
                degreesz = degreesz/50;
                if(degreesy<-77 || Math.abs(degreesx)>175){
                    mdirectionPic.setVisibility(View.INVISIBLE);
                }
                else if (Math.abs(degreesz)>100 && degreesy<-0){
                    degreesx = degreesx - 180;
                    mdirectionPic.setVisibility(View.VISIBLE);
                }
                else {
                    mdirectionPic.setVisibility(View.VISIBLE);
                }


                // device clockwise is positive, therefore the img need to rotate anti-clockwise 
                //rotate using the center of the object as center and save the current angle, next time start from this angle
                //RotateAnimation ra = new RotateAnimation(fromDegrees, -degrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                //set time of animation to 200 ms
                //ra.setDuration(200);
                //ra.setFillAfter(true);
                //fromDegrees = degrees;
                //permission check
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
                if (points2.size() > 1) {
                    angleRotating = angleFromCoordinate(c, (LatLng) points2.get(2));
                } else {
                    angleRotating = angleFromCoordinate(c, (LatLng) points2.get(1));
                }
                //mdirectionPic.setRotation((float)(angleText-degrees));
                Matrix mMatrix = new Matrix();
                Bitmap bmp = ((BitmapDrawable) getResources().getDrawable(
                        R.drawable.myup2)).getBitmap();
                Camera camera = new Camera();
                camera.save();
                //camera.rotateY(50f);
                camera.rotateX(Math.abs(degreesy));
                //camera.rotateZ((float)(degrees-angleRotating-10));
                camera.rotateZ((float) (degreesx - angleRotating));
                camera.getMatrix(mMatrix);
                camera.restore();
                mMatrix.preTranslate(-centx, -centy);
                mMatrix.postTranslate(centx, centy);
                Bitmap bm = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                        bmp.getHeight(), mMatrix, true);

                mdirectionPic.setImageBitmap(bm);
                //routeWeather.setText("x: " + degreesx + " y: " + degreesy + " z: " + degreesz);
                k=0;
            }
        else{
                if(Math.abs((int) Math.toDegrees(orientationAngles[0]))<5 || Math.abs((int) Math.toDegrees(orientationAngles[0]))>175){
                    k -=1;
                }
                else {
                    degreesx += (int) Math.toDegrees(orientationAngles[0]);
                    degreesy += (int) Math.toDegrees(orientationAngles[1]);
                    degreesz += (int) Math.toDegrees(orientationAngles[2]);
                }
                k +=1;
        }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        mCurrentLocation = location;

        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        addMarker();
    }
    private void addMarker() {
        MarkerOptions options = new MarkerOptions();
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(mLastUpdateTime)));
        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        options.position(currentLatLng);
        mCurrLocationMarker = map.addMarker(options);
        long atTime = mCurrentLocation.getTime();
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(atTime));
        mCurrLocationMarker.setTitle(mLastUpdateTime);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
                DEFAULT_ZOOM));
        map.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        PolylineOptions pOptions = new PolylineOptions()
                .width(5)
                .color(Color.BLACK)
                .geodesic(true);
        for (int z = 0; z < routePoints.size(); z++) {
            LatLng point = routePoints.get(z);
            pOptions.add(point);
        }
        line = map.addPolyline(pOptions);
        routePoints.add(currentLatLng);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}
