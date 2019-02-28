package levin;

import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import levin.Unit;


public class DistrictValidation {
    private ArrayList<String> errorMessages = new ArrayList();
    private static final String NON_CONTIG_FLAG = "Geometry is not contiguous: \n GEOMETRY";
    private static final String UNASSIGNED_UNITS = "There are unassigned units: \n UNITS";
    private static final String DEVIATION_OUT_OF_BOUNDS = "Deviation out of bounds. Expecting LIMIT, but got ACTUAL_DEV";
    private static final String POPULATION_CORRUPTED = "Population data corrupted: ASSIGNED_POP is assigned, but TOTAL_POP is expected";

    public ArrayList<String> getErrorMessages() {
        return this.errorMessages;
    }

    public boolean hasSuccessCode() {
        if (this.errorMessages.size() == 0) {
            return true;
        }
        return false;
    }

    public void setNonContiguousFlag(Geometry invalidGeom) {
        this.errorMessages.add(NON_CONTIG_FLAG.replaceAll("GEOMETRY", invalidGeom.toText()));
    }

    public void setUnassignedUnitsFlag(ArrayList<Unit> unassignedUnits) {
        String unitIds = "";
        for (Unit u : unassignedUnits) {
            unitIds = String.valueOf(unitIds) + u.getId() + ", ";
        }
        this.errorMessages.add(UNASSIGNED_UNITS.replaceAll("UNITS", unitIds));
    }

    public void setDeviationOutOfBoundsFlag(double expectedDeviation, double actualDeviation) {
        this.errorMessages.add(DEVIATION_OUT_OF_BOUNDS.replaceAll("LIMIT", Double.toString(expectedDeviation)).replaceAll("ACTUAL_DEVIATION", Double.toString(actualDeviation)));
    }

    public void setPopulationCorruptedFlag(int expectedPop, int actualPop) {
        this.errorMessages.add(POPULATION_CORRUPTED.replaceAll("ASSIGNED_POP", Integer.toString(expectedPop)).replaceAll("TOTAL_POP", Integer.toString(actualPop)));
    }

    public String toString() {
        return "Error Messages: " + this.errorMessages.toString();
    }
}