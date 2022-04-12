package physical.edges;

import circuit.Pair;
import graph.Edge;
import graph.GenerativeDirectedGraph;
import graph.IntegerIterable;
import graph.TwoWayDirectedGraph;
import physical.blocks.AnnotatedPathDrawer;
import physical.things.Bounds;
import physical.things.Point3D;

import java.util.*;
import java.util.function.Function;

/**
 * Note, we are placing edges at positions!
 *
 * @param <E> original node type in the base circuit
 * @param <T> generative node type
 */
public class TorchPlacer<E, T> {
  /**
   * Each node is a torch.
   * Each edge is a connection between two torches, which must be at most 8 distance.
   */
  private TwoWayDirectedGraph<T> edgeGraph;
  private Map<Edge<E>, T> edgeIds;
  private Map<T, Point3D> torchPositions;
  private Set<Point3D> consumedPositions;
  private Integer maxKnownDist;

  public TorchPlacer(TwoWayDirectedGraph<E> circuit, Iterable<T> generator) {
    Pair<TwoWayDirectedGraph<T>, Map<Edge<E>, T>> pair = invertNodesAndEdges(circuit, generator);
    edgeGraph = pair.getFirst();
    edgeIds = pair.getSecond();
    torchPositions = new HashMap<>();
    consumedPositions = new HashSet<>();
    maxKnownDist = -1;
  }

  public static <V> TorchPlacer<V, Integer> make(TwoWayDirectedGraph<V> circuit) {
    return new TorchPlacer<>(circuit, new IntegerIterable());
  }

  /**
   * We need to transform the edges (torches) and nodes (blobs) to the form a TorchPlacer expects.
   * <p>
   * Also, we need a way to do input and output. One input/output blob becomes many disparate edges,
   * so ideally, we would insert one extra edge immediately before any input and immediately after any output.
   * <p>
   * Assumes that all nodes without input are input nodes, and inserts a torch before them to attach a lever to.
   * Similarly with output nodes.
   */
  private Pair<TwoWayDirectedGraph<T>, Map<Edge<E>, T>> invertNodesAndEdges(TwoWayDirectedGraph<E> graph, Iterable<T> generator) {
    GenerativeDirectedGraph<T> oneWayEdgeGraph = new GenerativeDirectedGraph<>(generator);
    Map<Edge<E>, T> edgeIds = new HashMap<>();

    for (Iterator<Edge<E>> it = graph.getEdges(); it.hasNext(); ) {
      Edge<E> edgeItem = it.next();
      T edgeId = oneWayEdgeGraph.addNode();
      edgeIds.put(edgeItem, edgeId);
    }

    for (Iterator<Edge<E>> it = graph.getEdges(); it.hasNext(); ) {
      Edge<E> edgeItem = it.next();
      T edgeId = edgeIds.get(edgeItem);

      Set<E> inNeighborhood = graph.inNeighborhood(edgeItem.getStart());
      if (inNeighborhood.isEmpty()) {
        oneWayEdgeGraph.addEdge(new Edge<>(
            null,
            edgeId
        ));
      } else {
        for (E e : inNeighborhood) {
          Edge<E> inputEdge = new Edge<>(e, edgeItem.getStart());
          oneWayEdgeGraph.addEdge(new Edge<>(
              edgeIds.get(inputEdge),
              edgeId
          ));
        }
      }

      Set<E> outNeighborhood = graph.outNeighborhood(edgeItem.getEnd());
      if (outNeighborhood.isEmpty()) {
        oneWayEdgeGraph.addEdge(new Edge<>(
            edgeId,
            null
        ));
      } else {
        for (E e : outNeighborhood) {
          Edge<E> outputEdge = new Edge<>(edgeItem.getEnd(), e);
          oneWayEdgeGraph.addEdge(new Edge<>(
              edgeId,
              edgeIds.get(outputEdge)
          ));
        }
      }
    }

    return new Pair<>(new TwoWayDirectedGraph<>(oneWayEdgeGraph), edgeIds);
  }

  /**
   * Computes the min distance from the given point to each
   * other placed point (and maybe some other points as well).
   * <p>
   * There are no negative cycles, so we don't need to be smart about how we go about this.
   */
  private Map<T, Integer> distanceMap(T torch) {
    /* distance needs to be able to go up to maxKnownDist+1,
     * to allow for one extra node on the frontier
     * (noting that all placed nodes have a bounded distance).
     *
     * hence, cons
     */
    if (maxKnownDist == null) {
      return edgeGraph.distanceMap(torch);
    } else {
      return edgeGraph.distanceMap(torch, maxKnownDist + 2);
    }
  }

  /**
   * Pointwise functions
   */
  private static Function<Point3D, Point3D> toDiag =
      p -> new Point3D(p.getX() + p.getZ(), p.getY(), p.getX() - p.getZ());
  private static Function<Point3D, Point3D> fromDiag =
      p -> new Point3D((p.getX() + p.getZ()) / 2, p.getY(), (p.getX() - p.getZ()) / 2);


  private Bounds localDiagonalBounds(int graphStepCount, T placedPoint) {
    Point3D p = torchPositions.get(placedPoint);

    Point3D pDiag = toDiag.apply(p);

    int len = AnnotatedPathDrawer.HIGH_STRENGTH * graphStepCount;
    return Bounds.make(
        pDiag.translate(-len, -len, -len),
        pDiag.translate(len, len, len)
    );
  }

  public boolean hasPlacedTorch() {
    return !torchPositions.isEmpty();
  }

  /**
   * These bounds do not correspond directly to physical space.
   * Instead, the first item is a limit on x+z,
   * and the third item is a limit on x-z.
   * The second item is a limit on y.
   */
  private Bounds validDiagonalPositions(T torch) {
    if (torchPositions.isEmpty()) {
      throw new IllegalStateException("This thing is empty; just add a point");
    }
    Map<T, Integer> distances = distanceMap(torch);
    Iterator<T> placedNodeIter = torchPositions.keySet().iterator();

    Bounds b;
    {
      T placedTorch = placedNodeIter.next();
      b = localDiagonalBounds(distances.get(placedTorch), placedTorch);
    }

    while (placedNodeIter.hasNext()) {
      T placedTorch = placedNodeIter.next();
      Bounds localBounds = localDiagonalBounds(distances.get(placedTorch), placedTorch);
      b = Bounds.restrict(b, localBounds);
    }

    return b;
  }

  /**
   * Eliminates as many invalid positions as possible from a diagonally oriented Bounds
   */
  public static Bounds restrictToMapDiag(Bounds b) {
    // Basically, y \in [0,256) and x+z >= 2
    // x >= 2-z
    // x >= 2-zmin,  x >= 2-zmax
    // x >= 2-zmin is more restrictive
    // Then if x is lower bounded, max that lower bound with 2-zmin.
    // Same for z >= 2-xmin

    Bounds other = Bounds.make(
        new Point3D(2 - b.getLower().getZ(), 0, 2 - b.getLower().getX()),
        new Point3D(b.getUpper().getX(), 255, b.getUpper().getZ())
    );

    return Bounds.restrict(b, other);
  }

  /**
   * Eliminates as many invalid positions as possible from a orthogonally oriented Bounds
   */
  public static Bounds restrictToMap(Bounds b) {
    Bounds other = Bounds.make(
        new Point3D(1, 0, b.getLower().getZ()),
        new Point3D(b.getUpper().getX(), 255, b.getUpper().getZ())
    );

    return Bounds.restrict(b, other);
  }

  public static boolean isValidPos(Point3D p) {
    return 0 < p.getX() && 0 <= p.getY() && p.getY() < 255;
  }

  /**
   * @return The {@code T} object
   */
  public T edgeToTorch(Edge<E> edge) {
    return edgeIds.get(edge);
  }

  public void forcePlaceTorch(T torch, Point3D pos) {
    torchPositions.put(torch, pos);
    consumedPositions.add(pos);
  }

  public void attemptToPlaceTorch(T torch) {
    if (torchPositions.containsKey(torch)) {
      return;
    }
    if (!hasPlacedTorch()) {
      forcePlaceTorch(torch, new Point3D(0, 0, 0));
    }
    Bounds positionBounds = validDiagonalPositions(torch);

    Point3D interior;
    do {
      interior = positionBounds.randomInteriorPoint();
    } while (consumedPositions.contains(interior));

    forcePlaceTorch(torch, fromDiag.apply(interior));
  }

  public void placeTorchesRandomly() {
    for (T torch : edgeGraph.nodes()) {
      attemptToPlaceTorch(torch);
    }
  }
}
