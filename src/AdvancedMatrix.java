class AdvancedMatrix extends AMatrix {
  private int SPLIT_SIZE = 4;

  private int _resultHeight;
  private int _resultWidth;
  private int _chunkSideSize;

  AdvancedMatrix(int height, int width, long[] array) {
    super(height, width, array);
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

  private AMatrix[] computeM(AMatrix[] A, AMatrix[] B) {
    return new AMatrix[]{
      (A[0].add(A[3])).multiplyBy(B[0].add(B[3])),
      (A[2].add(A[3])).multiplyBy(B[0]),
      A[0].multiplyBy(B[1].subtract(B[3])),
      A[3].multiplyBy(B[2].subtract(B[0])),
      (A[0].add(A[1])).multiplyBy(B[3]),
      (A[2].subtract(A[0])).multiplyBy(B[0].add(B[1])),
      (A[1].subtract(A[3])).multiplyBy(B[2].add(B[3])),
    };
  }

  private AMatrix[] computeC(AMatrix[] M) {
    return new AMatrix[]{
      M[0].add(M[3]).subtract(M[4]).add(M[6]),
      M[2].add(M[4]),
      M[1].add(M[3]),
      M[0].subtract(M[1]).add(M[2]).add(M[5]),
    };
  }

  private AMatrix mergeMatricesBlocks(AMatrix[] C) {
    long[] resultArray = new long[_resultHeight * _resultWidth];

    for (int i = 0; i < _resultHeight; ++i) {
      for (int j = 0; j < _resultWidth; ++j) {
        int sendingMatrixId = (j >= _chunkSideSize ? 1 : 0) + (i >= _chunkSideSize ? 2 : 0);
        int sendingMatrixIndex =
          (i - (sendingMatrixId >= 2 ? _chunkSideSize : 0)) * _chunkSideSize
            + j - (sendingMatrixId == 1 || sendingMatrixId == 3 ? _chunkSideSize : 0);

        resultArray[i * _resultWidth + j] = C[sendingMatrixId].getArray()[sendingMatrixIndex];
      }
    }

    return new AdvancedMatrix(_resultHeight, _resultWidth, resultArray);
  }

  AMatrix multiplyBy(AMatrix m2) {
    _resultHeight = this.getHeight();
    _resultWidth = m2.getWidth();
    _chunkSideSize = getChunkSideSize(this.getHeight(), this.getWidth(), m2.getHeight(), m2.getWidth());

    AMatrix[] A = split(this);
    AMatrix[] B = split(m2);
    if (A == null || B == null) {
      return getSimpleMatrixFromAdvancedMatrix(this).multiplyBy(getSimpleMatrixFromAdvancedMatrix(m2));
    }

    AMatrix[] M = computeM(A, B);
    AMatrix[] C = computeC(M);
    return mergeMatricesBlocks(C);
  }
}
