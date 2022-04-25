package physical2.two;

public enum Side {
  UP, DOWN, LEFT, RIGHT;

  public Side flip() {
    switch (this) {
      case UP:
        return DOWN;
      case DOWN:
        return UP;
      case LEFT:
        return RIGHT;
      case RIGHT:
        return LEFT;
      default:
        throw new IllegalStateException();
    }
  }
  }
