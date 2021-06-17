package convoy.vehicle;

import convoy.abstracts.AbstractAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.time.HLAfloat64Time;

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
    protected String decodeFlavor(byte[] bytes) {
        return "";
    }

    @Override
    protected short decodeNumCups(byte[] bytes) {
        return 0;
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) {
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) {
    }
}