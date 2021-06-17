package convoy.trafficObstacles;

import convoy.abstracts.AbstractAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.time.HLAfloat64Time;

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