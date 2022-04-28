package physical2.tiny;

import physical2.two.Point2D;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static physical2.blocks.AbsolutePhysical3DMap2.mapY;
import static physical2.blocks.SideMapping.Y_BUFFER;

/**
 * In tiny coords
 */
public class DefaultLegalPositions implements Iterable<Point2D> {
  public static void main(String[] args) {
    Set<Point2D> points = new HashSet<>();
    Iterator<Point2D> iter = new DefaultLegalPositions().iterator();
    for (int i = 0; i < WORLD_HEIGHT * WORLD_HEIGHT; i++) {
      Point2D p = iter.next();
      if (points.contains(p)) {
        throw new IllegalStateException("Duplicate point " + p + " given");
      }
      points.add(p);
    }
  }

  private static final int WORLD_HEIGHT = 256;

  @Override
  public Iterator<Point2D> iterator() {
    return new Iter();
  }

  private static class Iter implements Iterator<Point2D> {
    int x, y;
    int localMax;

    public Iter() {
      x = 0;
      y = 0;
      localMax = 0;
    }

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public Point2D next() {
      if (y == localMax) {
        if (x == 0) {
          localMax++;
          x = localMax;
          y = 0;
        } else {
          x--;
        }
      } else {
        y++;
        if (mapY(y) + Y_BUFFER >= WORLD_HEIGHT) {
          y = 0;
          x++;
        }
      }

      return new Point2D(x, y);
    }
  }
}
