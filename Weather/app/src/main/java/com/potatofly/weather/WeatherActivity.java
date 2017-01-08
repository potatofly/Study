package com.potatofly.weather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import butterknife.BindView;

/**
 * Created by potatofly on 1/8/17.
 */

public class WeatherActivity extends AppCompatActivity {
    public @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    public @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.weather_layout)
    ScrollView weatherLayout;

    @BindView(R.id.nav_button)
    Button navButton;

    @BindView(R.id.title_city)
    TextView titleCity;

    @BindView(R.id.title_update_time)
    TextView titleUpdateTime;

    @BindView(R.id.degree_text)
    TextView degreeText;

    @BindView(R.id.weather_info_text)
    TextView weatherInfoText;

    @BindView(R.id.forecast_layout)
    LinearLayout forecastLayout;

    @BindView(R.id.aqi_text)
    TextView aqiText;

    @BindView(R.id.pm25_text)
    TextView pm25Text;

    @BindView(R.id.comfort_text)
    TextView comfortText;

    @BindView(R.id.car_wash_text)
    TextView carWashText;

    @BindView(R.id.sport_text)
    TextView sportText;

    @BindView(R.id.bing_pic_img)
    ImageView bingPicImg;

    private String mWeatherId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
