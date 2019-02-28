package levin;

import com.vividsolutions.jts.geom.Geometry;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import levin.District;
import levin.Main;
import levin.StateWideDistrict;
import levin.Unit;
import levin.printout.Logger;

public class DistrictList {
    private int k;
    private District[] districtList;

    public DistrictList(District d) {
        this.k = 1;
        this.districtList = new District[this.k];
        this.districtList[0] = d;
    }

    public DistrictList(District[] districts) {
        this.k = districts.length;
        this.districtList = districts;
    }

    public DistrictList(int k) {
        this.k = k;
        this.districtList = new District[k];
        int i = 0;
        while (i < k) {
            this.districtList[i] = new District();
            ++i;
        }
    }

    public DistrictList(int k, String stateId, String doc_root) {
        this.k = k;
        this.districtList = new District[k];
        if (k == 1) {
            this.districtList[0] = new StateWideDistrict(stateId, doc_root);
        } else {
            System.err.println("Invalid use of this constructor. Statewide districts can only have one district.");
            System.exit(0);
        }
    }

    public District getDistrict(int index) {
        return this.districtList[index];
    }

    public int getDeviation(int idealPop) {
        int largestDiff = 0;
        District[] arrdistrict = this.districtList;
        int n = arrdistrict.length;
        int n2 = 0;
        while (n2 < n) {
            District d = arrdistrict[n2];
            int diff = d.getDistrictPopulation() - idealPop;
            if (largestDiff < Math.abs(diff)) {
                largestDiff = Math.abs(diff);
            }
            ++n2;
        }
        return largestDiff;
    }

    public double getDeviationPercentage(int idealPop) {
        return (double)this.getDeviation(idealPop) / (double)idealPop * 100.0;
    }

    private int getTotalPopulation() {
        int result = 0;
        District[] arrdistrict = this.districtList;
        int n = arrdistrict.length;
        int n2 = 0;
        while (n2 < n) {
            District d = arrdistrict[n2];
            result += d.getDistrictPopulation();
            ++n2;
        }
        return result;
    }

    public int getFirstDistrictDev(int idealPop) {
        return Math.abs(this.districtList[0].getDistrictPopulation() - idealPop);
    }

    public String csvOutput() {
        String output = "";
        int i = 0;
        while (i < this.districtList.length) {
            for (Unit u : this.districtList[i].getMembers()) {
                String id = u.getId();
                output = String.valueOf(output) + id + "," + i + "\n";
            }
            ++i;
        }
        return output;
    }

    public void assignSkippedDistricts() {
        ArrayList<Unit> skippedUnits = new ArrayList();
        int i = 0;
        while (i < this.districtList.length) {
            skippedUnits.addAll(this.districtList[i].getSkippedUnits());
            ++i;
        }
        boolean madeChange = true;
        while (madeChange && skippedUnits.size() > 0) {
            madeChange = false;
            ArrayList<Unit> removeUnit = new ArrayList<Unit>();
            block2 : for (Unit u : skippedUnits) {
                int i2 = 0;
                while (i2 < this.districtList.length) {
                    if (this.districtList[i2].getGeometry().touches(u.getGeometry())) {
                        this.districtList[i2].add(u);
                        removeUnit.add(u);
                        madeChange = true;
                        continue block2;
                    }
                    ++i2;
                }
            }
            for (Unit u : removeUnit) {
                skippedUnits.remove((Object)u);
            }
        }
    }

    public void swap(Unit u, boolean validate) {
        if (this.districtList.length == 2) {
            if (this.districtList[0].contains(u)) {
                Logger.log((String)"d0");
                Logger.log((String)("Swapping: " + u.getId()));
                Logger.log((String)this.districtList[0].getGeometry().toText());
                Logger.log((String)"Unit geometry");
                Logger.log((String)u.getGeometry().toText());
                this.districtList[0].remove(u);
                this.districtList[1].add(u);
                Logger.log((String)("Swapped " + u.getId()));
                if (Main.DEBUG && (this.districtList[0].getGeometry().toText().contains("MULTIPOLYGON") || this.districtList[1].getGeometry().toText().contains("MULTIPOLYGON"))) {
                    System.out.println("Swapped " + u.getId() + " and made districts non-contig");
                }
            } else if (this.districtList[1].contains(u)) {
                Logger.log((String)"d1");
                Logger.log((String)("Swapping " + u.getId()));
                Logger.log((String)this.districtList[1].getGeometry().toText());
                Logger.log((String)"Unit geometry");
                Logger.log((String)u.getGeometry().toText());
                this.districtList[1].remove(u);
                this.districtList[0].add(u);
                Logger.log((String)("Swapped " + u.getId()));
            } else {
                System.err.println("Error: swapping district that is unassigned");
                System.exit(0);
            }
        }
    }

    public int getLength() {
        return this.districtList.length;
    }

    public District[] getDistrictList() {
        return this.districtList;
    }

    public double getAverageSimpleCompactnessScore() {
        double areaSum = 0.0;
        double perimSum = 0.0;
        District[] arrdistrict = this.districtList;
        int n = arrdistrict.length;
        int n2 = 0;
        while (n2 < n) {
            District d = arrdistrict[n2];
            areaSum += d.geometry.getArea();
            perimSum += d.geometry.getLength();
            ++n2;
        }
        double areaAvg = areaSum / (double)this.districtList.length;
        double perimAvg = perimSum / (double)this.districtList.length;
        return (areaAvg + perimAvg) / 2.0;
    }

    public String toString() {
        String result = "District Index;Population;Number of Counties;Geometry";
        int index = 1;
        District[] arrdistrict = this.districtList;
        int n = arrdistrict.length;
        int n2 = 0;
        while (n2 < n) {
            District d = arrdistrict[n2];
            result = String.valueOf(result) + "\n";
            result = String.valueOf(result) + index + ";" + d.getDistrictPopulation() + ";" + d.getNumCounties() + ";" + (Object)d.getGeometry();
            ++index;
            ++n2;
        }
        return result;
    }
}