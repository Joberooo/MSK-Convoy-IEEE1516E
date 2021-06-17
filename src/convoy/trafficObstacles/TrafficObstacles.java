package convoy.trafficObstacles;

import java.util.Random;

public class TrafficObstacles {

    private final float OBSTACLES_FREQUENCY = 0.8f;

    private final Random random = new Random();

    public TrafficObstacles() {
    }

    private boolean Shuffle(){
        float result = (float)random.nextGaussian();

        if(result > OBSTACLES_FREQUENCY) {
            return true;
        }
        return false;
    }

    public boolean closeSectionPermanently(){
        return Shuffle();
    }

    public boolean delaySection() {
        return  Shuffle();
    }


}
