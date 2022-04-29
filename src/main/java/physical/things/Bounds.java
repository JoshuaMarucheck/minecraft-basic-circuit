package physical.things;

import java.util.*;
import java.util.function.Function;

/**
 * Invariant: The values in {@code getLower()} are actually lower than the values in {@code getHigher()}
 * <p>
 * {@code null} means there are no points contained in the Bounds.
 * All functions are null-safe in this sense.
 * <p>
 * Merge: finds a region which contains all given points
 * Restrict: finds a region which all given ranges contain
 */
public class Bounds implements Bounded, Iterable<Point3D> {
  private Point3D lo;
  private Point3D hi;

  private Bounds(Point3D lower, Point3D upper) {
    lo = lower;
    hi = upper;
  }

  public Point3D getLower() {
    return lo;
  }

  public Point3D getUpper() {
    return hi;
  }

  public Bounds transform(Function<Point3D, Point3D> func) {
    return make(func.apply(lo), func.apply(hi));
  }

  public Point3D midpoint() {
    return new Point3D(
        (lo.getX() + hi.getX()) / 2,
        (lo.getY() + hi.getY()) / 2,
        (lo.getZ() + hi.getZ()) / 2
    );
  }

  /**
   * @return A uniformly random int from lo (inclusive) to hi (exclusive)
   */
  private static int randInt(int lo, int hi) {
    return (int) ((hi - lo) * Math.random()) + lo;
  }

  public Point3D randomInteriorPoint() {
    return new Point3D(
        randInt(lo.getX(), hi.getX() + 1),
        randInt(lo.getY(), hi.getY() + 1),
        randInt(lo.getZ(), hi.getZ() + 1)
    );
  }

  /**
   * @return An iterator over all points contained inside these bounds.
   */
  public Iterator<Point3D> iterator() {
    return new InteriorIterator();
  }

  private class InteriorIterator implements Iterator<Point3D> {
    private int x, y, z;

    InteriorIterator() {
      x = lo.getX();
      y = lo.getY();
      z = lo.getZ();
    }

    @Override
    public boolean hasNext() {
      return z <= hi.getZ();
    }

    @Override
    public Point3D next() {
      Point3D r = new Point3D(x, y, z);
      x++;
      if (x > hi.getX()) {
        x = lo.getX();
        y++;
        if (y > hi.getY()) {
          y = lo.getY();
          z++;
        }
      }
      return r;
    }
  }

  public static Bounds make(Point3D p) {
    return new Bounds(p, p);
  }

  public static Bounds make(Point3D lower, Point3D upper) {
    return new Bounds(
        minPoint(lower, upper),
        maxPoint(lower, upper)
    );
  }

  public static Bounds make(Collection<Point3D> points) {
    if (points.isEmpty()) {
      return null;
    }

    return mergePoints(make(points.iterator().next()), points);
  }

  public static Bounds makeFromBounds(Collection<? extends Bounded> bounds) {
    if (bounds.isEmpty()) {
      return null;
    }
    ArrayList<Point3D> list = new ArrayList<>(bounds.size() * 2);
    for (Bounded bounded : bounds) {
      Bounds b = bounded.bounds();
      if (b != null) {
        list.add(b.getLower());
        list.add(b.getUpper());
      }
    }

    return make(list);
  }

  public static Bounds merge(Bounds b, Point3D p) {
    return mergePoints(b, Collections.singletonList(p));
  }

  public static Bounds mergePoints(Bounds b, Collection<Point3D> points) {
    int xmin, ymin, zmin;
    int xmax, ymax, zmax;

    if (b == null) {
      if (points.isEmpty()) {
        return null;
      } else {
        Point3D p = points.iterator().next();
        xmin = p.getX();
        ymin = p.getY();
        zmin = p.getZ();
        xmax = p.getX();
        ymax = p.getY();
        zmax = p.getZ();
      }
    } else {
      xmin = b.getLower().getX();
      ymin = b.getLower().getY();
      zmin = b.getLower().getZ();
      xmax = b.getUpper().getX();
      ymax = b.getUpper().getY();
      zmax = b.getUpper().getZ();
    }


    for (Point3D p : points) {
      xmin = Math.min(xmin, p.getX());
      ymin = Math.min(ymin, p.getY());
      zmin = Math.min(zmin, p.getZ());
      xmax = Math.max(xmax, p.getX());
      ymax = Math.max(ymax, p.getY());
      zmax = Math.max(zmax, p.getZ());
    }

    return new Bounds(new Point3D(xmin, ymin, zmin), new Point3D(xmax, ymax, zmax));
  }

  public static Bounds merge(Bounds b1, Bounds b2) {
    if (b2 == null) {
      return b1;
    }
    return Bounds.mergePoints(b1, Arrays.asList(b2.getLower(), b2.getUpper()));
  }

  public static Bounds mergeBounds(Bounds b, Collection<? extends Bounded> blobs) {
    for (Bounded blob : blobs) {
      if (blob != null) {
        b = Bounds.merge(b, blob.bounds());
      }
    }

    return b;
  }

  public static Bounds restrict(Bounds b1, Bounds b2) {
    if (b1 == null || b2 == null) {
      return null;
    }
    return makeRestrict(maxPoint(b1.getLower(), b2.getLower()), minPoint(b1.getUpper(), b2.getUpper()));
  }

  public static Bounds restrict(Bounds b, Collection<? extends Bounded> blobs) {
    for (Bounded blob : blobs) {
      b = restrict(b, blob.bounds());
    }

    return b;
  }

  public static Bounds makeRestrict(Point3D lower, Point3D upper) {
    if (lower.getX() > upper.getX() || lower.getY() > upper.getY() || lower.getZ() > upper.getZ()) {
      return null;
    }
    return new Bounds(lower, upper);
  }

  private static Point3D maxPoint(Point3D p1, Point3D p2) {
    return new Point3D(
        Math.max(p1.getX(), p2.getX()),
        Math.max(p1.getY(), p2.getY()),
        Math.max(p1.getZ(), p2.getZ())
    );
  }

  private static Point3D minPoint(Point3D p1, Point3D p2) {
    return new Point3D(
        Math.min(p1.getX(), p2.getX()),
        Math.min(p1.getY(), p2.getY()),
        Math.min(p1.getZ(), p2.getZ())
    );
  }

  public static Bounds lowerRestrict(Bounds b, Point3D p) {
    return makeRestrict(maxPoint(b.getLower(), p), b.getUpper());
  }

  public static Bounds upperRestrict(Bounds b, Point3D p) {
    return makeRestrict(b.getLower(), minPoint(p, b.getUpper()));
  }

  @Override
  public Bounds bounds() {
    return this;
  }

  @Override
  public String toString() {
    return "Bounds(" + lo + ", " + hi + ")";
  }
}
