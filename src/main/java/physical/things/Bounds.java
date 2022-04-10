package physical.things;

import java.util.Collection;
import java.util.Collections;

/**
 * Invariant: The values in {@code getLower()} are actually lower than the values in {@code getHigher()}
 */
public class Bounds {
  private Point3D lo;
  private Point3D hi;

  private Bounds(Point3D lower, Point3D upper) {
    lo = lower;
    hi = upper;
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
      return Bounds.make(new Point3D(0, 0, 0), new Point3D(0, 0, 0));
    }
    Bounds b = Bounds.make(points.iterator().next());
    return b.merge(points);
  }


  public Point3D getLower() {
    return lo;
  }

  public Point3D getUpper() {
    return hi;
  }

  public Bounds merge(Point3D p) {
    return merge(Collections.singletonList(p));
  }

  public Bounds merge(Collection<Point3D> points) {
    int xmin = lo.getX(), ymin = lo.getY(), zmin = lo.getZ();
    int xmax = hi.getX(), ymax = hi.getY(), zmax = hi.getZ();

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
}
