package levin.kdtree;

import levin.printout.ErrorLog;

public class LineDistanceFunction implements DistanceFunction {
  public LineDistanceFunction() {}
  
  public double distance(double[] p2, double[] search) { double d = 0.0D;
    double p = 0.0D;
    double s = 0.0D;
    
    if (search[0] == 0.0D) {
      s = search[1];
      p = p2[1];
    } else if (search[1] == 0.0D) {
      s = search[0];
      p = p2[0];
    } else {
      ErrorLog.log("Line Function Must be configured with 0.0 coordinate");
    }
    double diff = Math.abs(s - p);
    d += diff * diff;
    
    return d;
  }
  
  public double distanceToRect(double[] point, double[] min, double[] max) {
    double d = 0.0D;
    double p = 0.0D;
    double mi = 0.0D;
    double ma = 0.0D;
    
    if (point[0] == 0.0D) {
      p = point[1];
    } else if (point[1] == 0.0D) {
      p = point[0];
    } else {
      ErrorLog.log("Line Function Must be configured with 0.0 coordinate");
    }
    double diff = 0.0D;
    if (p > ma) {
      diff = p - ma;
    }
    else if (p < mi) {
      diff = p - mi;
    }
    d += diff * diff;
    
    return d;
  }
}