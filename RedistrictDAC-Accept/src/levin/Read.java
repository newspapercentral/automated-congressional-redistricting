package levin;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import levin.District;
import levin.DistrictList;
import levin.StateWideDistrict;
import levin.Unit;
import levin.UnitGroup;
import levin.printout.Logger;
import levin.printout.Messenger;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;


public class Read {
    private static String SHAPE_FILE;
    private static String POP_FILE;
    private static int POPULATION;
    private int UNIT_COUNTER;
    private static String DOC_ROOT;
    private static final String CENSUS_BLOCK_ID_ATTR = "BLOCKID10";
    private static final String CENSUS_TRACT_ID_ATTR = "GEOID10";
    private static final String CENSUS_POP_ATTR = "POP10";
    private static ArrayList<Unit> RAW_UNITS;
    private static boolean IS_BLOCK;
    private static int EDITED_UNITS;

    public Read(String doc_root, String dataFilePath, String shapeFile, String popFile, boolean isBlock) {
        DOC_ROOT = doc_root;
        SHAPE_FILE = String.valueOf(doc_root) + dataFilePath + shapeFile;
        POP_FILE = String.valueOf(doc_root) + dataFilePath + popFile;
        this.UNIT_COUNTER = 0;
        POPULATION = 0;
        EDITED_UNITS = 0;
        RAW_UNITS = new ArrayList();
        IS_BLOCK = isBlock;
    }

    public DistrictList getDistrictList(String stateId) {
        DistrictList stateWideDistrictList = new DistrictList(1, stateId, DOC_ROOT);
        RAW_UNITS = this.read();
        Logger.log((String)"Finished reading now making state wide district");
        for (Unit u : RAW_UNITS) {
            ((StateWideDistrict)stateWideDistrictList.getDistrict(0)).add(u);
        }
        Logger.log((String)"Returning state wide district");
        Geometry stateGeometry = stateWideDistrictList.getDistrict(0).getGeometry();
        if (stateGeometry.getNumGeometries() > 1) {
            Logger.log((String)("State is a multipolygon, hope you made changes\n" + stateWideDistrictList.getDistrict(0).getGeometry().toText()));
        }
        return stateWideDistrictList;
    }

    public ArrayList<Unit> readRawDataList(ArrayList<String> shapeFiles) {
        ArrayList<Unit> allUnits = new ArrayList<Unit>();
        Iterator<String> iterator = shapeFiles.iterator();
        while (iterator.hasNext()) {
            String file;
            SHAPE_FILE = file = iterator.next();
            ArrayList<Unit> newUnits = this.readRawData();
            allUnits.addAll(newUnits);
        }
        return allUnits;
    }

    public ArrayList<Unit> readRawData() {
        ArrayList<Unit> unitList = new ArrayList<Unit>();
        ArrayList<String> uniqueCounties = new ArrayList<String>();
        SummaryStatistics populationStat = new SummaryStatistics();
        try {
            File file = new File(SHAPE_FILE);
            HashMap<String, URL> connect = new HashMap<String, URL>();
            connect.put("url", file.toURL());
            DataStore dataStore = DataStoreFinder.getDataStore(connect);
            String[] typeNames = dataStore.getTypeNames();
            String typeName = typeNames[0];
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
            FeatureCollection collection = featureSource.getFeatures();
            FeatureIterator iterator = collection.features();
            System.out.println("Collection Size:" + collection.size());
            this.UNIT_COUNTER = collection.size();
            HashMap<String, Integer> tractData = null;
            if (!IS_BLOCK) {
                tractData = Read.getPopData();
            }
            while (iterator.hasNext()) {
                String blockId;
                String county;
                int population;
                SimpleFeature feature = (SimpleFeature)iterator.next();
                if (IS_BLOCK) {
                    blockId = feature.getAttribute(CENSUS_BLOCK_ID_ATTR).toString();
                    population = Integer.parseInt(feature.getAttribute(CENSUS_POP_ATTR).toString());
                } else {
                    blockId = feature.getAttribute(CENSUS_TRACT_ID_ATTR).toString();
                    population = this.getTractPop(tractData, blockId);
                }
                POPULATION += population;
                MultiPolygon multiPolygon = (MultiPolygon)feature.getDefaultGeometry();
                Point centroid = multiPolygon.getCentroid();
                Unit u = new Unit(blockId, centroid, population, (Geometry)multiPolygon);
                populationStat.addValue((double)population);
                unitList.add(u);
                if (blockId.length() < 5 || uniqueCounties.contains(county = u.getId().substring(2, 5))) continue;
                uniqueCounties.add(county);
            }
            iterator.close();
            dataStore.dispose();
        }
        catch (Throwable e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        Messenger.log((String)("Average Unit Population=" + populationStat.getMean()));
        Messenger.log((String)("Stdev Unit Population=" + populationStat.getStandardDeviation()));
        Messenger.log((String)("Max Unit=" + populationStat.getMax()));
        Messenger.log((String)("Min Unit=" + populationStat.getMin()));
        Messenger.log((String)("NumCounties=" + uniqueCounties.size()));
        return unitList;
    }

    private ArrayList<Unit> read() {
        return this.cleanUnits(this.readRawData());
    }

    public int getNumUnits() {
        return this.UNIT_COUNTER;
    }

    public static int getPopulation() {
        return POPULATION;
    }

    public static HashMap<String, Integer> getPopData() {
        try {
            System.out.println("Attempting to read file " + POP_FILE);
            BufferedReader br = new BufferedReader(new FileReader(POP_FILE));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            String everything = sb.toString();
            br.close();
            return Read.readPop(everything);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, Integer> readPop(String data) {
        String[] line;
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        String[] arrstring = line = data.split("\n");
        int n = arrstring.length;
        int n2 = 0;
        while (n2 < n) {
            String s = arrstring[n2];
            String[] fields = s.split(",");
            int pop = Integer.parseInt(fields[13]);
            String id = fields[12];
            result.put(id, pop);
            ++n2;
        }
        System.out.println("Found " + result.size() + " entries");
        return result;
    }

    private Integer getTractPop(HashMap<String, Integer> tractData, String id) {
        Integer population = null;
        try {
            population = tractData.get(id);
            if (population == null) {
                throw new NullPointerException();
            }
        }
        catch (NullPointerException e) {
            System.err.println("Error: could not find data for unit " + id + "null returned. Exiting...");
            System.exit(0);
        }
        return population;
    }

    private ArrayList<Unit> cleanUnits(ArrayList<Unit> units) {
        int previousEditCount = -1;
        Logger.log((String)("mergeMultiPolygonUnitsLoop: " + previousEditCount + "<" + EDITED_UNITS));
        while (previousEditCount < EDITED_UNITS) {
            Logger.log((String)("mergeMultiPolygonUnitsLoop: " + previousEditCount + "<" + EDITED_UNITS));
            previousEditCount = EDITED_UNITS;
            this.mergeMultiPolygonUnits(units);
        }
        this.mergeNestedUnits(units);
        SummaryStatistics populationStat = new SummaryStatistics();
        for (Unit u : units) {
            populationStat.addValue((double)u.getPopulation());
        }
        Messenger.log((String)("CLEANEDAverage Unit Population=" + populationStat.getMean()));
        Messenger.log((String)("CLEANEDStdev Unit Population=" + populationStat.getStandardDeviation()));
        Messenger.log((String)("CLEANEDMax Unit=" + populationStat.getMax()));
        Messenger.log((String)("CLEANEDMin Unit=" + populationStat.getMin()));
        return units;
    }

    private ArrayList<Unit> mergeNestedUnits(ArrayList<Unit> units) {
        ArrayList<Unit> removeUnits = new ArrayList<Unit>();
        ArrayList<Unit> addUnits = new ArrayList<Unit>();
        for (Unit u : units) {
            boolean hasHoles;
            Logger.log((String)("Polygon Cast:" + u.getId()));
            boolean bl = hasHoles = ((Polygon)u.getGeometry().union()).getNumInteriorRing() != 0;
            if (!hasHoles || u.getId().length() <= 2) continue;
            Logger.log((String)("Editing: " + u.getId()));
            Logger.log((String)("Edit Count: " + ++EDITED_UNITS));
            removeUnits.add(u);
            UnitGroup mergedUnits = new UnitGroup(u.getId(), u.getCentroid(), u.getPopulation(), u.getGeometry());
            ArrayList<Unit> insideUnits = this.findInsideUnits(u, units);
            removeUnits.addAll(insideUnits);
            for (Unit innerUnit : insideUnits) {
                mergedUnits.addUnit(innerUnit);
            }
            addUnits.add((Unit)mergedUnits);
        }
        return this.updateUnitList(units, removeUnits, addUnits);
    }

    private ArrayList<Unit> findInsideUnits(Unit big, ArrayList<Unit> units) {
        ArrayList<Unit> insideUnits = new ArrayList<Unit>();
        for (Unit u : units) {
            if (u.getId().equals(big.getId()) || u.getId().length() <= 2 || !u.getId().substring(0, 11).equals(big.getId().substring(0, 11)) || !big.getGeometry().within(u.getGeometry())) continue;
            insideUnits.add(u);
        }
        return insideUnits;
    }

    private ArrayList<Unit> mergeMultiPolygonUnits(ArrayList<Unit> units) {
        ArrayList<Unit> addUnits = new ArrayList<Unit>();
        ArrayList<Unit> removeUnits = new ArrayList<Unit>();
        for (Unit u : units) {
            if (u.getGeometry().getNumGeometries() <= 1) continue;
            Logger.log((String)("Editing: " + u.getId()));
            Logger.log((String)("Edit Count: " + ++EDITED_UNITS));
            removeUnits.add(u);
            UnitGroup mergedUnits = new UnitGroup(u.getId(), u.getCentroid(), u.getPopulation(), u.getGeometry());
            for (Unit combineUnits : this.findInBetweenUnits(u, units)) {
                mergedUnits.addUnit(combineUnits);
                removeUnits.add(combineUnits);
            }
            addUnits.add((Unit)mergedUnits);
        }
        return this.updateUnitList(units, removeUnits, addUnits);
    }

    private ArrayList<Unit> findInBetweenUnits(Unit multi, ArrayList<Unit> units) {
        ArrayList<Unit> mergeUnits = new ArrayList<Unit>();
        ArrayList<Unit> neighbors = new ArrayList<Unit>();
        for (Unit u : units) {
            if (!u.getId().equals(multi.getId()) && multi.getGeometry().union(u.getGeometry()).getNumGeometries() == 1) {
                mergeUnits.add(u);
                break;
            }
            if (u.getId().equals(multi.getId()) || !multi.getGeometry().touches(u.getGeometry())) continue;
            neighbors.add(u);
        }
        return mergeUnits.size() > 0 ? mergeUnits : neighbors;
    }

    private ArrayList<Unit> updateUnitList(ArrayList<Unit> units, ArrayList<Unit> removeUnits, ArrayList<Unit> addUnits) {
        for (Unit u : removeUnits) {
            units.remove(u);
        }
        units.addAll(addUnits);
        return units;
    }

    public ArrayList<Unit> getRawUnits() {
        return RAW_UNITS;
    }
}