package physical.things;

import java.util.Iterator;

/**
 * Immutable
 */
public class Point3D {
  private int x;
  private int y;
  private int z;

  public Point3D(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getZ() {
    return z;
  }

  public Point3D negate() {
    return new Point3D(-x, -y, -z);
  }

  public Point3D translate(int x, int y, int z) {
    return new Point3D(this.x + x, this.y + y, this.z + z);
  }

  public static int taxicabDistance(Point3D p1, Point3D p2) {
    return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y) + Math.abs(p1.z - p2.z);
  }

  public static int verticalDistance(Point3D p1, Point3D p2) {
    return Math.abs(p1.y - p2.y);
  }

  public static int horizontalTaxicabDistance(Point3D p1, Point3D p2) {
    return Math.abs(p1.x - p2.x) + Math.abs(p1.z - p2.z);
  }

  public static boolean arePointsAdjacent(Point3D p1, Point3D p2) {
    return taxicabDistance(p1, p2) == 1;
  }

  public static boolean arePointsHorizAdjacent(Point3D p1, Point3D p2) {
    return horizontalTaxicabDistance(p1, p2) == 1 && verticalDistance(p1, p2) == 0;
  }

  public Iterator<Point3D> horizontalAdjacentPoints() {
    return new AdjIter(4);
  }

  public Iterator<Point3D> adjacentPoints() {
    return new AdjIter(6);
  }

  public Point3D translate(Point3D p) {
    return this.translate(p.getX(), p.getY(), p.getZ());
  }

  private class AdjIter implements Iterator<Point3D> {
    private int i;
    private int limit;

    private AdjIter(int limit) {
      i = 0;
      this.limit = limit;
    }

    @Override
    public boolean hasNext() {
      return i <= limit;
    }

    @Override
    public Point3D next() {
      Point3D r;
      switch (i) {
        case 0:
          r = translate(1, 0, 0);
          break;
        case 1:
          r = translate(0, 0, 1);
          break;
        case 2:
          r = translate(-1, 0, 0);
          break;
        case 3:
          r = translate(0, 0, -1);
          break;
        case 4:
          r = translate(0, 1, 0);
          break;
        case 5:
          r = translate(0, -1, 0);
          break;
        default:
          throw new IllegalStateException("Iterator variable somehow out of range");
      }
      i++;
      return r;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Point3D) {
      return ((Point3D) obj).x == x && ((Point3D) obj).y == y && ((Point3D) obj).z == z;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 31 * 37 * Integer.hashCode(x) + 31 * Integer.hashCode(y) + Integer.hashCode(z);
  }

  public String toString() {
    return "(" + x + ", " + y + ", " + z + ")";
  }

  public String toStringWithoutSpaces() {
    return x + "," + y + "," + z;
  }
}
