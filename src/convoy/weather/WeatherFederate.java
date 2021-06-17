package convoy.weather;

import convoy.abstracts.AbstractFederate;
import hla.rti1516e.exceptions.RTIexception;

public class WeatherFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "WeatherFederate";
    public static double TIME_STEP = 50.0;

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
            advanceTime();
            log( "Time Advanced to " + federationAmbassador.federateTime );
        }
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        finishSimulationHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.FinishSimulation" );
        rtiAmbassador.subscribeInteractionClass(finishSimulationHandle);
    }
}