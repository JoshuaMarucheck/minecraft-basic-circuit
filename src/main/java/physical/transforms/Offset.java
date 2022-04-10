package physical.transforms;

import physical.things.Point3D;

import java.util.function.Function;

public class Offset implements Function<Point3D, Point3D> {
  private int x,y,z;
  public Offset(Point3D p) {
    x = p.getX();
    y = p.getY();
    z = p.getZ();
  }

  @Override
  public Point3D apply(Point3D p) {
    return new Point3D(p.getX() + x, p.getY() + y, p.getZ() + z);
  }
}
