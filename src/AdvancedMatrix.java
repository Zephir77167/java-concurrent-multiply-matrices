class AdvancedMatrix extends AMatrix {
  private int SPLIT_SIZE = 4;

  AdvancedMatrix(int height, int width, long[] array) {
    super(height, width, array);
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

  private int getSplitSideSize(int height1, int width1, int height2, int width2) {
    int m1GreaterSide = height1 > width1 ? height1 : width1;
    int m2GreaterSide = height2 > width2 ? height2 : width2;
    int greaterSide = m1GreaterSide > m2GreaterSide ? m1GreaterSide : m2GreaterSide;

    return getNextPowerOfTwo(greaterSide) / (SPLIT_SIZE / 2);
  }

  private AMatrix[] createBlockMatrixFromSplitArray(long[][] splitArray, int splitSideSize) {
    AMatrix[] block = new AMatrix[SPLIT_SIZE];

    for (int i = 0; i < SPLIT_SIZE; ++i) {
      block[i] = new AdvancedMatrix(splitSideSize, splitSideSize, splitArray[i]);
    }

    return block;
  }

  private AMatrix[] split(AMatrix m, int splitSideSize) {
    int fullSize = splitSideSize * 2;

    if (fullSize < SPLIT_SIZE) {
      return null;
    }

    long[][] resultArray = new long[SPLIT_SIZE][fullSize * fullSize];

    for (int i = 0; i < fullSize; ++i) {
      for (int j = 0; j < fullSize; ++j) {
        int recipientMatrixId = (j >= splitSideSize ? 1 : 0) + (i >= splitSideSize ? 2 : 0);
        int recipientMatrixIndex =
          (i - (recipientMatrixId >= 2 ? splitSideSize : 0)) * splitSideSize
            + j - (recipientMatrixId == 1 || recipientMatrixId == 3 ? splitSideSize : 0);

        if (i < m.getHeight() && j < m.getWidth()) {
          resultArray[recipientMatrixId][recipientMatrixIndex] = m.getArray()[i * m.getWidth() + j];
        } else {
          resultArray[recipientMatrixId][recipientMatrixIndex] = 0;
        }
      }
    }

    return createBlockMatrixFromSplitArray(resultArray, splitSideSize);
  }

  private AMatrix[] computeAM(AMatrix[] A, AMatrix[] B) {
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

  private long[][] computeAC(AMatrix[] M) {
    return new long[][]{
      M[0].add(M[3]).subtract(M[4]).add(M[6]).getArray(),
      M[2].add(M[4]).getArray(),
      M[1].add(M[3]).getArray(),
      M[0].subtract(M[1]).add(M[2]).add(M[5]).getArray(),
    };
  }

  private AMatrix mergeMatricesBlocks(long[][] C, int splitSideSize, int resultHeight, int resultWidth) {
    long[] resultArray = new long[resultHeight * resultWidth];

    for (int i = 0; i < resultHeight; ++i) {
      for (int j = 0; j < resultWidth; ++j) {
        int sendingMatrixId = (j >= splitSideSize ? 1 : 0) + (i >= splitSideSize ? 2 : 0);
        int sendingMatrixIndex =
          (i - (sendingMatrixId >= 2 ? splitSideSize : 0)) * splitSideSize
            + j - (sendingMatrixId == 1 || sendingMatrixId == 3 ? splitSideSize : 0);

        resultArray[i * resultWidth + j] = C[sendingMatrixId][sendingMatrixIndex];
      }
    }

    return new AdvancedMatrix(resultHeight, resultWidth, resultArray);
  }

  AMatrix multiplyBy(AMatrix m2) {
    int resultHeight = this.getHeight();
    int resultWidth = m2.getWidth();

    int splitSideSize = getSplitSideSize(this.getHeight(), this.getWidth(), m2.getHeight(), m2.getWidth());

    AMatrix[] A = split(this, splitSideSize);
    AMatrix[] B = split(m2, splitSideSize);
    if (A == null || B == null) {
      return getSimpleMatrixFromAdvancedMatrix(this).multiplyBy(getSimpleMatrixFromAdvancedMatrix(m2));
    }

    AMatrix[] M = computeAM(A, B);
    long[][] C = computeAC(M);
    return mergeMatricesBlocks(C, splitSideSize, resultHeight, resultWidth);
  }
}
