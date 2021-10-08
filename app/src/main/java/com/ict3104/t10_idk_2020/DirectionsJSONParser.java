package com.ict3104.t10_idk_2020;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anupamchugh on 27/11/15.
 */

public class DirectionsJSONParser {

    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");

                List path = new ArrayList<HashMap<String, String>>();

                // Retrieve departure and arrival time
                HashMap<String, String> hashDetails = new HashMap<String, String>();

                //String departureTime = ((JSONObject)jLegs.get(0)).getJSONObject("departure_time").get("text").toString();
                //String arrivalTime = ((JSONObject)jLegs.get(0)).getJSONObject("arrival_time").get("text").toString();
                String distance = ((JSONObject)jLegs.get(0)).getJSONObject("distance").get("text").toString();
                String timeTaken = ((JSONObject)jLegs.get(0)).getJSONObject("duration").get("text").toString();
                String startLocationLat = ((JSONObject)jLegs.get(0)).getJSONObject("start_location").get("lat").toString();
                String startLocationLng = ((JSONObject)jLegs.get(0)).getJSONObject("start_location").get("lng").toString();
                String endLocationLat = ((JSONObject)jLegs.get(0)).getJSONObject("end_location").get("lat").toString();
                String endLocationLng = ((JSONObject)jLegs.get(0)).getJSONObject("end_location").get("lng").toString();

                // Push into List<HashMap>.
                //hashDetails.put("departure", departureTime);
                //hashDetails.put("arrival", arrivalTime);
                hashDetails.put("distance", distance);
                hashDetails.put("timetaken", timeTaken);
                hashDetails.put("startlocationlat", startLocationLat);
                hashDetails.put("startlocationlng", startLocationLng);
                hashDetails.put("endlocationlat", endLocationLat);
                hashDetails.put("endlocationlng", endLocationLng);

                path.add(hashDetails);

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List list = decodePoly(polyline);

                        /** Traversing all points */
                        for(int l=0;l <list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }

        return routes;
    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}