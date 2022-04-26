package physical2.tiny;

import circuit.Pair;
import physical2.blocks.SquareSpecifier;
import physical2.two.Point2D;
import physical2.two.Side;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
    private BentIterator iter;

    RepeaterInserter() {
      iter = new BentIterator();
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Pair<Point2D, SquareSpecifier> next() {
      Pair<Point2D, Pair<Side, Side>> pair = iter.next();
      Pair<Side, Side> sides = pair.getSecond();
      SquareSpecifier spec = new SquareSpecifier(sides.getFirst(), sides.getSecond(), true);
      return new Pair<>(pair.getFirst(), spec);
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

      Side nextSide;

      if (nextEdge.getPoint().equals(prevEdge.getPoint())) {
        nextSide = nextEdge.getSide();
        nextEdge = nextEdge.altPointSide();
      } else {
        nextSide = nextEdge.getSide().flip();
      }

      Pair<Point2D, Pair<Side, Side>> r = new Pair<>(
          prevEdge.getPoint(),
          new Pair<>(
              prevEdge.getSide(),
              nextSide
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

    private Point2D primaryDirection, secondaryDirection;

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
      primaryDirection = signDiffToEnd();
      secondaryDirection = null;
    }

    private boolean onDiagonal() {
      return pos.subtract(zoomedEnd).dot(primaryDirection) == 0;
    }

    private static int sign(int i) {
      return i >= 0 ? 1 : -1;
    }

    @Override
    public boolean hasNext() {
      return !done;
    }

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
        pos = pos.add(primaryDirection);
      }
      return prevPos;
    }
  }
}
