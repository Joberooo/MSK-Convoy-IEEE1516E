package convoy.vehicle;

import convoy.abstracts.AbstractFederate;
import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class VehicleFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "VehicleFederate";
    public static double TIME_STEP = 1.0;

    public static final int NUMBERS_OF_VEHICLES = 3;
    public static final float EXPECTED_VEHICLES_DISTANCE = 20F;
    public static final int CONVOY_VELOCITY = 50;
    public static final float START_VEHICLES_FUEL = 100F;
    public float END_OF_ROUT;

    protected ObjectClassHandle vehicleHandle;
    protected AttributeHandle vehicleNumberHandle;
    protected AttributeHandle vehiclePositionHandle;
    protected AttributeHandle vehicleRouteNumberHandle;

    protected ArrayList<Vehicle> vehiclesList = new ArrayList<>();
    protected ArrayList<ObjectInstanceHandle> vehicleObjectInstanceHandleList = new ArrayList<>();

    public ObjectClassHandle routeSectionHandle;
    public AttributeHandle routeSectionNumberHandle;
    public AttributeHandle routeSectionLengthHandle;
    public AttributeHandle routeSurfaceHandle;
    public AttributeHandle routeSectionIsClosedHandle;

    public ArrayList<SingleRouteSection> singleRouteSectionList = new ArrayList<>();

    public static class SingleRouteSection implements Comparable<SingleRouteSection>{
        public int routeNumber;
        public float routeLength;
        public int routeSurface;
        public boolean routeIsClosed;

        public Integer getRouteNumber(){
            return this.routeNumber;
        }

        @Override
        public int compareTo(@NotNull VehicleFederate.SingleRouteSection o) {
            return this.getRouteNumber().compareTo(o.getRouteNumber());
        }
    }

    protected VehicleFederate() {
        super(FEDERATE_NAME, FEDERATION_NAME, TIME_STEP);
    }

    public static void main( String[] args ){
        try
        {
            new VehicleFederate().runFederate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void createRTIAmbassador(){
        federationAmbassador = new VehicleAmbassador(this);
    }

    @Override
    protected void mainContent() throws Exception {
        publishAndSubscribe();
        log( "Published and Subscribed" );

        for(int i = 0; i < NUMBERS_OF_VEHICLES; i++){
            ObjectInstanceHandle objectHandle = registerObject(vehicleHandle);
            log( "Registered Object, handle=" + objectHandle );
            float startPosition = (NUMBERS_OF_VEHICLES - i - 1) * EXPECTED_VEHICLES_DISTANCE;
            vehiclesList.add(
                    new Vehicle(i, 0, START_VEHICLES_FUEL, 0F, startPosition, 120F));
            vehicleObjectInstanceHandleList.add(objectHandle);
            printVehicleData(vehiclesList.get(i));
        }

        while(federationAmbassador.isRunning)
        {
            if(singleRouteSectionList.size() == 0){
                log( "Waiting for routs..." );
            }
            else{
                calculateCompleteRout();

                for(int j = 0; j < vehicleObjectInstanceHandleList.size(); j++){
                    modifyCarParameters(j);
                    updateAttributeValues( vehicleObjectInstanceHandleList.get(j), j );
                }

                if(vehiclesList.get(0).getVehiclePosition() > END_OF_ROUT){
                    sendFinishSimulationInteraction();
                    federationAmbassador.stopRunning();
                }
            }

            advanceTime();
            log( "Time Advanced to " + federationAmbassador.federateTime );
        }

        for (ObjectInstanceHandle objectInstanceHandle : vehicleObjectInstanceHandleList) {
            deleteObject(objectInstanceHandle);
            log("Deleted Object, handle=" + objectInstanceHandle);
        }
    }

    private void calculateCompleteRout(){
        float completeRout = 0;
        for(SingleRouteSection s : singleRouteSectionList){
            completeRout += s.routeLength;
            log( "Route nr = " + s.routeNumber + ", Length = " + s.routeLength );
        }
        log( "CompleteRout = " + completeRout );
        END_OF_ROUT = completeRout;
    }

    protected void modifyCarParameters(int id){
        int thisRouteNumber = vehiclesList.get(id).getRouteSectionNumber();
        for(SingleRouteSection s : singleRouteSectionList){
            if(s.routeNumber == thisRouteNumber){
                if( id == 0){
                    vehiclesList.get(id).drive(
                            CONVOY_VELOCITY, 0, s.routeSurface,
                            0F, 0F, 0F, s.routeIsClosed);
                }
                else{
                    vehiclesList.get(id).drive(
                            vehiclesList.get(id - 1).getVehiclePosition(), EXPECTED_VEHICLES_DISTANCE, 0,
                            s.routeSurface, 0F, 0F, 0F, s.routeIsClosed);
                }
            }
        }

        for(Vehicle v : vehiclesList){
            float pos = v.getVehiclePosition();
            for(SingleRouteSection s : singleRouteSectionList){
                if( pos <= s.routeLength ){
                    v.setRouteSectionNumber(s.routeNumber);
                    break;
                }
                pos -= s.routeLength;
            }
        }
        printVehicleData(vehiclesList.get(id));
    }

    protected void printVehicleData(Vehicle vehicle){
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(FEDERATE_NAME + "    ::   Vehicle number = " + vehicle.getVehicleNumber());
        System.out.println(FEDERATE_NAME + "    ::   Position = " + vehicle.getVehiclePosition());
        System.out.println(FEDERATE_NAME + "    ::   Route = " + vehicle.getRouteSectionNumber());
        System.out.println(FEDERATE_NAME + "    ::   FuelLevel = " + vehicle.getFuelLevel());
        System.out.println("--------------------------------------------------------------------------------");
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        this.vehicleHandle = rtiAmbassador.getObjectClassHandle( "HLAobjectRoot.Vehicle" );
        this.vehicleNumberHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "VehicleNumber" );
        this.vehiclePositionHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "VehiclePosition" );
        this.vehicleRouteNumberHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "RouteNumber" );

        AttributeHandleSet attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        attributes.add( vehicleNumberHandle );
        attributes.add( vehiclePositionHandle );
        attributes.add( vehicleRouteNumberHandle );

        rtiAmbassador.publishObjectClassAttributes( vehicleHandle, attributes );

        finishSimulationHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.FinishSimulation" );
        rtiAmbassador.publishInteractionClass(finishSimulationHandle);

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

    protected void updateAttributeValues(ObjectInstanceHandle objectHandle, int id) throws RTIexception {
        AttributeHandleValueMap attributes = rtiAmbassador.getAttributeHandleValueMapFactory().create(3);

        HLAinteger32BE vehicleRouteSection = encoderFactory.createHLAinteger32BE( vehiclesList.get(id).getVehicleNumber() );
        attributes.put( vehicleNumberHandle, vehicleRouteSection.toByteArray() );

        HLAfloat32BE newPosition = encoderFactory.createHLAfloat32BE( vehiclesList.get(id).getVehiclePosition() );
        attributes.put( vehiclePositionHandle, newPosition.toByteArray() );

        HLAinteger32BE newVehicleRouteSection = encoderFactory.createHLAinteger32BE( vehiclesList.get(id).getRouteSectionNumber() );
        attributes.put( vehicleRouteNumberHandle, newVehicleRouteSection.toByteArray() );

        HLAfloat64Time time = timeFactory.makeTime( federationAmbassador.federateTime+ federationAmbassador.federateLookahead );
        rtiAmbassador.updateAttributeValues( objectHandle, attributes, generateTag(), time );
    }

    protected void sendFinishSimulationInteraction() throws RTIexception{
        ParameterHandleValueMap parameterHandleValueMap = rtiAmbassador.getParameterHandleValueMapFactory().create(1);
        rtiAmbassador.sendInteraction(finishSimulationHandle, parameterHandleValueMap, generateTag());
    }
}