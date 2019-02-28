package levin.geom.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import java.io.PrintStream;
import java.util.ArrayList;
import levin.District;
import levin.DistrictList;
import levin.Main;
import levin.Unit;
import levin.printout.Logger;


public class MultiPolygonFlatener {
    private DistrictList districts;
    private boolean hasChanged;

    public MultiPolygonFlatener(DistrictList districts) {
        this.districts = districts;
        this.hasChanged = false;
        Logger.log((String)("district len=" + districts.getDistrictList().length));
        boolean district0hasMulti = districts.getDistrict(0).getGeometry().toText().contains("MULTIPOLYGON");
        boolean district1hasMulti = districts.getDistrict(1).getGeometry().toText().contains("MULTIPOLYGON");
        Logger.log((String)("0 has multi: " + district0hasMulti + " , 1 has multi: " + district1hasMulti));
        Logger.log((String)("0 pop: " + districts.getDistrict(0).getDistrictPopulation() + " , 1 pop: " + districts.getDistrict(1).getDistrictPopulation()));
        Logger.log((String)districts.getDistrict(0).getGeometry().toText());
        Logger.log((String)districts.getDistrict(1).getGeometry().toText());
        if (districts.getDistrictList().length == 2) {
            if (districts.getDistrict(0).getGeometry().toText().contains("MULTIPOLYGON")) {
                this.hasChanged = true;
                this.flatten(districts.getDistrict(0));
            }
            if (districts.getDistrict(1).getGeometry().toText().contains("MULTIPOLYGON")) {
                this.hasChanged = true;
                this.flatten(districts.getDistrict(1));
            }
        }
    }

    private ArrayList<Geometry> findBadPieces(Geometry geom) {
        ArrayList<Geometry> badPieces = new ArrayList<Geometry>();
        int index = this.getMostCoordinatesIndex(geom);
        if (Main.DEBUG) {
            System.out.println("goodPieceIndex: " + index);
        }
        int i = 0;
        while (i < geom.getNumGeometries()) {
            if (i != index) {
                badPieces.add(geom.getGeometryN(i));
            }
            ++i;
        }
        return badPieces;
    }

    private int getMostCoordinatesIndex(Geometry geom) {
        int maxCoordinates = 0;
        int maxCoordIndex = 0;
        int i = 0;
        while (i < geom.getNumGeometries()) {
            int numCoord = geom.getGeometryN(i).getCoordinates().length;
            if (numCoord > maxCoordinates) {
                maxCoordIndex = i;
                maxCoordinates = numCoord;
            }
            ++i;
        }
        return maxCoordIndex;
    }

    private void flatten(District d) {
        ArrayList<Unit> swapUnits = new ArrayList<Unit>();
        for (Geometry geom : this.findBadPieces(d.getGeometry())) {
            ArrayList<Unit> members = d.getMembers();
            for (Unit u : members) {
                if (!geom.covers(u.getGeometry())) continue;
                swapUnits.add(u);
            }
        }
        Logger.log((String)("Swapping " + swapUnits.size() + " units"));
        for (Unit u : swapUnits) {
            this.districts.swap(u, false);
        }
    }

    public boolean hasChanged() {
        return this.hasChanged;
    }

    public DistrictList getNewDistrictList() {
        return this.districts;
    }
}