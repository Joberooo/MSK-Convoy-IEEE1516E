package convoy.vehicle;

import convoy.abstracts.AbstractFederate;
import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import java.util.Random;

public class VehicleFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "VehicleFederate";
    public static double TIME_STEP = 1.0;
    public static final int ITERATIONS = 20;

    protected ObjectClassHandle vehicleHandle;
    protected AttributeHandle vehicleNumberHandle;
    protected AttributeHandle vehiclePositionHandle;
    protected AttributeHandle vehicleRouteNumberHandle;
    protected float position = 0;

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

        ObjectInstanceHandle objectHandle = registerObject(vehicleHandle);
        log( "Registered Object, handle=" + objectHandle );

        for( int i = 0; i < ITERATIONS; i++ )
        {
            updateAttributeValues( objectHandle );

            sendInteraction();

            advanceTime();
            log( "Time Advanced to " + federationAmbassador.federateTime );
        }

        deleteObject( objectHandle );
        log( "Deleted Object, handle=" + objectHandle );
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
    }

    @Override
    protected ObjectInstanceHandle registerObject(ObjectClassHandle objectClassHandle) throws RTIexception {
        return rtiAmbassador.registerObjectInstance( objectClassHandle );
    }

    @Override
    protected void updateAttributeValues(ObjectInstanceHandle objectHandle) throws RTIexception {
        AttributeHandleValueMap attributes = rtiAmbassador.getAttributeHandleValueMapFactory().create(2);

        position += new Random().nextFloat();
        System.out.println(FEDERATE_NAME + ": Position = " + position);
        HLAfloat32BE newPosition = encoderFactory.createHLAfloat32BE( position );
        attributes.put( vehiclePositionHandle, newPosition.toByteArray() );

        int actualRouteNumber = (int) position / 100;
        System.out.println(FEDERATE_NAME + ": Route = " + actualRouteNumber);
        HLAinteger32BE availableValue = encoderFactory.createHLAinteger32BE( actualRouteNumber );
        attributes.put( vehicleRouteNumberHandle, availableValue.toByteArray() );

        rtiAmbassador.updateAttributeValues( objectHandle, attributes, generateTag() );

        HLAfloat64Time time = timeFactory.makeTime( federationAmbassador.federateTime+ federationAmbassador.federateLookahead );
        rtiAmbassador.updateAttributeValues( objectHandle, attributes, generateTag(), time );
    }

    @Override
    protected void sendInteraction(){
    }

    @Override
    protected void deleteObject(ObjectInstanceHandle handle) throws RTIexception {
        rtiAmbassador.deleteObjectInstance( handle, generateTag() );
    }
}