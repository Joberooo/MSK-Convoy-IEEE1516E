package convoy.vehicle;

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

public class VehicleAmbassador extends AbstractAmbassador {
    protected final VehicleFederate federate;

    public static double federateTime        = 0.0;
    public static double federateLookahead   = 1.0;

    protected VehicleAmbassador(VehicleFederate federate) {
        super(federate, federateTime, federateLookahead);
        this.federate = federate;
    }

    @Override
    protected void log(String message) {
        System.out.println( VehicleFederate.FEDERATE_NAME + "Ambassador: " + message );
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) {
        StringBuilder builder = new StringBuilder( "Reflection for object:" );

        builder.append(" handle=").append(theObject).append(", tag=").append(new String(tag));

        if( time != null ) builder.append(", time=").append(((HLAfloat64Time) time).getValue());

        builder.append(", attributeCount=").append(theAttributes.size()).append( "\n" );

        boolean isItRoute = false;
        VehicleFederate.SingleRouteSection singleRoute = new VehicleFederate.SingleRouteSection();

        boolean isItPetrol = false;
        VehicleFederate.SinglePetrolStation singlePetrol = new VehicleFederate.SinglePetrolStation();

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
            else if( attributeHandle.equals(federate.petrolStationNumberHandle) )
            {
                builder.append( attributeHandle ).append( " (petrolStationNumber) " ).append( ", attributeValue=" );
                HLAinteger32BE number = new HLA1516eInteger32BE();
                try {
                    number.decode(theAttributes.get(attributeHandle));
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                builder.append( number.getValue() );
                isItPetrol = true;
                singlePetrol.petrolNumber = number.getValue();
            }
            else if( attributeHandle.equals(federate.petrolStationPositionHandle) )
            {
                builder.append( attributeHandle ).append( " (petrolStationPosition) " ).append( ", attributeValue=" );
                HLAfloat32BE position = new HLA1516eFloat32BE();
                try {
                    position.decode(theAttributes.get(attributeHandle));
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                builder.append( position.getValue() );
                isItPetrol = true;
                singlePetrol.petrolPosition = position.getValue();
            }
            else if( attributeHandle.equals(federate.petrolStationRouteNumberHandle) )
            {
                builder.append( attributeHandle ).append( " (petrolStationRouteNumber) " ).append( ", attributeValue=" );
                HLAinteger32BE petrolStationRouteNumber = new HLA1516eInteger32BE();
                try {
                    petrolStationRouteNumber.decode(theAttributes.get(attributeHandle));
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                builder.append( petrolStationRouteNumber.getValue() );
                isItPetrol = true;
                singlePetrol.routeNumber = petrolStationRouteNumber.getValue();
            }
            else builder.append( attributeHandle ).append( " (Unknown)   " );
            builder.append( "\n" );
        }
        boolean add = true;
        if(isItRoute){
            for(VehicleFederate.SingleRouteSection s : federate.singleRouteSectionList){
                if(s.routeNumber == singleRoute.routeNumber){
                    s.routeIsClosed = singleRoute.routeIsClosed;
                    s.routeLength = singleRoute.routeLength;
                    s.routeSurface = singleRoute.routeSurface;
                    add = false;
                    break;
                }
            }
        }
        if(isItPetrol){
            for(VehicleFederate.SinglePetrolStation p : federate.singlePetrolStationsList){
                if(p.petrolNumber == singlePetrol.petrolNumber){
                    p.petrolPosition = singlePetrol.petrolPosition;
                    p.routeNumber = singlePetrol.routeNumber;
                    add = false;
                    break;
                }
            }
        }
        if(add && isItRoute){
            federate.singleRouteSectionList.add(singleRoute);
            Collections.sort(federate.singleRouteSectionList);
        }
        if(add && isItPetrol){
            federate.singlePetrolStationsList.add(singlePetrol);
            Collections.sort(federate.singlePetrolStationsList);
        }
        log( builder.toString() );
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) {
    }
}