package convoy.abstracts;

import hla.rti1516e.*;
import hla.rti1516e.time.HLAfloat64Time;

public abstract class AbstractAmbassador extends NullFederateAmbassador {
    protected final AbstractFederate federate;

    public double federateTime;
    public double federateLookahead;

    protected boolean isRegulating       = false;
    protected boolean isConstrained      = false;
    protected boolean isAdvancing        = false;

    public boolean isAnnounced        = false;
    public boolean isReadyToRun       = false;

    public boolean isRunning          = true;
    public boolean shouldUpdate       = true;

    protected AbstractAmbassador(AbstractFederate federate, double federateTime, double federateLookahead) {
        this.federate = federate;
        this.federateTime = federateTime;
        this.federateLookahead = federateLookahead;
    }

    public void stopRunning(){
        this.isRunning = false;
    }

    public void changeShouldUpdateToTrue(){
        this.shouldUpdate = true;
    }

    public void changeShouldUpdateToFalse(){
        this.shouldUpdate = false;
    }

    @Override
    public void synchronizationPointRegistrationFailed( String label, SynchronizationPointFailureReason reason ){
        log( "Failed to register sync point: " + label + ", reason="+reason );
    }

    @Override
    public void synchronizationPointRegistrationSucceeded( String label ){
        log( "Successfully registered sync point: " + label );
    }

    @Override
    public void announceSynchronizationPoint( String label, byte[] tag ){
        log( "Synchronization point announced: " + label );
        if( label.equals(AbstractFederate.READY_TO_RUN) )
            this.isAnnounced = true;
    }

    @Override
    public void federationSynchronized( String label, FederateHandleSet failed ){
        log( "Federation Synchronized: " + label );
        if( label.equals(AbstractFederate.READY_TO_RUN) )
            this.isReadyToRun = true;
    }

    @Override
    public void timeRegulationEnabled( LogicalTime time ){
        this.federateTime = ((HLAfloat64Time)time).getValue();
        this.isRegulating = true;
    }

    @Override
    public void timeConstrainedEnabled( LogicalTime time ){
        this.federateTime = ((HLAfloat64Time)time).getValue();
        this.isConstrained = true;
    }

    @Override
    public void timeAdvanceGrant( LogicalTime time ){
        this.federateTime = ((HLAfloat64Time)time).getValue();
        this.isAdvancing = false;
    }

    @Override
    public void discoverObjectInstance(ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass,
                                       String objectName ) {
        log( "Discovered Object: handle=" + theObject + ", classHandle=" + theObjectClass + ", name=" + objectName );
    }

    @Override
    public void reflectAttributeValues( ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
                                        byte[] tag, OrderType sentOrder, TransportationTypeHandle transport,
                                        SupplementalReflectInfo reflectInfo ) {
        reflectAttributeValues( theObject, theAttributes, tag, sentOrder, transport, null, sentOrder, reflectInfo );
    }

    @Override
    public void receiveInteraction( InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters,
                                    byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport,
                                    SupplementalReceiveInfo receiveInfo ) {
        this.receiveInteraction( interactionClass, theParameters, tag, sentOrdering, theTransport, null,
                sentOrdering, receiveInfo );
    }

    @Override
    public void removeObjectInstance( ObjectInstanceHandle theObject, byte[] tag, OrderType sentOrdering,
                                      SupplementalRemoveInfo removeInfo ) {
        log( "Object Removed: handle=" + theObject );
    }

    protected abstract void log(String message);

    @Override
    public abstract void reflectAttributeValues( ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
                                        byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport,
                                        LogicalTime time, OrderType receivedOrdering,
                                        SupplementalReflectInfo reflectInfo );

    @Override
    public abstract void receiveInteraction( InteractionClassHandle interactionClass,
                                    ParameterHandleValueMap theParameters,
                                    byte[] tag,
                                    OrderType sentOrdering,
                                    TransportationTypeHandle theTransport,
                                    LogicalTime time,
                                    OrderType receivedOrdering,
                                    SupplementalReceiveInfo receiveInfo );
}
