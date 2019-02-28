package levin.circle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import levin.circle.SECCircle;
import levin.circle.SECPoint;


public class Smallestenclosingcircle {
    public static SECCircle makeSECCircle(List<SECPoint> points) {
        ArrayList<SECPoint> shuffled = new ArrayList<SECPoint>(points);
        Collections.shuffle(shuffled, new Random());
        SECCircle c = null;
        int i = 0;
        while (i < shuffled.size()) {
            SECPoint p = shuffled.get(i);
            if (c == null || !c.contains(p)) {
                c = Smallestenclosingcircle.makeSECCircleOneSECPoint(shuffled.subList(0, i + 1), p);
            }
            ++i;
        }
        return c;
    }

    private static SECCircle makeSECCircleOneSECPoint(List<SECPoint> points, SECPoint p) {
        SECCircle c = new SECCircle(p, 0.0);
        int i = 0;
        while (i < points.size()) {
            SECPoint q = points.get(i);
            if (!c.contains(q)) {
                c = c.r == 0.0 ? Smallestenclosingcircle.makeDiameter(p, q) : Smallestenclosingcircle.makeSECCircleTwoSECPoints(points.subList(0, i + 1), p, q);
            }
            ++i;
        }
        return c;
    }

    private static SECCircle makeSECCircleTwoSECPoints(List<SECPoint> points, SECPoint p, SECPoint q) {
        SECCircle temp = Smallestenclosingcircle.makeDiameter(p, q);
        if (temp.contains(points)) {
            return temp;
        }
        SECCircle left = null;
        SECCircle right = null;
        for (SECPoint r : points) {
            SECPoint pq = q.subtract(p);
            double cross = pq.cross(r.subtract(p));
            SECCircle c = Smallestenclosingcircle.makeCircumcircle(p, q, r);
            if (c == null) continue;
            if (cross > 0.0 && (left == null || pq.cross(c.c.subtract(p)) > pq.cross(left.c.subtract(p)))) {
                left = c;
                continue;
            }
            if (cross >= 0.0 || right != null && pq.cross(c.c.subtract(p)) >= pq.cross(right.c.subtract(p))) continue;
            right = c;
        }
        return right == null || left != null && left.r <= right.r ? left : right;
    }

    static SECCircle makeDiameter(SECPoint a, SECPoint b) {
        return new SECCircle(new SECPoint((a.x + b.x) / 2.0, (a.y + b.y) / 2.0), a.distance(b) / 2.0);
    }

    static SECCircle makeCircumcircle(SECPoint a, SECPoint b, SECPoint c) {
        double d = (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y)) * 2.0;
        if (d == 0.0) {
            return null;
        }
        double x = (a.norm() * (b.y - c.y) + b.norm() * (c.y - a.y) + c.norm() * (a.y - b.y)) / d;
        double y = (a.norm() * (c.x - b.x) + b.norm() * (a.x - c.x) + c.norm() * (b.x - a.x)) / d;
        SECPoint p = new SECPoint(x, y);
        return new SECCircle(p, p.distance(a));
    }
}