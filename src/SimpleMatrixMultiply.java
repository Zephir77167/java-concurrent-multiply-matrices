public class SimpleMatrixMultiply {
  public static void main(String args[]) {
    if (args.length < 1 || (args.length == 1 && !args[0].contains("json"))) {
      System.err.println("Usage: java -cp \"out;libs/*\" SimpleMatrixMultiply <path-to.json> [-v]");
      System.exit(1);
    }

    Timer timer1 = new Timer(), timer2 = new Timer(), timer3 = new Timer();
    boolean verbose = false;
    String filePath = args[0];

    if (args.length > 1 && (args[0].equals("-v") || args[0].equals("--verbose")
        || args[1].equals("-v") || args[1].equals("--verbose"))) {
      verbose = true;
    }
    if (args.length > 1 && (args[0].equals("-v") || args[0].equals("--verbose"))) {
      filePath = args[1];
    }

    timer1.start();
    AMatrix[] matrices = MatricesCreator.createMatrices(filePath, true);
    timer1.end();
    System.out.println("Time spent generating matrices: " + timer1.getEllapsedTime());
    if (verbose) {
      matrices[0].parsablePrint();
      matrices[1].parsablePrint();
    }

    timer2.start();
    AMatrix result1 = matrices[0].multiplyBy(matrices[1]);
    timer2.end();
    System.out.println("Time spent doing m1 * m2: " + timer2.getEllapsedTime());
    if (verbose) {
      result1.prettyPrint();
    }

    timer3.start();
    AMatrix result2 = matrices[1].multiplyBy(matrices[0]);
    timer3.end();
    System.out.println("Time spent doing m2 * m1: " + timer3.getEllapsedTime());
    if (verbose) {
      result2.prettyPrint();
    }
  }
}
