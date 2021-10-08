package com.ict3104.t10_idk_2020.repository;

import com.ict3104.t10_idk_2020.model.Weather;

public interface AsyncResponse {
    void processFinish(Weather weather);
}
