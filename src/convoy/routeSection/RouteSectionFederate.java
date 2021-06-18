package convoy.routeSection;

import convoy.abstracts.AbstractFederate;
import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;

import java.util.ArrayList;

public class RouteSectionFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "RouteSectionFederate";
    public static double TIME_STEP = 5.0;

    public static final int NUMBERS_OF_ROUTE_SECTIONS = 10;

    protected ObjectClassHandle routeSectionHandle;
    protected AttributeHandle routeSectionNumberHandle;
    protected AttributeHandle routeSectionLengthHandle;
    protected AttributeHandle routeSectionSurfaceHandle;
    protected AttributeHandle routeSectionIsClosedHandle;

    protected ArrayList<RouteSection> routeSectionsList = new ArrayList<>();
    protected ArrayList<ObjectInstanceHandle> routeSectionObjectInstanceHandleList = new ArrayList<>();

    protected RouteSectionFederate() {
        super(FEDERATE_NAME, FEDERATION_NAME, TIME_STEP);
    }

    public static void main( String[] args ){
        try
        {
            new RouteSectionFederate().runFederate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void createRTIAmbassador(){
        federationAmbassador = new RouteSectionAmbassador(this);
    }

    @Override
    protected void mainContent() throws Exception {
        publishAndSubscribe();
        log( "Published and Subscribed" );

        for(int i = 0; i < NUMBERS_OF_ROUTE_SECTIONS; i++){
            ObjectInstanceHandle objectHandle = registerObject(routeSectionHandle);
            log( "Registered Object, handle=" + objectHandle );
            routeSectionsList.add(new RouteSection(i));
            routeSectionObjectInstanceHandleList.add(objectHandle);
            printRouteSectionData(routeSectionsList.get(i));
        }

        while(federationAmbassador.isRunning)
        {
            if(federationAmbassador.shouldUpdate){
                for(int j = 0; j < routeSectionObjectInstanceHandleList.size(); j++){
                    updateAttributeValues( routeSectionObjectInstanceHandleList.get(j), j );
                }
                federationAmbassador.changeShouldUpdate();
            }

            advanceTime();
            log( "Time Advanced to " + federationAmbassador.federateTime );
        }

        for (ObjectInstanceHandle objectInstanceHandle : routeSectionObjectInstanceHandleList) {
            deleteObject(objectInstanceHandle);
            log("Deleted Object, handle=" + objectInstanceHandle);
        }
    }

    protected void printRouteSectionData(RouteSection routeSection){
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(FEDERATE_NAME + "    ::   RouteSection number = " + routeSection.getRouteSectionNumber());
        System.out.println(FEDERATE_NAME + "    ::   Length = " + routeSection.getRouteSectionLength());
        System.out.println(FEDERATE_NAME + "    ::   Surface = " + routeSection.getRouteSurface());
        System.out.println(FEDERATE_NAME + "    ::   Surface = " + routeSection.getRouteSurface().ordinal());
        System.out.println(FEDERATE_NAME + "    ::   IsClosed = " + routeSection.getIsClosed());
        System.out.println("--------------------------------------------------------------------------------");
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        this.routeSectionHandle = rtiAmbassador.getObjectClassHandle( "HLAobjectRoot.RouteSection" );
        this.routeSectionNumberHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionNumber" );
        this.routeSectionLengthHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionLength" );
        this.routeSectionSurfaceHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionSurface" );
        this.routeSectionIsClosedHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionIsClosed" );

        AttributeHandleSet attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        attributes.add( routeSectionNumberHandle );
        attributes.add( routeSectionLengthHandle );
        attributes.add( routeSectionSurfaceHandle );
        attributes.add( routeSectionIsClosedHandle );

        rtiAmbassador.publishObjectClassAttributes( routeSectionHandle, attributes );

        finishSimulationHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.FinishSimulation" );
        rtiAmbassador.subscribeInteractionClass(finishSimulationHandle);
    }

    protected void updateAttributeValues(ObjectInstanceHandle objectHandle, int id) throws RTIexception {
        AttributeHandleValueMap attributes = rtiAmbassador.getAttributeHandleValueMapFactory().create(4);

        HLAinteger32BE routeNumber = encoderFactory.createHLAinteger32BE( routeSectionsList.get(id).getRouteSectionNumber() );
        attributes.put( routeSectionNumberHandle, routeNumber.toByteArray() );

        HLAfloat32BE routeSectionLength = encoderFactory.createHLAfloat32BE( routeSectionsList.get(id).getRouteSectionLength() );
        attributes.put( routeSectionLengthHandle, routeSectionLength.toByteArray() );

        HLAinteger32BE routeSurface = encoderFactory.createHLAinteger32BE( routeSectionsList.get(id).getRouteSurface().ordinal() );
        attributes.put(routeSectionSurfaceHandle, routeSurface.toByteArray() );

        HLAboolean routeIsClosed = encoderFactory.createHLAboolean( routeSectionsList.get(id).getIsClosed() );
        attributes.put(routeSectionIsClosedHandle, routeIsClosed.toByteArray() );

        HLAfloat64Time time = timeFactory.makeTime( federationAmbassador.federateTime + federationAmbassador.federateLookahead );
        rtiAmbassador.updateAttributeValues( objectHandle, attributes, generateTag(), time );
    }
}