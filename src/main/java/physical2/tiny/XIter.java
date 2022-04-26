package physical2.tiny;

import physical2.two.Point2D;

import java.util.Iterator;

/**
 * Iterates in the +x direction, until hitting {@code Integer.MAX_VALUE}.
 */
public class XIter implements Iterator<Point2D> {
  private int x, y;

  /**
   * Requires that the input x is not Integer.MIN_VALUE
   */
  public XIter(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean hasNext() {
    return x != Integer.MIN_VALUE;
  }

  @Override
  public Point2D next() {
    return new Point2D(x++, y);
  }
}
