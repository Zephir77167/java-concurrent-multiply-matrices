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
      block[i] = new SimpleMatrix(_chunkSideSize, _chunkSideSize, splitArray[i]);
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

    AMatrix[] A = split(this);
    AMatrix[] B = split(m2);
    if (A == null || B == null) {
      return getSimpleMatrixFromAdvancedMatrix(this).multiplyBy(getSimpleMatrixFromAdvancedMatrix(m2));
    }

    AMatrix M0 = (A[0].add(A[3])).multiplyBy(B[0].add(B[3]));
    AMatrix M1 = (A[2].add(A[3])).multiplyBy(B[0]);
    AMatrix M2 = A[0].multiplyBy(B[1].subtract(B[3]));
    AMatrix M3 = A[3].multiplyBy(B[2].subtract(B[0]));
    AMatrix M4 = (A[0].add(A[1])).multiplyBy(B[3]);
    AMatrix M5 = (A[2].subtract(A[0])).multiplyBy(B[0].add(B[1]));
    AMatrix M6 = (A[1].subtract(A[3])).multiplyBy(B[2].add(B[3]));

    AMatrix C0 = M0.add(M3).subtract(M4).add(M6);
    AMatrix C1 = M2.add(M4);
    AMatrix C2 = M1.add(M3);
    AMatrix C3 = M0.subtract(M1).add(M2).add(M5);

    return mergeMatricesBlocks(C0, C1, C2, C3);
  }
}
