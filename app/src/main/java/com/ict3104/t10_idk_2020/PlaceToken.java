package com.ict3104.t10_idk_2020;

import com.google.android.gms.maps.model.LatLng;
import com.google.ar.sceneform.math.Vector3;
import com.google.maps.android.SphericalUtil;

public class PlaceToken {
    String id;
    String name;
    String desc;
    LatLng latlng;

    public PlaceToken(String id1, String n, String d, LatLng ll){
        this.id = id1;
        this.name = n;
        this.desc = d;
        this.latlng = ll;
    }

    public Vector3 getPositionVector(Float azimuth, LatLng latlng) {
        LatLng tokenLatLng = this.latlng;
        double heading = SphericalUtil.computeHeading(latlng, tokenLatLng);
        float r = -2f;
        float x = (float) (r * Math.sin(azimuth + heading));
        float y = 1f;
        float z = (float) (r * Math.cos(azimuth + heading));
        return new Vector3(x, y, z);
    }
}