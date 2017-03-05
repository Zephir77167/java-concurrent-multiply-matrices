public class SimpleMatrixMultiply implements Runnable {
  public void run() {
    System.out.println("Hello again!");
  }

  public static void main(String args[]) {
    if (args.length != 1) {
      System.err.println("This program accepts one argument: a path to the JSON file containing the 2 matrices");
      System.exit(1);
    }

    Matrix[] matrices = MatricesCreator.createMatrices(args[0]);

    matrices[0].print();
    System.out.println();
    matrices[1].print();
  }
}
