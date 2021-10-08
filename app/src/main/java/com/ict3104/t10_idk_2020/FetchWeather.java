package com.ict3104.t10_idk_2020;

import android.os.AsyncTask;

import com.ict3104.t10_idk_2020.model.Weather;
import com.ict3104.t10_idk_2020.repository.AsyncResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchWeather extends AsyncTask<String, String, String> {
    public AsyncResponse delegate = null; //Call back interface

    public FetchWeather(AsyncResponse asyncResponse) {
        delegate = asyncResponse; //Assigning call back interface through constructor
    }

    protected String doInBackground(String... params) {
        StringBuffer content = new StringBuffer();
        try {
            //Establish HTTP connection to openWeatherMap API
            URL url = new URL("https://api.openweathermap.org/data/2.5/weather?lat=" + params[0] + "&lon=" + params[1] + "&units=metric&appid=638951c821f8f33ff944698a7a1044bd");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            //Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    protected void onPostExecute(String result) {
        //This is executed on the main thread after the process is over
        try {
            //Convert JSON String to JSONObject
            JSONObject jObj = new JSONObject(result);

            //Get temperature from JSONObject
            String temp = jObj.getJSONObject("main").getString("temp");

            //Get weather condition from JSONObject
            JSONArray jsonArray = jObj.getJSONArray("weather");
            JSONObject jWeather = jsonArray.getJSONObject(0);

            //Get weather icon code from JSONObject
            String iconCode = jWeather.getString("icon");

            //Set temp, weather and icon code into weather object
            Weather weather = new Weather();
            weather.setTemp(temp);
            weather.setWeather(jWeather.getString("description"));
            weather.setIconCode(iconCode);

            //Call back weather object - this is to return the weather object for further use
            delegate.processFinish(weather);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}