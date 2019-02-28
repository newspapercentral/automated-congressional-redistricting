package levin;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import levin.Unit;


public class District {
    protected ArrayList<Unit> members = new ArrayList();
    protected ArrayList<Unit> actualMembers = new ArrayList();
    protected ArrayList<Unit> skippedUnits = new ArrayList();
    protected Geometry geometry = null;
    protected int population = 0;
    protected ArrayList<Geometry> geomList = new ArrayList();
    protected ArrayList<Geometry> actualGeomList = new ArrayList();

    public void add(Unit u) {
        this.members.add(u);
        this.population += u.getPopulation();
        this.addGeometryCollection(u.getGeometry());
        if (u.getId().length() > 5) {
            this.actualMembers.add(u);
            this.actualGeomList.add(u.getGeometry());
        }
    }

    public void remove(Unit u) {
        if (this.members.contains((Object)u)) {
            this.members.remove((Object)u);
            this.population -= u.getPopulation();
            this.geometry = this.geometry.difference(u.getGeometry());
        } else {
            System.err.println("Trying to remove unit that's not in this district");
            System.exit(0);
        }
    }

    public boolean contains(Unit u) {
        return this.members.contains((Object)u);
    }

    public boolean containsId(String id) {
        boolean result = false;
        for (Unit u : this.members) {
            if (!u.id.equals(id)) continue;
            result = true;
            break;
        }
        return result;
    }

    public int getDistrictPopulation() {
        return this.population;
    }

    public ArrayList<Unit> getMembers() {
        return this.members;
    }

    public Geometry getGeometry() {
        if (this.geometry == null) {
            GeometryFactory factory = new GeometryFactory();
            GeometryCollection geometryCollection = (GeometryCollection)factory.buildGeometry(this.geomList);
            this.geometry = geometryCollection.union();
        }
        return this.geometry;
    }

    private void addGeometryCollection(Geometry geometry) {
        this.geomList.add(geometry);
        if (this.geometry != null) {
            this.geometry = this.geometry.union(geometry);
        }
    }

    public ArrayList<Unit> getSkippedUnits() {
        return this.skippedUnits;
    }

    public Geometry getRealGeometry() {
        Geometry result = null;
        if (this.actualGeomList.size() > 0) {
            GeometryFactory factory = new GeometryFactory();
            GeometryCollection geometryCollection = (GeometryCollection)factory.buildGeometry(this.actualGeomList);
            result = geometryCollection.union();
        } else {
            result = this.getGeometry();
        }
        return result;
    }

    public ArrayList<Unit> getActualMembers() {
        return this.actualMembers;
    }

    public int getNumCounties() {
        ArrayList<String> uniqueCounties = new ArrayList<String>();
        for (Unit u : this.actualMembers) {
            String county = u.getId().substring(2, 5);
            if (uniqueCounties.contains(county)) continue;
            uniqueCounties.add(county);
        }
        return uniqueCounties.size();
    }
}