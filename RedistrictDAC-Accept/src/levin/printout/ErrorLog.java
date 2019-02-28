package levin.printout;

import java.io.PrintStream;

public class ErrorLog {
  public ErrorLog() {}
  
  public static void log(String message) {
    System.err.println(message);
    System.exit(0);
  }
}