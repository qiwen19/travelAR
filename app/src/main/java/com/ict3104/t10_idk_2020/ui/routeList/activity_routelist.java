package com.ict3104.t10_idk_2020.ui.routeList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.ict3104.t10_idk_2020.MainActivity;
import com.ict3104.t10_idk_2020.R;
import com.ict3104.t10_idk_2020.adapter.RouteListAdapter;

import java.io.Console;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class activity_routelist extends AppCompatActivity {

    private GoogleMapHelper gmHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routelist);

        // init GoogleMapHelper
        gmHelper = new GoogleMapHelper(getApplicationContext());

        /////////////////////////////////// Test Data
        String originTest = "1.376959, 103.889792";
        String destTest = "1.371216, 103.892302";
        // can switch the method of travelling by change bywhat, 3 options
        String bywhatTest = "transit";
        //String bywhat = "walking";
        //String bywhat = "bicycling";

        getIntent().putExtra("ORIGIN", originTest);
        getIntent().putExtra("DEST", destTest);
        getIntent().putExtra("MODE", bywhatTest);
        /////////////////////////////////// End of Test Data

        // Getting origin, destination, mode
        String[] origintemp = getIntent().getStringExtra("ORIGIN").split(",");
        final LatLng origin = new LatLng(Double.parseDouble(origintemp[0]), Double.parseDouble(origintemp[1]));
        String[] desttemp = getIntent().getStringExtra("DEST").split(",");
        final LatLng dest = new LatLng(Double.parseDouble(desttemp[0]), Double.parseDouble(desttemp[1]));
        // can switch the method of travelling by change bywhat, 3 options
        //String bywhat = "driving";
        //String bywhat = "walking";
        //String bywhat = "bicycling";
        final String bywhat = getIntent().getStringExtra("MODE");


        // Getting URL to the Google Directions API
        String url = gmHelper.getDirectionsUrl(origin, dest, bywhat);
        // Start downloading json data from Google Directions API
        try {
            List<List<HashMap<String, String>>> routes = gmHelper.downloadTask.execute(url).get();

//                new RouteListData[]{
//                new RouteListData("ROUTE A", 1.232, 1.232, "10 Min",
//                        "Start: 1am, End 2am","This is just a dummy text " +
//                        "used to please the crowd"),
//                new RouteListData("ROUTE B", 1.232, 1.232, "10 Min",
//                        "Start: 1am, End 2am","This is just a dummy text " +
//                        "used to please the crowd"),
//                new RouteListData("ROUTE C", 1.232, 1.232, "10 Min",
//                        "Start: 1am, End 2am","This is just a dummy text " +
//                        "used to please the crowd"),
//                new RouteListData("ROUTE D", 1.232, 1.232, "10 Min",
//                        "Start: 1am, End 2am","This is just a dummy text " +
//                        "used to please the crowd"),
//                new RouteListData("ROUTE E", 1.232, 1.232, "10 Min",
//                        "Start: 1am, End 2am","This is just a dummy text " +
//                        "used to please the crowd"),
//                new RouteListData("ROUTE F", 1.232, 1.232, "10 Min",
//                        "Start: 1am, End 2am","This is just a dummy text " +
//                        "used to please the crowd"),
//        };

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.routeListRecycleView);
            RouteListAdapter adapter = new RouteListAdapter(routes, getApplicationContext());
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}