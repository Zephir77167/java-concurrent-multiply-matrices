import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

class MatricesCreator {
  static int MATRICES_VALUES_BOUND = 100;

  static class MatrixFromJSONGenerator implements Runnable {
    int _height;
    int _width;
    long _seed;
    Matrix _result;

    MatrixFromJSONGenerator(int height, int width, long seed) {
      _height = height;
      _width = width;
      _seed = seed;
    }

    Matrix getResult() {
      return _result;
    }

    public void run () {
      int size = _height * _width;
      long[] array = new long[size];
      Random random = new Random(_seed);

      for (int i = 0; i < size; ++i) {
        array[i] = random.nextInt(MATRICES_VALUES_BOUND * 2) - MATRICES_VALUES_BOUND;
      }

      _result = new Matrix(_height, _width, array);
    }
  }

  static Matrix[] createMatrices(String fromFileName) {
    String jsonString = "";
    Matrix[] matrices = new Matrix[2];

    try (BufferedReader br = new BufferedReader(new FileReader(fromFileName))) {
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
        sb.append(line);
        sb.append(System.lineSeparator());
        line = br.readLine();
      }

      jsonString = sb.toString();
    } catch (Exception e) {
      System.err.println("The 2 matrices should be stored in a JSON file, which path should be passed as an argument");
      System.exit(1);
    }

    try {
      JSONObject jsonObj = new JSONObject(jsonString);
      MatrixFromJSONGenerator[] matrixCreators = new MatrixFromJSONGenerator[2];
      Thread[] threads = new Thread[2];

      int height = jsonObj.getInt("height");
      int width = jsonObj.getInt("width");
      long seed = jsonObj.getInt("seed");

      if (height < 1 || width < 1) {
        System.err.print("The \"height\" and \"width\" properties of the JSON files must have a value of at least 1");
        System.exit(1);
      }

      try {
        matrixCreators[0] = new MatrixFromJSONGenerator(height, width, seed);
        matrixCreators[1] = new MatrixFromJSONGenerator(width, height, seed);
        threads[0] = new Thread(matrixCreators[0]);
        threads[1] = new Thread(matrixCreators[1]);
        threads[0].start();
        threads[1].start();
        threads[0].join();
        threads[1].join();
      } catch (InterruptedException e) {
        System.err.println("Thread supposed to parse matrice has been unexpectedly interrupted");
        System.exit(1);
      }

      matrices[0] = matrixCreators[0].getResult();
      matrices[1] = matrixCreators[1].getResult();
    } catch (JSONException e) {
      System.err.println("The JSON file should contain 3 properties: \"height\", \"width\" and \"seed\""
        + " (the latter being the seed from which the random numbers are generated to fill the matrices)");
      System.exit(1);
    }

    return matrices;
  }
}
