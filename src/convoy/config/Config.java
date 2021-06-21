package convoy.config;

public final class Config {
    public static final String FEDERATION_NAME = "ConvoyFederation";

    public static final double GUI_TIME_STEP = 1.0;
    public static final int PANEL_WIDTH = 300;
    public static final int PANEL_HEIGHT = 900;

    public static final double PETROL_STATION_STEP = 1.0;

    public static final int MAX_ROUTE_SECTION_LENGTH = 200;
    public static final double ROUTE_SECTION_TIME_STEP = 5.0;
    public static final int MIN_NUMBER_OF_ROUTE_SECTION = 10;
    public static final int MAX_NUMBER_OF_ROUTE_SECTION = 20;

    public static final int MIN_SECTION = 2;
    public static final int MAX_SECTION = 5;

    public static final double STATISTIC_TIME_STEP = 1.0;

    public static final float OBSTACLES_FREQUENCY = 0.999999f;

    public static final double OBSTACLES_TIME_STEP = 10.0;

    public static final float MIN_FUEL_CONSUMPTION = 0.15f;
    public static final float MAX_FUEL_CONSUMPTION = 0.25f;

    public static final double VEHICLE_TIME_STEP = 1.0;
    public static final float EXPECTED_VEHICLES_DISTANCE = 30f;
    public static final float CONVOY_VELOCITY = 10f;
    public static final float START_VEHICLES_FUEL = 100f;
    public static final float MAX_VELOCITY = 120f;

    public static final double WEATHER_TIME_STEP = 5.0;
}
