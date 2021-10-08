package com.ict3104.t10_idk_2020.ui.routeList;

import java.util.ArrayList;

public class RouteListData {
    private String routeName;
    private Double routeLong;
    private Double routeLat;
    private String routeTime;
    private String timeStartandEnd;
    private String routeDetails;

    public RouteListData(String routeName, String routeTime, String timeStartandEnd, String routeDetails){
        this.routeName = routeName;
        this.routeTime = routeTime;
        this.routeDetails = routeDetails;
        this.timeStartandEnd = timeStartandEnd;
    }

    //GET & SET route name
    public String getRouteName(){
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    //GET & SET route Time
    public String getRouteTime() {
        return routeTime;
    }

    public void setRouteTime(String routeTime) {
        this.routeTime = routeTime;
    }

    // GET & SET route time start and end

    public String getTimeStartandEnd() {return  timeStartandEnd;}

    public void setTimeStartandEnd(String timeStartandEnd) {
        this.timeStartandEnd = timeStartandEnd;
    }

    // GET & SET route details


    public String getRouteDetails() {
        return routeDetails;
    }

    public void setRouteDetails(String routeDetails) {
        this.routeDetails = routeDetails;
    }
}
