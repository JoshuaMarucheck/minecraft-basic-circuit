package physical.things;

public enum Direction {
  X, Y, Z, _X, _Y, _Z;

  public Axis toAxis() {
    switch (this) {
      case X:
      case _X:
        return Axis.X;
      case Y:
      case _Y:
        return Axis.Y;
      case Z:
      case _Z:
        return Axis.Z;
      default:
        throw new IllegalArgumentException();
    }
  }
}
