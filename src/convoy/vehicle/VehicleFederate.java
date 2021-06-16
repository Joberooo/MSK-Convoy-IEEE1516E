package convoy.vehicle;

import convoy.abstracts.AbstractFederate;
import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import java.util.Random;

public class VehicleFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "VehicleFederate";
    public static double TIME_STEP = 1.0;
    public static final int ITERATIONS = 20;

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

        ObjectInstanceHandle objectHandle = registerObject();
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
        this.sodaHandle = rtiAmbassador.getObjectClassHandle( "HLAobjectRoot.Food.Drink.Soda" );
        this.cupsHandle = rtiAmbassador.getAttributeHandle( sodaHandle, "NumberCups" );
        this.attributeHandle = rtiAmbassador.getAttributeHandle( sodaHandle, "Flavor" );

        AttributeHandleSet attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        attributes.add( cupsHandle );
        attributes.add(attributeHandle);

        rtiAmbassador.publishObjectClassAttributes( sodaHandle, attributes );

        rtiAmbassador.subscribeObjectClassAttributes( sodaHandle, attributes );

        String interactionName = "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed";
        servedHandle = rtiAmbassador.getInteractionClassHandle( interactionName );

        rtiAmbassador.publishInteractionClass( servedHandle );

        rtiAmbassador.subscribeInteractionClass( servedHandle );
    }

    @Override
    protected ObjectInstanceHandle registerObject() throws RTIexception {
        return rtiAmbassador.registerObjectInstance( sodaHandle );
    }

    @Override
    protected void updateAttributeValues(ObjectInstanceHandle objectHandle) throws RTIexception {
        AttributeHandleValueMap attributes = rtiAmbassador.getAttributeHandleValueMapFactory().create(2);

        HLAinteger16BE cupsValue = encoderFactory.createHLAinteger16BE( getTimeAsShort() );
        attributes.put( cupsHandle, cupsValue.toByteArray() );

        int randomValue = 101 + new Random().nextInt(3);
        HLAinteger32BE value = encoderFactory.createHLAinteger32BE( randomValue );
        attributes.put(attributeHandle, value.toByteArray() );

        rtiAmbassador.updateAttributeValues( objectHandle, attributes, generateTag() );

        HLAfloat64Time time = timeFactory.makeTime( federationAmbassador.federateTime+ federationAmbassador.federateLookahead );
        rtiAmbassador.updateAttributeValues( objectHandle, attributes, generateTag(), time );
    }

    @Override
    protected void sendInteraction() throws RTIexception {
        ParameterHandleValueMap parameters = rtiAmbassador.getParameterHandleValueMapFactory().create(0);
        rtiAmbassador.sendInteraction( servedHandle, parameters, generateTag() );

        HLAfloat64Time time = timeFactory.makeTime( federationAmbassador.federateTime+ federationAmbassador.federateLookahead );
        rtiAmbassador.sendInteraction( servedHandle, parameters, generateTag(), time );
    }

    @Override
    protected void deleteObject(ObjectInstanceHandle handle) throws RTIexception {
        rtiAmbassador.deleteObjectInstance( handle, generateTag() );
    }
}