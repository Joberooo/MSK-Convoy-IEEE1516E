package convoy.weather;

import convoy.abstracts.AbstractFederate;
import convoy.config.Config;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;

public class WeatherFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "WeatherFederate";
    public static double TIME_STEP = Config.WEATHER_TIME_STEP;

    private Weather weather;

    protected InteractionClassHandle changeWeatherHandle;

    protected WeatherFederate() {
        super(FEDERATE_NAME, FEDERATION_NAME, TIME_STEP);
    }

    public static void main( String[] args ){
        try
        {
            new WeatherFederate().runFederate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void createRTIAmbassador() {
        federationAmbassador = new WeatherAmbassador(this);
    }

    @Override
    protected void mainContent() throws Exception {
        publishAndSubscribe();
        log( "Published and Subscribed" );

        while(federationAmbassador.isRunning)
        {
            weather = new Weather();
            weather.printWeather(FEDERATE_NAME);

            sendInteraction();

            advanceTime();
            log( "Time Advanced to " + federationAmbassador.federateTime );
        }
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        changeWeatherHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.ChangeWeather" );
        rtiAmbassador.publishInteractionClass(changeWeatherHandle);

        finishSimulationHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.FinishSimulation" );
        rtiAmbassador.subscribeInteractionClass(finishSimulationHandle);
    }

    protected void sendInteraction() throws RTIexception{
        ParameterHandleValueMap parameterHandleValueMap = rtiAmbassador.getParameterHandleValueMapFactory().create(4);
        ParameterHandle typeHandle = rtiAmbassador.getParameterHandle(changeWeatherHandle, "typeOfPrecipitation");
        ParameterHandle powerHandle = rtiAmbassador.getParameterHandle(changeWeatherHandle, "WindPower");
        ParameterHandle xDirectionHandle = rtiAmbassador.getParameterHandle(changeWeatherHandle, "DirectionWindX");
        ParameterHandle yDirectionHandle = rtiAmbassador.getParameterHandle(changeWeatherHandle, "DirectionWindY");

        HLAinteger32BE weatherType = encoderFactory.createHLAinteger32BE(weather.getWeatherType().ordinal());
        HLAfloat32BE windPower = encoderFactory.createHLAfloat32BE(weather.getWindPower());
        HLAfloat32BE windDirectionX = encoderFactory.createHLAfloat32BE(weather.getWindDirectionX());
        HLAfloat32BE windDirectionY = encoderFactory.createHLAfloat32BE(weather.getWindDirectionY());

        parameterHandleValueMap.put(typeHandle, weatherType.toByteArray());
        parameterHandleValueMap.put(powerHandle, windPower.toByteArray());
        parameterHandleValueMap.put(xDirectionHandle, windDirectionX.toByteArray());
        parameterHandleValueMap.put(yDirectionHandle, windDirectionY.toByteArray());
        rtiAmbassador.sendInteraction(changeWeatherHandle, parameterHandleValueMap, generateTag());
    }
}