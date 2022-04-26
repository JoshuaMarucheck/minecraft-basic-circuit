package physical2.tiny;

import physical2.two.Point2D;
import physical2.two.Side;

import static physical2.two.Side.*;

/**
 * Represents points on edges
 */
public class EdgePoint {

  private Point2D point;
  private Side side;

  public EdgePoint(Point2D point, Side side) {
    this.point = point;
    this.side = side;
  }

  public Side getSide() {
    return side;
  }

  public Point2D getPoint() {
    return point;
  }

  /**
   * @param point A zoomed in point
   * @return A full sized point
   */
  public static EdgePoint zoomOut(Point2D point) {
    Point2D p = new Point2D(point.getX() / 2, point.getY() / 2);
    Side side;
    if (!isEven(point.getX())) {
      side = DOWN;
      if (!isEven(point.getY())) {
        throw new IllegalArgumentException("Point not on edge");
      }
    } else {
      side = LEFT;
      if (isEven(point.getY())) {
        throw new IllegalArgumentException("Point not on edge");
      }
    }
    return new EdgePoint(p, side);
  }

  private static boolean isEven(int a) {
    return a % 2 == 0;
  }

  public EdgePoint altPointSide() {
    switch (side) {
      case UP:
        return new EdgePoint(point.translate(0, 1), DOWN);
      case DOWN:
        return new EdgePoint(point.translate(0, -1), UP);
      case RIGHT:
        return new EdgePoint(point.translate(1, 0), LEFT);
      case LEFT:
        return new EdgePoint(point.translate(-1, 0), RIGHT);
      default:
        throw new IllegalArgumentException("Illegal side " + side);
    }
  }

  public Point2D zoomIn() {
    int x = -1;
    int y = -1;

    switch (side) {
      case UP:
        x = 1;
        y = 2;
        break;
      case DOWN:
        x = 1;
        y = 0;
        break;
      case LEFT:
        x = 0;
        y = 1;
        break;
      case RIGHT:
        x = 2;
        y = 1;
        break;
    }

    return new Point2D(point.getX() * 2 + x, point.getY() * 2 + y);
  }
}
