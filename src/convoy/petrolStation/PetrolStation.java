package convoy.petrolStation;

import java.util.Random;

public class PetrolStation {

    private static final float FUELING_TIME_RATIO = 10;
    private static final float PETROL_TIME_RATIO = 0.5f;

    private static final Random random = new Random();

    private boolean fueling = false;
    private float fuelingTime;
    private final int petrolStationNumber;
    private final float stationPosition;
    private final int routeSectionNumber;

    public PetrolStation(int petrolStationNumber, int routeSectionNumber, float stationPosition) {
        this.petrolStationNumber = petrolStationNumber;
        this.routeSectionNumber = routeSectionNumber;
        this.stationPosition = stationPosition;
    }

    public boolean isFueling() {
        return fueling;
    }

    public static int startFueling(float fuelAmount){
        return Math.round(fuelAmount / 100) + 1;
    }

    public int fuelingTime(){
        return (int)fuelingTime;
    }

    public void finishFueling(){
        fueling = false;
    }

    public int getPetrolStationNumber(){
        return this.petrolStationNumber;
    }

    public float getStationPosition() {
        return stationPosition;
    }

    public int getRouteSectionNumber(){
        return routeSectionNumber;
    }
}
