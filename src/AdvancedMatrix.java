import java.util.Arrays;

class AdvancedMatrix extends AMatrix {
  private int SPLIT_SIZE = 4;
  private int LEAF_SIZE = 256;

  private int _resultHeight;
  private int _resultWidth;
  private int _chunkSideSize;

  private long[][][] _splitArrays;
  private boolean[][] _areSplitArraysEmpty;
  private AMatrix[][] _AB;

  private AMatrix[] _M;
  private AMatrix[] _C;

  private AdvancedMatrix() {
    super();
  }

  AdvancedMatrix(int height, int width, long[] array) {
    super(height, width, array);
  }

  private AdvancedMatrix(int sideSize, long[] array) {
    super(sideSize, sideSize, array);

    _resultHeight = sideSize;
    _resultWidth = sideSize;
    _chunkSideSize = sideSize / 2;
  }

  class MatrixSplitter implements Runnable {
    int _start;
    int _end;
    int _matrixId;
    AMatrix _m;

    MatrixSplitter(int start, int end, int matrixId, AMatrix m) {
      _start = start;
      _end = end;
      _matrixId = matrixId;
      _m = m;
    }

    public void run() {
      for (int i = _start; i < _end; ++i) {
        for (int j = 0; j < _chunkSideSize; ++j) {
          int recipientIdx = i * _chunkSideSize + j;

          for (int k = 0; k < SPLIT_SIZE; ++k) {
            int newI = i + (k / 2 * _chunkSideSize);
            int newJ = k != 0 && k != 2 ? j + _chunkSideSize : j;

            if (newI < _m.getHeight() && newJ < _m.getWidth()) {
              long value = _m.getArray()[newI * _m.getWidth() + newJ];

              _splitArrays[_matrixId][k][recipientIdx] = value;

              if (value != 0 && _areSplitArraysEmpty[_matrixId][k]) {
                _areSplitArraysEmpty[_matrixId][k] = false;
              }
            } else {
              _splitArrays[_matrixId][k][recipientIdx] = 0;
            }
          }
        }
      }
    }
  }

  class SubMatrixCalculator implements Runnable {
    int _start;
    int _end;

    SubMatrixCalculator(int start, int end) {
      _start = start;
      _end = end;
    }

    private void compute(int index) {
      switch (index) {
        case 0:
          _M[0] = (_AB[0][0].add(_AB[0][3])).multiplyBy(_AB[1][0].add(_AB[1][3]), false);
          break;
        case 1:
          _M[1] = (_AB[0][2].add(_AB[0][3])).multiplyBy(_AB[1][0], false);
          break;
        case 2:
          _M[2] = _AB[0][0].multiplyBy(_AB[1][1].subtract(_AB[1][3]), false);
          break;
        case 3:
          _M[3] = _AB[0][3].multiplyBy(_AB[1][2].subtract(_AB[1][0]), false);
          break;
        case 4:
          _M[4] = (_AB[0][0].add(_AB[0][1])).multiplyBy(_AB[1][3], false);
          break;
        case 5:
          _M[5] = (_AB[0][2].subtract(_AB[0][0])).multiplyBy(_AB[1][0].add(_AB[1][1]), false);
          break;
        case 6:
          _M[6] = (_AB[0][1].subtract(_AB[0][3])).multiplyBy(_AB[1][2].add(_AB[1][3]), false);
          break;
        default:
      }
    }

    public void run() {
      for(int i = _start; i < _end; ++i) {
        compute(i);
      }
    }
  }

    class FinalSubMatrixCalculator implements Runnable {
      int _start;
      int _end;

      FinalSubMatrixCalculator(int start, int end) {
        _start = start;
        _end = end;
      }

      private void compute(int index) {
        switch (index) {
          case 0:
            _C[0] = _M[0].add(_M[3]).subtract(_M[4]).add(_M[6]);
            break;
          case 1:
            _C[1] = _M[2].add(_M[4]);
            break;
          case 2:
            _C[2] = _M[1].add(_M[3]);
            break;
          case 3:
            _C[3] = _M[0].subtract(_M[1]).add(_M[2]).add(_M[5]);
            break;
          default:
        }
      }

    public void run() {
      for(int i = _start; i < _end; ++i) {
        compute(i);
      }
    }
  }

  AMatrix add(AMatrix m2) {
    if (this.isEmpty()) {
      return m2;
    }
    if (m2.isEmpty()) {
      return this;
    }

    int sideSize = this.getHeight();
    long[] resultArray = new long[sideSize * sideSize];

    for (int i = 0; i < sideSize; ++i) {
      for (int j = 0; j < sideSize; ++j) {
        int index = i * sideSize + j;

        resultArray[index] = this.getArray()[index] + m2.getArray()[index];
      }
    }

    return new AdvancedMatrix(sideSize, resultArray);
  }

  AMatrix subtract(AMatrix m2) {
    if (m2.isEmpty()) {
      return this;
    }
    if (this.isEmpty()) {
      return ((AdvancedMatrix)(m2)).multiplyByInt(-1);
    }

    int sideSize = this.getHeight();
    long[] resultArray = new long[sideSize * sideSize];

    for (int i = 0; i < sideSize; ++i) {
      for (int j = 0; j < sideSize; ++j) {
        int index = i * sideSize + j;

        resultArray[index] = this.getArray()[index] - m2.getArray()[index];
      }
    }

    return new AdvancedMatrix(sideSize, resultArray);
  }

  private static int getNextPowerOfTwo(int nb) {
    int power = 1;

    while (power < nb) {
      power *= 2;
    }

    return power;
  }

  private int getChunkSideSize(int height1, int width1, int height2, int width2) {
    int m1GreaterSide = height1 > width1 ? height1 : width1;
    int m2GreaterSide = height2 > width2 ? height2 : width2;
    int greaterSide = m1GreaterSide > m2GreaterSide ? m1GreaterSide : m2GreaterSide;

    return getNextPowerOfTwo(greaterSide) / (SPLIT_SIZE / 2);
  }

  private void runParallelSplit(int matrixId, AMatrix m, int nbThreads, int nbWorkToDo) {
    Thread[] threads = new Thread[nbThreads];
    int sectionSize = (nbWorkToDo / nbThreads) + (nbWorkToDo % nbThreads != 0 ? 1 : 0);

    for (int i = 0; i < nbThreads; ++i) {
      int start = i * sectionSize;
      int end = (i == nbThreads - 1 ? nbWorkToDo : (i + 1) * sectionSize);

      threads[i] = new Thread(new MatrixSplitter(start, end, matrixId, m));
      threads[i].start();
    }

    try {
      for (int i = 0; i < nbThreads; ++i) {
        threads[i].join();
      }
    } catch (InterruptedException e) {
      System.err.println("Thread supposed to compute line has been unexpectedly interrupted");
    }
  }

  private void runSequentialSplit(int matrixId, AMatrix m, int nbWorkToDo) {
    new MatrixSplitter(0, nbWorkToDo, matrixId, m).run();
  }

  private void createBlockMatricesFromSplitArrays() {
    for (int i = 0; i < 2; ++i) {
      for (int j = 0; j < SPLIT_SIZE; ++j) {
        _AB[i][j] = !_areSplitArraysEmpty[i][j] ?
          new AdvancedMatrix(_chunkSideSize, _splitArrays[i][j]) :
          new AdvancedMatrix();
      }
    }
  }

  // No check on empty matrices because already checked in multiplyBy()
  private void splitMatrices(AMatrix m1, AMatrix m2) {
    if (_chunkSideSize * 2 <= LEAF_SIZE) {
      _AB = null;
      return;
    }

    _splitArrays = new long[2][SPLIT_SIZE][_chunkSideSize * _chunkSideSize];
    _areSplitArraysEmpty = new boolean[2][SPLIT_SIZE];
    Arrays.fill(_areSplitArraysEmpty[0], true);
    Arrays.fill(_areSplitArraysEmpty[1], true);

    int nbWorkToDo = _chunkSideSize;
    int nbThreads = nbWorkToDo * 2 > NB_THREADS_AVAILABLE ? NB_THREADS_AVAILABLE : nbWorkToDo * 2;

    if (nbThreads > 1) {
      runParallelSplit(0, m1, nbThreads, nbWorkToDo);
    } else {
      runSequentialSplit(0, m1, nbWorkToDo);
    }
    if (nbThreads > 1) {
      runParallelSplit(1, m2, nbThreads, nbWorkToDo);
    } else {
      runSequentialSplit(1, m2, nbWorkToDo);
    }

    _AB = new AMatrix[2][SPLIT_SIZE];
    createBlockMatricesFromSplitArrays();
  }

  private void runParallelComputeM(int nbThreads, int nbWorkToDo) {
    Thread[] threads = new Thread[nbThreads];
    int sectionSize = (nbWorkToDo / nbThreads) + (nbWorkToDo % nbThreads != 0 ? 1 : 0);

    for (int i = 0; i < nbThreads; ++i) {
      int start = i * sectionSize;
      int end = (i == nbThreads - 1 ? nbWorkToDo : (i + 1) * sectionSize);

      threads[i] = new Thread(new SubMatrixCalculator(start, end));
      threads[i].start();
    }

    try {
      for (int i = 0; i < nbThreads; ++i) {
        threads[i].join();
      }
    } catch (InterruptedException e) {
      System.err.println("Thread supposed to compute line has been unexpectedly interrupted");
    }
  }

  private void runSequentialComputeM(int nbWorkToDo) {
    new SubMatrixCalculator(0, nbWorkToDo).run();
  }

  private void computeM() {
    int nbWorkToDo = 7;
    int nbThreads = nbWorkToDo > NB_THREADS_AVAILABLE ? NB_THREADS_AVAILABLE : nbWorkToDo;
    _M = new AMatrix[nbWorkToDo];

    if (nbThreads > 1) {
      runParallelComputeM(nbThreads, nbWorkToDo);
    } else {
      runSequentialComputeM(nbWorkToDo);
    }
  }

  private void runParallelComputeC(int nbThreads, int nbWorkToDo) {
    Thread[] threads = new Thread[nbThreads];
    int sectionSize = (nbWorkToDo / nbThreads) + (nbWorkToDo % nbThreads != 0 ? 1 : 0);

    for (int i = 0; i < nbThreads; ++i) {
      int start = i * sectionSize;
      int end = (i == nbThreads - 1 ? nbWorkToDo : (i + 1) * sectionSize);

      threads[i] = new Thread(new FinalSubMatrixCalculator(start, end));
      threads[i].start();
    }

    try {
      for (int i = 0; i < nbThreads; ++i) {
        threads[i].join();
      }
    } catch (InterruptedException e) {
      System.err.println("Thread supposed to compute line has been unexpectedly interrupted");
    }
  }

  private void runSequentialComputeC(int nbWorkToDo) {
    new FinalSubMatrixCalculator(0, nbWorkToDo).run();
  }

  private void computeC() {
    int nbWorkToDo = 4;
    int nbThreads = nbWorkToDo > NB_THREADS_AVAILABLE ? NB_THREADS_AVAILABLE : nbWorkToDo;
    _C = new AMatrix[nbWorkToDo];

    if (nbThreads > 1) {
      runParallelComputeC(nbThreads, nbWorkToDo);
    } else {
      runSequentialComputeC(nbWorkToDo);
    }
  }

  // No check on empty matrices because empty matrices won't be split - and therefore won't have to be merged
  private AMatrix mergeMatricesBlocks() {
    long[] resultArray = new long[_resultHeight * _resultWidth];

    for (int i = 0; i < _resultHeight; ++i) {
      for (int j = 0; j < _resultWidth; ++j) {
        int sendingMatrixId = (j >= _chunkSideSize ? 1 : 0) + (i >= _chunkSideSize ? 2 : 0);
        int sendingMatrixIndex =
          (i - (sendingMatrixId >= 2 ? _chunkSideSize : 0)) * _chunkSideSize
            + j - (sendingMatrixId == 1 || sendingMatrixId == 3 ? _chunkSideSize : 0);

        resultArray[i * _resultWidth + j] = !_C[sendingMatrixId].isEmpty() ?
          _C[sendingMatrixId].getArray()[sendingMatrixIndex] :
          0;
      }
    }

    return new AdvancedMatrix(_resultHeight, _resultWidth, resultArray);
  }

  // No check on empty matrices because already checked in subtract()
  private AMatrix multiplyByInt(int nb) {
    int sideSize = this.getHeight();
    long[] resultArray = new long[sideSize * sideSize];

    for (int i = 0; i < sideSize; ++i) {
      for (int j = 0; j < sideSize; ++j) {
        int index = i * sideSize + j;

        resultArray[index] = this.getArray()[index] * nb;
      }
    }

    return new AdvancedMatrix(sideSize, resultArray);
  }

  private AMatrix simpleSquareMultiply(AMatrix m2) {
    int resultSideSize = this.getHeight();
    long[] resultArray = new long[resultSideSize * resultSideSize];

    for (int i = 0; i < resultSideSize; ++i) {
      for (int k = 0; k < resultSideSize; ++k) {
        for (int j = 0; j < resultSideSize; ++j) {
          resultArray[i * resultSideSize + j] +=
            this.getArray()[i * resultSideSize + k] * m2.getArray()[k * resultSideSize + j];
        }
      }
    }

    return new AdvancedMatrix(resultSideSize, resultArray);
  }

  private AMatrix simpleNotSquareMultiply(AMatrix m2) {
    int resultHeight = this.getHeight();
    int resultWidth = m2.getWidth();
    long[] resultArray = new long[resultHeight * resultWidth];

    for (int i = 0; i < resultHeight; ++i) {
      for (int k = 0; k < this.getWidth(); ++k) {
        for (int j = 0; j < resultWidth; ++j) {
          resultArray[i * resultWidth + j] +=
            this.getArray()[i * this.getWidth() + k] * m2.getArray()[k * resultWidth + j];
        }
      }
    }

    return new AdvancedMatrix(resultHeight, resultWidth, resultArray);
  }

  // No check on empty matrices because already checked in multiplyBy()
  private AMatrix simpleMultiply(AMatrix m2) {
    if (this.getHeight() == this.getWidth()) {
      return this.simpleSquareMultiply(m2);
    }

    return this.simpleNotSquareMultiply(m2);
  }

  AMatrix multiplyBy(AMatrix m2) {
    if (this.isEmpty() || m2.isEmpty()) {
      return new AdvancedMatrix();
    }

    if (_chunkSideSize == 0) {
      _resultHeight = this.getHeight();
      _resultWidth = m2.getWidth();
      _chunkSideSize = getChunkSideSize(this.getHeight(), this.getWidth(), m2.getHeight(), m2.getWidth());
    }

    Timer timer2 = new Timer();
    timer2.start();

    splitMatrices(this, m2);

    timer2.end();
    long time2 = timer2.getEllapsedTime();
    if (time2 != 0 && NB_THREADS_AVAILABLE != 0) {
      //System.out.println("Time spent splitting matrices: " + time2 + "ms");
    }

    if (_AB == null) {
      return this.simpleMultiply(m2);
    }

    Timer timer3 = new Timer();
    timer3.start();

    computeM();

    timer3.end();
    long time3 = timer3.getEllapsedTime();
    if (time3 != 0) {
      //System.out.println("Time spent computing M: " + time3 + "ms");
    }

    computeC();

    Timer timer5 = new Timer();
    timer5.start();

    AMatrix result = mergeMatricesBlocks();

    timer5.end();
    long time5 = timer5.getEllapsedTime();
    if (time5 != 0) {
      //System.out.println("Time spent merging matrices: " + time5 + "ms");
    }

    return result;
  }
}
