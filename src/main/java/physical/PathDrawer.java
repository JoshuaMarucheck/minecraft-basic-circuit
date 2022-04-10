package physical;

import graph.Edge;
import graph.TwoWayDirectedWritableGraph;
import physical.things.Bounds;
import physical.things.Point3D;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Function;

public class PathDrawer implements Iterable<Edge<Point3D>> {
  private final HashSet<Point3D> points;
  private final TwoWayDirectedWritableGraph<Point3D> connections;

  public PathDrawer(HashSet<Point3D> points) {
    this.points = points;
    connections = new TwoWayDirectedWritableGraph<>();
  }

  public PathDrawer() {
    this(new HashSet<>());
  }

  public void addPoint(Point3D p) {
    points.add(p);
  }

  public void addConnection(Point3D from, Point3D to) {
    points.add(from);
    points.add(to);
    connections.addEdge(new Edge<>(from, to));
    connections.addEdge(new Edge<>(to, from));
  }

  public Bounds bounds() {
    return Bounds.make(points);
  }

  public Iterator<Edge<Point3D>> iterator() {
    return connections.getEdges();
  }

  public Iterator<Point3D> pointIterator() {
    return points.iterator();
  }

  public PathDrawer transform(Function<Point3D, Point3D> f) {
    PathDrawer r = new PathDrawer();
    for (Point3D p : points) {
      r.addPoint(f.apply(p));
    }
    for (Iterator<Edge<Point3D>> it = connections.getEdges(); it.hasNext(); ) {
      Edge<Point3D> edge = it.next();
      r.addConnection(
          f.apply(edge.getStart()),
          f.apply(edge.getEnd())
      );
    }
    return r;
  }

  /**
   * @return A map from point to the signal distance from the specified point to that point
   */
  public HashMap<Point3D, Integer> propagateSignal(Point3D point) {
    int i = 0;
    HashMap<Point3D, Integer> distanceMap = new HashMap<>();
    distanceMap.put(point, i);

    HashSet<Point3D> layer = new HashSet<>();
    layer.add(point);

    while (!layer.isEmpty()) {
      HashSet<Point3D> newLayer = new HashSet<>();
      for (Point3D layerPoint : layer) {
        distanceMap.put(layerPoint, i);
        for (Point3D p : connections.outNeighborhood(layerPoint)) {
          if (!distanceMap.containsKey(p)) {
            newLayer.add(p);
          }
        }
        layer = newLayer;
        i++;
      }
    }

    return distanceMap;
  }

  private static class ArbitraryPointOrder implements Comparator<Point3D> {

    @Override
    public int compare(Point3D o1, Point3D o2) {
      if (o1.getX() != o2.getX()) {
        return o2.getX() - o1.getX();
      }
      if (o1.getY() != o2.getY()) {
        return o2.getY() - o1.getY();
      }
      if (o1.getZ() != o2.getZ()) {
        return o2.getZ() - o1.getZ();
      }
      return 0;
    }
  }
}
