public class SimpleMatrixMultiply {
  public static void main(String args[]) {
    if (args.length != 1) {
      System.err.println("This program accepts one argument: a path to the JSON file containing the 2 matrices parameters");
      System.exit(1);
    }

    Timer timer1 = new Timer(), timer2 = new Timer(), timer3 = new Timer();

    timer1.start();
    Matrix[] matrices = MatricesCreator.createMatrices(args[0]);
    timer1.end();

    matrices[0].parsablePrint();
    matrices[1].parsablePrint();

    timer2.start();
    Matrix result1 = matrices[0].multiplyBy(matrices[1]);
    timer2.end();
    result1.prettyPrint();

    timer3.start();
    Matrix result2 = matrices[1].multiplyBy(matrices[0]);
    timer3.end();
    result2.prettyPrint();

    System.out.println("Time spent parsing matrices: " + timer1.getEllapsedTime());
    System.out.println("Time spent doing m1 * m2: " + timer2.getEllapsedTime());
    System.out.println("Time spent doing m2 * m1: " + timer3.getEllapsedTime());
  }
}
