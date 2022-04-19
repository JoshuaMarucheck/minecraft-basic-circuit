package graph;

import java.util.*;

/**
 * Read only and protected, ideally
 */
public class TwoWayDirectedGraph<T> {
  protected DirectedGraph<T> forward;
  protected DirectedGraph<T> backward;

  public TwoWayDirectedGraph(DirectedGraph<T> graph) {
    forward = graph.copy();
    backward = graph.invert();
  }

//  private TwoWayDirectedGraph(DirectedGraph forward, DirectedGraph backward) {
//    this.forward = forward;
//    this.backward = backward;
//  }
//
//  public TwoWayDirectedGraph copy() {
//    return new TwoWayDirectedGraph(forward.copy(), backward.copy());
//  }

  public boolean hasEdge(Edge<T> edge) {
    return forward.hasEdge(edge);
  }

  public Iterator<Edge<T>> getEdges() {
    return forward.getEdges();
  }

  /**
   * @return a Set view of the nodes in this graph
   */
  public Set<T> nodes() {
    return forward.nodes();
  }

  public Set<T> outNeighborhood(T node) {
    return forward.outNeighborhood(node);
  }

  public Set<T> inNeighborhood(T node) {
    return backward.outNeighborhood(node);
  }

  public int size() {
    return forward.size();
  }

  /**
   * @return The set of nodes which aren't affected by the value of any other node
   */
  public Set<T> inputs() {
    return backward.getNodesWithoutOutput();
  }

  /**
   * @return The set of nodes do not affect the value of any other node
   */
  public Set<T> outputs() {
    return forward.getNodesWithoutOutput();
  }

  public Set<T> traceForward(T[] input) {
    return forward.trace(input);
  }

  public Set<T> traceBackward(T[] input) {
    return backward.trace(input);
  }

  /**
   * Computes the distance from the given node to each other node.
   * Follows forward and backward links.
   *
   * Doesn't compute distances greater than or equal to maxDist. (Make it {@code null} to compute all distances.)
   *
   */
  public Map<T, Integer> distanceMap(T start, Integer maxDist) {
    HashMap<T, Integer> r = new HashMap<>();

    int dist = 0;

    Stack<T> layer = new Stack<>();
    layer.push(start);

    while (!layer.isEmpty() && (maxDist == null || dist < maxDist)) {
      Stack<T> newLayer = new Stack<>();
      for (T node : layer) {
        if (!r.containsKey(node)) {
          r.put(node, dist);
          newLayer.addAll(outNeighborhood(node));
          newLayer.addAll(inNeighborhood(node));
        }
      }
      layer = newLayer;
      dist++;
    }

    return r;
  }

  public Map<T, Integer> distanceMap(T start) {
    return distanceMap(start, null);
  }
}
