package levin.circle;

import java.util.Collection;
import levin.circle.SECPoint;


public class SECCircle {
    private static double EPSILON = 1.0E-12;
    public final SECPoint c;
    public final double r;

    public SECCircle(SECPoint c, double r) {
        this.c = c;
        this.r = r;
    }

    public boolean contains(SECPoint p) {
        if (this.c.distance(p) <= this.r + EPSILON) {
            return true;
        }
        return false;
    }

    public boolean contains(Collection<SECPoint> ps) {
        for (SECPoint p : ps) {
            if (this.contains(p)) continue;
            return false;
        }
        return true;
    }

    public String toString() {
        return String.format("SECCircle(x=%g, y=%g, r=%g)", this.c.x, this.c.y, this.r);
    }
}