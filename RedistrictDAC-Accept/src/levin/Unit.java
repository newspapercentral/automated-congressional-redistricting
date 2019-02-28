package levin;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class Unit {
    protected String id;
    protected int population;
    protected Point centroid;
    protected Geometry geom;
    protected int districtAssignment;

    public Unit(String _id, Point cen, int pop, Geometry geometry) {
        this.id = _id;
        this.centroid = cen;
        this.population = pop;
        this.geom = geometry;
        this.districtAssignment = -1;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPopulation() {
        return this.population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public Point getCentroid() {
        return this.centroid;
    }

    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }

    public Geometry getGeometry() {
        return this.geom;
    }

    public void setGeometry(Geometry geometry) {
        this.geom = geometry;
    }

    public int getNumUnits() {
        return this.id.split(",").length;
    }

    public void setDistrictAssignment(int districtNum) {
        this.districtAssignment = districtNum;
    }

    public int getDistrictAssignment() {
        return this.districtAssignment;
    }

    public String toString() {
        return "[id:" + this.id + ", population:" + this.population + ", centroid" + this.centroid.toString() + "]";
    }
}