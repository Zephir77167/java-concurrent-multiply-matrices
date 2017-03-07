class SimpleMatrix extends AMatrix {
  int NB_THREADS = 4;

  AMatrix _m1;
  AMatrix _m2;
  long[] _resultArray;
  int _resultHeight;
  int _resultWidth;
  int _diagonal;

  SimpleMatrix(int height, int width, long[] array) {
    super(height, width, array);
  }

  class LineCalculator implements Runnable {
    AMatrix _m1;
    AMatrix _m2;
    long[] _resultArray;
    int _resultHeight;
    int _resultWidth;
    int _diagonal;

    LineCalculator(int diagonal) {
      _diagonal = diagonal;
    }

    public void run () {
      for (int i = 0; i < _resultHeight; ++i) {
        for (int j = 0; j < _resultWidth; ++j) {
          _resultArray[i * _resultWidth + j] +=
            _m1.getArray()[i * _m1.getWidth() + _diagonal] * _m2.getArray()[_diagonal * _m2.getWidth() + j];
        }
      }
    }
  }

  AMatrix multiplyBy(AMatrix m2) {
    _m1 = this;
    _m2 = m2;
    _resultHeight = _m1.getHeight();
    _resultWidth = _m2.getWidth();
    _resultArray = new long[_resultHeight * _resultWidth];

    Thread[] threads = new Thread[_resultHeight];

    for (int k = 0; k < _m1.getWidth(); ++k) {
      threads[k] = new Thread(new LineCalculator(k));
      threads[k].start();
    }

    try {
      for (int i = 0; i < _resultHeight; ++i) {
        threads[i].join();
      }
    } catch (InterruptedException e) {
      System.err.println("Thread supposed to compute line has been unexpectedly interrupted");
    }

    return new SimpleMatrix(_resultHeight, _resultWidth, _resultArray);
  }
}
