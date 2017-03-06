public class SimpleMatrixMultiply {
  public static void main(String args[]) {
    if (args.length < 1 || (args.length == 1 && !args[0].contains("json"))) {
      System.err.println("Usage: java -cp \"out;libs/*\" SimpleMatrixMultiply <path-to.json> [-v]");
      System.exit(1);
    }

    boolean verbose = false;
    String filePath = args[0];

    if (args.length > 1 && (args[0].equals("-v") || args[0].equals("--verbose")
        || args[1].equals("-v") || args[1].equals("--verbose"))) {
      verbose = true;
    }
    if (args.length > 1 && (args[0].equals("-v") || args[0].equals("--verbose"))) {
      filePath = args[1];
    }

    Timer timer1 = new Timer();
    timer1.start();
    AMatrix[] matrices = MatricesCreator.createMatrices(filePath, true);
    timer1.end();
    System.out.println("Time spent generating matrices: " + timer1.getEllapsedTime() + "ms");
    if (verbose) {
      matrices[0].parsablePrint();
      matrices[1].parsablePrint();
    }

    if (matrices[0].getWidth() != matrices[1].getHeight()) {
      System.out.println("Skipping m1 * m2 as it is not doable");
    } else {
      Timer timer2 = new Timer();
      System.out.println("Computing m1 * m2...");
      timer2.start();
      AMatrix result1 = matrices[0].multiplyBy(matrices[1]);
      timer2.end();
      System.out.println("Done! Time spent computing m1 * m2: " + timer2.getEllapsedTime() + "ms");
      if (verbose) {
        result1.prettyPrint();
      }
    }

    if (matrices[1].getWidth() != matrices[0].getHeight()) {
      System.out.println("Skipping m2 * m1 as it is not doable");
    } else if (matrices[0].getHeight() == matrices[1].getHeight() && matrices[0].getWidth() == matrices[1].getWidth()) {
      System.out.println("Skipping m2 * m1 as it is the same as m1 * m2");
    } else {
      Timer timer3 = new Timer();
      System.out.println("Computing m2 * m1...");
      timer3.start();
      AMatrix result2 = matrices[1].multiplyBy(matrices[0]);
      timer3.end();
      System.out.println("Done! Time spent computing m2 * m1: " + timer3.getEllapsedTime() + "ms");
      if (verbose) {
        result2.prettyPrint();
      }
    }
  }
}
