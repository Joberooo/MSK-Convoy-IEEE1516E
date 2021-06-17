package convoy.vehicle;

import convoy.abstracts.AbstractAmbassador;
import hla.rti1516e.*;

public class VehicleAmbassador extends AbstractAmbassador {
    public static double federateTime        = 0.0;
    public static double federateLookahead   = 1.0;

    protected VehicleAmbassador(VehicleFederate federate) {
        super(federate, federateTime, federateLookahead);
    }

    @Override
    protected void log(String message) {
        System.out.println( federate.FEDERATE_NAME + "Ambassador: " + message );
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) {
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) {
    }
}