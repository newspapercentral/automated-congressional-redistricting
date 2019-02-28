package levin;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import levin.Unit;
import levin.printout.Logger;

public class UnitGroup
extends Unit {
    private int numUnits = 1;

    public UnitGroup(String _id, Point cen, int pop, Geometry geometry) {
        super(_id, cen, pop, geometry);
    }

    public void addUnit(Unit u) {
        if (u.getId().length() < 5) {
            Logger.log((String)("trying to add bad unit " + u.getId() + "to UnitGroup"));
        }
        this.id = String.valueOf(this.id) + "," + u.getId();
        this.population += u.getPopulation();
        this.geom = this.geom.union(u.getGeometry());
        this.centroid = super.getGeometry().getCentroid();
        ++this.numUnits;
    }
}