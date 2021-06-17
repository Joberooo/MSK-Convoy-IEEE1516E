package convoy.petrolStation;

import java.util.Random;

public class PetrolStation {

    private final float FUELING_TIME_RATIO = 10;
    private final float PETROL_TIME_RATIO = 0.5f;

    private final Random random = new Random();

    private boolean fueling = false;
    private float fuelingTime;

    public PetrolStation() {
    }

    public boolean isFueling() {
        return fueling;
    }

    public void startFueling(float fuelAmount){
        fueling = true;
        fuelingTime = shuffleTime()*FUELING_TIME_RATIO+PETROL_TIME_RATIO*fuelAmount;
    }

    public int fuelingTime(){
        return (int)fuelingTime;
    }

    public void finishFueling(){
        fueling = false;
    }

    private float shuffleTime(){
        return random.nextFloat();
    }
}
