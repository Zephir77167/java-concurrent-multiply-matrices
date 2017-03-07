class SimpleMatrix extends AMatrix {
  private AMatrix _m1;
  private AMatrix _m2;
  private int _resultHeight;
  private int _resultWidth;
  private long[] _resultArray;

  SimpleMatrix(int height, int width, long[] array) {
    super(height, width, array);
  }

  class LineCalculator implements Runnable {
    int _i;

    LineCalculator(int i) {
      _i = i;
    }

    public void run () {
      for (int k = 0; k < _m1.getWidth(); ++k) {
        for (int j = 0; j < _resultWidth; ++j) {
          _resultArray[_i * _resultWidth + j] +=
            _m1.getArray()[_i * _m1.getWidth() + k] * _m2.getArray()[k * _m2.getWidth() + j];
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

    int nbThreads = _resultHeight;
    Thread[] threads = new Thread[nbThreads];

    for (int i = 0; i < nbThreads; ++i) {
      threads[i] = new Thread(new LineCalculator(i));
      threads[i].start();
    }

    try {
      for (int i = 0; i < nbThreads; ++i) {
        threads[i].join();
      }
    } catch (InterruptedException e) {
      System.err.println("Thread supposed to compute line has been unexpectedly interrupted");
    }

    return new SimpleMatrix(_resultHeight, _resultWidth, _resultArray);
  }
}
