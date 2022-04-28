package physical2.tiny;

import physical2.two.Point2D;

import java.util.Iterator;

import static physical2.blocks.AbsolutePhysical3DMap2.mapY;
import static physical2.blocks.SideMapping.Y_BUFFER;

/**
 * In tiny coords
 */
public class DefaultLegalPositions implements Iterable<Point2D> {
  private static final int WORLD_HEIGHT = 256;

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
      y++;
      if (mapY(y) + Y_BUFFER >= WORLD_HEIGHT) {
        y = 0;
        x++;
      }

      return new Point2D(x, y);
    }
  }
}
