package physical.transforms;

import physical.things.Point3D;

import java.util.function.Function;

public class Offset implements Function<Point3D, Point3D> {
  private int x, y, z;

  public Offset(Point3D p) {
    x = p.getX();
    y = p.getY();
    z = p.getZ();
  }

  public Offset(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Point3D toPoint() {
    return new Point3D(x, y, z);
  }

  @Override
  public Point3D apply(Point3D p) {
    return new Point3D(p.getX() + x, p.getY() + y, p.getZ() + z);
  }
}
