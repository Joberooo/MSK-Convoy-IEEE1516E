package convoy.petrolStation;

public class PetrolStation {
    private final int petrolStationNumber;
    private final float stationPosition;
    private final int routeSectionNumber;

    public PetrolStation(int petrolStationNumber, int routeSectionNumber, float stationPosition) {
        this.petrolStationNumber = petrolStationNumber;
        this.routeSectionNumber = routeSectionNumber;
        this.stationPosition = stationPosition;
    }

    public static int startFueling(float fuelAmount){
        return Math.round(fuelAmount / 100) + 1;
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
