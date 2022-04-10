package graph;

import java.util.Iterator;
import java.util.Set;

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

  public Iterator<Edge<T>> getEdges() {
    return forward.getEdges();
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


}
