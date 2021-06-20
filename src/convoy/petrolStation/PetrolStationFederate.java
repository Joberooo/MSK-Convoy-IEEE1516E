package convoy.petrolStation;

import convoy.abstracts.AbstractFederate;
import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PetrolStationFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "PetrolStationFederate";
    public static double TIME_STEP = 1.0;

    private boolean isCreated = false;

    protected ObjectClassHandle petrolStationHandle;
    protected AttributeHandle petrolStationNumberHandle;
    protected AttributeHandle petrolStationPositionHandle;
    protected AttributeHandle routeNumberHandle;

    protected ArrayList<PetrolStation> petrolStationsList = new ArrayList<>();
    protected ArrayList<ObjectInstanceHandle> petrolStationsObjectInstanceHandleList = new ArrayList<>();

    public ObjectClassHandle routeSectionHandle;
    public AttributeHandle routeSectionNumberHandle;
    public AttributeHandle routeSectionLengthHandle;
    public AttributeHandle routeSurfaceHandle;
    public AttributeHandle routeSectionIsClosedHandle;

    public ArrayList<PetrolStationFederate.SingleRouteSection> singleRouteSectionList = new ArrayList<>();

    public static class SingleRouteSection implements Comparable<PetrolStationFederate.SingleRouteSection>{
        public int routeNumber;
        public float routeLength;
        public int routeSurface;
        public boolean routeIsClosed;

        public Integer getRouteNumber(){
            return this.routeNumber;
        }

        @Override
        public int compareTo(@NotNull PetrolStationFederate.SingleRouteSection o) {
            return this.getRouteNumber().compareTo(o.getRouteNumber());
        }
    }

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
            if(singleRouteSectionList.size() == 0){
                log( "Waiting for routs..." );
            }
            else{
                if(isCreated){
                    if(federationAmbassador.shouldUpdate){
                        for(int j = 0; j < petrolStationsObjectInstanceHandleList.size(); j++){
                            updateAttributeValues( petrolStationsObjectInstanceHandleList.get(j), j );
                        }
                        federationAmbassador.changeShouldUpdate();
                    }
                }
                else{
                    int newNumbers = 0;
                    petrolStationsList.clear();
                    for(SingleRouteSection s : singleRouteSectionList){
                        for(float i = 10; i < s.routeLength; i += 40){
                            ObjectInstanceHandle objectHandle = registerObject(petrolStationHandle);
                            log( "Registered Object, handle=" + objectHandle );
                            petrolStationsList.add(new PetrolStation(newNumbers, s.routeNumber,i));
                            petrolStationsObjectInstanceHandleList.add(objectHandle);
                            newNumbers++;
                        }
                    }
                    for(PetrolStation p : petrolStationsList){
                        printRouteSectionData(p);
                    }
                    for(int j = 0; j < petrolStationsObjectInstanceHandleList.size(); j++){
                        updateAttributeValues( petrolStationsObjectInstanceHandleList.get(j), j );
                    }
                    isCreated = true;
                    federationAmbassador.changeShouldUpdate();
                }
            }

            advanceTime();
            log( "Time Advanced to " + federationAmbassador.federateTime );
        }

        for (ObjectInstanceHandle objectInstanceHandle : petrolStationsObjectInstanceHandleList) {
            deleteObject(objectInstanceHandle);
            log("Deleted Object, handle=" + objectInstanceHandle);
        }
    }

    protected void printRouteSectionData(PetrolStation petrolStation){
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(FEDERATE_NAME + "    ::   PetrolStation number = " + petrolStation.getPetrolStationNumber());
        System.out.println(FEDERATE_NAME + "    ::   Position = " + petrolStation.getStationPosition());
        System.out.println(FEDERATE_NAME + "    ::   RouteNumber = " + petrolStation.getRouteSectionNumber());
        System.out.println("--------------------------------------------------------------------------------");
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        this.petrolStationHandle = rtiAmbassador.getObjectClassHandle( "HLAobjectRoot.PetrolStation" );
        this.petrolStationNumberHandle = rtiAmbassador.getAttributeHandle( petrolStationHandle, "PetrolStationNumber" );
        this.petrolStationPositionHandle = rtiAmbassador.getAttributeHandle( petrolStationHandle, "PetrolStationPosition" );
        this.routeNumberHandle = rtiAmbassador.getAttributeHandle( petrolStationHandle, "RouteNumber" );

        AttributeHandleSet attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        attributes.add( petrolStationNumberHandle );
        attributes.add( petrolStationPositionHandle );
        attributes.add( routeNumberHandle );

        rtiAmbassador.publishObjectClassAttributes( petrolStationHandle, attributes );

        finishSimulationHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.FinishSimulation" );
        rtiAmbassador.subscribeInteractionClass(finishSimulationHandle);

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

        HLAinteger32BE petrolNumber = encoderFactory.createHLAinteger32BE( petrolStationsList.get(id).getPetrolStationNumber() );
        attributes.put( petrolStationNumberHandle, petrolNumber.toByteArray() );

        HLAfloat32BE petrolPosition = encoderFactory.createHLAfloat32BE( petrolStationsList.get(id).getStationPosition() );
        attributes.put( petrolStationPositionHandle, petrolPosition.toByteArray() );

        HLAinteger32BE routeNumber = encoderFactory.createHLAinteger32BE( petrolStationsList.get(id).getRouteSectionNumber() );
        attributes.put( routeNumberHandle, routeNumber.toByteArray() );

        HLAfloat64Time time = timeFactory.makeTime( federationAmbassador.federateTime + federationAmbassador.federateLookahead );
        rtiAmbassador.updateAttributeValues( objectHandle, attributes, generateTag(), time );
    }
}