package levin;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import levin.CompactnessCalculator;
import levin.District;
import levin.Unit;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

public class StateWideDistrict
extends District {
    private int FIPS;
    private String ID;
    private String DOC_ROOT;

    public StateWideDistrict(String stateId, String doc_root) {
        this.ID = stateId;
        this.FIPS = CompactnessCalculator.getFIPS((String)stateId);
        this.DOC_ROOT = doc_root;
        this.geometry = this.readStateGeometry();
    }

    public void add(Unit u) {
        this.members.add(u);
        this.population += u.getPopulation();
    }

    private Geometry readStateGeometry() {
        MultiPolygon stateGeom = null;
        try {
            File file = new File(String.valueOf(this.DOC_ROOT) + "/tl_2010_" + CompactnessCalculator.getFIPSString((String)this.ID) + "_state10/tl_2010_" + CompactnessCalculator.getFIPSString((String)this.ID) + "_state10.shp");
            HashMap<String, URL> connect = new HashMap<String, URL>();
            connect.put("url", file.toURL());
            DataStore dataStore = DataStoreFinder.getDataStore(connect);
            String[] typeNames = dataStore.getTypeNames();
            String typeName = typeNames[0];
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
            FeatureCollection collection = featureSource.getFeatures();
            FeatureIterator iterator = collection.features();
            System.out.println("Collection Size:" + collection.size());
            if (iterator.hasNext()) {
                MultiPolygon multiPolygon;
                SimpleFeature feature = (SimpleFeature)iterator.next();
                stateGeom = multiPolygon = (MultiPolygon)feature.getDefaultGeometry();
            }
            iterator.close();
            dataStore.dispose();
        }
        catch (Throwable e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        return stateGeom;
    }
}