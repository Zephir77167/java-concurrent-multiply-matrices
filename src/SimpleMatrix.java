class SimpleMatrix extends AMatrix {
  SimpleMatrix(int height, int width, long[][] array) {
    super(height, width, array);
  }

  class LineCalculator implements Runnable {
    AMatrix _m1;
    AMatrix _m2;
    long[][] _resultArray;
    int _resultWidth;
    int _line;

    LineCalculator(AMatrix m1, AMatrix m2, long[][] resultArray, int resultWidth, int line) {
      _m1 = m1;
      _m2 = m2;
      _resultArray = resultArray;
      _resultWidth = resultWidth;
      _line = line;
    }

    private long multiplyLineByColumn(int column) {
      long result = 0;

      for (int k = 0; k < _m1.getWidth(); ++k) {
        result += _m1.getArray()[_line][k] * _m2.getArray()[k][column];
      }

      return result;
    }

    public void run () {
      for (int k = 0; k < _m1.getWidth(); ++k) {
        for (int j = 0; j < _resultWidth; ++j) {
          _resultArray[_line][j] += _m1.getArray()[_line][k] * _m2.getArray()[k][j];
        }
      }
    }
  }

  AMatrix multiplyBy(AMatrix m2) {
    AMatrix m1 = this;

    int resultHeight = m1.getHeight();
    int resultWidth = m2.getWidth();
    long[][] resultArray = new long[resultHeight][resultWidth];
    Thread[] threads = new Thread[resultHeight];

    for (int i = 0; i < resultHeight; ++i) {
      threads[i] = new Thread(new LineCalculator(m1, m2, resultArray, resultWidth, i));
      threads[i].start();
    }

    try {
      for (int i = 0; i < resultHeight; ++i) {
        threads[i].join();
      }
    } catch (InterruptedException e) {
      System.err.println("Thread supposed to compute line has been unexpectedly interrupted");
    }

    return new SimpleMatrix(resultHeight, resultWidth, resultArray);
  }
}
