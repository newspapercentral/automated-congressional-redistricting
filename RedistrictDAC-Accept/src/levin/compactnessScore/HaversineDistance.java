package levin.compactnessScore;

public class HaversineDistance {
    private double distance;

    public HaversineDistance(Double lat1, Double lat2, Double lon1, Double lon2) {
        int R = 6371;
        Double latDistance = HaversineDistance.toRad(lat2 - lat1);
        Double lonDistance = HaversineDistance.toRad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2.0) * Math.sin(latDistance / 2.0) + Math.cos(HaversineDistance.toRad(lat1)) * Math.cos(HaversineDistance.toRad(lat2)) * Math.sin(lonDistance / 2.0) * Math.sin(lonDistance / 2.0);
        Double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        this.distance = 6371.0 * c;
    }

    public Double getDistance() {
        return this.distance;
    }

    private static Double toRad(Double value) {
        return value * 3.141592653589793 / 180.0;
    }
}