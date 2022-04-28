package physical2.blocks;

import circuit.Pair;
import graph.Edge;
import graph.OrderedEdgeIterable;
import graph.TwoWayDirectedGraph;
import physical.things.Bounded;
import physical.things.Bounds;
import physical.things.Point3D;
import physical2.one.Range;
import physical2.tiny.BentPath;
import physical2.tiny.VariableSignalPosMap;
import physical2.two.Point2D;

import java.util.*;

public class PathAccumulator<T> implements Bounded {
  private VariableSignalPosMap<T> varPosMap;
  private Map<Integer, Set<Point2D>> consumedPoints;
  private Map<Integer, Collection<BentPath>> paths;
  private Map<Point2D, Pair<Range, Range>> zRange;

  public PathAccumulator(VariableSignalPosMap<T> varPosMap) {
    this.varPosMap = varPosMap;
    paths = new HashMap<>();
    zRange = new HashMap<>();
    consumedPoints = new HashMap<>();
  }

  public VariableSignalPosMap<T> getVarPosMap() {
    return varPosMap;
  }

  public Map<Integer, Collection<BentPath>> getPaths() {
    return paths;
  }

  public Collection<Point2D> rangePositions() {
    return zRange.keySet();
  }

  public Range getZRange(Point2D p) {
    Pair<Range, Range> pair = zRange.get(p);
    return Range.merge(pair.getFirst(), pair.getSecond());
  }

  public static <T> PathAccumulator<T> makeLinear(VariableSignalPosMap<T> varPosMap, TwoWayDirectedGraph<T> graph) {
    PathAccumulator<T> r = new PathAccumulator<>(varPosMap);
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

    Set<Point2D> layer = consumedPoints.get(z);

    for (Pair<Point2D, SquareSpecifier> pair : path) {
      Point2D point = pair.getFirst();

      if (layer.contains(point)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Also registers the input and output
   */
  private void addPathUnsafe(Integer z, BentPath path) {
    // register input and output
    registerInputZ(z, path.getEnd());
    registerOutputZ(z, path.getStart());

    // Add blocks
    Set<Point2D> layer = ensureConsumed(z);

    for (Pair<Point2D, SquareSpecifier> pair : path) {
      layer.add(pair.getFirst());
    }
  }

  /**
   * Registers an input to the wire at position {@code p}.
   * This means this is the position of the output of a BentPath.
   */
  private void registerInputZ(Integer z, Point2D p) {
    if (zRange.containsKey(p)) {
      Pair<Range, Range> pair = zRange.get(p);
      Range inputs = pair.getFirst();
      Range outputs = pair.getSecond();
      if (outputs != null && z != null && z >= outputs.getLower()) {
        throw new IllegalArgumentException("Overlapping inputs and outputs");
      }
      zRange.put(p, new Pair<>(Range.merge(inputs, z), outputs));
    } else {
      zRange.put(p, new Pair<>(Range.make(z), null));
    }
  }

  private void registerOutputZ(Integer z, Point2D p) {
    if (zRange.containsKey(p)) {
      Pair<Range, Range> pair = zRange.get(p);
      Range inputs = pair.getFirst();
      Range outputs = pair.getSecond();
      if (inputs != null && z != null && z <= inputs.getUpper()) {
        throw new IllegalArgumentException("Overlapping inputs and outputs");
      }
      zRange.put(p, new Pair<>(inputs, Range.merge(outputs, z)));
    } else {
      zRange.put(p, new Pair<>(null, Range.make(z)));
    }
  }

  private Collection<BentPath> ensureLayer(Integer z) {
    if (!paths.containsKey(z)) {
      paths.put(z, new HashSet<>());
    }
    return paths.get(z);
  }

  private Set<Point2D> ensureConsumed(Integer z) {
    if (!consumedPoints.containsKey(z)) {
      consumedPoints.put(z, new HashSet<>());
    }
    return consumedPoints.get(z);
  }


  @Override
  public Bounds bounds() {
    Bounds b = null;

    for (int z : consumedPoints.keySet()) {
      Set<Point2D> layer = consumedPoints.get(z);

      for (Point2D p : layer) {
        b = Bounds.merge(b, new Point3D(p.getX(), p.getY(), z));
      }
    }

    return b;
  }
}
