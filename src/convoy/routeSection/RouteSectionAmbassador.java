package convoy.routeSection;

import convoy.abstracts.AbstractAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.time.HLAfloat64Time;

public class RouteSectionAmbassador extends AbstractAmbassador {
    public static double federateTime        = 0.0;
    public static double federateLookahead   = 1.0;

    protected RouteSectionAmbassador(RouteSectionFederate federate) {
        super(federate, federateTime, federateLookahead);
    }

    @Override
    protected void log(String message) {
        System.out.println( federate.FEDERATE_NAME + "Ambassador: " + message );
    }

    @Override
    protected String decodeFlavor(byte[] bytes) {
        HLAinteger32BE value = federate.encoderFactory.createHLAinteger32BE();

        try
        {
            value.decode( bytes );
        }
        catch( DecoderException de )
        {
            return "Decoder Exception: "+de.getMessage();
        }

        switch( value.getValue() )
        {
            case 101:
                return "Cola";
            case 102:
                return "Orange";
            case 103:
                return "RootBeer";
            case 104:
                return "Cream";
            default:
                return "Unknown";
        }
    }

    @Override
    protected short decodeNumCups(byte[] bytes) {
        HLAinteger16BE value = federate.encoderFactory.createHLAinteger16BE();

        try
        {
            value.decode( bytes );
            return value.getValue();
        }
        catch( DecoderException de )
        {
            de.printStackTrace();
            return 0;
        }
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, FederateAmbassador.SupplementalReflectInfo reflectInfo) {
        StringBuilder builder = new StringBuilder( "Reflection for object:" );

        builder.append(" handle=").append(theObject).append(", tag=").append(new String(tag));

        if( time != null ){
            builder.append(", time=").append(((HLAfloat64Time) time).getValue());
        }

        builder.append(", attributeCount=").append(theAttributes.size()).append( "\n" );
        for( AttributeHandle attributeHandle : theAttributes.keySet() )
        {
            builder.append( "\tattributeHandle=" );

            if( attributeHandle.equals(federate.attributeHandle) )
            {
                builder.append( attributeHandle );
                builder.append( " (Flavor)    " );
                builder.append( ", attributeValue=" );
                builder.append( decodeFlavor(theAttributes.get(attributeHandle)) );
            }
            else if( attributeHandle.equals(federate.cupsHandle) )
            {
                builder.append( attributeHandle );
                builder.append( " (NumberCups)" );
                builder.append( ", attributeValue=" );
                builder.append( decodeNumCups(theAttributes.get(attributeHandle)) );
            }
            else
            {
                builder.append( attributeHandle );
                builder.append( " (Unknown)   " );
            }

            builder.append( "\n" );
        }

        log( builder.toString() );
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, FederateAmbassador.SupplementalReceiveInfo receiveInfo) {
        StringBuilder builder = new StringBuilder( "Interaction Received:" );

        // print the handle
        builder.append(" handle=").append(interactionClass);
        if( interactionClass.equals(federate.servedHandle) )
        {
            builder.append( " (DrinkServed)" );
        }

        // print the tag
        builder.append(", tag=").append(new String(tag));
        // print the time (if we have it) we'll get null if we are just receiving
        // a forwarded call from the other reflect callback above
        if( time != null )
        {
            builder.append(", time=").append(((HLAfloat64Time) time).getValue());
        }

        // print the parameter information
        builder.append(", parameterCount=").append(theParameters.size());
        builder.append( "\n" );
        for( ParameterHandle parameter : theParameters.keySet() )
        {
            // print the parameter handle
            builder.append( "\tparamHandle=" );
            builder.append( parameter );
            // print the parameter value
            builder.append( ", paramValue=" );
            builder.append( theParameters.get(parameter).length );
            builder.append( " bytes" );
            builder.append( "\n" );
        }

        log( builder.toString() );
    }
}