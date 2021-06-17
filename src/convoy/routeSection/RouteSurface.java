package convoy.routeSection;

import java.util.Random;

public enum RouteSurface {
    ASPHALT, //asfalt
    CONCRETE, //beton
    PAVEMENT, //bruk
    GRAVEL;// żwir

    public static RouteSurface getRandomSurface() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }
}
