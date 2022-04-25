package physical2.two;

public class Point2D {
  private int x, y;

  public Point2D(int x, int y) {
    this.x = x;
    this.y = y;

  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Point2D translate(int x, int y) {
    return new Point2D(this.x + x, this.y + y);
  }

  /**
   * Computes this - other in vector notation
   */
  public Point2D subtract(Point2D other) {
    return new Point2D(x - other.x, y - other.y);
  }

  public Point2D add(Point2D other) {
    return new Point2D(x + other.x, y + other.y);
  }

  public Point2D negate() {
    return new Point2D(-x, -y);
  }

  public int dot(Point2D other) {
    return x * other.x + y * other.y;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Point2D) {
      return x == ((Point2D) obj).x && y == ((Point2D) obj).y;
    }
    return false;
  }
}
