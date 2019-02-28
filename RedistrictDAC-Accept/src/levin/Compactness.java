package levin;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import levin.District;
import levin.circle.SECCircle;
import levin.circle.SECPoint;
import levin.circle.Smallestenclosingcircle;
import levin.compactnessScore.HaversineDistance;


public class Compactness {
    private District district;
    private double area;
    private double perimeter;
    private Geometry convexHull;
    private double smallestCircleArea;
    private double smallestCirclePerim;

    public Compactness(District d) {
        this.district = d;
        Geometry districtGeom = d.getRealGeometry();
        this.area = districtGeom.getArea();
        this.perimeter = districtGeom.getLength();
        this.convexHull = districtGeom.convexHull();
    }

    public double getConvexHullMeasure() {
        double result = this.district.getGeometry().getArea() / this.convexHull.getArea();
        if (!this.validate(result)) {
            System.err.println("Result out of bounds: " + result);
            System.exit(0);
        }
        return result;
    }

    public double getReockMeasure() {
        double result = this.district.getGeometry().getArea() / this.smallestCircleArea;
        if (!this.validate(result)) {
            System.err.println("Result out of bounds: " + result);
            System.exit(0);
        }
        return result;
    }

    public double getPolsbyPopperMeasure() {
        double districtPerim = this.district.getGeometry().getLength();
        double radius = districtPerim / 6.283185307179586;
        double circleArea = Math.pow(radius, 2.0) * 3.141592653589793;
        double result = this.district.getGeometry().getArea() / circleArea;
        if (!this.validate(result)) {
            System.err.println("Result out of bounds: " + result);
            System.exit(0);
        }
        return result;
    }

    public double getModifiedSchwartzberg() {
        double districtArea = this.district.getGeometry().getArea();
        double radius = Math.sqrt(districtArea / 3.141592653589793);
        double circlePerim = radius * 2.0 * 3.141592653589793;
        double result = circlePerim / this.district.getGeometry().getLength();
        if (!this.validate(result)) {
            System.err.println("Result out of bounds: " + result);
            System.exit(0);
        }
        return result;
    }

    private boolean validate(double result) {
        if (result >= 0.0 && result <= 1.0) {
            return true;
        }
        return false;
    }

    public double getDistrictSmallestEnclosingCircleRadius(District d) {
        List<SECPoint> points = this.getPointsForGeometry(d.getGeometry());
        Smallestenclosingcircle circle = new Smallestenclosingcircle();
        SECCircle b = Smallestenclosingcircle.makeSECCircle(points);
        double radius = b.r;
        System.out.println("radius= " + radius);
        SECPoint centerPoint = b.c;
        double[] center = new double[]{centerPoint.x, centerPoint.y};
        System.out.println("center= " + center[0] + " , " + center[1]);
        double[] coordinates = new double[]{center[1], center[0]};
        HaversineDistance h = new HaversineDistance(Double.valueOf(center[0]), Double.valueOf(center[0] - radius), Double.valueOf(center[1]), Double.valueOf(center[1]));
        double actualRadius = h.getDistance();
        return actualRadius;
    }

    public List<SECPoint> getPointsForGeometry(Geometry geom) {
        Coordinate[] coordinates = geom.getCoordinates();
        System.out.println("Num Coordinates: " + coordinates.length);
        ArrayList<SECPoint> points = new ArrayList<SECPoint>();
        Coordinate[] arrcoordinate = coordinates;
        int n = arrcoordinate.length;
        int n2 = 0;
        while (n2 < n) {
            Coordinate c = arrcoordinate[n2];
            SECPoint p = new SECPoint(c.x, c.y);
            points.add(p);
            ++n2;
        }
        return points;
    }
}