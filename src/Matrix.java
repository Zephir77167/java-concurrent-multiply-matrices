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
          if (_array[i] >= 10) {
            System.out.print("  ");
          } else {
            System.out.print("   ");
          }
        }
      }
      System.out.print(_array[i]);
    }
    System.out.println();
  }
}
