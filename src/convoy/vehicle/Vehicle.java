package convoy.vehicle;

public class Vehicle {
    private final int vehicleNumber;
    private int routeSectionNumber;
    private float vehiclePosition;
    private float carVelocity;
    private float fuelLevel;

    public Vehicle(int vehicleNumber, int routeSectionNumber, float fuelLevel,
                   float carVelocity, float vehiclePosition){

        this.vehicleNumber = vehicleNumber;
        this.routeSectionNumber = routeSectionNumber;
        this.fuelLevel = fuelLevel;
        this.carVelocity = carVelocity;
        this.vehiclePosition = vehiclePosition;
    }

    public void drive(float previousVehiclePosition, float expectedDistance){
        float distance = previousVehiclePosition - this.vehiclePosition;
        this.carVelocity = (distance - (distance/expectedDistance) - expectedDistance*1.01f);
        this.fuelLevel = (this.fuelLevel - Math.abs(this.carVelocity)*0.01f);
        this.vehiclePosition = this.vehiclePosition + this.carVelocity;
    }
    public void drive(float convoyVelocity){
        this.carVelocity = 0;
        this.carVelocity = convoyVelocity + this.carVelocity;
        this.fuelLevel = (this.fuelLevel - this.carVelocity*0.01f - Math.abs(this.carVelocity)*0.01f);
        this.vehiclePosition = this.vehiclePosition + this.carVelocity;
    }

    public int getVehicleNumber() {
        return vehicleNumber;
    }

    public int getRouteSectionNumber() {
        return routeSectionNumber;
    }

    public float getVehiclePosition() {
        return vehiclePosition;
    }

    public float getCarVelocity() {
        return carVelocity;
    }

    public float getFuelLevel() {
        return fuelLevel;
    }
}
