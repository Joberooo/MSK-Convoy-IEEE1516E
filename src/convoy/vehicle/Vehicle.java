package convoy.vehicle;

import java.util.Random;

public class Vehicle {
    private static final Random random = new Random();
    private static final float minFuelConsumption = 0.15f;
    private static final float maxFuelConsumption = 0.25f;
    private final float maxFuelLevel;

    private final int vehicleNumber;
    private int routeSectionNumber;
    private float vehiclePosition;
    private float carVelocity;
    private float fuelLevel;
    private final float maxVelocity;
    private boolean fuelReserve;

    public Vehicle(int vehicleNumber, int routeSectionNumber, float fuelLevel,
                   float vehiclePosition, float maxVelocity) {

        this.vehicleNumber = vehicleNumber;
        this.routeSectionNumber = routeSectionNumber;
        this.fuelLevel = fuelLevel;
        this.maxFuelLevel = fuelLevel;
        this.vehiclePosition = vehiclePosition;
        this.maxVelocity = maxVelocity;
        this.fuelReserve = false;
    }

    public void drive(float previousVehiclePosition, float expectedDistance, int typeOfWeather,
                      float windDirectionX, float windDirectionY, float windForce, boolean isSectionDelayed) {
        if(fuelLevel > 0){
            float distance = previousVehiclePosition - this.vehiclePosition;
            this.carVelocity = (distance - expectedDistance) - lifeDelay();
            delayAndFuel(typeOfWeather, windDirectionX, windDirectionY, windForce, isSectionDelayed);
        }
        else this.carVelocity = 0;
        this.vehiclePosition = this.vehiclePosition + this.carVelocity;
    }

    public void drive(float convoyVelocity, int typeOfWeather, int typeOfRoute, float windDirectionX,
                      float windDirectionY, float windForce, boolean isSectionDelayed) {
        if(fuelLevel > 0){
            this.carVelocity = 0;
            this.carVelocity = convoyVelocity + this.carVelocity  - lifeDelay();

            delayAndFuel(typeOfWeather, typeOfRoute, windDirectionX, windDirectionY, windForce, isSectionDelayed);
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
        this.carVelocity = this.carVelocity - this.carVelocity * typeOfRoute / 10f;
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
    private float lifeDelay(){
        return random.nextFloat() * (-0.005f - 0.005f);
    }

    public void delayAndFuel(int typeOfWeather, int typeOfRoute, float windDirectionX, float windDirectionY,
                             float windForce, boolean isSectionDelayed){
        weatherDelay(typeOfWeather);
        routeDelay(typeOfRoute);
        windDelay(windDirectionX, windDirectionY, windForce);
        if (this.carVelocity > this.maxVelocity) this.carVelocity = this.maxVelocity;
        if (isSectionDelayed) sectionDelay();
        if (this.fuelLevel < 20.0f) this.fuelReserve = true;
        this.fuelLevel = (this.fuelLevel - Math.abs(this.carVelocity) * randomFuelConsumption());
    }
    public void delayAndFuel(int typeOfWeather, float windDirectionX, float windDirectionY,
                             float windForce, boolean isSectionDelayed){
        weatherDelay(typeOfWeather);
        windDelay(windDirectionX, windDirectionY, windForce);
        if (this.carVelocity > this.maxVelocity) this.carVelocity = this.maxVelocity;
        if (isSectionDelayed) sectionDelay();
        if (this.fuelLevel < 20.0f) this.fuelReserve = true;
        this.fuelLevel = (this.fuelLevel - Math.abs(this.carVelocity) * randomFuelConsumption());
    }

    public void fuelToFull(){
        this.carVelocity = 0;
        this.fuelLevel = this.maxFuelLevel;
        this.fuelReserve = false;
    }

    protected void printVehicleData(String federateName){
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(federateName + "    ::   Vehicle number = " + this.vehicleNumber);
        System.out.println(federateName + "    ::   Position = " + this.vehiclePosition);
        System.out.println(federateName + "    ::   Route = " + this.routeSectionNumber);
        System.out.println(federateName + "    ::   FuelLevel = " + this.fuelLevel);
        System.out.println("--------------------------------------------------------------------------------");
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
