class AdvancedMatrix extends AMatrix {
  private int NB_THREADS_AVAILABLE = Runtime.getRuntime().availableProcessors();
  private int SPLIT_SIZE = 4;

  private AMatrix[] _matrices = new AMatrix[2];
  private int _resultHeight;
  private int _resultWidth;
  private long[] _resultArray;

  private AMatrix[] _A = new AMatrix[SPLIT_SIZE];
  private AMatrix[] _B = new AMatrix[SPLIT_SIZE];
  private long[][] _C;

  private int _tmpSideSize;
  private long[][][] _splitMatricesArrays;

  AdvancedMatrix(int height, int width, long[] array) {
    super(height, width, array);
  }

  class MatrixSplitter implements Runnable {
    int _start;
    int _end;
    int _matrixId;

    MatrixSplitter(int start, int end, int matrixId) {
      _start = start;
      _end = end;
      _matrixId = matrixId;
    }

    public void run () {
      if (_tmpSideSize < SPLIT_SIZE) {
        _splitMatricesArrays[_matrixId] = null;
        return;
      }

      int splitPoint = _tmpSideSize / 2;
      long[][] resultArray = new long[SPLIT_SIZE][splitPoint * splitPoint];

      for (int i = _start; i < _end; ++i) {
        for (int j = 0; j < _tmpSideSize; ++j) {
          int recipientMatrixId = (j >= splitPoint ? 1 : 0) + (i >= splitPoint ? 2 : 0);
          int recipientMatrixIndex =
            (i - (recipientMatrixId >= 2 ? splitPoint : 0)) * splitPoint
              + j - (recipientMatrixId == 1 || recipientMatrixId == 3 ? splitPoint : 0);

          if (i < _matrices[_matrixId].getHeight() && j < _matrices[_matrixId].getWidth()) {
            _splitMatricesArrays[_matrixId][recipientMatrixId][recipientMatrixIndex] =
              _matrices[_matrixId].getArray()[i * _matrices[_matrixId].getWidth() + j];
          } else {
            resultArray[recipientMatrixId][recipientMatrixIndex] = 0;
          }
        }
      }
    }
  }

  class MatrixBlocksMerger implements Runnable {
    int _start;
    int _end;

    MatrixBlocksMerger(int start, int end) {
      _start = start;
      _end = end;
    }

    public void run () {
      int splitPoint = _tmpSideSize / 2;

      for (int i = _start; i < _end; ++i) {
        for (int j = 0; j < _resultWidth; ++j) {
          int sendingMatrixId = (j >= splitPoint ? 1 : 0) + (i >= splitPoint ? 2 : 0);
          int sendingMatrixIndex =
            (i - (sendingMatrixId >= 2 ? splitPoint : 0)) * splitPoint
              + j - (sendingMatrixId == 1 || sendingMatrixId == 3 ? splitPoint : 0);

          _resultArray[i * _resultWidth + j] = _C[sendingMatrixId][sendingMatrixIndex];
        }
      }
    }
  }

  private static SimpleMatrix getSimpleMatrixFromAdvancedMatrix(AMatrix m) {
    return new SimpleMatrix(m.getHeight(), m.getWidth(), m.getArray());
  }

  AMatrix add(AMatrix m2) {
    AMatrix m1 = this;
    long resultArray[] = new long[m1.getHeight() * m1.getWidth()];

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
    long resultArray[] = new long[m1.getHeight() * m1.getWidth()];

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

  private int getTmpSideSize(int height1, int width1, int height2, int width2) {
    int m1GreaterSide = height1 > width1 ? height1 : width1;
    int m2GreaterSide = height2 > width2 ? height2 : width2;
    int greaterSide = m1GreaterSide > m2GreaterSide ? m1GreaterSide : m2GreaterSide;

    return getNextPowerOfTwo(greaterSide);
  }

  private void createBlockMatrixFromArrays() {
    int splitPoint = _tmpSideSize / 2;

    _A = new AMatrix[SPLIT_SIZE];
    for (int i = 0; i < SPLIT_SIZE; ++i) {
      _A[i] = new AdvancedMatrix(splitPoint, splitPoint, _splitMatricesArrays[0][i]);
    }

    _B = new AMatrix[SPLIT_SIZE];
    for (int i = 0; i < SPLIT_SIZE; ++i) {
      _B[i] = new AdvancedMatrix(splitPoint, splitPoint, _splitMatricesArrays[1][i]);
    }
  }

  private void runParallelSplit(int nbThreads, int matrixId) {
    Thread[] threads = new Thread[nbThreads];
    int sectionSize = _resultHeight / nbThreads;

    for (int i = 0; i < nbThreads; ++i) {
      int start = i * sectionSize;
      int end = (i == nbThreads - 1 ? _resultHeight : (i + 1) * sectionSize);

      threads[i] = new Thread(new MatrixSplitter(start, end, matrixId));
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

  private void runSequentialSplit(int matrixId) {
    new MatrixSplitter(0, _resultHeight, matrixId).run();
  }

  private void splitMatrices(int nbThreads) {
    if (nbThreads > 0) {
      runParallelSplit(nbThreads > 1 ? nbThreads / 2 + nbThreads % 2 : nbThreads, 0);
    } else {
      runSequentialSplit(0);
    }
    if (nbThreads > 1) {
      runParallelSplit(nbThreads / 2, 1);
    } else {
      runSequentialSplit(1);
    }
  }

  private void runParallelMerge(int nbThreads) {
    Thread[] threads = new Thread[nbThreads];
    int sectionSize = _resultHeight / nbThreads;

    for (int i = 0; i < nbThreads; ++i) {
      int start = i * sectionSize;
      int end = (i == nbThreads - 1 ? _resultHeight : (i + 1) * sectionSize);

      threads[i] = new Thread(new MatrixBlocksMerger(start, end));
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

  private void runSequentialMerge() {
    new MatrixBlocksMerger(0, _resultHeight).run();
  }

  private void mergeMatricesBlocks(int nbThreads) {
    if (nbThreads > 0) {
      runParallelMerge(nbThreads);
    } else {
      runSequentialMerge();
    }
  }

  AMatrix multiplyBy(AMatrix m2) {
    _matrices[0] = this;
    _matrices[1] = m2;
    _resultHeight = this.getHeight();
    _resultWidth = m2.getWidth();
    _resultArray = new long[_resultHeight * _resultWidth];

    _tmpSideSize = getTmpSideSize(this.getHeight(), this.getWidth(), m2.getHeight(), m2.getWidth());
    _splitMatricesArrays = new long[2][SPLIT_SIZE][_tmpSideSize * _tmpSideSize / SPLIT_SIZE];

    int nbThreads = _resultHeight > NB_THREADS_AVAILABLE ? NB_THREADS_AVAILABLE : _resultHeight;

    splitMatrices(nbThreads);
    if (_splitMatricesArrays[0] == null || _splitMatricesArrays[1] == null) {
      return getSimpleMatrixFromAdvancedMatrix(this).multiplyBy(getSimpleMatrixFromAdvancedMatrix(m2));
    }

    createBlockMatrixFromArrays();

    int childThreads = nbThreads;

    AMatrix M1 = (_A[0].add(_A[3])).multiplyBy(_B[0].add(_B[3]), childThreads != 0 ? childThreads-- : childThreads);
    AMatrix M2 = (_A[2].add(_A[3])).multiplyBy(_B[0], childThreads != 0 ? childThreads-- : childThreads);
    AMatrix M3 = _A[0].multiplyBy(_B[1].subtract(_B[3]), childThreads != 0 ? childThreads-- : childThreads);
    AMatrix M4 = _A[3].multiplyBy(_B[2].subtract(_B[0]), childThreads != 0 ? childThreads-- : childThreads);
    AMatrix M5 = (_A[0].add(_A[1])).multiplyBy(_B[3], childThreads != 0 ? childThreads-- : childThreads);
    AMatrix M6 = (_A[2].subtract(_A[0])).multiplyBy(_B[0].add(_B[1]), childThreads != 0 ? childThreads-- : childThreads);
    AMatrix M7 = (_A[1].subtract(_A[3])).multiplyBy(_B[2].add(_B[3]), childThreads != 0 ? childThreads : childThreads);

    _C = new long[SPLIT_SIZE][];
    _C[0] = M1.add(M4).subtract(M5).add(M7).getArray();
    _C[1] = M3.add(M5).getArray();
    _C[2] = M2.add(M4).getArray();
    _C[3] = M1.subtract(M2).add(M3).add(M6).getArray();

    mergeMatricesBlocks(nbThreads);

    return new AdvancedMatrix(_resultHeight, _resultWidth, _resultArray);
  }

  AMatrix multiplyBy(AMatrix m2, int availableCores) {
    NB_THREADS_AVAILABLE = availableCores;

    return this.multiplyBy(m2);
  }
}
