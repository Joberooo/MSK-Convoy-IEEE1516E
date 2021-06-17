package convoy.vehicle;

import convoy.abstracts.AbstractFederate;
import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import java.util.ArrayList;

public class VehicleFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "VehicleFederate";
    public static double TIME_STEP = 1.0;

    public static final int NUMBERS_OF_VEHICLES = 3;
    public static final float EXPECTED_VEHICLES_DISTANCE = 20F;
    public static final int CONVOY_VELOCITY = 50;
    public static final float START_VEHICLES_FUEL = 100F;
    public static final float END_OF_ROUTS = 1000.0F;

    protected ObjectClassHandle vehicleHandle;
    protected AttributeHandle vehicleNumberHandle;
    protected AttributeHandle vehiclePositionHandle;
    protected AttributeHandle vehicleRouteNumberHandle;

    protected ArrayList<Vehicle> vehiclesList = new ArrayList<>();
    protected ArrayList<ObjectInstanceHandle> vehicleObjectInstanceHandleList = new ArrayList<>();

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
            float startPosition = (NUMBERS_OF_VEHICLES - i) * EXPECTED_VEHICLES_DISTANCE;
            vehiclesList.add(
                    new Vehicle(i, 0, START_VEHICLES_FUEL, 0F, startPosition, 120F));
            vehicleObjectInstanceHandleList.add(objectHandle);
            printVehicleData(vehiclesList.get(i));
        }

        while(federationAmbassador.isRunning)
        {
            for(int j = 0; j < vehicleObjectInstanceHandleList.size(); j++){
                modifyCarParameters(j);
                updateAttributeValues( vehicleObjectInstanceHandleList.get(j), j );
            }

            if(vehiclesList.get(0).getVehiclePosition() > END_OF_ROUTS){
                sendFinishSimulationInteraction();
                federationAmbassador.stopRunning();
            }

            advanceTime();
            log( "Time Advanced to " + federationAmbassador.federateTime );
        }

        for (ObjectInstanceHandle objectInstanceHandle : vehicleObjectInstanceHandleList) {
            deleteObject(objectInstanceHandle);
            log("Deleted Object, handle=" + objectInstanceHandle);
        }
    }

    protected void modifyCarParameters(int id){
        if( id == 0) vehiclesList.get(id).drive(
                CONVOY_VELOCITY, 0, 0,
                0, 0, 0, false);
        else vehiclesList.get(id).drive(
                vehiclesList.get(id - 1).getVehiclePosition(), EXPECTED_VEHICLES_DISTANCE, 0,
                0, 0F, 0F, 0, false);
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
    }

    protected void updateAttributeValues(ObjectInstanceHandle objectHandle, int id) throws RTIexception {
        AttributeHandleValueMap attributes = rtiAmbassador.getAttributeHandleValueMapFactory().create(2);

        HLAfloat32BE newPosition = encoderFactory.createHLAfloat32BE( vehiclesList.get(id).getVehiclePosition() );
        attributes.put( vehiclePositionHandle, newPosition.toByteArray() );

        HLAinteger32BE availableValue = encoderFactory.createHLAinteger32BE( vehiclesList.get(id).getRouteSectionNumber() );
        attributes.put( vehicleRouteNumberHandle, availableValue.toByteArray() );

        rtiAmbassador.updateAttributeValues( objectHandle, attributes, generateTag() );

        HLAfloat64Time time = timeFactory.makeTime( federationAmbassador.federateTime+ federationAmbassador.federateLookahead );
        rtiAmbassador.updateAttributeValues( objectHandle, attributes, generateTag(), time );
    }

    protected void sendFinishSimulationInteraction() throws RTIexception{
        ParameterHandleValueMap parameterHandleValueMap = rtiAmbassador.getParameterHandleValueMapFactory().create(1);
        rtiAmbassador.sendInteraction(finishSimulationHandle, parameterHandleValueMap, generateTag());
    }
}