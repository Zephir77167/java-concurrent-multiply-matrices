class SimpleMatrix extends AMatrix {
  int NB_THREADS = 4;

  private AMatrix _m1;
  private AMatrix _m2;
  private int _resultHeight;
  private int _resultWidth;
  private long[] _resultArray;

  SimpleMatrix(int height, int width, long[] array) {
    super(height, width, array);
  }

  class SectionCalculator implements Runnable {
    int _start;
    int _end;

    SectionCalculator(int start, int end) {
      _start = start;
      _end = end;
    }

    public void run () {
      for (int k = _start; k < _end; ++k) {
        for (int i = 0; i < _resultHeight; ++i) {
          for (int j = 0; j < _resultWidth; ++j) {
            _resultArray[i * _resultWidth + j] +=
              _m1.getArray()[i * _m1.getWidth() + k] * _m2.getArray()[k * _m2.getWidth() + j];
          }
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

    Thread[] threads = new Thread[NB_THREADS];

    for (int i = 0; i < NB_THREADS; ++i) {
      threads[i] = new Thread(new SectionCalculator(i * _m1.getWidth() / NB_THREADS, (i + 1) * _m1.getWidth() / NB_THREADS));
      threads[i].start();
    }

    try {
      for (int i = 0; i < NB_THREADS; ++i) {
        threads[i].join();
      }
    } catch (InterruptedException e) {
      System.err.println("Thread supposed to compute line has been unexpectedly interrupted");
    }

    return new SimpleMatrix(_resultHeight, _resultWidth, _resultArray);
  }
}
