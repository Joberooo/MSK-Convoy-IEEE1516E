package convoy.petrolStation;

import convoy.abstracts.AbstractFederate;
import hla.rti1516e.exceptions.RTIexception;

public class PetrolStationFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "PetrolStationFederate";
    public static double TIME_STEP = 1.0;

    protected PetrolStationFederate() {
        super(FEDERATE_NAME, FEDERATION_NAME, TIME_STEP);
    }

    public static void main( String[] args ){
        try
        {
            new PetrolStationFederate().runFederate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void createRTIAmbassador() {
        federationAmbassador = new PetrolStationAmbassador(this);
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