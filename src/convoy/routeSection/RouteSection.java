package convoy.routeSection;

import convoy.config.Config;

import java.util.Random;

public class RouteSection {
    private int routeSectionNumber;
    private final float routeSectionLength;
    private final RouteSurface routeSurface;
    private boolean isClosed = false;

    public RouteSection(int routeSectionNumber) {
        this.routeSectionNumber = routeSectionNumber;
        float MAX_LENGTH = Config.MAX_ROUTE_SECTION_LENGTH;
        Random random = new Random();
        this.routeSectionLength = random.nextFloat() * MAX_LENGTH;
        this.routeSurface = RouteSurface.getRandomSurface();
    }

    public int getRouteSectionNumber() {
        return routeSectionNumber;
    }

    public void setRouteSectionNumber(int id){
        this.routeSectionNumber = id;
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

    public void changeIsClosed(){
        this.isClosed = !this.isClosed;
    }

}
