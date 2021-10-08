package com.ict3104.t10_idk_2020.ui.routeList;

import android.content.Context;
import android.os.AsyncTask;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.ict3104.t10_idk_2020.DirectionsJSONParser;
import com.ict3104.t10_idk_2020.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class GoogleMapHelper {

    private Context context;
    public DownloadTask downloadTask;

    public GoogleMapHelper(Context context) {
        this.context = context;
        this.downloadTask = new DownloadTask();
    }

    public class DownloadTask extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... url) {

            String data = "";
            List<List<HashMap<String, String>>> routes = null;

            try {
                data = downloadUrl(url[0]);

                JSONObject jObject;
                routes = null;

                jObject = new JSONObject(data);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);

            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            //this is the data of route
            return routes;
        }

//        @Override
//        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
//            super.onPostExecute(result);
//
//            GoogleMapHelper.ParserTask parserTask = new GoogleMapHelper.ParserTask();
//
//
//            parserTask.execute(result);
//
//        }
    }

//    public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
//
//        // Parsing the data in non-ui thread
//        @Override
//        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
//
//            JSONObject jObject;
//            List<List<HashMap<String, String>>> routes = null;
//
//            try {
//                jObject = new JSONObject(jsonData[0]);
//                DirectionsJSONParser parser = new DirectionsJSONParser();
//
//                routes = parser.parse(jObject);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            //route we want is stored in routes
//            return routes;
//        }
//
//        @Override
//        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
//            ArrayList points = null;
//            PolylineOptions lineOptions = null;
//            MarkerOptions markerOptions = new MarkerOptions();
//
//            for (int i = result.size()-1; i >=0 ; i--) {
//                points = new ArrayList();
//                lineOptions = new PolylineOptions();
//
//                List<HashMap<String, String>> path = result.get(i);
//
//                for (int j = 0; j < path.size(); j++) {
//                    HashMap<String, String> point = path.get(j);
//
//                    double lat = Double.parseDouble(point.get("lat"));
//                    double lng = Double.parseDouble(point.get("lng"));
//                    LatLng position = new LatLng(lat, lng);
//
//                    points.add(position);
//                }
//
//                lineOptions.addAll(points);
//                lineOptions.width(12);
//                if(i==0) {
//                    lineOptions.color(Color.RED);
//                }else{
//                    lineOptions.color(Color.GRAY);
//                }
//                lineOptions.geodesic(true);
//                mMap.addPolyline(lineOptions);
//          }

// Drawing polyline in the Google Map for the i-th route

//        }
//        }
////
////    }

    public String getDirectionsUrl(LatLng origin, LatLng dest, String chosenMethod) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=" + chosenMethod;
        String key = "key=" + context.getString(R.string.google_maps_key);
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
    public String downloadUrl(String strUrl) throws IOException {
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
}
