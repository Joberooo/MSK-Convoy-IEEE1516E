package convoy.trafficObstacles;

import convoy.abstracts.AbstractFederate;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.RTIexception;

public class TrafficObstaclesFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "TrafficObstaclesFederate";
    public static double TIME_STEP = 100.0;
    public static final int ITERATIONS = 20;

    protected TrafficObstaclesFederate() {
        super(FEDERATE_NAME, FEDERATION_NAME, TIME_STEP);
    }

    public static void main( String[] args ){
        try
        {
            new TrafficObstaclesFederate().runFederate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void createRTIAmbassador(){
        federationAmbassador = new TrafficObstaclesAmbassador(this);
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

    @Override
    protected ObjectInstanceHandle registerObject(ObjectClassHandle objectClassHandle) {
        return null;
    }

    @Override
    protected void updateAttributeValues(ObjectInstanceHandle objectHandle, int id) {

    }

    @Override
    protected void deleteObject(ObjectInstanceHandle handle) {

    }
}