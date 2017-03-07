import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

class MatricesCreator {
  static int MATRICES_VALUES_BOUND = 1000000;

  static class MatrixFromJSONGenerator implements Runnable {
    int _height;
    int _width;
    long _seed;
    boolean _isSimpleMatrix;
    AMatrix _result;

    MatrixFromJSONGenerator(int height, int width, long seed, boolean isSimpleMatrix) {
      _height = height;
      _width = width;
      _seed = seed;
      _isSimpleMatrix = isSimpleMatrix;
    }

    AMatrix getResult() {
      return _result;
    }

    public void run () {
      int size = _height * _width;
      long[] array = new long[size];
      Random random = new Random(_seed);

      for (int i = 0; i < size; ++i) {
        array[i] = random.nextInt(MATRICES_VALUES_BOUND * 2) - MATRICES_VALUES_BOUND;
      }

      if (_isSimpleMatrix) {
        _result = new SimpleMatrix(_height, _width, array);
      } else {
        _result = new AdvancedMatrix(_height, _width, array);
      }
    }
  }

  static AMatrix[] createMatrices(String fromFileName, boolean isSimpleMatrix) {
    String jsonString = "";
    AMatrix[] matrices = new AMatrix[2];

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

      int height1 = jsonObj.getInt("height1");
      int width1 = jsonObj.getInt("width1");
      int height2 = jsonObj.getInt("height2");
      int width2 = jsonObj.getInt("width2");
      long seed = jsonObj.getInt("seed");

      if (height1 < 1 || width1 < 1 || height2 < 1 || width2 < 1) {
        System.err.print("The \"height\" and \"width\" properties of the JSON files must have a value of at least 1");
        System.exit(1);
      }
      if (width1 != height2 && width2 != height1) {
        System.err.print("One of the two matrices' \"width\" must match the other matrice's \"height\""
          + "for the multiplication to be doable");
        System.exit(1);
      }

      try {
        matrixCreators[0] = new MatrixFromJSONGenerator(height1, width1, seed, isSimpleMatrix);
        matrixCreators[1] = new MatrixFromJSONGenerator(height2, width2, seed, isSimpleMatrix);
        threads[0] = new Thread(matrixCreators[0]);
        threads[1] = new Thread(matrixCreators[1]);
        threads[0].start();
        threads[1].start();
        threads[0].join();
        threads[1].join();
      } catch (InterruptedException e) {
        System.err.println("Thread supposed to parse matrice has been unexpectedly interrupted");
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
