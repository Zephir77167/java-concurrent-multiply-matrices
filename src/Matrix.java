class Matrix {
  private int _height;
  private int _width;
  private int[] _array;

  Matrix(int height, int width, int[] array) {
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

  int[] getArray() {
    return _array;
  }

  void print() {
    for (int i = 0; i < _array.length; i++)
    {
      if (i != 0) {
        if (i % _width == 0) {
          System.out.println();
        } else {
          int value = _array[i];
          int nbDecimals = 0;

          while (value >= 10) {
            nbDecimals += 1;
            value /= 10;
          }

          String formattingSpaces = "";
          for (int j = nbDecimals; j < 5; ++j) {
            formattingSpaces += " ";
          }

          System.out.print(formattingSpaces);
        }
      }
      System.out.print(_array[i]);
    }
    System.out.println();
    System.out.println();
  }

  private int multiplyLineByColumn(Matrix m2, int line, int column) {
    Matrix m1 = this;

    int result = 0;

    for (int i = 0; i < m1.getWidth(); ++i) {
      result += m1.getArray()[line * m1.getWidth() + i] * m2.getArray()[i * m2.getWidth() + column];
    }

    return result;
  }

  Matrix multiplyBy(Matrix m2) {
    Matrix m1 = this;

    int resultSideSize = m1.getHeight();
    int[] resultArray = new int[resultSideSize * resultSideSize];

    for (int i = 0; i < resultSideSize; ++i) {
      for (int j = 0; j < resultSideSize; ++j) {
        resultArray[i * resultSideSize + j] = multiplyLineByColumn(m2, i, j);
      }
    }

    return new Matrix(resultSideSize, resultSideSize, resultArray);
  }
}
