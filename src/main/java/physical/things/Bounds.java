package physical.things;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * Invariant: The values in {@code getLower()} are actually lower than the values in {@code getHigher()}
 *
 * {@code null} means there are no points contained in the Bounds.
 * All functions are null-safe in this sense.
 *
 * Merge: finds a region which contains all given points
 * Restrict: finds a region which all given ranges contain
 */
public class Bounds implements Bounded {
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

  public static Bounds make(Point3D p) {
    return new Bounds(p, p);
  }

  public static Bounds make(Point3D lower, Point3D upper) {
    int x1 = lower.getX();
    int y1 = lower.getY();
    int z1 = lower.getZ();
    int x2 = upper.getX();
    int y2 = upper.getY();
    int z2 = upper.getZ();

    return new Bounds(
        new Point3D(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2)),
        new Point3D(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2))
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
    if (b2 == null){
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

  @Override
  public Bounds bounds() {
    return this;
  }
}
