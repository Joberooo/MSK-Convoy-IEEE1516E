package convoy.routeSection;

import convoy.abstracts.AbstractAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.time.HLAfloat64Time;
import org.portico.impl.hla1516e.types.encoding.HLA1516eInteger32BE;

public class RouteSectionAmbassador extends AbstractAmbassador {
    protected final RouteSectionFederate federate;

    public static double federateTime        = 0.0;
    public static double federateLookahead   = 1.0;

    protected RouteSectionAmbassador(RouteSectionFederate federate) {
        super(federate, federateTime, federateLookahead);
        this.federate = federate;
    }

    @Override
    protected void log(String message) {
        System.out.println( RouteSectionFederate.FEDERATE_NAME + "Ambassador: " + message );
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, FederateAmbassador.SupplementalReflectInfo reflectInfo) {
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, FederateAmbassador.SupplementalReceiveInfo receiveInfo) {
        StringBuilder builder = new StringBuilder( "Interaction Received:" );
        builder.append(" handle=").append(interactionClass);

        boolean ifDelete = false;
        boolean ifChange = false;

        if( interactionClass.equals(federate.finishSimulationHandle) ){
            builder.append(" (FinishSimulation) ");
            stopRunning();
        }
        else if( interactionClass.equals(federate.deleteRouteSectionHandle) ){
            builder.append(" (DeleteRouteSection) ");
            ifDelete = true;
        }
        else if( interactionClass.equals(federate.closeOrOpenRouteSectionHandle) ){
            builder.append(" (CloseOrOpenRouteSection" );
            ifChange = true;
        }

        builder.append(", tag=").append(new String(tag));

        if( time != null ) builder.append(", time=").append(((HLAfloat64Time) time).getValue());

        builder.append(", parameterCount=").append(theParameters.size()).append( "\n" );

        for( ParameterHandle parameter : theParameters.keySet() )
        {
            if(parameter.equals(federate.numberToDeleteHandle))
            {
                builder.append( "\tnumberToDelete param!" );
                byte[] bytes = theParameters.get(federate.numberToDeleteHandle);
                HLAinteger32BE number = new HLA1516eInteger32BE();
                try {
                    number.decode(bytes);
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                federate.numberToDelete = number.getValue();
                builder.append("\tnumberToDelete Value=").append(federate.numberToDelete);
            }
            else if(parameter.equals(federate.numberToChangeHandle))
            {
                builder.append( "\tnumberToChange param!" );
                byte[] bytes = theParameters.get(federate.numberToChangeHandle);
                HLAinteger32BE number = new HLA1516eInteger32BE();
                try {
                    number.decode(bytes);
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                federate.numberToChange = number.getValue();
                builder.append("\tnumberToChange Value=").append(federate.numberToDelete);
            }
            else
            {
                builder.append( "\tparamHandle=" ).append( parameter ).append( ", paramValue=" );
                builder.append( theParameters.get(parameter).length ).append( " bytes" ).append( "\n" );
            }
        }

        if(ifDelete) federate.deleteRouteSectionByNumber(federate.numberToDelete);
        if(ifChange) federate.closeOrOpenRouteSectionByNumber(federate.numberToChange);

        log( builder.toString() );
    }
}