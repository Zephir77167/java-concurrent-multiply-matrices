abstract class AMatrix {
  private int _height;
  private int _width;
  private long[][] _array;

  AMatrix(int height, int width, long[][] array) {
    _height = height;
    _width = width;
    _array = array;
  }

  int getHeight() {
    return _height;
  }

  int getWidth() {
    return _width;
  }

  long[][] getArray() {
    return _array;
  }

  void prettyPrint() {
    for (int i = 0; i < _height; ++i) {
      if (i != 0) {
        System.out.println();
      }

      for (int j = 0; j < _width; ++j) {
        if (j != 0) {
          System.out.print(", ");
        }

        long value = _array[i][j];
        int nbDecimals = 0;

        while (value >= 10 || value <= -10) {
          nbDecimals += 1;
          value /= 10;
        }

        String formattingSpaces = "";
        for (int k = nbDecimals; k < 5; ++k) {
          formattingSpaces += " ";
        }
        if (value >= 0) {
          formattingSpaces += " ";
        }

        System.out.print(formattingSpaces);
        System.out.print(_array[i][j]);
      }
    }

    System.out.println();
    System.out.println();
  }

  void parsablePrint() {
    for (int i = 0; i < _height; ++i) {
      if (i != 0) {
        System.out.println();
      }

      for (int j = 0; j < _width; ++j) {
        if (j != 0) {
          System.out.print(", ");
        }

        System.out.print(_array[i][j]);
      }
    }

    System.out.println();
    System.out.println();
  }

  abstract AMatrix multiplyBy(AMatrix m2);
}
