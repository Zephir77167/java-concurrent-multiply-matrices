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
          if (_array[i] >= 100) {
            System.out.print("  ");
          } else if (_array[i] >= 10) {
            System.out.print("   ");
          } else {
            System.out.print("    ");
          }
        }
      }
      System.out.print(_array[i]);
    }
    System.out.println();
  }

  private int multiplyLineByColumn(Matrix m2, int line, int column, int resultHeight, int resultWidth) {
    Matrix m1 = this;

    int result = 0;

    for (int i = 0; i < resultHeight; ++i) {
      result += m1.getArray()[line * resultWidth + i] * m2.getArray()[i * resultWidth + column];
    }

    return result;
  }

  Matrix multiplyBy(Matrix m2) {
    Matrix m1 = this;

    int resultHeight = m1.getHeight() > m2.getHeight() ? m1.getHeight() : m2.getHeight();
    int resultWidth = m1.getWidth() > m2.getWidth() ? m1.getWidth() : m2.getWidth();
    int[] resultArray = new int[resultHeight * resultWidth];

    for (int i = 0; i < resultHeight; ++i) {
      for (int j = 0; j < resultWidth; ++j) {
        resultArray[i * resultWidth + j] = multiplyLineByColumn(m2, i, j, resultHeight, resultWidth);
      }
    }

    return new Matrix(resultHeight, resultWidth, resultArray);
  }
}
