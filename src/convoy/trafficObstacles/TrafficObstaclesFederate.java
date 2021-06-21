package convoy.trafficObstacles;

import convoy.abstracts.AbstractFederate;
import convoy.config.Config;
import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TrafficObstaclesFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "TrafficObstaclesFederate";
    public static double TIME_STEP = Config.OBSTACLES_TIME_STEP;

    public int currentRouteSectionNumberToDelete = 0;
    public int currentRouteSectionNumberToCloseOrOpen = 0;

    protected InteractionClassHandle deleteRouteSectionHandle;
    protected InteractionClassHandle closeOrOpenRouteSectionHandle;

    protected ObjectClassHandle vehicleHandle;
    protected AttributeHandle vehicleNumberHandle;
    protected AttributeHandle vehiclePositionHandle;
    protected AttributeHandle vehicleRouteNumberHandle;

    public ArrayList<TrafficObstaclesFederate.SingleVehicle> singleVehiclesList = new ArrayList<>();

    public static class SingleVehicle implements Comparable<TrafficObstaclesFederate.SingleVehicle>{
        public int vehicleNumber;
        public float vehiclePosition;
        public int vehicleRouteNumber;

        public Integer getRouteNumber(){
            return this.vehicleNumber;
        }

        @Override
        public int compareTo(@NotNull TrafficObstaclesFederate.SingleVehicle o) {
            return this.getRouteNumber().compareTo(o.getRouteNumber());
        }
    }

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
            TrafficObstacles trafficObstacles = new TrafficObstacles();
            takeCurrentRouteSectionNumberToChange();
            if(trafficObstacles.closeSectionPermanently()) sendDeleteRouteSectionInteraction();
            if(trafficObstacles.delaySection()) sendCloseOrOpenRouteSectionInteraction();
            advanceTime();
            log( "Time Advanced to " + federationAmbassador.federateTime );
        }
    }

    private void takeCurrentRouteSectionNumberToChange(){
        for(SingleVehicle v: singleVehiclesList){
            if(v.vehicleNumber == 0){
                this.currentRouteSectionNumberToDelete = v.vehicleRouteNumber + 1;
                this.currentRouteSectionNumberToCloseOrOpen = v.vehicleRouteNumber;
            }
        }
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        finishSimulationHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.FinishSimulation" );
        rtiAmbassador.subscribeInteractionClass(finishSimulationHandle);

        deleteRouteSectionHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.DeleteRouteSection" );
        rtiAmbassador.publishInteractionClass(deleteRouteSectionHandle);

        closeOrOpenRouteSectionHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.CloseOrOpenRouteSection" );
        rtiAmbassador.publishInteractionClass(closeOrOpenRouteSectionHandle);

        this.vehicleHandle = rtiAmbassador.getObjectClassHandle( "HLAobjectRoot.Vehicle" );
        this.vehicleNumberHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "VehicleNumber" );
        this.vehiclePositionHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "VehiclePosition" );
        this.vehicleRouteNumberHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "RouteNumber" );

        AttributeHandleSet attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        attributes.add( vehicleNumberHandle );
        attributes.add( vehiclePositionHandle );
        attributes.add( vehicleRouteNumberHandle );

        rtiAmbassador.subscribeObjectClassAttributes( vehicleHandle, attributes );
    }

    protected void sendDeleteRouteSectionInteraction() throws RTIexception{
        ParameterHandleValueMap parameterHandleValueMap = rtiAmbassador.getParameterHandleValueMapFactory().create(1);
        ParameterHandle numberOfRouteSectionHandle = rtiAmbassador.getParameterHandle(deleteRouteSectionHandle, "NumberOfRouteSection");
        HLAinteger32BE number = encoderFactory.createHLAinteger32BE(this.currentRouteSectionNumberToDelete);
        parameterHandleValueMap.put(numberOfRouteSectionHandle, number.toByteArray());
        rtiAmbassador.sendInteraction(deleteRouteSectionHandle, parameterHandleValueMap, generateTag());
        log( "Delete Route Section Interaction Sent" );
    }

    protected void sendCloseOrOpenRouteSectionInteraction() throws RTIexception{
        ParameterHandleValueMap parameterHandleValueMap = rtiAmbassador.getParameterHandleValueMapFactory().create(1);
        ParameterHandle numberOfRouteSectionHandle = rtiAmbassador.getParameterHandle(closeOrOpenRouteSectionHandle, "NumberOfRouteSection");
        HLAinteger32BE number = encoderFactory.createHLAinteger32BE(this.currentRouteSectionNumberToCloseOrOpen);
        parameterHandleValueMap.put(numberOfRouteSectionHandle, number.toByteArray());
        rtiAmbassador.sendInteraction(closeOrOpenRouteSectionHandle, parameterHandleValueMap, generateTag());
        log( "Close or Open Route Section Interaction Sent" );
    }
}