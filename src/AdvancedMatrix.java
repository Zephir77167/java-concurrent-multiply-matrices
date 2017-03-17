class AdvancedMatrix extends AMatrix {
  private int SPLIT_SIZE = 4;

  private int _resultHeight;
  private int _resultWidth;
  private int _chunkSideSize;

  private AMatrix[] _A;
  private AMatrix[] _B;

  private AMatrix _M0;
  private AMatrix _M1;
  private AMatrix _M2;
  private AMatrix _M3;
  private AMatrix _M4;
  private AMatrix _M5;
  private AMatrix _M6;

  AdvancedMatrix(int height, int width, long[] array) {
    super(height, width, array);
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
          _M0 = (_A[0].add(_A[3])).multiplyBy(_B[0].add(_B[3]), false);
          break;
        case 1:
          _M1 = (_A[2].add(_A[3])).multiplyBy(_B[0], false);
          break;
        case 2:
          _M2 = _A[0].multiplyBy(_B[1].subtract(_B[3]), false);
          break;
        case 3:
          _M3 = _A[3].multiplyBy(_B[2].subtract(_B[0]), false);
          break;
        case 4:
          _M4 = (_A[0].add(_A[1])).multiplyBy(_B[3], false);
          break;
        case 5:
          _M5 = (_A[2].subtract(_A[0])).multiplyBy(_B[0].add(_B[1]), false);
          break;
        case 6:
          _M6 = (_A[1].subtract(_A[3])).multiplyBy(_B[2].add(_B[3]), false);
          break;
        default:
      }
    }

    public void run () {
      for(int i = _start; i < _end; ++i) {
        compute(i);
      }
    }
  }

  private static SimpleMatrix getSimpleMatrixFromAdvancedMatrix(AMatrix m) {
    return new SimpleMatrix(m.getHeight(), m.getWidth(), m.getArray());
  }

  AMatrix add(AMatrix m2) {
    AMatrix m1 = this;
    long[] resultArray = new long[m1.getHeight() * m1.getWidth()];

    for (int i = 0; i < m1.getHeight(); ++i) {
      for (int j = 0; j < m1.getWidth(); ++j) {
        int index = i * m1.getWidth() + j;

        resultArray[index] = m1.getArray()[index] + m2.getArray()[index];
      }
    }

    return new AdvancedMatrix(m1.getHeight(), m1.getWidth(), resultArray);
  }

  AMatrix subtract(AMatrix m2) {
    AMatrix m1 = this;
    long[] resultArray = new long[m1.getHeight() * m1.getWidth()];

    for (int i = 0; i < m1.getHeight(); ++i) {
      for (int j = 0; j < m1.getWidth(); ++j) {
        int index = i * m1.getWidth() + j;

        resultArray[index] = m1.getArray()[index] - m2.getArray()[index];
      }
    }

    return new AdvancedMatrix(m1.getHeight(), m1.getWidth(), resultArray);
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

  private AMatrix[] createBlockMatrixFromSplitArray(long[][] splitArray) {
    AMatrix[] block = new AMatrix[SPLIT_SIZE];

    for (int i = 0; i < SPLIT_SIZE; ++i) {
      block[i] = new AdvancedMatrix(_chunkSideSize, _chunkSideSize, splitArray[i]);
    }

    return block;
  }

  private AMatrix[] split(AMatrix m) {
    int fullSize = _chunkSideSize * 2;

    if (fullSize < SPLIT_SIZE) {
      return null;
    }

    long[][] resultArrays = new long[SPLIT_SIZE][fullSize * fullSize];

    for (int i = 0; i < fullSize; ++i) {
      for (int j = 0; j < fullSize; ++j) {
        int recipientMatrixId = (j >= _chunkSideSize ? 1 : 0) + (i >= _chunkSideSize ? 2 : 0);
        int recipientMatrixIndex =
          (i - (recipientMatrixId >= 2 ? _chunkSideSize : 0)) * _chunkSideSize
            + j - (recipientMatrixId == 1 || recipientMatrixId == 3 ? _chunkSideSize : 0);

        if (i < m.getHeight() && j < m.getWidth()) {
          resultArrays[recipientMatrixId][recipientMatrixIndex] = m.getArray()[i * m.getWidth() + j];
        } else {
          resultArrays[recipientMatrixId][recipientMatrixIndex] = 0;
        }
      }
    }

    return createBlockMatrixFromSplitArray(resultArrays);
  }

  private void runParallelCompute(AMatrix[] A, AMatrix[] B, int nbThreads) {
    Thread[] threads = new Thread[nbThreads];
    int sectionSize = (7 / nbThreads) + 1;

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

  private AMatrix mergeMatricesBlocks(AMatrix C0, AMatrix C1, AMatrix C2, AMatrix C3) {
    long[] resultArray = new long[_resultHeight * _resultWidth];
    long[][] sendingArrays = new long[][]{ C0.getArray(), C1.getArray(), C2.getArray(), C3.getArray() };

    for (int i = 0; i < _resultHeight; ++i) {
      for (int j = 0; j < _resultWidth; ++j) {
        int sendingMatrixId = (j >= _chunkSideSize ? 1 : 0) + (i >= _chunkSideSize ? 2 : 0);
        int sendingMatrixIndex =
          (i - (sendingMatrixId >= 2 ? _chunkSideSize : 0)) * _chunkSideSize
            + j - (sendingMatrixId == 1 || sendingMatrixId == 3 ? _chunkSideSize : 0);

        resultArray[i * _resultWidth + j] = sendingArrays[sendingMatrixId][sendingMatrixIndex];
      }
    }

    return new AdvancedMatrix(_resultHeight, _resultWidth, resultArray);
  }

  AMatrix multiplyBy(AMatrix m2) {
    _resultHeight = this.getHeight();
    _resultWidth = m2.getWidth();
    _chunkSideSize = getChunkSideSize(this.getHeight(), this.getWidth(), m2.getHeight(), m2.getWidth());

    int nbThreads = 7 > NB_THREADS_AVAILABLE ? NB_THREADS_AVAILABLE : 7;

    AMatrix[] A = split(this);
    AMatrix[] B = split(m2);
    if (A == null || B == null) {
      return getSimpleMatrixFromAdvancedMatrix(this).multiplyBy(getSimpleMatrixFromAdvancedMatrix(m2));
    }

    if (nbThreads > 1) {
      runParallelCompute(A, B, nbThreads);
    } else {
      runSequentialCompute(A, B);
    }

    AMatrix C0 = _M0.add(_M3).subtract(_M4).add(_M6);
    AMatrix C1 = _M2.add(_M4);
    AMatrix C2 = _M1.add(_M3);
    AMatrix C3 = _M0.subtract(_M1).add(_M2).add(_M5);

    return mergeMatricesBlocks(C0, C1, C2, C3);
  }
}
