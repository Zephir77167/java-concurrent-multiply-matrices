import java.util.Arrays;

class AdvancedMatrix extends AMatrix {
  private int SPLIT_SIZE = 4;
  private int LEAF_SIZE = 512;

  private int _resultHeight;
  private int _resultWidth;
  private int _chunkSideSize;

  private AMatrix[][] _AB;

  private AMatrix[] _M;

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

    private void createBlockMatrixFromSplitArrays(long[][] splitArray, boolean[] isMatrixEmpty) {
      for (int i = 0; i < SPLIT_SIZE; ++i) {
        _AB[_matrixId][i] = !isMatrixEmpty[i] ?
          new AdvancedMatrix(_chunkSideSize, splitArray[i]) :
          new AdvancedMatrix();
      }
    }

    public void run() {
      int fullSize = _chunkSideSize * 2;

      long[][] resultArrays = new long[SPLIT_SIZE][fullSize * fullSize];
      boolean[] isMatrixEmpty = new boolean[SPLIT_SIZE];
      Arrays.fill(isMatrixEmpty, true);

      for (int i = 0; i < fullSize; ++i) {
        for (int j = 0; j < fullSize; ++j) {
          int recipientMatrixId = (j >= _chunkSideSize ? 1 : 0) + (i >= _chunkSideSize ? 2 : 0);
          int recipientMatrixIndex =
            (i - (recipientMatrixId >= 2 ? _chunkSideSize : 0)) * _chunkSideSize
              + j - (recipientMatrixId == 1 || recipientMatrixId == 3 ? _chunkSideSize : 0);

          if (i < _m.getHeight() && j < _m.getWidth()) {
            long value = _m.getArray()[i * _m.getWidth() + j];
            resultArrays[recipientMatrixId][recipientMatrixIndex] = value;

            if (value != 0 && isMatrixEmpty[recipientMatrixId]) {
              isMatrixEmpty[recipientMatrixId] = false;
            }
          } else {
            resultArrays[recipientMatrixId][recipientMatrixIndex] = 0;
          }
        }
      }

      createBlockMatrixFromSplitArrays(resultArrays, isMatrixEmpty);
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

  // No check on empty matrices because already checked in multiplyBy()
  private void splitMatrices(AMatrix m1, AMatrix m2) {
    if (_chunkSideSize * 2 <= LEAF_SIZE) {
      _AB = null;
      return;
    }

    int nbWorkToDo = _chunkSideSize;
    int nbThreads = nbWorkToDo * 2 > NB_THREADS_AVAILABLE ? NB_THREADS_AVAILABLE : nbWorkToDo * 2;
    _AB = new AMatrix[2][SPLIT_SIZE];

    if (nbThreads > 1) {
      runParallelSplit(0, m1, nbThreads / 2 + nbThreads % 2, nbWorkToDo);
    } else {
      runSequentialSplit(0, m1, nbWorkToDo);
    }
    if (nbThreads > 2) {
      runParallelSplit(1, m2, nbThreads / 2, nbWorkToDo);
    } else {
      runSequentialSplit(1, m2, nbWorkToDo);
    }
  }

  private void runParallelCompute(AMatrix[] A, AMatrix[] B, int nbThreads, int nbWorkToDo) {
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

  private void runSequentialCompute(AMatrix[] A, AMatrix[] B, int nbWorkToDo) {
    new SubMatrixCalculator(0, nbWorkToDo).run();
  }

  private void computeM() {
    int nbWorkToDo = 7;
    int nbThreads = nbWorkToDo > NB_THREADS_AVAILABLE ? NB_THREADS_AVAILABLE : nbWorkToDo;
    _M = new AMatrix[nbWorkToDo];

    if (nbThreads > 1) {
      runParallelCompute(_AB[0], _AB[1], nbThreads, nbWorkToDo);
    } else {
      runSequentialCompute(_AB[0], _AB[1], nbWorkToDo);
    }
  }

  // No check on empty matrices because empty matrices won't be split - and therefore won't have to be merged
  private AMatrix mergeMatricesBlocks(AMatrix[] C) {
    long[] resultArray = new long[_resultHeight * _resultWidth];

    for (int i = 0; i < _resultHeight; ++i) {
      for (int j = 0; j < _resultWidth; ++j) {
        int sendingMatrixId = (j >= _chunkSideSize ? 1 : 0) + (i >= _chunkSideSize ? 2 : 0);
        int sendingMatrixIndex =
          (i - (sendingMatrixId >= 2 ? _chunkSideSize : 0)) * _chunkSideSize
            + j - (sendingMatrixId == 1 || sendingMatrixId == 3 ? _chunkSideSize : 0);

        resultArray[i * _resultWidth + j] = !C[sendingMatrixId].isEmpty() ?
          C[sendingMatrixId].getArray()[sendingMatrixIndex] :
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
      for (int k = 0; k < resultWidth; ++k) {
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
    Timer timer1 = new Timer();
    timer1.start();

    if (_chunkSideSize == 0) {
      _resultHeight = this.getHeight();
      _resultWidth = m2.getWidth();
      _chunkSideSize = getChunkSideSize(this.getHeight(), this.getWidth(), m2.getHeight(), m2.getWidth());
    }

    timer1.end();
    long time1 = timer1.getEllapsedTime();
    if (time1 != 0) {
      //System.out.println("Time spent initializing vars: " + time1 + "ms");
    }

    Timer timer2 = new Timer();
    timer2.start();

    splitMatrices(this, m2);

    timer2.end();
    long time2 = timer2.getEllapsedTime();
    if (time2 != 0) {
      System.out.println("Time spent splitting matrices: " + time2 + "ms");
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

    Timer timer4 = new Timer();
    timer4.start();

    AMatrix[] C = new AMatrix[]{
      _M[0].add(_M[3]).subtract(_M[4]).add(_M[6]),
      _M[2].add(_M[4]),
      _M[1].add(_M[3]),
      _M[0].subtract(_M[1]).add(_M[2]).add(_M[5]),
    };

    timer4.end();
    long time4 = timer4.getEllapsedTime();
    if (time4 != 0) {
      //System.out.println("Time spent computing C: " + time4 + "ms");
    }

    Timer timer5 = new Timer();
    timer5.start();

    AMatrix result = mergeMatricesBlocks(C);

    timer5.end();
    long time5 = timer5.getEllapsedTime();
    if (time5 != 0) {
      //System.out.println("Time spent merging matrices: " + time5 + "ms");
    }

    return result;
  }
}
