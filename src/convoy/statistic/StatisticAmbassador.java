package convoy.statistic;

import convoy.abstracts.AbstractAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.time.HLAfloat64Time;
import org.portico.impl.hla1516e.types.encoding.HLA1516eBoolean;
import org.portico.impl.hla1516e.types.encoding.HLA1516eFloat32BE;
import org.portico.impl.hla1516e.types.encoding.HLA1516eInteger32BE;

import java.util.Collections;

public class StatisticAmbassador extends AbstractAmbassador {
    protected final StatisticFederate federate;

    public static double federateTime        = 0.0;
    public static double federateLookahead   = 1.0;

    protected StatisticAmbassador(StatisticFederate federate) {
        super(federate, federateTime, federateLookahead);
        this.federate = federate;
    }

    @Override
    protected void log(String message) {
        System.out.println( StatisticFederate.FEDERATE_NAME + "Ambassador: " + message );
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) {
        StringBuilder builder = new StringBuilder( "Reflection for object:" );

        builder.append(" handle=").append(theObject).append(", tag=").append(new String(tag));

        if( time != null ) builder.append(", time=").append(((HLAfloat64Time) time).getValue());

        builder.append(", attributeCount=").append(theAttributes.size()).append( "\n" );

        boolean isItRoute = false;
        StatisticFederate.SingleRouteSection singleRoute = new StatisticFederate.SingleRouteSection();

        boolean isItVehicle = false;
        StatisticFederate.SingleVehicle singleVehicle = new StatisticFederate.SingleVehicle();

        for( AttributeHandle attributeHandle : theAttributes.keySet() )
        {
            builder.append( "\tattributeHandle=" );

            if( attributeHandle.equals(federate.routeSectionNumberHandle) )
            {
                builder.append( attributeHandle ).append( " (routeSectionNumber) " ).append( ", attributeValue=" );
                HLAinteger32BE number = new HLA1516eInteger32BE();
                try {
                    number.decode(theAttributes.get(attributeHandle));
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                builder.append( number.getValue() );
                isItRoute = true;
                singleRoute.routeNumber = number.getValue();
            }
            else if( attributeHandle.equals(federate.routeSectionLengthHandle) )
            {
                builder.append( attributeHandle ).append( " (routeSectionLength) " ).append( ", attributeValue=" );
                HLAfloat32BE length = new HLA1516eFloat32BE();
                try {
                    length.decode(theAttributes.get(attributeHandle));
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                builder.append( length.getValue() );
                isItRoute = true;
                singleRoute.routeLength = length.getValue();
            }
            else if( attributeHandle.equals(federate.routeSurfaceHandle) )
            {
                builder.append( attributeHandle ).append( " (routeSurface) " ).append( ", attributeValue=" );
                HLAinteger32BE surface = new HLA1516eInteger32BE();
                try {
                    surface.decode(theAttributes.get(attributeHandle));
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                builder.append( surface.getValue() );
                isItRoute = true;
                singleRoute.routeSurface = surface.getValue();
            }
            else if( attributeHandle.equals(federate.routeSectionIsClosedHandle) )
            {
                builder.append( attributeHandle ).append( " (routeSectionIsClosed) " ).append( ", attributeValue=" );
                HLA1516eBoolean isClosed = new HLA1516eBoolean();
                try {
                    isClosed.decode(theAttributes.get(attributeHandle));
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                builder.append( isClosed.getValue() );
                isItRoute = true;
                singleRoute.routeIsClosed = isClosed.getValue();
            }
            else if( attributeHandle.equals(federate.vehicleNumberHandle) )
            {
                builder.append( attributeHandle ).append( " (vehicleNumber) " ).append( ", attributeValue=" );
                HLAinteger32BE number = new HLA1516eInteger32BE();
                try {
                    number.decode(theAttributes.get(attributeHandle));
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                builder.append( number.getValue() );
                isItVehicle = true;
                singleVehicle.vehicleNumber = number.getValue();
            }
            else if( attributeHandle.equals(federate.vehiclePositionHandle) )
            {
                builder.append( attributeHandle ).append( " (vehiclePosition) " ).append( ", attributeValue=" );
                HLAfloat32BE position = new HLA1516eFloat32BE();
                try {
                    position.decode(theAttributes.get(attributeHandle));
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                builder.append( position.getValue() );
                isItVehicle = true;
                singleVehicle.vehiclePosition = position.getValue();
            }
            else if( attributeHandle.equals(federate.vehicleRouteNumberHandle) )
            {
                builder.append( attributeHandle ).append( " (vehicleRouteNumber) " ).append( ", attributeValue=" );
                HLAinteger32BE vehicleRouteNumber = new HLA1516eInteger32BE();
                try {
                    vehicleRouteNumber.decode(theAttributes.get(attributeHandle));
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                builder.append( vehicleRouteNumber.getValue() );
                isItVehicle = true;
                singleVehicle.vehicleRouteNumber = vehicleRouteNumber.getValue();
            }
            else builder.append( attributeHandle ).append( " (Unknown)   " );
            builder.append( "\n" );
        }
        boolean add = true;
        if(isItRoute){
            for(StatisticFederate.SingleRouteSection s : federate.singleRouteSectionList){
                if(s.routeNumber == singleRoute.routeNumber){
                    s.routeIsClosed = singleRoute.routeIsClosed;
                    s.routeLength = singleRoute.routeLength;
                    s.routeSurface = singleRoute.routeSurface;
                    add = false;
                    break;
                }
            }
        }
        if(isItVehicle){
            for(StatisticFederate.SingleVehicle v : federate.singleVehiclesList){
                if(v.vehicleNumber == singleVehicle.vehicleNumber){
                    v.vehiclePosition = singleVehicle.vehiclePosition;
                    v.vehicleRouteNumber = singleVehicle.vehicleRouteNumber;
                    add = false;
                    break;
                }
            }
        }
        if(add && isItRoute){
            federate.singleRouteSectionList.add(singleRoute);
            Collections.sort(federate.singleRouteSectionList);
        }
        if(add && isItVehicle){
            federate.singleVehiclesList.add(singleVehicle);
            Collections.sort(federate.singleVehiclesList);
        }
        log( builder.toString() );
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) {
        StringBuilder builder = new StringBuilder( "Interaction Received:" );
        builder.append(" handle=").append(interactionClass);

        if( interactionClass.equals(federate.finishSimulationHandle) ){
            builder.append(" (FinishSimulation) ");
            stopRunning();
        }

        builder.append(", tag=").append(new String(tag));

        if( time != null ) builder.append(", time=").append(((HLAfloat64Time) time).getValue());

        builder.append(", parameterCount=").append(theParameters.size()).append( "\n" );

        log( builder.toString() );
    }
}