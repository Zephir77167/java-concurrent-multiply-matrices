import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;

class MatricesCreator {
  static class MatrixFromJSONCreator implements Runnable {
    JSONObject _subObj;
    Matrix _result;

    MatrixFromJSONCreator(JSONObject subObj) {
      _subObj = subObj;
    }

    Matrix getResult() {
      return _result;
    }

    public void run () {
      JSONArray matrixObj = _subObj.getJSONArray("matrice");

      int height = _subObj.getInt("height");
      int width = _subObj.getInt("width");
      int size = height * width;
      int[] array = new int[size];

      int i;
      boolean outOfBound = false;
      for (i = 0; i < matrixObj.length(); ++i) {
        if (i == size) {
          outOfBound = true;
          break;
        }
        array[i] = matrixObj.getInt(i);
      }

      if (outOfBound || i != array.length) {
        System.err.println("The \"height\" and \"width\" properties of the matrice " + (i + 1) + " don't match its real dimensions");
        System.exit(1);
      }

      _result = new Matrix(height, width, array);
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
      System.err.println("The 2 matrices should be stored in a JSON file, which name should be passed as an argument");
      System.exit(1);
    }

    try {
      JSONObject jsonObj = new JSONObject(jsonString);
      MatrixFromJSONCreator[] matrixCreators = new MatrixFromJSONCreator[2];
      Thread[] threads = new Thread[2];

      for (int i = 0; i < 2; ++i) {
        JSONObject subObj = jsonObj.getJSONObject(Integer.toString(i + 1));

        matrixCreators[i] = new MatrixFromJSONCreator(subObj);
        threads[i] = new Thread(matrixCreators[i]);
        threads[i].start();
      }

      try {
        threads[0].join();
        threads[1].join();
      } catch (InterruptedException e) {
        System.err.println("Thread supposed to parse matrice has been unexpectedly interrupted");
        System.exit(1);
      }

      matrices[0] = matrixCreators[0].getResult();
      matrices[1] = matrixCreators[1].getResult();

      if (matrices[0].getHeight() != matrices[1].getWidth() || matrices[1].getHeight() != matrices[0].getWidth()) {
        System.err.print("For the two matrices to be multipliable, ");
        if (matrices[0].getHeight() == matrices[0].getWidth() || matrices[1].getHeight() == matrices[1].getWidth()) {
          System.err.println("if one of the two matrices is a square matrix, then the other must be too");
        } else {
          System.err.println("one matrice's height must match the other matrice's width, and vice versa");
        }
        System.exit(1);
      }
    } catch (JSONException e) {
      System.err.println("The JSON file should contain two objects respectively named \"1\" and \"2\", which should each have "
        + "3 properties: \"height\", \"width\" and \"matrice\" (the latter being an array representing the matrice)");
      System.exit(1);
    }

    return matrices;
  }
}
