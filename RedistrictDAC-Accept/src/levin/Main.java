package levin;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import levin.CompactnessCalculator;
import levin.District;
import levin.DistrictList;
import levin.DistrictValidation;
import levin.Read;
import levin.Unit;
import levin.Write;
import levin.geom.utils.MultiPolygonFlatener;
import levin.kdtree.DistanceFunction;
import levin.kdtree.KdTree;
import levin.kdtree.LineDistanceFunction;
import levin.kdtree.NearestNeighborIterator;
import levin.kdtree.SquareEuclideanDistanceFunction;
import levin.printout.ErrorLog;
import levin.printout.Logger;
import levin.printout.Messenger;


public class Main {
    public static boolean DEBUG;
    private static String STATE;
    public static boolean IS_BLOCK;
    private static boolean SWAPS;
    private static boolean CONTIG;
    private static String MAX_FUNCTION;
    private static String SITE;
    private static int k;
    public static String DOC_ROOT;
    private static String dataFilePath;
    private static String shapeFilePath;
    private static String popFilePath;
    private static String csvFilePath;
    public static String ADDED_UNIT_ID;
    private static double[][] defaultSearchPoints;

    static {
        CONTIG = true;
        ADDED_UNIT_ID = "13";
    }

    public static void main(String[] args) throws IOException {
        Messenger.log((String)("Processing with " + args.length + " args"));
        if (args.length > 3) {
            STATE = args[0];
            k = Integer.parseInt(args[1]);
            DOC_ROOT = args[2];
            DEBUG = args[3].equals("true");
            Logger.setDebugFlag((boolean)DEBUG);
            IS_BLOCK = args[4].equals("block");
            SWAPS = args[5].equals("true");
            if (args.length > 7) {
                CONTIG = args[6].equals("true");
                MAX_FUNCTION = args[7];
                SITE = args[8];
            }
            if (IS_BLOCK) {
                dataFilePath = "/tabblock2010_" + CompactnessCalculator.getFIPSString((String)STATE) + "_pophu/";
                shapeFilePath = "tabblock2010_" + CompactnessCalculator.getFIPSString((String)STATE) + "_pophu.shp";
                popFilePath = "";
            } else {
                dataFilePath = "/tl_2010_" + CompactnessCalculator.getFIPSString((String)STATE) + "_tract10/";
                shapeFilePath = "tl_2010_" + CompactnessCalculator.getFIPSString((String)STATE) + "_tract10.shp";
                popFilePath = "tract-pop.txt";
            }
            csvFilePath = String.valueOf(STATE) + "-csv.csv";
            Messenger.log((String)("STATE=" + STATE + ", k=" + k + " , DOC_ROOT=" + DOC_ROOT + " , DEBUG=" + DEBUG + "SWAPS= " + SWAPS + "dataFilePath= " + dataFilePath + "shapeFilePath= " + shapeFilePath + "popFilePath= " + popFilePath + "IS_BLOCK" + IS_BLOCK));
        } else {
            Logger.log((String)"USSAGE: RedistricterDAC STATE #districts dataFilePath DEBUG [block/tract] SWAPS[true/false]");
            Logger.log((String)"Example: RedistricterDAC nh 2 /Users/levin/Desktop/tabblock2010_33_pophu/tabblock2010_33_pophu.shp false block true");
            Logger.log((String)"Running with default values");
            STATE = "hi";
            k = 2;
            DOC_ROOT = "/Users/levin/Desktop/Archive/data";
            DEBUG = false;
            Logger.setDebugFlag((boolean)DEBUG);
            IS_BLOCK = false;
            SWAPS = true;
            dataFilePath = "/tl_2010_" + CompactnessCalculator.getFIPSString((String)STATE) + "_tract10/";
            shapeFilePath = "tl_2010_15_tract10.shp";
            popFilePath = "tract-pop.txt";
            csvFilePath = String.valueOf(STATE) + "-csv.csv";
            MAX_FUNCTION = "pop";
            SITE = "point";
            Messenger.log((String)("STATE=" + STATE + ", k=" + k + " , DOC_ROOT=" + DOC_ROOT + " , DEBUG=" + DEBUG + " , IS_BLOCK = " + IS_BLOCK + " , SWAPS= " + SWAPS));
        }
        Main.validateRequirements();
        Read r = new Read(DOC_ROOT, dataFilePath, shapeFilePath, popFilePath, IS_BLOCK);
        District stateWideDistrict = r.getDistrictList(STATE).getDistrict(0);
        defaultSearchPoints = Main.getDefaultSearchPoints(stateWideDistrict.getGeometry());
        DistrictList finalDistricts = Main.divideAndConquer(k, stateWideDistrict);
        Messenger.log((String)"-----------------FINAL DISTRICTS---------------------");
        double devPercentage = finalDistricts.getDeviationPercentage(stateWideDistrict.getDistrictPopulation() / k);
        Messenger.log((String)("FINAL Deviation Percentage:" + finalDistricts.getDeviation(stateWideDistrict.getDistrictPopulation() / k) + " people=" + devPercentage + "%"));
        String blockString = IS_BLOCK ? "block" : "tract";
        String fileName = String.valueOf(CompactnessCalculator.getFIPSString((String)STATE)) + "_" + STATE + "_" + blockString + "_" + MAX_FUNCTION + "_" + SITE + "-shapedata.csv";
        Write.write((String)fileName, (String)finalDistricts.toString());
        Messenger.log((String)fileName);
        CompactnessCalculator calculator = new CompactnessCalculator(DOC_ROOT, finalDistricts, STATE);
        Messenger.log((String)calculator.toString());
        String[] blockAssignmentData = Main.printBlockAssignmentList(r.getRawUnits(), finalDistricts);
        Write.write((String)blockAssignmentData[0], (String)blockAssignmentData[1]);
        Messenger.log((String)blockAssignmentData[0]);
        Messenger.log((String)"DONE :-)");
    }

    private static DistrictList divideAndConquer(int numDistrictsLeft, District d) {
        Messenger.log((String)("Districts Left: " + numDistrictsLeft + " , totalPop= " + d.getDistrictPopulation() + ", idealPop= " + d.getDistrictPopulation() / numDistrictsLeft));
        if (numDistrictsLeft == 1) {
            return new DistrictList(d);
        }
        if (numDistrictsLeft == 2) {
            return Main.runWithSearchPoints(d, d.getDistrictPopulation() / 2, false);
        }
        if (numDistrictsLeft % 2 != 0) {
            DistrictList oddRecursionList = Main.runWithSearchPoints(d, d.getDistrictPopulation() / numDistrictsLeft, false);
            DistrictList left = Main.divideAndConquer(1, oddRecursionList.getDistrict(0));
            DistrictList right = Main.divideAndConquer(numDistrictsLeft - 1, oddRecursionList.getDistrict(1));
            return Main.merge(left.getDistrictList(), right.getDistrictList());
        }
        if (numDistrictsLeft % 2 == 0) {
            DistrictList evenRecursionList = Main.runWithSearchPoints(d, d.getDistrictPopulation() / 2, false);
            DistrictList left = Main.divideAndConquer(numDistrictsLeft / 2, evenRecursionList.getDistrict(0));
            DistrictList right = Main.divideAndConquer(numDistrictsLeft / 2, evenRecursionList.getDistrict(1));
            return Main.merge(left.getDistrictList(), right.getDistrictList());
        }
        ErrorLog.log((String)("Invalid value for numDistrictsLeft: " + numDistrictsLeft));
        return null;
    }

    private static DistrictList merge(District[] left, District[] right) {
        int leftLen = left.length;
        int rightLen = right.length;
        District[] merged = new District[leftLen + rightLen];
        System.arraycopy(left, 0, merged, 0, leftLen);
        System.arraycopy(right, 0, merged, leftLen, rightLen);
        return new DistrictList(merged);
    }

    private static double getScore(DistrictList districts, int idealPop) {
        double result = 0.0;
        if (MAX_FUNCTION.equals("pop")) {
            result = Math.abs(districts.getDistrict(0).getDistrictPopulation() - idealPop);
        } else if (MAX_FUNCTION.equals("contig")) {
            double worstComp = Main.getWorstCompactness(districts);
            result = 1.0 - worstComp;
        } else if (MAX_FUNCTION.equals("both")) {
            double compScore = 1.0 - Main.getWorstCompactness(districts);
            double popScore = 100.0 * (Math.abs((double)districts.getDistrict(0).getDistrictPopulation() * 1.0 - (double)idealPop * 1.0) / (double)idealPop * 1.0);
            if (popScore > 0.5) {
                popScore += 100000.0;
            }
            result = (compScore + popScore) / 2.0;
        }
        return result;
    }

    private static double getWorstCompactness(DistrictList districts) {
        double districtArea = districts.getDistrict(0).getGeometry().getArea();
        double radius = Math.sqrt(districtArea / 3.141592653589793);
        double circlePerim = radius * 2.0 * 3.141592653589793;
        double comp1 = circlePerim / districts.getDistrict(0).getGeometry().getLength();
        districtArea = districts.getDistrict(1).getGeometry().getArea();
        radius = Math.sqrt(districtArea / 3.141592653589793);
        circlePerim = radius * 2.0 * 3.141592653589793;
        double comp2 = circlePerim / districts.getDistrict(1).getGeometry().getLength();
        double worstComp = Math.min(comp1, comp2);
        return worstComp;
    }

    private static DistrictList runWithSearchPoints(District d, int idealPop, boolean maxOptimize) {
        double bestDeviation = 2.147483647E9;
        DistrictList bestDistricts = null;
        int index = 0;
        int bestIndex = 0;
        double[][] arrd = defaultSearchPoints;
        int n = arrd.length;
        int n2 = 0;
        while (n2 < n) {
            double[] searchPoint = arrd[n2];
            Logger.log((String)"Calling redistrict");
            DistrictList districts = Main.redistrict(d, idealPop, searchPoint, maxOptimize);
            Logger.log((String)"return from redistrict");
            Messenger.log((String)("\tPop0:" + districts.getDistrict(0).getDistrictPopulation() + "Pop1:" + districts.getDistrict(1).getDistrictPopulation()));
            double score = Main.getScore(districts, idealPop);
            if (score < bestDeviation && Main.validateDistrictList(districts, idealPop, d.getMembers().size(), d.getDistrictPopulation()).hasSuccessCode()) {
                bestDistricts = districts;
                bestDeviation = score;
                bestIndex = index;
            } else if (score == bestDeviation && Main.validateDistrictList(districts, idealPop, d.getMembers().size(), d.getDistrictPopulation()).hasSuccessCode()) {
                Logger.log((String)"tie means check compactness");
                double proposedScore = districts.getAverageSimpleCompactnessScore();
                double bestScore = bestDistricts.getAverageSimpleCompactnessScore();
                proposedScore = districts.getAverageSimpleCompactnessScore();
                if (proposedScore < bestScore) {
                    bestDistricts = districts;
                    bestDeviation = Math.abs(districts.getDistrict(0).getDistrictPopulation() - idealPop);
                    bestIndex = index;
                }
            }
            ++index;
            ++n2;
        }
        Logger.log((String)("-----------Best-------------" + bestIndex));
        Logger.log((String)bestDistricts.getDistrict(1).getGeometry().toText());
        Logger.log((String)bestDistricts.getDistrict(0).getGeometry().toText());
        Messenger.log((String)("\tBest0:" + bestDistricts.getDistrict(0).getDistrictPopulation() + " \n\t Best1: " + bestDistricts.getDistrict(1).getDistrictPopulation()));
        Logger.log((String)("DIFF: " + (bestDistricts.getDistrict(0).getDistrictPopulation() - bestDistricts.getDistrict(1).getDistrictPopulation())));
        if (!IS_BLOCK && MAX_FUNCTION.equals("pop") && !maxOptimize && bestDistricts.getFirstDistrictDev(idealPop) > 1) {
            Messenger.log((String)"********turning on maxOptimize");
            bestDistricts = Main.runWithSearchPoints(d, idealPop, true);
        }
        return bestDistricts;
    }

    private static DistrictList redistrict(District district, int idealPop, double[] searchPoint, boolean optimizeMax) {
        DistrictList districts = new DistrictList(2);
        KdTree<Unit> kd = Main.makeKdTree(district.getMembers());
        int maxPointsReturned = kd.size();
        DistanceFunction d = null;
        if (SITE.equals("point")) {
            d = new SquareEuclideanDistanceFunction();
        } else if (SITE.equals("line")) {
            d = new LineDistanceFunction();
        } else {
            ErrorLog.log((String)("Invalid SITE value: " + SITE));
        }
        Messenger.log((String)"");
        Messenger.log((String)("\tUsing searchPoint: " + searchPoint[0] + " , " + searchPoint[1]));
        NearestNeighborIterator iterator = kd.getNearestNeighborIterator(searchPoint, maxPointsReturned, (DistanceFunction)d);
        Messenger.log((String)("\tsize=" + kd.size()));
        Messenger.log((String)("\tidealPop=" + idealPop));
        while (iterator.hasNext()) {
            Unit u = (Unit)iterator.next();
            if (districts.getDistrict(0).getDistrictPopulation() <= idealPop) {
                districts.getDistrict(0).add(u);
                continue;
            }
            districts.getDistrict(1).add(u);
        }
        districts.assignSkippedDistricts();
        Logger.log((String)"Voronoi Districts");
        Logger.log((String)districts.getDistrict(0).getGeometry().toText());
        Logger.log((String)districts.getDistrict(1).getGeometry().toText());
        if (CONTIG) {
            Logger.log((String)"MultiPolygonFlatener process starting");
            MultiPolygonFlatener mpf = new MultiPolygonFlatener(districts);
            Logger.log((String)("made changes: " + mpf.hasChanged()));
            if (mpf.hasChanged()) {
                districts = mpf.getNewDistrictList();
            }
            Logger.log((String)"Flattener Test: ");
            Logger.log((String)String.valueOf(districts.getDistrict(0).getGeometry().toText().contains("MULTIPOLYGON")));
            Logger.log((String)String.valueOf(districts.getDistrict(0).getGeometry().toText().contains("MULTIPOLYGON")));
            Logger.log((String)districts.getDistrict(0).getGeometry().toText());
            Logger.log((String)districts.getDistrict(1).getGeometry().toText());
            Logger.log((String)("Deviation: " + districts.getDeviation(idealPop)));
        }
        if (SWAPS) {
            int lastDeviation = Integer.MAX_VALUE;
            Logger.log((String)"starting optimize loop");
            while (districts.getFirstDistrictDev(idealPop) > 1 && districts.getFirstDistrictDev(idealPop) < lastDeviation) {
                Logger.log((String)(String.valueOf(districts.getFirstDistrictDev(idealPop)) + "> 1 && " + "<" + lastDeviation));
                lastDeviation = districts.getFirstDistrictDev(idealPop);
                Main.optimizePopulation(districts, idealPop);
                if (!optimizeMax) break;
            }
            Logger.log((String)"end optimize loop");
        }
        double devPercentage = districts.getDeviationPercentage(idealPop);
        Messenger.log((String)("\tDeviation: " + districts.getDeviation(idealPop) + " people = " + (double)Math.round(devPercentage * 1000.0) / 1000.0 + "%"));
        Logger.log((String)String.valueOf(districts.getDistrict(1).getSkippedUnits().size()));
        Logger.log((String)districts.getDistrict(1).getGeometry().toText());
        Logger.log((String)districts.getDistrict(0).getGeometry().toText());
        Logger.log((String)("Is multipolygon: " + districts.getDistrict(0).getGeometry().toText().contains("MULTIPOLYGON")));
        Logger.log((String)"---------OUTPUTING-------");
        Logger.log((String)(String.valueOf(districts.getDistrict(0).getDistrictPopulation()) + " \n" + districts.getDistrict(1).getDistrictPopulation()));
        Logger.log((String)("DIFF: " + (districts.getDistrict(0).getDistrictPopulation() - districts.getDistrict(1).getDistrictPopulation())));
        return districts;
    }

    private static void optimizePopulation(DistrictList districts, int idealPop) {
        Logger.log((String)"starting optimize pop");
        Unit bestSwap = null;
        ArrayList<Unit> swappablesD1 = Main.getSwappabe(districts.getDistrict(1), districts.getDistrict(0));
        ArrayList<Unit> swappablesD2 = Main.getSwappabe(districts.getDistrict(0), districts.getDistrict(1));
        while (swappablesD1.size() > 0 && swappablesD2.size() > 0) {
            int currentDev;
            Logger.log((String)("sizes: " + swappablesD1.size() + " && " + swappablesD2.size()));
            if (districts.getDistrict(0).getDistrictPopulation() > idealPop) {
                currentDev = idealPop - districts.getDistrict(0).getDistrictPopulation();
                Logger.log((String)("0 > ideal: currentDev = " + currentDev));
                bestSwap = Main.getBestSwappable(swappablesD1, currentDev, districts.getDistrict(1), districts.getDistrict(0));
            } else if (districts.getDistrict(0).getDistrictPopulation() < idealPop) {
                currentDev = districts.getDistrict(0).getDistrictPopulation() - idealPop;
                Logger.log((String)("1 > ideal: currentDev = " + currentDev));
                bestSwap = Main.getBestSwappable(swappablesD2, currentDev, districts.getDistrict(0), districts.getDistrict(1));
            } else {
                Messenger.log((String)"\tPERFECT DIVISION! :-)");
                break;
            }
            if (bestSwap == null) break;
            Logger.log((String)("bestSwap= " + bestSwap.getId()));
            districts.swap(bestSwap, true);
        }
    }

    private static ArrayList<Unit> getSwappabe(District from, District to) {
        ArrayList<Unit> result = new ArrayList<Unit>();
        for (Unit u : to.getMembers()) {
            if (!u.getGeometry().touches(from.getGeometry()) || u.getGeometry().union(from.getGeometry()).toText().contains("MULTIPOLYGON") || to.getGeometry().difference(u.getGeometry()).toText().contains("MULTIPOLYGON")) continue;
            result.add(u);
        }
        return result;
    }

    private static Unit getBestSwappable(ArrayList<Unit> swappables, int currentDev, District from, District to) {
        Unit bestUnit = null;
        int bestDeviation = currentDev;
        for (Unit u : swappables) {
            int newDev = currentDev + u.getPopulation();
            if (Math.abs(newDev) >= Math.abs(bestDeviation) || u.getGeometry().union(from.getGeometry()).toText().contains("MULTIPOLYGON") || to.getGeometry().difference(u.getGeometry()).toText().contains("MULTIPOLYGON")) continue;
            bestUnit = u;
            bestDeviation = newDev;
        }
        swappables.remove(bestUnit);
        return bestUnit;
    }

    private static DistrictValidation validateDistrictList(DistrictList districts, int idealPop, int numUnits, int totalPop) {
        DistrictValidation valid = new DistrictValidation();
        int totalUnitsAssigned = 0;
        int totalPopulationAssigned = 0;
        District[] arrdistrict = districts.getDistrictList();
        int n = arrdistrict.length;
        int n2 = 0;
        while (n2 < n) {
            District d = arrdistrict[n2];
            Main.validateDistrict(d, idealPop, valid);
            totalUnitsAssigned += d.getMembers().size();
            totalPopulationAssigned += d.getDistrictPopulation();
            ++n2;
        }
        if (totalPop != totalPopulationAssigned) {
            valid.setPopulationCorruptedFlag(totalPop, totalPopulationAssigned);
        }
        if (numUnits != totalUnitsAssigned) {
            valid.setUnassignedUnitsFlag(new ArrayList());
        }
        Messenger.log((String)valid.toString());
        return valid;
    }

    private static void validateDistrict(District d, int idealPop, DistrictValidation valid) {
        if (d.getGeometry().toText().contains("MULTIPOLYGON")) {
            valid.setNonContiguousFlag(d.getGeometry());
        }
    }

    private static KdTree<Unit> makeKdTree(ArrayList<Unit> units) {
        KdTree kd = new KdTree(2);
        for (Unit u : units) {
            Point centroid = u.getCentroid();
            double[] cen = new double[]{centroid.getY(), centroid.getX()};
            kd.addPoint(cen, (Object)u);
        }
        return kd;
    }

    private static double[][] getDefaultSearchPoints(Geometry stateWideGeom) {
        double[][] result = new double[4][2];
        Coordinate[] coords = stateWideGeom.getEnvelope().getCoordinates();
        int i = 0;
        while (i < coords.length - 1) {
            Coordinate c = coords[i];
            if (SITE.equals("point")) {
                result[i][0] = c.y;
                result[i][1] = c.x;
            } else if (SITE.equals("line")) {
                if (i == 1 || i == 3) {
                    result[i][0] = c.y;
                    result[i][1] = 0.0;
                } else {
                    result[i][0] = 0.0;
                    result[i][1] = c.x;
                }
            } else {
                ErrorLog.log((String)("Invalid value for SITE " + SITE));
            }
            ++i;
        }
        return result;
    }

    private static boolean validateRequirements() {
        File stateShapeFile = new File(String.valueOf(DOC_ROOT) + "/tl_2010_" + CompactnessCalculator.getFIPSString((String)STATE) + "_state10/tl_2010_" + CompactnessCalculator.getFIPSString((String)STATE) + "_state10.shp");
        File actualDistrictsShapeFile = new File(String.valueOf(DOC_ROOT) + "/2012Congress/2012Congress.shp");
        File blockShapeFile = new File(String.valueOf(DOC_ROOT) + "/tabblock2010_" + CompactnessCalculator.getFIPSString((String)STATE) + "_pophu/tabblock2010_" + CompactnessCalculator.getFIPSString((String)STATE) + "_pophu.shp");
        Logger.log((String)(String.valueOf(stateShapeFile.getAbsolutePath()) + " " + stateShapeFile.exists()));
        Logger.log((String)(String.valueOf(actualDistrictsShapeFile.getAbsolutePath()) + " " + actualDistrictsShapeFile.exists()));
        Logger.log((String)(String.valueOf(blockShapeFile.getAbsolutePath()) + " " + blockShapeFile.exists()));
        if (stateShapeFile.exists() && actualDistrictsShapeFile.exists() && blockShapeFile.exists()) {
            return true;
        }
        return false;
    }

    private static String[] printBlockAssignmentList(ArrayList<Unit> rawUnits, DistrictList districts) {
        String blockString = IS_BLOCK ? "block" : "tract";
        String fileName = "BlockAssign_ST_" + CompactnessCalculator.getFIPSString((String)STATE) + "_" + STATE + "_" + blockString + "_" + MAX_FUNCTION + "_" + SITE + "_CD.txt";
        String assignments = "";
        int index = 1;
        District[] arrdistrict = districts.getDistrictList();
        int n = arrdistrict.length;
        int n2 = 0;
        while (n2 < n) {
            District d = arrdistrict[n2];
            for (Unit u2 : rawUnits) {
                if (u2.getDistrictAssignment() != -1 || !d.containsId(u2.getId())) continue;
                u2.setDistrictAssignment(index);
            }
            ++index;
            ++n2;
        }
        for (Unit u : rawUnits) {
            if (u.getId().contains(",")) {
                String[] arrstring = u.getId().split(",");
                int u2 = arrstring.length;
                int n3 = 0;
                while (n3 < u2) {
                    String piece = arrstring[n3];
                    assignments = String.valueOf(assignments) + piece + "," + u.getDistrictAssignment() + "\n";
                    ++n3;
                }
                continue;
            }
            assignments = String.valueOf(assignments) + u.getId() + "," + u.getDistrictAssignment() + "\n";
        }
        return new String[]{fileName, assignments};
    }
}