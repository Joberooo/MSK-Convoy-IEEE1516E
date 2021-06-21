package convoy.statistic;

import convoy.abstracts.AbstractFederate;
import convoy.config.Config;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.exceptions.RTIexception;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.util.ArrayList;

public class StatisticFederate extends AbstractFederate {
    public static String FEDERATION_NAME = "ConvoyFederation";
    public static String FEDERATE_NAME = "StatisticFederate";
    public static double TIME_STEP = Config.STATISTIC_TIME_STEP;
    public float END_OF_ROUT;

    protected ObjectClassHandle vehicleHandle;
    protected AttributeHandle vehicleNumberHandle;
    protected AttributeHandle vehiclePositionHandle;
    protected AttributeHandle vehicleRouteNumberHandle;

    public ArrayList<StatisticFederate.SingleVehicle> singleVehiclesList = new ArrayList<>();

    public static class SingleVehicle implements Comparable<StatisticFederate.SingleVehicle>{
        public int vehicleNumber;
        public float vehiclePosition;
        public int vehicleRouteNumber;

        public Integer getRouteNumber(){
            return this.vehicleNumber;
        }

        @Override
        public int compareTo(@NotNull StatisticFederate.SingleVehicle o) {
            return this.getRouteNumber().compareTo(o.getRouteNumber());
        }
    }

    public ObjectClassHandle routeSectionHandle;
    public AttributeHandle routeSectionNumberHandle;
    public AttributeHandle routeSectionLengthHandle;
    public AttributeHandle routeSurfaceHandle;
    public AttributeHandle routeSectionIsClosedHandle;

    public ArrayList<StatisticFederate.SingleRouteSection> singleRouteSectionList = new ArrayList<>();

    public static class SingleRouteSection implements Comparable<StatisticFederate.SingleRouteSection>{
        public int routeNumber;
        public float routeLength;
        public int routeSurface;
        public boolean routeIsClosed;

        public Integer getRouteNumber(){
            return this.routeNumber;
        }

        @Override
        public int compareTo(@NotNull StatisticFederate.SingleRouteSection o) {
            return this.getRouteNumber().compareTo(o.getRouteNumber());
        }
    }

    protected StatisticFederate() {
        super(FEDERATE_NAME, FEDERATION_NAME, TIME_STEP);
    }

    public static void main( String[] args ){
        try
        {
            new StatisticFederate().runFederate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void createRTIAmbassador() {
        federationAmbassador = new StatisticAmbassador(this);
    }

    @Override
    protected void mainContent() throws Exception {
        publishAndSubscribe();
        log( "Published and Subscribed" );

        ArrayList<Double> timeArray = new ArrayList<>();

        ArrayList<Double> timeLine = new ArrayList<>();
        ArrayList<ArrayList<Float>> allPositions = new ArrayList<>();

        boolean itIsFirstTime = true;

        while(federationAmbassador.isRunning)
        {
            if(singleRouteSectionList.size() == 0){
                log( "Waiting for routs..." );
            }
            else if(singleVehiclesList.size() == 0){
                log( "Waiting for vehicles...");
            }
            else{
                calculateCompleteRout();
                if(itIsFirstTime){
                    for(SingleVehicle v: singleVehiclesList){
                        Float obj = v.vehiclePosition;
                        ArrayList<Float> list = new ArrayList<>();
                        list.add( obj );
                        allPositions.add( list );
                    }
                    timeLine.add(federationAmbassador.federateTime);
                    itIsFirstTime = false;
                }
                else{
                    for(SingleVehicle v: singleVehiclesList){
                        if(v.vehiclePosition <= END_OF_ROUT) allPositions.get(v.vehicleNumber).add(v.vehiclePosition);
                    }
                    timeLine.add(federationAmbassador.federateTime);
                }
                for(SingleVehicle v: singleVehiclesList){
                    if(v.vehiclePosition >= END_OF_ROUT) timeArray.add(federationAmbassador.federateTime);
                }
            }
            advanceTime();
            log( "Time Advanced to " + federationAmbassador.federateTime );
        }
        double sum = 0;
        for(Double val: timeArray){
            sum += val;
        }
        double avg = sum / timeArray.size();
        log( "The average travel time over a given distance of the entire column: " + avg );

        XYChart chart = new XYChartBuilder().width(800).height(600).xAxisTitle("Time").yAxisTitle("Position").build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);

        for(SingleVehicle v: singleVehiclesList){
            String name = "Vehicle - " + v.vehicleNumber;
            int size = allPositions.get(v.vehicleNumber).size();
            double[] time = new double[size];
            for(int i = 0; i < size; i++){
                time[i] = timeLine.get(i);
            }
            double[] positions = new double[size];
            for(int i = 0; i < size; i++){
                positions[i] = allPositions.get(v.vehicleNumber).get(i);
            }
            chart.addSeries(name, time, positions).setMarker(SeriesMarkers.NONE);
        }

        new SwingWrapper(chart).displayChart();
    }

    private void calculateCompleteRout(){
        float completeRout = 0;
        for(StatisticFederate.SingleRouteSection s : singleRouteSectionList){
            completeRout += s.routeLength;
        }
        log( "CompleteRout = " + completeRout);
        END_OF_ROUT = completeRout;
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        finishSimulationHandle = rtiAmbassador.getInteractionClassHandle( "HLAinteractionRoot.FinishSimulation" );
        rtiAmbassador.subscribeInteractionClass(finishSimulationHandle);

        this.vehicleHandle = rtiAmbassador.getObjectClassHandle( "HLAobjectRoot.Vehicle" );
        this.vehicleNumberHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "VehicleNumber" );
        this.vehiclePositionHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "VehiclePosition" );
        this.vehicleRouteNumberHandle = rtiAmbassador.getAttributeHandle( vehicleHandle, "RouteNumber" );

        AttributeHandleSet attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        attributes.add( vehicleNumberHandle );
        attributes.add( vehiclePositionHandle );
        attributes.add( vehicleRouteNumberHandle );

        rtiAmbassador.subscribeObjectClassAttributes( vehicleHandle, attributes );

        this.routeSectionHandle = rtiAmbassador.getObjectClassHandle( "HLAobjectRoot.RouteSection" );
        this.routeSectionNumberHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionNumber" );
        this.routeSectionLengthHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionLength" );
        this.routeSurfaceHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionSurface" );
        this.routeSectionIsClosedHandle = rtiAmbassador.getAttributeHandle( routeSectionHandle, "RouteSectionIsClosed" );

        attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        attributes.add( routeSectionNumberHandle );
        attributes.add( routeSectionLengthHandle );
        attributes.add( routeSurfaceHandle );
        attributes.add( routeSectionIsClosedHandle );
        rtiAmbassador.subscribeObjectClassAttributes(routeSectionHandle, attributes);
    }
}