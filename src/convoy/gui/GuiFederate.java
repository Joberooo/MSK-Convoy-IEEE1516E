package convoy.gui;

import convoy.abstracts.AbstractFederate;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.exceptions.RTIexception;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class GuiFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "GuiFederate";
    public static double TIME_STEP = 1.0;
    public float END_OF_ROUT;

    protected ObjectClassHandle vehicleHandle;
    protected AttributeHandle vehicleNumberHandle;
    protected AttributeHandle vehiclePositionHandle;
    protected AttributeHandle vehicleRouteNumberHandle;

    public ArrayList<GuiFederate.SingleVehicle> singleVehiclesList = new ArrayList<>();

    public static class SingleVehicle implements Comparable<GuiFederate.SingleVehicle>{
        public int vehicleNumber;
        public float vehiclePosition;
        public int vehicleRouteNumber;

        public Integer getRouteNumber(){
            return this.vehicleNumber;
        }

        @Override
        public int compareTo(@NotNull GuiFederate.SingleVehicle o) {
            return this.getRouteNumber().compareTo(o.getRouteNumber());
        }
    }

    public ObjectClassHandle routeSectionHandle;
    public AttributeHandle routeSectionNumberHandle;
    public AttributeHandle routeSectionLengthHandle;
    public AttributeHandle routeSurfaceHandle;
    public AttributeHandle routeSectionIsClosedHandle;

    public ArrayList<GuiFederate.SingleRouteSection> singleRouteSectionList = new ArrayList<>();

    public static class SingleRouteSection implements Comparable<GuiFederate.SingleRouteSection>{
        public int routeNumber;
        public float routeLength;
        public int routeSurface;
        public boolean routeIsClosed;

        public Integer getRouteNumber(){
            return this.routeNumber;
        }

        @Override
        public int compareTo(@NotNull GuiFederate.SingleRouteSection o) {
            return this.getRouteNumber().compareTo(o.getRouteNumber());
        }
    }

    protected GuiFederate() {
        super(FEDERATE_NAME, FEDERATION_NAME, TIME_STEP);
    }

    public static void main( String[] args ){
        try
        {
            new GuiFederate().runFederate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void createRTIAmbassador() {
        federationAmbassador = new GuiAmbassador(this);
    }

    @Override
    protected void mainContent() throws Exception {
        publishAndSubscribe();
        log( "Published and Subscribed" );

        GUI gui = new GUI();

        while(federationAmbassador.isRunning)
        {
            if(singleRouteSectionList.size() == 0){
                log( "Waiting for routs..." );
            }
            else if(singleVehiclesList.size() == 0){
                log( "Waiting for vehicles...");
            }
            else{
                calculateCompleteRout();
                ArrayList<Float> positionList = new ArrayList<>();
                for (SingleVehicle v : singleVehiclesList) {
                    positionList.add(v.vehiclePosition);
                }
                float proportion = this.END_OF_ROUT / 800;
                gui.myFrame.panel.setVehiclesPosition(positionList, proportion);
            }

            advanceTime();
            log( "Time Advanced to " + federationAmbassador.federateTime );
        }
    }

    private void calculateCompleteRout(){
        float completeRout = 0;
        for(GuiFederate.SingleRouteSection s : singleRouteSectionList){
            completeRout += s.routeLength;
        }
        log( "CompleteRout = " + completeRout);
        END_OF_ROUT = completeRout;
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        finishSimulationHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.FinishSimulation" );
        rtiAmbassador.subscribeInteractionClass(finishSimulationHandle);

        this.vehicleHandle = rtiAmbassador.getObjectClassHandle( "HLAobjectRoot.Vehicle" );
        this.vehicleNumberHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "VehicleNumber" );
        this.vehiclePositionHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "VehiclePosition" );
        this.vehicleRouteNumberHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "RouteNumber" );

        AttributeHandleSet attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        attributes.add( vehicleNumberHandle );
        attributes.add( vehiclePositionHandle );
        attributes.add( vehicleRouteNumberHandle );

        rtiAmbassador.subscribeObjectClassAttributes( vehicleHandle, attributes );

        this.routeSectionHandle = rtiAmbassador.getObjectClassHandle( "HLAobjectRoot.RouteSection" );
        this.routeSectionNumberHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionNumber" );
        this.routeSectionLengthHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionLength" );
        this.routeSurfaceHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionSurface" );
        this.routeSectionIsClosedHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionIsClosed" );

        attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        attributes.add( routeSectionNumberHandle );
        attributes.add( routeSectionLengthHandle );
        attributes.add( routeSurfaceHandle );
        attributes.add( routeSectionIsClosedHandle );
        rtiAmbassador.subscribeObjectClassAttributes(routeSectionHandle, attributes);
    }
}