package convoy.vehicle;

import java.util.Random;

public class Vehicle {
    private static final Random random = new Random();
    private static final float minFuelConsumption = 0.00015f;
    private static final float maxFuelConsumption = 0.00025f;

    private final int vehicleNumber;
    private int routeSectionNumber;
    private float vehiclePosition;
    private float carVelocity;
    private float fuelLevel;
    private final float maxVelocity;
    private boolean fuelReserve;

    public Vehicle(int vehicleNumber, int routeSectionNumber, float fuelLevel,
                   float carVelocity, float vehiclePosition, float maxVelocity) {

        this.vehicleNumber = vehicleNumber;
        this.routeSectionNumber = routeSectionNumber;
        this.fuelLevel = fuelLevel;
        this.carVelocity = carVelocity;
        this.vehiclePosition = vehiclePosition;
        this.maxVelocity = maxVelocity;
        this.fuelReserve = false;
    }

    public void drive(float previousVehiclePosition, float expectedDistance, int typeOfWeather, int typeOfRoute,
                      float windDirectionX, float windDirectionY, float windForce, boolean isSectionDelayed) {
        if(fuelLevel > 0){
            float distance = previousVehiclePosition - this.vehiclePosition;
            this.carVelocity = (distance - (distance / expectedDistance) - expectedDistance * 1.1f);
            weatherDelay(typeOfWeather);
            routeDelay(typeOfRoute);
            windDelay(windDirectionX, windDirectionY, windForce);
            if (this.carVelocity > this.maxVelocity) this.carVelocity = this.maxVelocity;
            if (this.fuelLevel < 10.0f) fuelReserve = true;
            if (isSectionDelayed) sectionDelay();
            this.fuelLevel = (this.fuelLevel - Math.abs(this.carVelocity) * randomFuelConsumption());
        }
        else this.carVelocity = 0;
        this.vehiclePosition = this.vehiclePosition + this.carVelocity;
    }

    public void drive(float convoyVelocity, int typeOfWeather, int typeOfRoute, float windDirectionX,
                      float windDirectionY, float windForce, boolean isSectionDelayed) {
        if(fuelLevel > 0){
            this.carVelocity = 0;
            this.carVelocity = convoyVelocity + this.carVelocity;
            weatherDelay(typeOfWeather);
            routeDelay(typeOfRoute);
            windDelay(windDirectionX, windDirectionY, windForce);
            if (this.carVelocity > this.maxVelocity) this.carVelocity = this.maxVelocity;
            if (isSectionDelayed) sectionDelay();
            this.fuelLevel = (this.fuelLevel - Math.abs(this.carVelocity) * randomFuelConsumption());
        }
        else this.carVelocity = 0;
        this.vehiclePosition = this.vehiclePosition + this.carVelocity;
    }

    public void sectionDelay() {
        this.carVelocity = 0;
    }

    public void weatherDelay(int typeOfWeather) {
        this.carVelocity = this.carVelocity - this.carVelocity * ((float) Math.pow(typeOfWeather, 2) / 10);
    }

    public void routeDelay(int typeOfRoute) {
        this.carVelocity = this.carVelocity - this.carVelocity * ((float) typeOfRoute / 10);
    }

    public void windDelay(float windDirectionX, float windDirectionY, float windForce) {
        if (windForce > 1.0f || windForce < 0.0f) {
            System.out.println("Wind force shall be 0.0 to 1.0");
        } else {
            if (windDirectionX < 0) {
                if (windDirectionY > 0) {
                    this.carVelocity = this.carVelocity + this.carVelocity * windForce / 4;
                } else if (windDirectionY < 0) {
                    this.carVelocity = this.carVelocity - this.carVelocity * windForce / 4;
                }
            } else if (windDirectionX > 0) {
                if (windDirectionY > 0) {
                    this.carVelocity = this.carVelocity + this.carVelocity * windForce / 4;
                } else if (windDirectionY < 0) {
                    this.carVelocity = this.carVelocity - this.carVelocity * windForce / 4;
                }
            } else if (windDirectionX == 0) {
                if (windDirectionY > 0) {
                    this.carVelocity = this.carVelocity + this.carVelocity * windForce / 2;
                } else if (windDirectionY < 0) {
                    this.carVelocity = this.carVelocity - this.carVelocity * windForce / 2;
                }
            }
        }
    }

    private float randomFuelConsumption(){
        return minFuelConsumption + random.nextFloat() * (maxFuelConsumption - minFuelConsumption);
    }

    public int getVehicleNumber() {
        return vehicleNumber;
    }

    public int getRouteSectionNumber() {
        return routeSectionNumber;
    }

    public void setRouteSectionNumber(int num){
        this.routeSectionNumber = num;
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

    public float getMaxVelocity() {
        return maxVelocity;
    }

    public boolean isFuelReserve() {
        return fuelReserve;
    }
}
