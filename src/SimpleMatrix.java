class SimpleMatrix extends AMatrix {
  SimpleMatrix(int height, int width, long[] array) {
    super(height, width, array);
  }

  class LineCalculator implements Runnable {
    AMatrix _m1;
    AMatrix _m2;
    long[] _resultArray;
    int _resultSideSize;
    int _line;

    LineCalculator(AMatrix m1, AMatrix m2, long[] resultArray, int resultSideSize, int line) {
      _m1 = m1;
      _m2 = m2;
      _resultArray = resultArray;
      _resultSideSize = resultSideSize;
      _line = line;
    }

    private long multiplyLineByColumn(int column) {
      long result = 0;

      for (int i = 0; i < _m1.getWidth(); ++i) {
        result += _m1.getArray()[_line * _m1.getWidth() + i] * _m2.getArray()[i * _m2.getWidth() + column];
      }

      return result;
    }

    public void run () {
      for (int i = 0; i < _resultSideSize; ++i) {
        _resultArray[_line * _resultSideSize + i] = multiplyLineByColumn(i);
      }
    }
  }

  AMatrix multiplyBy(AMatrix m2) {
    AMatrix m1 = this;

    int resultSideSize = m1.getHeight();
    long[] resultArray = new long[resultSideSize * resultSideSize];
    Thread[] threads = new Thread[resultSideSize];

    for (int i = 0; i < resultSideSize; ++i) {
      threads[i] = new Thread(new LineCalculator(m1, m2, resultArray, resultSideSize, i));
      threads[i].start();
    }

    try {
      for (int i = 0; i < resultSideSize; ++i) {
        threads[i].join();
      }
    } catch (InterruptedException e) {
      System.err.println("Thread supposed to compute line has been unexpectedly interrupted");
    }

    return new SimpleMatrix(resultSideSize, resultSideSize, resultArray);
  }
}
