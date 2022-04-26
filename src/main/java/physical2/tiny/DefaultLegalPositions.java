package physical2.tiny;

import physical2.two.Point2D;

import java.util.Iterator;

/**
 * In tiny coords
 */
public class DefaultLegalPositions implements Iterable<Point2D> {

  @Override
  public Iterator<Point2D> iterator() {
    return new Iter();
  }

  private static class Iter implements Iterator<Point2D> {
    int x, y;

    public Iter() {
      x = 0;
      y = 0;
    }

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public Point2D next() {
      if (y >= 255 / 5) {
        y = 0;
        x++;
      } else {
        y++;
      }

      return new Point2D(x, y);
    }
  }
}
