package com.potatofly.weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by potatofly on 1/8/17.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
