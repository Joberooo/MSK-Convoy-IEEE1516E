package convoy.vehicle;

import convoy.abstracts.AbstractFederate;
import convoy.config.Config;
import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class VehicleFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "VehicleFederate";
    public static double TIME_STEP = Config.VEHICLE_TIME_STEP;

    public static final int NUMBERS_OF_VEHICLES =  ThreadLocalRandom.current().nextInt(3, 10);
    public static final float EXPECTED_VEHICLES_DISTANCE = Config.EXPECTED_VEHICLES_DISTANCE;
    public static final float CONVOY_VELOCITY = Config.CONVOY_VELOCITY;
    public static final float START_VEHICLES_FUEL = Config.START_VEHICLES_FUEL;
    public static final float MAX_VELOCITY = Config.MAX_VELOCITY;
    public float END_OF_ROUT;
    public boolean needFueling = false;
    public boolean isFueling = false;
    public float distanceToNearestPetrolStation;
    public float positionAtActualRoute;

    protected ObjectClassHandle vehicleHandle;
    protected AttributeHandle vehicleNumberHandle;
    protected AttributeHandle vehiclePositionHandle;
    protected AttributeHandle vehicleRouteNumberHandle;

    protected InteractionClassHandle changeWeatherHandle;
    protected ParameterHandle typeHandle;
    protected ParameterHandle powerHandle;
    protected ParameterHandle xDirectionHandle;
    protected ParameterHandle yDirectionHandle;

    public int weatherType;
    public float windPower;
    public float windDirectionX;
    public float windDirectionY;

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

    protected ObjectClassHandle petrolStationHandle;
    protected AttributeHandle petrolStationNumberHandle;
    protected AttributeHandle petrolStationPositionHandle;
    protected AttributeHandle petrolStationRouteNumberHandle;

    public ArrayList<SinglePetrolStation> singlePetrolStationsList = new ArrayList<>();

    public static class SinglePetrolStation implements Comparable<SinglePetrolStation>{
        public int petrolNumber;
        public float petrolPosition;
        public int routeNumber;

        public Integer getPetrolNumber(){
            return this.petrolNumber;
        }

        @Override
        public int compareTo(@NotNull VehicleFederate.SinglePetrolStation o) {
            return this.getPetrolNumber().compareTo(o.getPetrolNumber());
        }
    }

    protected InteractionClassHandle startFuelingHandle;
    protected InteractionClassHandle endOfFuelingHandle;

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
                    new Vehicle(i, 0, START_VEHICLES_FUEL, startPosition, MAX_VELOCITY));
            vehicleObjectInstanceHandleList.add(objectHandle);
            vehiclesList.get(i).printVehicleData(FEDERATE_NAME);
        }

        while(federationAmbassador.isRunning)
        {
            if(singleRouteSectionList.size() == 0){
                log( "Waiting for routs..." );
            }
            else{
                calculateCompleteRout();

                if(!isFueling){
                    for(int j = 0; j < vehicleObjectInstanceHandleList.size(); j++){
                        modifyCarParameters(j);
                        updateAttributeValues( vehicleObjectInstanceHandleList.get(j), j );
                    }
                }

                if(vehiclesList.get(vehiclesList.size() - 1).getVehiclePosition() > END_OF_ROUT){
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
        }
        log( "CompleteRout = " + completeRout);
        END_OF_ROUT = completeRout;
    }

    protected void modifyCarParameters(int id) throws RTIexception {
        int thisRouteNumber = vehiclesList.get(id).getRouteSectionNumber();
        for(SingleRouteSection s : singleRouteSectionList){
            if(s.routeNumber == thisRouteNumber){
                if( id == 0){
                    if(this.needFueling){
                        boolean ifInRange = vehiclesList.get(id).driveToPetrolStation(CONVOY_VELOCITY, weatherType,
                                s.routeSurface, windDirectionX, windDirectionY, windPower, s.routeIsClosed,
                                this.distanceToNearestPetrolStation);
                        if(ifInRange){
                            this.isFueling = true;
                            sendFuelingInteraction();
                        }
                    }
                    else{
                        vehiclesList.get(id).drive(
                                CONVOY_VELOCITY, weatherType, s.routeSurface,
                                windDirectionX, windDirectionY, windPower, s.routeIsClosed);
                    }
                }
                else{
                    vehiclesList.get(id).drive(
                            vehiclesList.get(id - 1).getVehiclePosition(), EXPECTED_VEHICLES_DISTANCE,
                            weatherType, windDirectionX, windDirectionY, windPower, s.routeIsClosed);
                }
            }
        }

        for(Vehicle v : vehiclesList){
            float pos = v.getVehiclePosition();
            for(SingleRouteSection s : singleRouteSectionList){
                if( pos <= s.routeLength ){
                    v.setRouteSectionNumber(s.routeNumber);
                    if(v.getVehicleNumber() == 0) this.positionAtActualRoute = pos;
                    break;
                }
                pos -= s.routeLength;
            }

        }

        for(Vehicle v : vehiclesList){
            if(v.isFuelReserve()){
                this.needFueling = true;
                break;
            }
        }

        if(this.needFueling){
            for(Vehicle v : vehiclesList){
                if(v.getVehicleNumber() == 0){
                    for(SinglePetrolStation s : singlePetrolStationsList){
                        if(s.routeNumber == v.getRouteSectionNumber() && s.petrolPosition > this.positionAtActualRoute){
                            this.distanceToNearestPetrolStation = s.petrolPosition - this.positionAtActualRoute;
                            break;
                        }
                    }
                    break;
                }
            }
        }
        vehiclesList.get(id).printVehicleData(FEDERATE_NAME);
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

        changeWeatherHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.ChangeWeather" );
        typeHandle = rtiAmbassador.getParameterHandle(changeWeatherHandle, "typeOfPrecipitation");
        powerHandle = rtiAmbassador.getParameterHandle(changeWeatherHandle, "WindPower");
        xDirectionHandle = rtiAmbassador.getParameterHandle(changeWeatherHandle, "DirectionWindX");
        yDirectionHandle = rtiAmbassador.getParameterHandle(changeWeatherHandle, "DirectionWindY");
        rtiAmbassador.subscribeInteractionClass(changeWeatherHandle);

        finishSimulationHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.FinishSimulation" );
        rtiAmbassador.publishInteractionClass(finishSimulationHandle);

        startFuelingHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.StartFueling" );
        rtiAmbassador.publishInteractionClass(startFuelingHandle);

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

        this.petrolStationHandle = rtiAmbassador.getObjectClassHandle( "HLAobjectRoot.PetrolStation" );
        this.petrolStationNumberHandle = rtiAmbassador.getAttributeHandle( petrolStationHandle, "PetrolStationNumber" );
        this.petrolStationPositionHandle = rtiAmbassador.getAttributeHandle( petrolStationHandle, "PetrolStationPosition" );
        this.petrolStationRouteNumberHandle = rtiAmbassador.getAttributeHandle( petrolStationHandle, "RouteNumber" );

        attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        attributes.add( petrolStationNumberHandle );
        attributes.add( petrolStationPositionHandle );
        attributes.add(petrolStationRouteNumberHandle);

        rtiAmbassador.subscribeObjectClassAttributes( petrolStationHandle, attributes );

        endOfFuelingHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.EndOfFueling" );
        rtiAmbassador.subscribeInteractionClass(endOfFuelingHandle);
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
        HLAfloat64Time time = timeFactory.makeTime( federationAmbassador.federateTime+federationAmbassador.federateLookahead );
        rtiAmbassador.sendInteraction(finishSimulationHandle, parameterHandleValueMap, generateTag(), time);
    }

    protected void sendFuelingInteraction() throws RTIexception{
        ParameterHandleValueMap parameterHandleValueMap = rtiAmbassador.getParameterHandleValueMapFactory().create(1);
        ParameterHandle fuelQuantityHandle = rtiAmbassador.getParameterHandle(startFuelingHandle, "FuelQuantity");

        float sum = 0;
        for(Vehicle v: vehiclesList){
            sum += v.getMaxVelocity() - v.getFuelLevel();
        }

        HLAfloat32BE fuelQuantity = encoderFactory.createHLAfloat32BE(sum);

        parameterHandleValueMap.put(fuelQuantityHandle, fuelQuantity.toByteArray());
        HLAfloat64Time time = timeFactory.makeTime( federationAmbassador.federateTime+federationAmbassador.federateLookahead );
        rtiAmbassador.sendInteraction(startFuelingHandle, parameterHandleValueMap, generateTag(), time);
    }
}