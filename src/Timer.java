class Timer {
  private long _startTime = 0;
  private long _endTime = 0;

  void start() {
    _startTime = System.currentTimeMillis();
  }

  void end() {
    if (_startTime == 0) {
      System.err.println("Timer.start() must be called before calling Timer.end()");
    }

    _endTime = System.currentTimeMillis();
  }

  long getEllapsedTime() {
    if (_startTime == 0 || _endTime == 0) {
      System.err.println("Timer.start() and Timer.end() must be called before calling Timer.getEllapsedTime()");
      return -1;
    }

    return _endTime - _startTime;
  }
}
