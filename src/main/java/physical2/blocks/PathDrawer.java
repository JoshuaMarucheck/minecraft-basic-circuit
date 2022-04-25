package physical2.blocks;

import circuit.Pair;
import graph.Edge;
import graph.OrderedEdgeIterable;
import graph.TwoWayDirectedGraph;
import physical2.tiny.BentPath;
import physical2.tiny.VariableSignalPosMap;
import physical2.two.Point2D;
import physical2.two.Side;

import java.util.HashMap;
import java.util.Map;

public class PathDrawer<T> {
  private VariableSignalPosMap<T> varPosMap;
  private Map<Integer, Map<Point2D, Pair<Side, Side>>> paths;

  public PathDrawer(VariableSignalPosMap<T> varPosMap) {
    this.varPosMap = varPosMap;
  }

  public PathDrawer<T> makeLinear(VariableSignalPosMap<T> varPosMap, TwoWayDirectedGraph<T> graph) {
    PathDrawer<T> r = new PathDrawer<>(varPosMap);
    r.placeAllLinear(graph);
    return r;
  }

  // TODO: add makeQuadratic

  private void placeAllLinear(TwoWayDirectedGraph<T> graph) {
    int z = 0;
    for (Edge<T> torch : new OrderedEdgeIterable<>(graph)) {
      addPathUnsafe(z, varPosMap.getPath(torch));
      z++;
    }
  }

  private boolean pathOverlap(Integer z, BentPath path) {
    if (!paths.containsKey(z)) {
      return false;
    }

    Map<Point2D, Pair<Side, Side>> layer = paths.get(z);

    for (Pair<Point2D, Pair<Side, Side>> pair : path) {
      Point2D point = pair.getFirst();

      if (layer.containsKey(point)) {
        return true;
      }
    }
    return false;
  }

  private void addPathUnsafe(Integer z, BentPath path) {
    Map<Point2D, Pair<Side, Side>> layer = ensureLayer(z);

    for (Pair<Point2D, Pair<Side, Side>> pair : path) {
      layer.put(pair.getFirst(), pair.getSecond());
    }
  }

  private Map<Point2D, Pair<Side, Side>> ensureLayer(Integer z) {
    if (!paths.containsKey(z)) {
      paths.put(z, new HashMap<>());
    }
    return paths.get(z);
  }


}
