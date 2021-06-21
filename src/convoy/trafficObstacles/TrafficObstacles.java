package convoy.trafficObstacles;

import convoy.config.Config;
import java.util.Random;

public class TrafficObstacles {
    private final Random random = new Random();

    public TrafficObstacles() {
    }

    private boolean Shuffle(){
        float result = (float)random.nextGaussian();
        return result > Config.OBSTACLES_FREQUENCY;
    }

    public boolean closeSectionPermanently(){
        return Shuffle();
    }

    public boolean delaySection() {
        return  Shuffle();
    }
}
