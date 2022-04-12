package physical.edges;

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
 * @param <T> generative node type
 */
public class TorchPlacer<T> {
  /**
   * Each node is a torch.
   * Each edge is a connection between two torches, which must be at most 8 distance.
   */
  private TwoWayDirectedGraph<T> edgeGraph;
  private Map<T, Point3D> torchPositions;
  private Set<Point3D> consumedPositions;
  private Integer maxKnownDist;

  public TorchPlacer(TwoWayDirectedGraph<?> circuit, Iterable<T> generator) {
    edgeGraph = invertNodesAndEdges(circuit, generator);
    torchPositions = new HashMap<>();
    consumedPositions = new HashSet<>();
    maxKnownDist = -1;
  }

  public static TorchPlacer<Integer> make(TwoWayDirectedGraph<?> circuit) {
    return new TorchPlacer<>(circuit, new IntegerIterable());
  }

  /**
   * We need to transform the edges (torches) and nodes (blobs) to the form a TorchPlacer expects.
   */
  private <E> TwoWayDirectedGraph<T> invertNodesAndEdges(TwoWayDirectedGraph<E> graph, Iterable<T> generator) {
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

      for (E e : graph.inNeighborhood(edgeItem.getStart())) {
        Edge<E> inputEdge = new Edge<>(e, edgeItem.getStart());
        oneWayEdgeGraph.addEdge(new Edge<>(
            edgeIds.get(inputEdge),
            edgeId
        ));
      }

      for (E e : graph.outNeighborhood(edgeItem.getEnd())) {
        Edge<E> outputEdge = new Edge<>(edgeItem.getEnd(), e);
        oneWayEdgeGraph.addEdge(new Edge<>(
            edgeId,
            edgeIds.get(outputEdge)
        ));
      }
    }

    return new TwoWayDirectedGraph<>(oneWayEdgeGraph);
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

  private void forcePlaceTorch(T torch, Point3D pos) {
    torchPositions.put(torch, pos);
    consumedPositions.add(pos);
  }

  private void attemptToPlaceTorch(T torch) {
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
