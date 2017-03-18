class AdvancedMatrix extends AMatrix {
  private int SPLIT_SIZE = 4;

  private int _resultHeight;
  private int _resultWidth;
  private int _chunkSideSize;

  private AMatrix[] _M;

  private AdvancedMatrix() {
    super();
  }

  AdvancedMatrix(int height, int width, long[] array) {
    super(height, width, array);
  }

  AdvancedMatrix(int sideSize, long[] array) {
    super(sideSize, sideSize, array);

    _resultHeight = sideSize;
    _resultWidth = sideSize;
    _chunkSideSize = sideSize / 2;
  }

  class SubMatrixCalculator implements Runnable {
    int _start;
    int _end;
    AMatrix[] _A;
    AMatrix[] _B;

    SubMatrixCalculator(int start, int end, AMatrix[] A, AMatrix[] B) {
      _start = start;
      _end = end;
      _A = A;
      _B = B;
    }

    private void compute(int index) {
      switch (index) {
        case 0:
          _M[0] = (_A[0].add(_A[3])).multiplyBy(_B[0].add(_B[3]), false);
          break;
        case 1:
          _M[1] = (_A[2].add(_A[3])).multiplyBy(_B[0], false);
          break;
        case 2:
          _M[2] = _A[0].multiplyBy(_B[1].subtract(_B[3]), false);
          break;
        case 3:
          _M[3] = _A[3].multiplyBy(_B[2].subtract(_B[0]), false);
          break;
        case 4:
          _M[4] = (_A[0].add(_A[1])).multiplyBy(_B[3], false);
          break;
        case 5:
          _M[5] = (_A[2].subtract(_A[0])).multiplyBy(_B[0].add(_B[1]), false);
          break;
        case 6:
          _M[6] = (_A[1].subtract(_A[3])).multiplyBy(_B[2].add(_B[3]), false);
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

  private AMatrix[] createBlockMatrixFromSplitArray(long[][] splitArray, boolean[] isMatrixEmpty) {
    AMatrix[] block = new AMatrix[SPLIT_SIZE];

    for (int i = 0; i < SPLIT_SIZE; ++i) {
      block[i] = !isMatrixEmpty[i] ?
        new AdvancedMatrix(_chunkSideSize, splitArray[i]) :
        new AdvancedMatrix();
    }

    return block;
  }

  // No check on empty matrices because already checked in multiplyBy()
  private AMatrix[] split(AMatrix m) {
    int fullSize = _chunkSideSize * 2;

    if (fullSize < SPLIT_SIZE) {
      return null;
    }

    long[][] resultArrays = new long[SPLIT_SIZE][fullSize * fullSize];
    boolean[] isMatrixEmpty = new boolean[SPLIT_SIZE];
    for (int i = 0; i < SPLIT_SIZE; ++i) {
      isMatrixEmpty[i] = true;
    }

    for (int i = 0; i < fullSize; ++i) {
      for (int j = 0; j < fullSize; ++j) {
        int recipientMatrixId = (j >= _chunkSideSize ? 1 : 0) + (i >= _chunkSideSize ? 2 : 0);
        int recipientMatrixIndex =
          (i - (recipientMatrixId >= 2 ? _chunkSideSize : 0)) * _chunkSideSize
            + j - (recipientMatrixId == 1 || recipientMatrixId == 3 ? _chunkSideSize : 0);

        if (i < m.getHeight() && j < m.getWidth()) {
          long value = m.getArray()[i * m.getWidth() + j];
          resultArrays[recipientMatrixId][recipientMatrixIndex] = value;

          if (value != 0 && isMatrixEmpty[recipientMatrixId]) {
            isMatrixEmpty[recipientMatrixId] = false;
          }
        } else {
          resultArrays[recipientMatrixId][recipientMatrixIndex] = 0;
        }
      }
    }

    return createBlockMatrixFromSplitArray(resultArrays, isMatrixEmpty);
  }

  private void runParallelCompute(AMatrix[] A, AMatrix[] B, int nbThreads) {
    Thread[] threads = new Thread[nbThreads];
    int sectionSize = (7 / nbThreads) + (7 % nbThreads != 0 ? 1 : 0);

    for (int i = 0; i < nbThreads; ++i) {
      int start = i * sectionSize;
      int end = (i == nbThreads - 1 ? 7 : (i + 1) * sectionSize);

      threads[i] = new Thread(new SubMatrixCalculator(start, end, A, B));
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

  private void runSequentialCompute(AMatrix[] A, AMatrix[] B) {
    new SubMatrixCalculator(0, 7, A, B).run();
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

  // No check on empty matrices because already checked in multiplyBy()
  private AMatrix simpleMultiply(AMatrix m2) {
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

    AMatrix[] A = split(this);
    AMatrix[] B = split(m2);

    timer2.end();
    long time2 = timer2.getEllapsedTime();
    if (time2 != 0) {
      System.out.println("Time spent splitting matrices: " + time2 + "ms");
    }

    if (A == null || B == null) {
      return this.simpleMultiply(m2);
    }

    Timer timer3 = new Timer();
    timer3.start();

    int nbThreads = 7 > NB_THREADS_AVAILABLE ? NB_THREADS_AVAILABLE : 7;
    _M = new AMatrix[7];
    if (nbThreads > 1) {
      runParallelCompute(A, B, nbThreads);
    } else {
      runSequentialCompute(A, B);
    }

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
