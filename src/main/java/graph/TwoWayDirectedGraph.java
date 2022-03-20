package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Read only and protected, ideally
 */
public class TwoWayDirectedGraph {
  private DirectedGraph forward;
  private DirectedGraph backward;

  public TwoWayDirectedGraph(DirectedGraph graph) {
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

  public Iterator<Edge> getEdges() {
    return forward.getEdges();
  }

  public Set<Integer> outNeighborhood(int node) {
    return forward.outNeighborhood(node);
  }

  public Set<Integer> inNeighborhood(int node) {
    return backward.outNeighborhood(node);
  }

  public int size() {
    return forward.size();
  }

  /**
   * @return The set of nodes which aren't affected by the value of any other node
   */
  public Set<Integer> inputs() {
    return backward.getNodesWithoutOutput();
  }

  /**
   * @return The set of nodes do not affect the value of any other node
   */
  public Set<Integer> outputs() {
    return forward.getNodesWithoutOutput();
  }

  public Set<Integer> traceForward(Integer[] input) {
    return forward.trace(input);
  }

  public Set<Integer> traceBackward(Integer[] input) {
    return backward.trace(input);
  }


}
