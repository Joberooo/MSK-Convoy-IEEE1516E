package convoy.vehicle;

public class Vehicle {
    private final int vehicleNumber;
    private int routeSectionNumber;
    private float vehiclePosition;
    private float carVelocity;
    private float carAcceleration;
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
        this.carAcceleration = (float) (distance - (distance/expectedDistance) - expectedDistance*1.01);
        this.fuelLevel = (float) (this.fuelLevel - Math.abs(this.carAcceleration)*0.01);
        this.vehiclePosition = this.vehiclePosition + this.carAcceleration;
    }
    public void drive(float convoyVelocity){
        this.carAcceleration = 0;
        this.carVelocity = convoyVelocity + this.carAcceleration;
        this.fuelLevel = (float) (this.fuelLevel - this.carVelocity*0.01 - Math.abs(this.carAcceleration)*0.01);
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

    public float getCarAcceleration() {
        return carAcceleration;
    }

    public float getFuelLevel() {
        return fuelLevel;
    }
}
