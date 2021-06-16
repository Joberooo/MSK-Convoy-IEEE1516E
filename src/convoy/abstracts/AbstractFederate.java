package convoy.abstracts;

import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractFederate {
    public String FEDERATE_NAME;
    public String FEDERATION_NAME;
    public double TIME_STEP;
    public static final String READY_TO_RUN = "ReadyToRun";

    protected RTIambassador rtiAmbassador;
    protected AbstractAmbassador federationAmbassador;
    protected HLAfloat64TimeFactory timeFactory;
    public EncoderFactory encoderFactory;

    public ObjectClassHandle sodaHandle;
    public AttributeHandle cupsHandle;
    public AttributeHandle attributeHandle;
    public InteractionClassHandle servedHandle;

    protected AbstractFederate(String federateName, String federationName,double timeStep) {
        this.FEDERATE_NAME = federateName;
        this.FEDERATION_NAME = federationName;
        this.TIME_STEP = timeStep;
    }

    protected void log( String message ){
        System.out.println( FEDERATE_NAME + "   : " + message );
    }

    protected void waitForUser(){
        log( " >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" );
        BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
        try
        {
            reader.readLine();
        }
        catch( Exception e )
        {
            log( "Error while waiting for user input: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    protected void enableTimePolicy() throws Exception{
        HLAfloat64Interval lookahead = timeFactory.makeInterval( federationAmbassador.federateLookahead );

        this.rtiAmbassador.enableTimeRegulation( lookahead );

        while(!federationAmbassador.isRegulating){
            rtiAmbassador.evokeMultipleCallbacks( 0.1, 0.2 );
        }

        this.rtiAmbassador.enableTimeConstrained();

        while(!federationAmbassador.isConstrained){
            rtiAmbassador.evokeMultipleCallbacks( 0.1, 0.2 );
        }
    }

    protected void advanceTime() throws RTIexception {
        federationAmbassador.isAdvancing = true;
        HLAfloat64Time time = timeFactory.makeTime( federationAmbassador.federateTime + TIME_STEP );
        rtiAmbassador.timeAdvanceRequest( time );

        while( federationAmbassador.isAdvancing )
        {
            rtiAmbassador.evokeMultipleCallbacks( 0.1, 0.2 );
        }
    }

    protected short getTimeAsShort(){
        return (short) federationAmbassador.federateTime;
    }

    protected byte[] generateTag(){
        return ("(timestamp) "+System.currentTimeMillis()).getBytes();
    }

    protected void runFederate() throws Exception{
        log( "Creating RTIAmbassador" );
        rtiAmbassador = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

        log( "Connecting..." );
        createRTIAmbassador();

        rtiAmbassador.connect(federationAmbassador, CallbackModel.HLA_EVOKED );

        log( "Creating " + FEDERATION_NAME + "..." );

        loadModules();

        this.timeFactory = (HLAfloat64TimeFactory) rtiAmbassador.getTimeFactory();

        rtiAmbassador.registerFederationSynchronizationPoint( READY_TO_RUN, null );
        while(!federationAmbassador.isAnnounced){
            rtiAmbassador.evokeMultipleCallbacks( 0.1, 0.2 );
        }
        waitForUser();

        rtiAmbassador.synchronizationPointAchieved( READY_TO_RUN );
        log( "Achieved sync point: " + READY_TO_RUN + ", waiting for " + FEDERATION_NAME + "..." );
        while(!federationAmbassador.isReadyToRun){
            rtiAmbassador.evokeMultipleCallbacks( 0.1, 0.2 );
        }

        enableTimePolicy();
        log( "Time Policy Enabled" );

        mainContent();

        rtiAmbassador.resignFederationExecution( ResignAction.DELETE_OBJECTS );
        log( "Resigned from " + FEDERATION_NAME );

        try{
            rtiAmbassador.destroyFederationExecution( FEDERATION_NAME );
            log( "Destroyed " + FEDERATION_NAME );
        }
        catch( FederationExecutionDoesNotExist dne ){
            log( "No need to destroy " + FEDERATION_NAME + ", it doesn't exist" );
        }
        catch( FederatesCurrentlyJoined fcj ){
            log( "Didn't destroy " + FEDERATION_NAME + ", federates still joined" );
        }
    }

    protected void loadModules() throws Exception{
        try
        {
            URL[] modules = new URL[]{
                    (new File("foms/RestaurantProcesses.xml")).toURI().toURL(),
                    (new File("foms/RestaurantFood.xml")).toURI().toURL(),
                    (new File("foms/RestaurantDrinks.xml")).toURI().toURL()
            };

            rtiAmbassador.createFederationExecution( FEDERATION_NAME, modules );
            log( "Created " + FEDERATION_NAME );
        }
        catch( FederationExecutionAlreadyExists exists )
        {
            log( "Didn't create " + FEDERATION_NAME + ", it already existed" );
        }
        catch( MalformedURLException e )
        {
            log( "Exception loading one of the FOM modules from disk: " + e.getMessage() );
            e.printStackTrace();
            return;
        }

        URL[] joinModules = new URL[]{
                (new File("foms/RestaurantSoup.xml")).toURI().toURL()
        };

        rtiAmbassador.joinFederationExecution( FEDERATE_NAME, "ExampleFederateType", FEDERATION_NAME, joinModules );

        log( "Joined " + FEDERATION_NAME + " as " + FEDERATE_NAME );
    } // ZAKUTALIZOWAC PO ZMIANIE FOM!!!

    protected abstract void createRTIAmbassador();

    protected abstract void mainContent() throws Exception;

    protected abstract void publishAndSubscribe() throws RTIexception;

    protected abstract ObjectInstanceHandle registerObject() throws RTIexception;

    protected abstract void updateAttributeValues( ObjectInstanceHandle objectHandle ) throws RTIexception;

    protected abstract void sendInteraction() throws RTIexception;

    protected abstract void deleteObject( ObjectInstanceHandle handle ) throws RTIexception;
}
