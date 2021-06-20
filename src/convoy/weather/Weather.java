package convoy.weather;

import java.util.Random;

public final class Weather {
    private final WeatherPrecipitation weatherType;
    private final float windPower;
    private final float windDirectionX;
    private final float windDirectionY;

    private final Random random = new Random();

    public Weather(){
        this.weatherType = WeatherPrecipitation.getWeatherPrecipitation();
        this.windPower = generateWindPower();
        this.windDirectionX = generateWindDirection();
        this.windDirectionY = generateWindDirection();
    }

    public void printWeather(String federateName){
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(federateName + "    ::   Weather type = " + this.weatherType);
        System.out.println(federateName + "    ::   Wind power = " + this.windPower);
        System.out.println(federateName + "    ::   Wind direction X = " + this.windDirectionX);
        System.out.println(federateName + "    ::   Wind direction Y = " + this.windDirectionY);
        System.out.println("--------------------------------------------------------------------------------");
    }

    private float generateWindDirection(){
        return -1f + random.nextFloat() * 2f;
    }

    private float generateWindPower(){
        return random.nextFloat();
    }

    public WeatherPrecipitation getWeatherType(){
        return this.weatherType;
    }

    public float getWindPower(){
        return this.windPower;
    }

    public float getWindDirectionX(){
        return this.windDirectionX;
    }

    public float getWindDirectionY(){
        return this.windDirectionY;
    }
}
