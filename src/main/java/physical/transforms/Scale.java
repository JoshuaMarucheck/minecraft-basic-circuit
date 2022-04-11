package physical.transforms;

import physical.things.Point3D;

import java.util.function.Function;

public class Scale implements Function<Point3D, Point3D> {
  private int scale;

  public Scale(int scale) {
    this.scale = scale;
  }

  @Override
  public Point3D apply(Point3D p) {
    return new Point3D(p.getX() * scale, p.getY() * scale, p.getZ() * scale);
  }
}
