package convoy.weather;

import java.util.Random;

public enum WeatherPrecipitation {
    SUN,  //słonecznie
    RAIN, //deszcz
    HAIL, //grad
    SNOW; //śnieg

    public static WeatherPrecipitation getWeatherPrecipitation() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }
}
