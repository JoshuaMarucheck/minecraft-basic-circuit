package circuit;

import graph.DirectedGraph;
import graph.Edge;
import graph.TwoWayDirectedGraph;

import java.util.*;

/**
 * @param <T> Node type.
 */
public class GraphMerger<T> {
  private Map<T, Set<T>> forwards;
  private Map<T, Set<T>> backwards;
  /**
   * Invariant: the canonical items in {@code nodeMerger} are exactly
   * the nodes with mappings in {@code forwards} and {@code backwards}.
   */
  private Merger<T> nodeMerger;

  public GraphMerger(TwoWayDirectedGraph<T> graph) {
    forwards = new HashMap<>();
    backwards = new HashMap<>();
    nodeMerger = new Merger<>();

    for (T node : graph.nodes()) {
      ensureNode(node);
    }

    for (Iterator<Edge<T>> it = graph.getEdges(); it.hasNext(); ) {
      addEdge(it.next());
    }
  }

  /**
   * Guarantees that {@code node} is a node in this graph.
   */
  public void ensureNode(T node) {
    ensureMapping(forwards, node);
    ensureMapping(backwards, node);
  }

  private void addEdge(T from, T to) {
    ensureMapping(forwards, from).add(to);
    ensureMapping(backwards, to).add(from);
  }

  private void addEdge(Edge<T> edge) {
    addEdge(edge.getStart(), edge.getEnd());
  }

  private static <T> Set<T> ensureMapping(Map<T, Set<T>> map, T node) {
    if (!map.containsKey(node)) {
      map.put(node, new HashSet<>());
    }
    return map.get(node);
  }

  /**
   * Deletes {@code node} from this graph, as well as all edges connected to it.
   */
  public void removeNode(T node) {
    removeNode(node, true);
  }

  /**
   * Deletes {@code node} from this graph, as well as all edges connected to it.
   */
  private void removeNode(T node, boolean removeFromMerger) {

    for (T t : forwards.get(node)) {
      if (!backwards.get(t).remove(node)) {
        throw new IllegalStateException("forwards and backwards did not reflect each other");
      }
    }

    for (T t : backwards.get(node)) {
      if (!forwards.get(t).remove(node)) {
        throw new IllegalStateException("forwards and backwards did not reflect each other");
      }
    }

    forwards.remove(node);
    backwards.remove(node);
    if (removeFromMerger) {
      nodeMerger.delete(node);
    }
  }

  /**
   * Makes {@code node1} and {@code node2} represent the same node.
   * That is, all inputs and outputs of either node become inputs
   * and outputs of the new merged node.
   */
  public void mergeNodes(T node1, T node2) {
    if (nodeMerger.isDeleted(node1)) {
      if (!nodeMerger.isDeleted(node2)) {
        removeNode(node2);
      }
    } else if (nodeMerger.isDeleted(node2)) {
      removeNode(node1);
    } else {
      node1 = nodeMerger.getMapping(node1);
      node2 = nodeMerger.getMapping(node2);

      mergeNodesUnsafe(node1, node2);
    }
  }

  /**
   * Both params should already be put through {@code nodeMerger.getMapping}.
   */
  private void mergeNodesUnsafe(T node1, T node2) {
    nodeMerger.merge(node1, node2);

    T to = nodeMerger.getMapping(node2);
    T from = Objects.equals(node1, to) ? node2 : node1;

    for (T t : forwards.get(from)) {
      addEdge(to, t);
    }

    for (T t : backwards.get(from)) {
      addEdge(t, to);
    }

    removeNode(from, false);
  }

  /**
   * Forwards the signal passing through the given edge, so that it's done in one connection instead of three.
   * Since all relevant signals now skip this edge, the edge and its endpoints are removed from the graph.
   */
  public void forwardEdge(T from, T to) {
    from = getMapping(from);
    to = getMapping(to);

    if (!hasEdge(from, to)) {
      throw new IllegalArgumentException(new Edge<>(from, to) + " is not an edge in this graph!");
    }
    if (outNeighborhood(from).size() != 1 || inNeighborhood(to).size() != 1) {
      throw new IllegalArgumentException(new Edge<>(from, to) + " is not lonely with respect to its input and output nodes.");
    }

    for (T input : inNeighborhood(from)) {
      for (T output: outNeighborhood(to)) {
        addEdge(input, output);
      }
    }
    removeNode(from);
    removeNode(to);
  }

  /**
   * @return A {@code DirectedGraph<T>} which is a view of this GraphMerger.
   */
  public DirectedGraph<T> toDirectedGraphView() {
    return new DirectedGraph<>(forwards);
  }

  public Set<T> nodes() {
    return forwards.keySet();
  }

  public Set<T> inNeighborhood(T t) {
    return backwards.get(getMapping(t));
  }

  public Set<T> outNeighborhood(T t) {
    return forwards.get(getMapping(t));
  }

  public boolean hasEdge(Edge<T> edge){
    return hasEdge(edge.getStart(), edge.getEnd());
  }

  public boolean hasEdge(T from, T to) {
    return forwards.containsKey(getMapping(from)) && forwards.get(getMapping(from)).contains(getMapping(to));
  }

  public boolean isDeleted(T node) {
    return nodeMerger.isDeleted(node);
  }

  public boolean isSelfMapped(T node) {
    return nodeMerger.isSelfMapped(node);
  }

  public T getMapping(T node) {
    return nodeMerger.getMapping(node);
  }
}
