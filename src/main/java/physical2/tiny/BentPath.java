package physical2.tiny;

import circuit.Pair;
import physical2.blocks.SquareSpecifier;
import physical2.two.Point2D;
import physical2.two.Side;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static physical2.tiny.EdgePoint.canonicalSide;
import static physical2.two.Side.DOWN;


public class BentPath implements Iterable<Pair<Point2D, SquareSpecifier>> {
  /**
   * Points representing the positions of redstone goals.
   */
  private Point2D start;
  private Point2D end;

  public BentPath(Point2D start, Point2D end) {
    this.start = start;
    this.end = end;
  }

  public Point2D getStart() {
    return start;
  }

  public Point2D getEnd() {
    return end;
  }

  @Override
  public Iterator<Pair<Point2D, SquareSpecifier>> iterator() {
    return new RepeaterInserter();
  }

  /**
   * @return {@code true} if the set of points this takes up
   * overlaps with the set of points {@code other} takes up.
   */
  public boolean overlap(BentPath other) {
    Set<Point2D> points = consumedSpace();
    points.retainAll(other.consumedSpace());
    return points.size() != 0;
  }

  public Set<Point2D> consumedSpace() {
    Set<Point2D> points = new HashSet<>();

    for (Pair<Point2D, SquareSpecifier> pair : this) {
      Point2D p = pair.getFirst();
      points.add(p);
    }

    return points;
  }

  private class RepeaterInserter implements Iterator<Pair<Point2D, SquareSpecifier>> {
    private FilteredBentIterator iter;
    private int repeatIndex;

    RepeaterInserter() {
      iter = new FilteredBentIterator();
      repeatIndex = 0;
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Pair<Point2D, SquareSpecifier> next() {
      repeatIndex++;
      if (repeatIndex % 3 == 0) {
        repeatIndex = 0;
      }

      Pair<Point2D, Pair<Side, Side>> pair = iter.next();
      Pair<Side, Side> sides = pair.getSecond();
      SquareSpecifier spec = new SquareSpecifier(sides.getFirst(), sides.getSecond(), repeatIndex == 0);
      return new Pair<>(pair.getFirst(), spec);
    }
  }

  private class FilteredBentIterator implements Iterator<Pair<Point2D, Pair<Side, Side>>> {
    private BentIterator iter;
    boolean prepped;
    boolean done;
    boolean nextDone;
    /**
     * The accumulated item to return next
     */
    Pair<Point2D, Pair<Side, Side>> next;
    /**
     * The first item popped from the iterator with a different Point2D than the ones before it
     */
    Pair<Point2D, Pair<Side, Side>> nextNext;

    FilteredBentIterator() {
      this.iter = new BentIterator();
      done = false;
      prepped = false;
      if (iter.hasNext()) {
        nextNext = iter.next();
        nextDone = false;
      } else {
        nextDone = true;
      }
    }

    private void prepNext() {
      // next is invalid
      if (!prepped) {
        if (iter.hasNext()) {
          Point2D p = nextNext.getFirst();
          Side start = nextNext.getSecond().getFirst();
          Side end = nextNext.getSecond().getSecond();
          Pair<Point2D, Pair<Side, Side>> item;
          do {
            item = iter.next();
            if (end == item.getSecond().getFirst()) {
              end = item.getSecond().getSecond();
            } else {
              break;
            }
          } while (iter.hasNext());
          next = new Pair<>(p, new Pair<>(start, end));
          nextNext = item;
        } else {
          if (nextDone) {
            done = true;
          } else {
            nextDone = true;
            next = nextNext;
          }
        }
        prepped = true;
      }
    }

    @Override
    public boolean hasNext() {
      prepNext();
      return !done;
    }

    @Override
    public Pair<Point2D, Pair<Side, Side>> next() {
      prepNext();
      prepped = false;
      return next;
    }
  }

  /**
   * The first item returned will include {@code null} as its first side,
   * indicating that this is the start of the path.
   */
  private class BentIterator implements Iterator<Pair<Point2D, Pair<Side, Side>>> {
    private ZoomedBentIterator iter;
    private EdgePoint prevEdge;
    boolean started;

    public BentIterator() {
      started = false;
      iter = new ZoomedBentIterator(start, end);
      // iter really should have at least one item.
      prevEdge = EdgePoint.zoomOut(iter.next());
      if (prevEdge.getSide() != DOWN) {
        prevEdge = prevEdge.altPointSide();
      }
    }

    @Override
    public boolean hasNext() {
      return !started || iter.hasNext();
    }

    @Override
    public Pair<Point2D, Pair<Side, Side>> next() {
      if (!started) {
        // prevEdge is really our next edge
        started = true;
        return new Pair<>(prevEdge.getPoint(), new Pair<>(null, prevEdge.getSide()));
      }

      EdgePoint nextEdge = EdgePoint.zoomOut(iter.next());

      Side returnSide;

      // Try all orientations of both nextEdge and prevEdge to get their Point2Ds to overlap
      if (!nextEdge.getPoint().equals(prevEdge.getPoint())) {
        nextEdge = nextEdge.altPointSide();
        if (!nextEdge.getPoint().equals(prevEdge.getPoint())) {
          prevEdge = prevEdge.altPointSide();
          if (!nextEdge.getPoint().equals(prevEdge.getPoint())) {
            nextEdge = nextEdge.altPointSide();
            if (!nextEdge.getPoint().equals(prevEdge.getPoint())) {
              prevEdge = prevEdge.altPointSide();
              throw new IllegalStateException("No overlap between " + prevEdge + " and " + nextEdge + "!");
            }
          }
        }
      }

      returnSide = nextEdge.getSide();
      nextEdge = nextEdge.altPointSide();

      Pair<Point2D, Pair<Side, Side>> r = new Pair<>(
          prevEdge.getPoint(),
          new Pair<>(
              prevEdge.getSide(),
              returnSide
          )
      );
      prevEdge = nextEdge;
      return r;
    }
  }

  /**
   * A class representing a zoomed in view of positions.
   * Iterates over these zoomed positions.
   * <p>
   * Specifically, to get the real representation:
   * - divide both x and y by 2 to get the coords
   * - modulo both x and y by 2 to get the side.
   * <p>
   * This means that for all valid points, one value is even, and one value is odd.
   */
  private static class ZoomedBentIterator implements Iterator<Point2D> {
    private Point2D pos;
    private Point2D zoomedEnd;
    boolean done;

    /**
     * Might be horizontal if that's a better first direction to move.
     */
    private Point2D primaryDirection;
    private Point2D secondaryDirection;

    private static boolean onDiag(Point2D a) {
      return (a.getX() + a.getY()) % 2 != 0;
    }

    /**
     * Params are not zoomed in.
     */
    public ZoomedBentIterator(Point2D start, Point2D end) {
      done = false;
      int zoomedOutXDiff = end.getX() - start.getX();
      Point2D zoomedStart;
      if (zoomedOutXDiff > 0) {
        zoomedStart = new EdgePoint(start, DOWN).zoomIn();
        zoomedEnd = new EdgePoint(end.translate(-1, 0), DOWN).zoomIn();
      } else if (zoomedOutXDiff < 0) {
        zoomedStart = new EdgePoint(start.translate(-1, 0), DOWN).zoomIn();
        zoomedEnd = new EdgePoint(end, DOWN).zoomIn();
      } else {
        zoomedStart = new EdgePoint(start, DOWN).zoomIn();
        zoomedEnd = new EdgePoint(end, DOWN).zoomIn();
      }

      pos = zoomedStart;
      if (Math.abs(zoomedEnd.getX() - zoomedStart.getX()) >= Math.abs(zoomedEnd.getY() - zoomedStart.getY())) {
        primaryDirection = new Point2D(2 * sign(zoomedEnd.getX() - zoomedStart.getX()), 0);
      } else {
        primaryDirection = signDiffToEnd();
      }
      secondaryDirection = null;
    }

    private boolean onDiagonal() {
      Point2D p = pos.subtract(zoomedEnd);
      return Math.abs(p.getX()) == Math.abs(p.getY());
    }

    private static int sign(int i) {
      return i >= 0 ? 1 : -1;
    }

    @Override
    public boolean hasNext() {
      return !done;
    }

    /**
     * @return A diagonal-axis-aligned vector vaguely pointing towards {@code zoomedEnd} from {@code pos}
     */
    public Point2D signDiffToEnd() {
      Point2D diff = zoomedEnd.subtract(pos);
      int xDiff = diff.getX();
      int yDiff = diff.getY();
      return new Point2D(sign(xDiff), sign(yDiff));
    }

    private void generateSecondaryDirection() {
      // Assumes you're on the diagonal.
      secondaryDirection = signDiffToEnd();
    }

    @Override
    public Point2D next() {
      Point2D prevPos = pos;
      if (prevPos.equals(zoomedEnd)) {
        done = true;
      }
      if (onDiagonal()) {
        if (secondaryDirection == null) {
          generateSecondaryDirection();
        }
        pos = pos.add(secondaryDirection);
      } else {
        if (primaryDirection.getX() % 2 == 0) {
          switch (canonicalSide(pos)) {
            case UP:
            case DOWN:
              pos = pos.add(signDiffToEnd());
              break;
            default:
              pos = pos.add(primaryDirection);
              break;
          }
        } else {
          pos = pos.add(primaryDirection);
        }
      }
      return prevPos;
    }
  }
}
