package convoy.statistic;

import convoy.abstracts.AbstractFederate;
import hla.rti1516e.exceptions.RTIexception;

public class StatisticFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "StatisticFederate";
    public static double TIME_STEP = 1.0;

    protected StatisticFederate() {
        super(FEDERATE_NAME, FEDERATION_NAME, TIME_STEP);
    }

    public static void main( String[] args ){
        try
        {
            new StatisticFederate().runFederate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void createRTIAmbassador() {
        federationAmbassador = new StatisticAmbassador(this);
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