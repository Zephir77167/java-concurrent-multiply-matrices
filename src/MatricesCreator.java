import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;

class MatricesCreator {
  static Matrix[] createMatrices(String fromFileName) {
    String jsonString = "";
    int[] heights = new int[2], widths = new int[2];
    int[][] matrices = new int[][] { null, null };

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

      for (int i = 0; i < 2; ++i) {
        JSONObject subObj = jsonObj.getJSONObject(Integer.toString(i + 1));
        JSONArray matriceObj = subObj.getJSONArray("matrice");

        heights[i] = subObj.getInt("height");
        widths[i] = subObj.getInt("width");
        int size = heights[i] * widths[i];
        matrices[i] = new int[size];

        int j;
        boolean outOfBound = false;
        for (j = 0; j < matriceObj.length(); ++j) {
          if (j == size) {
            outOfBound = true;
            break;
          }
          matrices[i][j] = matriceObj.getInt(j);
        }

        if (outOfBound || j != matrices[i].length) {
          System.err.println("The \"height\" and \"width\" properties of the matrice " + (i + 1) + " don't match its real dimensions");
          System.exit(1);
        }
      }

      if (heights[0] != widths[1] || heights[1] != widths[0]) {
        System.err.print("For the two matrices to be multipliable, ");
        if (heights[0] == widths[0] || heights[1] == widths[1]) {
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

    return new Matrix[] { new Matrix(heights[0], widths[0], matrices[0]), new Matrix(heights[1], widths[1], matrices[1]) };
  }
}
