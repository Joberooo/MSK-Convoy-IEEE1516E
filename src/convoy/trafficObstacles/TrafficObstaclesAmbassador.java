package convoy.trafficObstacles;

import convoy.abstracts.AbstractAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.time.HLAfloat64Time;
import org.portico.impl.hla1516e.types.encoding.HLA1516eFloat32BE;
import org.portico.impl.hla1516e.types.encoding.HLA1516eInteger32BE;

import java.util.Collections;

public class TrafficObstaclesAmbassador extends AbstractAmbassador {
    protected final TrafficObstaclesFederate federate;

    public static double federateTime        = 0.0;
    public static double federateLookahead   = 1.0;

    protected TrafficObstaclesAmbassador(TrafficObstaclesFederate federate) {
        super(federate, federateTime, federateLookahead);
        this.federate = federate;
    }

    @Override
    protected void log(String message) {
        System.out.println( TrafficObstaclesFederate.FEDERATE_NAME + "Ambassador: " + message );
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, FederateAmbassador.SupplementalReflectInfo reflectInfo) {
        StringBuilder builder = new StringBuilder( "Reflection for object:" );

        builder.append(" handle=").append(theObject).append(", tag=").append(new String(tag));

        if( time != null ) builder.append(", time=").append(((HLAfloat64Time) time).getValue());

        builder.append(", attributeCount=").append(theAttributes.size()).append( "\n" );

        boolean isItVehicle = false;
        TrafficObstaclesFederate.SingleVehicle singleVehicle = new TrafficObstaclesFederate.SingleVehicle();

        for( AttributeHandle attributeHandle : theAttributes.keySet() )
        {
            builder.append( "\tattributeHandle=" );

            if( attributeHandle.equals(federate.vehicleNumberHandle) )
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
        if(isItVehicle){
            for(TrafficObstaclesFederate.SingleVehicle v : federate.singleVehiclesList){
                if(v.vehicleNumber == singleVehicle.vehicleNumber){
                    v.vehiclePosition = singleVehicle.vehiclePosition;
                    v.vehicleRouteNumber = singleVehicle.vehicleRouteNumber;
                    add = false;
                    break;
                }
            }
        }
        if(add && isItVehicle){
            federate.singleVehiclesList.add(singleVehicle);
            Collections.sort(federate.singleVehiclesList);
        }
        log( builder.toString() );
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, FederateAmbassador.SupplementalReceiveInfo receiveInfo) {
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