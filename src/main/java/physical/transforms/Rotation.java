package physical.transforms;

import physical.things.Axis;
import physical.things.Point3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class Rotation implements Function<Point3D, Point3D> {
  private Axis from;
  private Axis to;

  public Rotation(Axis from, Axis to) {
    this.from = from;
    this.to = to;
  }

  public static Collection<Function<Point3D, Point3D>> horizontalRotations() {
    ArrayList<Function<Point3D, Point3D>> r = new ArrayList<>();

    Rotation rotation = new Rotation(Axis.X, Axis.Z);
    Function<Point3D, Point3D> func = new Identity<>();
    r.add(func);

    for (int i = 0; i < 4; i++) {
      func = func.compose(rotation);
      r.add(func);
    }

    return r;
  }

  public Function<Point3D, Point3D> consecutiveRotations(int times) {
    if (times < 0) {
      throw new IllegalArgumentException();
    }
    if (times == 0) {
      return new Identity<>();
    }
    Function<Point3D, Point3D> r = this;
    for (int i = 1; i < times; i++) {
      r = r.compose(this);
    }
    return r;
  }

  @Override
  public Point3D apply(Point3D p) {
    if (from == to) {
      return p;
    }
    int x = p.getX(), y = p.getY(), z = p.getZ(), tmp;

    switch (from) {
      case X:
        tmp = p.getX();
        break;
      case Y:
        tmp = p.getY();
        break;
      case Z:
        tmp = p.getZ();
        break;
      default:
        throw new IllegalStateException("Unknown Axis enum value " + from);
    }

    switch (to) {
      case X:
        x = tmp;
        tmp = p.getX();
        break;
      case Y:
        y = tmp;
        tmp = p.getY();
        break;
      case Z:
        z = tmp;
        tmp = p.getZ();
        break;
      default:
        throw new IllegalStateException("Unknown Axis enum value " + from);
    }

    switch (from) {
      case X:
        x = -tmp;
        break;
      case Y:
        y = -tmp;
        break;
      case Z:
        z = -tmp;
        break;
      default:
        throw new IllegalStateException("Unknown Axis enum value " + from);
    }

    return new Point3D(x, y, z);
  }
}
