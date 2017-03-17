abstract class AMatrix {
  int NB_THREADS_AVAILABLE = Runtime.getRuntime().availableProcessors();

  private int _height;
  private int _width;
  private long[] _array;
  private boolean _isMatrixEmpty = false;

  AMatrix(int height, int width, long[] array) {
    _height = height;
    _width = width;
    _array = array;
  }

  AMatrix(int height, int width, long[] array, boolean isMatrixEmpty) {
    _height = height;
    _width = width;
    _array = array;
    _isMatrixEmpty = isMatrixEmpty;
  }

  int getHeight() {
    return _height;
  }

  int getWidth() {
    return _width;
  }

  long[] getArray() {
    return _array;
  }

  boolean isEmpty() {
    return _isMatrixEmpty;
  }

  void prettyPrint() {
    for (int i = 0; i < _array.length; ++i) {
      if (i != 0) {
        if (i % _width == 0) {
          System.out.println();
        } else {
          System.out.print(", ");
        }
      }

      long value = _array[i];
      int nbDecimals = 0;

      while (value >= 10 || value <= -10) {
        nbDecimals += 1;
        value /= 10;
      }

      String formattingSpaces = "";
      for (int j = nbDecimals; j < 5; ++j) {
        formattingSpaces += " ";
      }
      if (value >= 0) {
        formattingSpaces += " ";
      }

      System.out.print(formattingSpaces);
      System.out.print(_array[i]);
    }

    System.out.println();
    System.out.println();
  }

  void parsablePrint() {
    for (int i = 0; i < _array.length; ++i) {
      if (i != 0) {
        if (i % _width == 0) {
          System.out.println();
        } else {
          System.out.print(", ");
        }
      }

      System.out.print(_array[i]);
    }

    System.out.println();
    System.out.println();
  }

  abstract AMatrix add(AMatrix m2);
  abstract AMatrix subtract(AMatrix m2);
  abstract AMatrix multiplyBy(AMatrix m2);

  AMatrix multiplyBy(AMatrix m2, boolean doMultiThread) {
    if (!doMultiThread) {
      NB_THREADS_AVAILABLE = 0;
    }

    return this.multiplyBy(m2);
  }
}
