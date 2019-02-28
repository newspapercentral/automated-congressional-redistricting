package levin.circle;

public class SECPoint {
    public final double x;
    public final double y;

    public SECPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public SECPoint subtract(SECPoint p) {
        return new SECPoint(this.x - p.x, this.y - p.y);
    }

    public double distance(SECPoint p) {
        return Math.hypot(this.x - p.x, this.y - p.y);
    }

    public double cross(SECPoint p) {
        return this.x * p.y - this.y * p.x;
    }

    public double norm() {
        return this.x * this.x + this.y * this.y;
    }

    public String toString() {
        return String.format("SECPoint(%g, %g)", this.x, this.y);
    }
}