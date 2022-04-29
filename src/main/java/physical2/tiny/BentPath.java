package physical2.tiny;

import circuit.Pair;
import physical2.blocks.SquareSpecifier;
import physical2.one.BiSide;
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

  /**
   * The side of the square which hold the given wire end, if any.
   */
  private BiSide startSide;
  private BiSide endSide;

  public BentPath(Point2D start, Point2D end) {
    if (start.equals(end)) {
      throw new IllegalArgumentException("Self-loops are not allowed in the circuit");
    }
    this.start = start;
    this.end = end;

    int xDiff = end.getX() - start.getX();
    if (Math.abs(xDiff) <= 1) {
      endSide = BiSide.LEFT;
      startSide = BiSide.LEFT;
    } else if (start.getX() < end.getX()) {
      startSide = BiSide.LEFT;
      endSide = BiSide.RIGHT;
    } else if (end.getX() < start.getX()) {
      endSide = BiSide.LEFT;
      startSide = BiSide.RIGHT;
    }
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

  @Override
  public String toString() {
    return "BentPath(" + start + " -> " + end + ")";
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
      SquareSpecifier spec = buildSquareSpec(sides.getFirst(), sides.getSecond(), repeatIndex == 0 || sides.getSecond() == null);
      return new Pair<>(pair.getFirst(), spec);
    }
  }

  private SquareSpecifier buildSquareSpec(Side first, Side second, boolean repeater) {
    return new SquareSpecifier(
        first,
        second,
        first == null ? startSide : null,
        second == null ? endSide : null,
        repeater
    );
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
          Pair<Point2D, Pair<Side, Side>> localNext;
          do {
            localNext = iter.next();
            if (p.equals(localNext.getFirst())) {
              if (end != localNext.getSecond().getFirst()) {
                throw new IllegalStateException("bug");
              }
              end = localNext.getSecond().getSecond();
              nextDone = true;
            } else {
              nextDone = false;
              break;
            }
          } while (iter.hasNext());
          next = new Pair<>(p, new Pair<>(start, end));
          nextNext = localNext;
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
    boolean done;

    public BentIterator() {
      started = false;
      done = false;
      iter = new ZoomedBentIterator(start, end);
      // iter really should have at least one item.
      prevEdge = EdgePoint.zoomOut(iter.next());
      if (prevEdge.getSide() != DOWN) {
        prevEdge = prevEdge.altPointSide();
      }
    }

    private boolean hasNextExceptLast() {return !started || iter.hasNext();}

    @Override
    public boolean hasNext() {
      return !done;
    }

    @Override
    public Pair<Point2D, Pair<Side, Side>> next() {
      if (!started) {
        // prevEdge is really our next edge
        started = true;
        return new Pair<>(prevEdge.getPoint(), new Pair<>(null, prevEdge.getSide()));
      }
      if (!hasNextExceptLast()) {
        // We need to return the last thing
        done = true;

        EdgePoint ep;
        if (prevEdge.getSide() == DOWN){
         ep = prevEdge;
        } else {
          ep = prevEdge.altPointSide();
          if (ep.getSide() != DOWN) {
            throw new IllegalStateException("Path did not end on a horizontal tile edge");
          }
        }
        return new Pair<>(ep.getPoint(), new Pair<>(ep.getSide(), null));
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
  private class ZoomedBentIterator implements Iterator<Point2D> {
    private Point2D pos;
    private Point2D zoomedEnd;
    boolean done;

    /**
     * Might be horizontal if that's a better first direction to move.
     */
    private Point2D primaryDirection;
    private Point2D secondaryDirection;

    private boolean onDiag(Point2D a) {
      return (a.getX() + a.getY()) % 2 != 0;
    }

    /**
     * @param p    A zoomed out point
     * @param side The side which the wire extends out of p from
     */
    private Point2D pickPathEndpoint(Point2D p, BiSide side) {
      switch (side) {
        case LEFT:
          return new EdgePoint(p, DOWN).zoomIn();
        case RIGHT:
          return new EdgePoint(p.translate(-1, 0), DOWN).zoomIn();
        default:
          throw new IllegalArgumentException();
      }
    }

    /**
     * Params are not zoomed in.
     */
    public ZoomedBentIterator(Point2D start, Point2D end) {
      done = false;

      Point2D zoomedStart = pickPathEndpoint(start, startSide);
      zoomedEnd = pickPathEndpoint(end, endSide);

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

  private static int sign(int i) {
    return i >= 0 ? 1 : -1;
  }
}
