import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;

class MatricesCreator {
  static Matrix[] createMatrices(String fromFileName) {
    String objString = "";
    int   height1 = 0, width1 = 0, height2 = 0, width2 = 0;
    int[] matrice1 = null, matrice2 = null;

    try (BufferedReader br = new BufferedReader(new FileReader(fromFileName))) {
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
        sb.append(line);
        sb.append(System.lineSeparator());
        line = br.readLine();
      }

      objString = sb.toString();
    } catch (Exception e) {
      System.err.println("The 2 matrices should be stored in a JSON file, which name should be passed as an argument");
      System.exit(1);
    }

    try {
      JSONObject jsonObj = new JSONObject(objString);

      JSONObject  jsonObj1 = jsonObj.getJSONObject("1");
      JSONObject  jsonObj2 = jsonObj.getJSONObject("2");
      JSONArray matriceObj1 = jsonObj1.getJSONArray("matrice");
      JSONArray matriceObj2 = jsonObj2.getJSONArray("matrice");

      height1 = jsonObj1.getInt("height");
      width1 = jsonObj1.getInt("width");
      matrice1 = new int[height1 * width1];
      for (int i = 0; i < matriceObj1.length(); i++)
      {
        matrice1[i] = matriceObj1.getInt(i);
      }

      height2 = jsonObj2.getInt("height");
      width2 = jsonObj2.getInt("width");
      matrice2 = new int[height2 * width2];
      for (int i = 0; i < matriceObj2.length(); i++)
      {
        matrice2[i] = matriceObj2.getInt(i);
      }
    } catch (JSONException e) {
      System.err.println("The JSON file should contain two objects respectively named \"1\" and \"2\", "
        + "which each should have 3 properties: \"height\", \"width\" and \"matrice\" (the latter being an array representing the matrice) ");
      System.exit(1);
    }

    if (matrice1.length != height1 * width1) {
      System.err.println("The \"height\" and \"width\" properties of the first matrice don't match its real dimensions");
      System.exit(1);
    }
    if (matrice2.length != height2 * width2) {
      System.err.println("The \"height\" and \"width\" properties of the second matrice don't match its real dimensions");
      System.exit(1);
    }

    Matrix[] matrices = new Matrix[2];
    matrices[0] = new Matrix(height1, width1, matrice1);
    matrices[1] = new Matrix(height2, width2, matrice2);

    return matrices;
  }
}
