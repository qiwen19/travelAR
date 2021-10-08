package com.ict3104.t10_idk_2020.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.ict3104.t10_idk_2020.R;
import com.ict3104.t10_idk_2020.ui.home.HomeFragment;
import com.ict3104.t10_idk_2020.ui.routeList.RouteListData;
import com.squareup.okhttp.Route;

//import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.ViewHolder> {
    private List<List<HashMap<String, String>>> listData;
    private Context context;

    //RecyclerView recyclerView;
    public RouteListAdapter(List<List<HashMap<String, String>>> listData, Context context){
        this.listData = listData;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listRoute = layoutInflater.inflate(R.layout.route_list_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(listRoute);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        HashMap<String, String> hashData = listData.get(position).get(0);
        String departureTime = hashData.get("departure");
        String arrivalTime = hashData.get("arrival");
        String distance = hashData.get("distance");
        String timeTaken = hashData.get("timetaken");

        final RouteListData routeListData = new RouteListData(
                "Route " + (position + 1),
                timeTaken,
                String.format("Start: %s, End: %s", departureTime, arrivalTime),
                "Distance: " + distance);

        holder.textViewRouteName.setText(routeListData.getRouteName());
        holder.textViewRouteTime.setText(routeListData.getRouteTime());
        holder.textViewRouteDetails.setText(routeListData.getRouteDetails());
        holder.textViewRouteStartEndTime.setText(routeListData.getTimeStartandEnd());
        holder.relativeLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

//                Bundle bundle = new Bundle();
//                bundle.putSerializable("route", (Serializable) listData.get(position));
                Gson gson = new Gson();
                String routeSerialized = gson.toJson(listData.get(position));

                Intent intent = new Intent(context, HomeFragment.class);
                intent.putExtra("route", routeSerialized);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent);
                //Toast.makeText(v.getContext(),"Click on item: " + routeListData.getRouteName(), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textViewRouteName;
        public TextView textViewRouteLong;
        public TextView textViewRouteLad;
        public TextView textViewRouteTime;
        public TextView textViewRouteDetails;
        public TextView textViewRouteStartEndTime;
        public RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.textViewRouteName = (TextView) itemView.findViewById(R.id.textViewRouteName);
            this.textViewRouteTime = (TextView) itemView.findViewById(R.id.textViewTimeTaken);
            this.textViewRouteDetails = (TextView) itemView.findViewById(R.id.textViewRouteDetails);
            this.textViewRouteStartEndTime = (TextView) itemView.findViewById(R.id.textViewRouteStartEndTime);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
        }
    }
}
