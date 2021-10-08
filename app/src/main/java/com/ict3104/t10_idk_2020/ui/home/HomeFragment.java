package com.ict3104.t10_idk_2020.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.android.ui.IconGenerator;
import com.ict3104.t10_idk_2020.DirectionsJSONParser;
import com.ict3104.t10_idk_2020.FetchWeather;
import com.ict3104.t10_idk_2020.ARActivity;
import com.ict3104.t10_idk_2020.MainActivity;
import com.ict3104.t10_idk_2020.R;
import com.ict3104.t10_idk_2020.model.Weather;
import com.ict3104.t10_idk_2020.repository.AsyncResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private GoogleMap mMap;
    private HashMap<String, Polyline> polylineHashMap = new HashMap<>();
    private HashMap<String, HashMap<String, String>> routeDetailsHashMap = new HashMap<>();
    private HashMap<String, List<HashMap<String, String>>> routeHashMap = new HashMap<>();
    private HashMap<String, Marker> markerHashMap = new HashMap<>();
    private String selectedRoute = "";
    private LatLng latlngStartingPoint;
    private LatLng latlngDestinationPoint;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;
    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private CameraPosition cameraPosition;
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private String selectedInputPlace = null;
    private String selectedDestinationPlace = null;
    private FetchWeather fetchWeather = null;

    //for current location
    private Location location;
    private LatLng c;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());

        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        final Button viewRouteInfo = root.findViewById(R.id.buttonViewSelected);
        final Button findRoutes = root.findViewById(R.id.btnFindRoutes);
        final Button closeSelected = root.findViewById(R.id.ok_button);
        final CardView details = root.findViewById(R.id.routesDetailsCard);
        final TextView routeName = root.findViewById(R.id.textViewRouteNames);
        final TextView routeDetails = root.findViewById(R.id.textViewDetails);
        final TextView routeTime = root.findViewById(R.id.textViewTimeNeeded);
        final TextView routeWeather = root.findViewById(R.id.textViewWeather);

        //Initialize Places API with API key
        Places.initialize(getActivity().getApplicationContext(), getString(R.string.google_maps_key));

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragmentInput = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment_start_point);
        AutocompleteSupportFragment autocompleteFragmentDestination = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment_destination_point);

        // Specify the types of place data to return.
        autocompleteFragmentInput.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragmentDestination.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG));

        //Set placeholder text and limit searches by country - Singapore
        autocompleteFragmentInput.setHint(getString(R.string.inputStartPoint));
        autocompleteFragmentInput.setCountries("SG");
        autocompleteFragmentDestination.setHint(getString(R.string.inputDestinationPoint));
        autocompleteFragmentDestination.setCountries("SG");

        // Disable viewRouteInfo button first
        viewRouteInfo.setEnabled(false);

        //set default start location to current location

        //获取系统的LocationManager对象
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        //从GPS获取最新的定位信息
        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else {
            Log.e("DB", "PERMISSION GRANTED");
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        latlngStartingPoint = new LatLng(location.getLatitude(), location.getLongitude());
        selectedInputPlace = "Current location";
        autocompleteFragmentInput.setText("current location");

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragmentInput.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                latlngStartingPoint = place.getLatLng();
                selectedInputPlace = place.getName();

                MarkerOptions options = new MarkerOptions();
                options.position(place.getLatLng());
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));

            }

            @Override
            public void onError(Status status) {
                //Error Handling
                Log.i("TEST", "An error occurred: " + status);
            }
        });

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragmentDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                latlngDestinationPoint = place.getLatLng();
                selectedDestinationPlace = place.getName();

                MarkerOptions options = new MarkerOptions();
                options.position(place.getLatLng());
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));
            }

            @Override
            public void onError(Status status) {
                //Error Handling
                Log.i("TEST", "An error occurred: " + status);
            }
        });

        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                //LatLng singapore = new LatLng(1.3521, 103.8198);
                //change the size of map here
               // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 12));

                // Prompt the user for permission.
                getLocationPermission();

                // Turn on the My Location layer and the related control on the map.
                updateLocationUI();

                // Get the current location of the device and set the position of the map.
                getDeviceLocation();

                MarkerOptions options = new MarkerOptions();
                options.position(latlngStartingPoint);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlngStartingPoint, 16));

                viewRouteInfo.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        Intent i = new Intent(v.getContext(),ARActivity.class);
                        i.putExtra("ResultList", (Serializable) (routeHashMap.get(selectedRoute)));
                        v.getContext().startActivity(i);

                        /*
                        final LatLng origin = latlngStartingPoint;
                        // get data from hashmap routedetails
                        HashMap<String, String> routeDetail = routeDetailsHashMap.get(selectedRoute);

                        routeName.setText("Route Number: Route " + (Integer.parseInt(selectedRoute.substring(2))+1));
                        routeDetails.setText("Route Details: " + routeDetail.get("distance"));
                        routeTime.setText("Time Needed: "+ routeDetail.get("timetaken"));
*/
                        /* Retrieve weather from API */
                        /*
                        FetchWeather fetchWeather = new FetchWeather(new AsyncResponse() {

                            @Override
                            public void processFinish(Weather weather) {
                                routeWeather.setText("Weather: " + weather.getWeather());
                            }
                        });

                        fetchWeather.execute(String.valueOf(origin.latitude), String.valueOf(origin.longitude)); // Singapore Lat & Long

                        details.setVisibility(View.VISIBLE);
                        */

                    }
                });
/*
                closeSelected.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        details.setVisibility(View.INVISIBLE);
                    }
                });

 */

                findRoutes.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (selectedInputPlace == null || selectedDestinationPlace == null){
                            if (selectedDestinationPlace != null){
                                Toast.makeText(getActivity(), "Please select start point location first", Toast.LENGTH_LONG).show();
                            } else if (selectedInputPlace != null){
                                Toast.makeText(getActivity(), "Please select end point location first", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), "Please select start and end point location first", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            mMap.clear();

                            // Remove all cached routeDetail, polyline and marker
                            routeDetailsHashMap.clear();
                            polylineHashMap.clear();
                            markerHashMap.clear();
                            routeHashMap.clear();

                            // disable viewRouteInfo button first
                            if (!viewRouteInfo.isEnabled()) {
                                viewRouteInfo.setEnabled(false);
                            }

                            //-------------------------------change the value of origin and dest at above------------
                            final LatLng origin = latlngStartingPoint;
                            final LatLng dest = latlngDestinationPoint;

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 16));
//        String[] origintemp = getIntent().getStringExtra("ORIGIN").split(",");
//        final LatLng origin = new LatLng(Double.parseDouble(origintemp[0]), Double.parseDouble(origintemp[1]));
//        String[] desttemp = getIntent().getStringExtra("DEST").split(",");
//        final LatLng dest = new LatLng(Double.parseDouble(desttemp[0]), Double.parseDouble(desttemp[1]));
                            // can switch the method of travelling by change bywhat, 3 options
                            //String bywhat = "driving";
                            String bywhat = "walking";
                            //String bywhat = "bicycling";
                            //        String bywhat = "transit";
                            //final String bywhat = getIntent().getStringExtra("MODE");
                            // Creating MarkerOptions

                            // Add marker into ArrayList for future use
                            MarkerOptions options = new MarkerOptions();

                            // this is the start location marker in green
                            options.position(origin);
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            // the is the end location marker in red
                            mMap.addMarker(options);
                            options.position(dest);
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            mMap.addMarker(options);
                            //-------------------------------here is where we called google api---------------------
                            // Getting URL to the Google Directions API
                            String url = getDirectionsUrl(origin, dest, bywhat);
                            HomeFragment.DownloadTask downloadTask = new HomeFragment.DownloadTask();
                            // Start downloading json data from Google Directions API
                            downloadTask.execute(url);

                            mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                                @Override
                                public void onPolylineClick(Polyline polyline) {
                                    viewRouteInfo.setEnabled(true);
                                    RefreshLinesColour();
                                    RefreshMarker();
                                    ShowFullRouteDetails(polyline.getId());
                                    polyline.setColor(Color.RED);
                                    selectedRoute = polyline.getId();
                                }
                            });
                            //switch travel method
                            //        Button buttonWalk = (Button) findViewById(R.id.walking_button);
                            //        Button buttonDrive = (Button) findViewById(R.id.driving_button);
                            //        Button buttonCycle = (Button) findViewById(R.id.cycling_button);
                        }
                    }
                 });
                // ------------------here is the end of calling api as well as drawing lines----
            }
        });

//        fetchWeather = new FetchWeather(new AsyncResponse() {
//
//            @Override
//            public void processFinish(Weather weather) {
//                /* Display Temperature and Weather Condition into TextView */
//                //textViewTemp.setText(String.format("%s℃", weather.getTemp()));
//                //textViewWeather.setText(weather.getWeather());
//
//            }
//        });
//
//        //fetchWeather.execute("1.302404", "103.780459"); // Singapore Lat & Long

        return root;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            //this is the data of route
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            HomeFragment.ParserTask parserTask = new HomeFragment.ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //route we want is stored in routes
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;

            for (int i = result.size()-1; i >=0 ; i--) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                // Determine the middle index of location to place marker with icon for time taken
                int locationIndex = (int) Math.floor(path.size() / 2);

                Marker marker = null;

                // Skip creation of marker for the first line. It'll be done below with all the details
                for (int j = 1; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);


                    // If middle index, add marker to show time taken
                    if (j == locationIndex)
                    {
                        StringBuilder sb = new StringBuilder();
                        String timeTaken = path.get(0).get("timetaken");
                        sb.append("Time required: " + timeTaken);

                        // If is the first line (default selected)
                        if (i == 0)
                        {
                            String distance = path.get(0).get("distance");
                            sb.append("\n");
                            sb.append("Distance: " + distance);
                        }

                        IconGenerator iconFactory = new IconGenerator(getActivity().getApplicationContext());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(sb.toString())))
                                .position(position)
                                .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
                        marker = mMap.addMarker(markerOptions);
                    }

                    points.add(position);
                }
                lineOptions.clickable(true);
                lineOptions.addAll(points);
                lineOptions.width(12);
                if(i==0) {
                    lineOptions.color(Color.RED);
                }else{
                    lineOptions.color(Color.GRAY);
                }
                lineOptions.geodesic(true);

                // Store the polylines for us to use later
                Polyline curLine = mMap.addPolyline(lineOptions);
                polylineHashMap.put(curLine.getId(), curLine);
                // Store route details in arraylist for easy access
                routeDetailsHashMap.put(curLine.getId(), path.get(0));
                // Store markerOptions with line ID as key
                markerHashMap.put(curLine.getId(), marker);

                routeHashMap.put(curLine.getId(), path);

                // if it is the first polyline, set it to selectedRoute
                if (i == 0)
                {
                    selectedRoute = curLine.getId();
                }
            }

// Drawing polyline in the Google Map for the i-th route

        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest, String choosenmathod) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=" + choosenmathod;
        String key = "key="+getResources().getString(R.string.google_maps_key);
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode + "&alternatives=true&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private void RefreshLinesColour()
    {
        Iterator iter = polylineHashMap.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry mapEntry = (Map.Entry) iter.next();
            Polyline polyLine = (Polyline) mapEntry.getValue();
            polyLine.setColor(Color.GRAY);
        }
    }

    private void RefreshMarker()
    {
        Iterator iter = markerHashMap.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry mapEntry = (Map.Entry) iter.next();
            Marker marker = (Marker) mapEntry.getValue();
            String lineID = (String) mapEntry.getKey();
            StringBuilder sb = new StringBuilder();

            String timeTaken = routeDetailsHashMap.get(lineID).get("timetaken");

            sb.append("Time required: " + timeTaken);

            IconGenerator iconFactory = new IconGenerator(getActivity().getApplicationContext());
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(sb.toString())));
            marker.setAnchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        }
    }

    private void ShowFullRouteDetails(String lineID)
    {
        Marker marker = markerHashMap.get(lineID);
        StringBuilder sb = new StringBuilder();

        String timeTaken = routeDetailsHashMap.get(lineID).get("timetaken");
        String distance = routeDetailsHashMap.get(lineID).get("distance");

        sb.append("Time required: " + timeTaken);
        sb.append("\n");
        sb.append("Distance: " + distance);

        IconGenerator iconFactory = new IconGenerator(getActivity().getApplicationContext());
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(sb.toString())));
        marker.setAnchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
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
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}
