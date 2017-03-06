class SimpleMatrix extends AMatrix {
  SimpleMatrix(int height, int width, long[] array) {
    super(height, width, array);
  }

  private long multiplyLineByColumn(AMatrix m2, int line, int column) {
    AMatrix m1 = this;

    long result = 0;

    for (int i = 0; i < m1.getWidth(); ++i) {
      result += m1.getArray()[line * m1.getWidth() + i] * m2.getArray()[i * m2.getWidth() + column];
    }

    return result;
  }

  AMatrix multiplyBy(AMatrix m2) {
    AMatrix m1 = this;

    int resultSideSize = m1.getHeight();
    long[] resultArray = new long[resultSideSize * resultSideSize];

    for (int i = 0; i < resultSideSize; ++i) {
      for (int j = 0; j < resultSideSize; ++j) {
        resultArray[i * resultSideSize + j] = multiplyLineByColumn(m2, i, j);
      }
    }

    return new SimpleMatrix(resultSideSize, resultSideSize, resultArray);
  }
}
