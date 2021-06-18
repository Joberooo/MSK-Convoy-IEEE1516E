package convoy.routeSection;

import java.util.Random;



public class RouteSection {
    //Constants
    private final float MAX_LENGTH = 100;

    //From FOM
    private final int routeSectionNumber;
    private final float routeSectionLength;
    private final RouteSurface routeSurface;
    private boolean isClosed = false;

    //Utils
    private final Random random = new Random();

    public RouteSection(int routeSectionNumber) {
        this.routeSectionNumber = routeSectionNumber;
        this.routeSectionLength = random.nextFloat() * MAX_LENGTH;
        this.routeSurface = RouteSurface.getRandomSurface();
    }

    public int getRouteSectionNumber() {
        return routeSectionNumber;
    }

    public float getRouteSectionLength() {
        return routeSectionLength;
    }

    public RouteSurface getRouteSurface() {
        return routeSurface;
    }

    public boolean getIsClosed() {
        return isClosed;
    }

    public void closeSection () {
        isClosed = true;
    }

    public void openSection () {
        isClosed = false;
    }

}
