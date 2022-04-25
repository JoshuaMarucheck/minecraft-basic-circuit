package physical2.tiny;

import graph.Edge;
import graph.TwoWayDirectedGraph;
import physical2.two.Point2D;

import java.util.*;

/**
 * Assigns each variable to a position.
 * <p>
 * Represents the tiny scale version of positions. Everything will be scaled up by x5 at some point later.
 *
 * @param <T> graph element type
 */
public class VariableSignalPosMap<T> {
  private TwoWayDirectedGraph<T> redstone;
  private Map<T, Point2D> posMap;
  private Set<Point2D> consumedPoints;
  private Iterator<Point2D> legalPositions;

  /**
   *
   * @param redstone
   * @param legalPositions An infinite iterable
   */
  public VariableSignalPosMap(TwoWayDirectedGraph<T> redstone, Iterable<Point2D> legalPositions) {
    this.redstone = redstone;
    this.posMap = new HashMap<>();
    this.legalPositions = legalPositions.iterator();
    this.consumedPoints = new HashSet<>();
  }

  public void put(T blob, Point2D pos) {
    posMap.put(blob, pos);
  }

  public Point2D getPos(T blob) {
    return posMap.get(blob);
  }

  public Point2D assignToNext(T blob) {
    Point2D p;
    do {
      p = legalPositions.next();
    } while (consumedPoints.contains(p));
    put(blob, p);
    return p;
  }

  public BentPath getPath(Edge<T> torch) {
    ensurePlaced(torch.getStart());
    ensurePlaced(torch.getEnd());

    return new BentPath(getPos(torch.getStart()), getPos(torch.getEnd()));
  }

  private void ensurePlaced(T t) {
    if (getPos(t) == null) {
      assignToNext(t);
    }
  }
}
