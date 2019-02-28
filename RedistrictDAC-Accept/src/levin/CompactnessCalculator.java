package levin;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import levin.Compactness;
import levin.District;
import levin.DistrictList;
import levin.Unit;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;


public class CompactnessCalculator {
    private static String dataFilePath = "";
    private static final String FIPS_ATTR = "STATEFP";
    private DistrictList district;
    private int FIPS;
    private SummaryStatistics statOurConvexHull;
    private SummaryStatistics statOurReock;
    private SummaryStatistics statOurPolsbyPopper;
    private SummaryStatistics statOurModifiedSchwartzberg;
    private SummaryStatistics statCongConvexHull;
    private SummaryStatistics statCongPolsbyPopper = new SummaryStatistics();
    private SummaryStatistics statCongModifiedSchwartzberg = new SummaryStatistics();

    public CompactnessCalculator(String docroot, DistrictList districts, String stateId) {
        dataFilePath = String.valueOf(docroot) + "/2012Congress/2012Congress.shp";
        this.district = districts;
        this.FIPS = CompactnessCalculator.getFIPS(stateId);
        this.statOurConvexHull = new SummaryStatistics();
        this.statOurPolsbyPopper = new SummaryStatistics();
        this.statOurModifiedSchwartzberg = new SummaryStatistics();
        District[] arrdistrict = districts.getDistrictList();
        int n = arrdistrict.length;
        int n2 = 0;
        while (n2 < n) {
            District ourDistrict = arrdistrict[n2];
            Compactness congCompactness = new Compactness(ourDistrict);
            this.statOurConvexHull.addValue(congCompactness.getConvexHullMeasure());
            this.statOurPolsbyPopper.addValue(congCompactness.getPolsbyPopperMeasure());
            this.statOurModifiedSchwartzberg.addValue(congCompactness.getModifiedSchwartzberg());
            ++n2;
        }
        ArrayList<District> congDistricts = this.read();
        this.statCongConvexHull = new SummaryStatistics();
        this.statCongPolsbyPopper = new SummaryStatistics();
        this.statCongModifiedSchwartzberg = new SummaryStatistics();
        for (District actualCongDistrict : congDistricts) {
            Compactness congCompactness = new Compactness(actualCongDistrict);
            this.statCongConvexHull.addValue(congCompactness.getConvexHullMeasure());
            this.statCongPolsbyPopper.addValue(congCompactness.getPolsbyPopperMeasure());
            this.statCongModifiedSchwartzberg.addValue(congCompactness.getModifiedSchwartzberg());
        }
    }

    private ArrayList<District> read() {
        ArrayList<District> districtList = new ArrayList<District>();
        try {
            File file = new File(dataFilePath);
            HashMap<String, URL> connect = new HashMap<String, URL>();
            connect.put("url", file.toURL());
            DataStore dataStore = DataStoreFinder.getDataStore(connect);
            String[] typeNames = dataStore.getTypeNames();
            String typeName = typeNames[0];
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
            FeatureCollection collection = featureSource.getFeatures();
            FeatureIterator iterator = collection.features();
            while (iterator.hasNext()) {
                District d = new District();
                SimpleFeature feature = (SimpleFeature)iterator.next();
                int fips = Integer.parseInt(feature.getAttribute(FIPS_ATTR).toString());
                if (fips != this.FIPS) continue;
                MultiPolygon multiPolygon = (MultiPolygon)feature.getDefaultGeometry();
                Point centroid = multiPolygon.getCentroid();
                Unit u = new Unit(String.valueOf(fips), centroid, 0, (Geometry)multiPolygon);
                d.add(u);
                districtList.add(d);
            }
            iterator.close();
            dataStore.dispose();
        }
        catch (Throwable e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        return districtList;
    }

    public static String getFIPSString(String state) {
        int FIPS = CompactnessCalculator.getFIPS(state);
        String result = String.valueOf(FIPS);
        if (FIPS < 10) {
            result = "0" + String.valueOf(FIPS);
        }
        return result;
    }

    public static int getFIPS(String state) {
        if (state.toLowerCase().equals("nh")) {
            return 33;
        }
        if (state.toLowerCase().equals("ne")) {
            return 31;
        }
        if (state.toLowerCase().equals("pa")) {
            return 42;
        }
        if (state.toLowerCase().equals("al")) {
            return 1;
        }
        if (state.toLowerCase().equals("az")) {
            return 4;
        }
        if (state.toLowerCase().equals("ar")) {
            return 5;
        }
        if (state.toLowerCase().equals("ca")) {
            return 6;
        }
        if (state.toLowerCase().equals("co")) {
            return 8;
        }
        if (state.toLowerCase().equals("ct")) {
            return 9;
        }
        if (state.toLowerCase().equals("fl")) {
            return 12;
        }
        if (state.toLowerCase().equals("ga")) {
            return 13;
        }
        if (state.toLowerCase().equals("hi")) {
            return 15;
        }
        if (state.toLowerCase().equals("id")) {
            return 16;
        }
        if (state.toLowerCase().equals("il")) {
            return 17;
        }
        if (state.toLowerCase().equals("in")) {
            return 18;
        }
        if (state.toLowerCase().equals("ia")) {
            return 19;
        }
        if (state.toLowerCase().equals("ks")) {
            return 20;
        }
        if (state.toLowerCase().equals("ky")) {
            return 21;
        }
        if (state.toLowerCase().equals("la")) {
            return 22;
        }
        if (state.toLowerCase().equals("me")) {
            return 23;
        }
        if (state.toLowerCase().equals("md")) {
            return 24;
        }
        if (state.toLowerCase().equals("ma")) {
            return 25;
        }
        if (state.toLowerCase().equals("mi")) {
            return 26;
        }
        if (state.toLowerCase().equals("mn")) {
            return 27;
        }
        if (state.toLowerCase().equals("ms")) {
            return 28;
        }
        if (state.toLowerCase().equals("mo")) {
            return 29;
        }
        if (state.toLowerCase().equals("nv")) {
            return 32;
        }
        if (state.toLowerCase().equals("nj")) {
            return 34;
        }
        if (state.toLowerCase().equals("nm")) {
            return 35;
        }
        if (state.toLowerCase().equals("ny")) {
            return 36;
        }
        if (state.toLowerCase().equals("nc")) {
            return 37;
        }
        if (state.toLowerCase().equals("oh")) {
            return 39;
        }
        if (state.toLowerCase().equals("ok")) {
            return 40;
        }
        if (state.toLowerCase().equals("or")) {
            return 41;
        }
        if (state.toLowerCase().equals("ri")) {
            return 44;
        }
        if (state.toLowerCase().equals("sc")) {
            return 45;
        }
        if (state.toLowerCase().equals("tn")) {
            return 47;
        }
        if (state.toLowerCase().equals("tx")) {
            return 48;
        }
        if (state.toLowerCase().equals("ut")) {
            return 49;
        }
        if (state.toLowerCase().equals("va")) {
            return 51;
        }
        if (state.toLowerCase().equals("wa")) {
            return 53;
        }
        if (state.toLowerCase().equals("wv")) {
            return 54;
        }
        if (state.toLowerCase().equals("wi")) {
            return 55;
        }
        return 0;
    }

    public double getAverageScore() {
        return (this.statOurConvexHull.getMean() + this.statOurReock.getMean() + this.statOurPolsbyPopper.getMean() + this.statOurModifiedSchwartzberg.getMean()) / 4.0;
    }

    public String toString() {
        String result = "";
        result = String.valueOf(result) + "-------------------Compactness-------------------\n";
        result = String.valueOf(result) + "------Ours-------------------------Existing------\n";
        result = String.valueOf(result) + "ConvexHullOursMean=" + this.statOurConvexHull.getMean() + "<>" + this.statCongConvexHull.getMean() + "\n";
        result = String.valueOf(result) + "ConvexHullOursSdev=" + this.statOurConvexHull.getStandardDeviation() + "<>" + this.statCongConvexHull.getStandardDeviation() + "\n\n";
        result = String.valueOf(result) + "PolsbyPopperOursMean=" + this.statOurPolsbyPopper.getMean() + "<>" + this.statCongPolsbyPopper.getMean() + "\n";
        result = String.valueOf(result) + "PolsbyPopperOursSdev=" + this.statOurPolsbyPopper.getStandardDeviation() + "<>" + this.statCongPolsbyPopper.getStandardDeviation() + "\n\n";
        result = String.valueOf(result) + "ModifiedSchwartzbergOursMean=" + this.statOurModifiedSchwartzberg.getMean() + "<>" + this.statCongModifiedSchwartzberg.getMean() + "\n";
        result = String.valueOf(result) + "ModifiedSchwartzbergOursSdev=" + this.statOurModifiedSchwartzberg.getStandardDeviation() + "<>" + this.statCongModifiedSchwartzberg.getStandardDeviation() + "\n\n";
        return result;
    }
}