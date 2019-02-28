package levin.printout;

import java.io.PrintStream;

public class Logger { private static boolean printMessage;
  
  public Logger() {}
  
  public static void setDebugFlag(boolean flag) { printMessage = flag; }
  
  public static void log(String message)
  {
    if (printMessage) {
      System.out.println(message);
    }
  }
}