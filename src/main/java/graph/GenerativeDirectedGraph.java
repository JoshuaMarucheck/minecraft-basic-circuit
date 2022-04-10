package graph;

import java.util.*;

/**
 * A wrapper for DirectedGraph, allowing you to hand it
 * an iterator when you create it instead of needing to
 * generate each node yourself every time you add a node.
 */
public class GenerativeDirectedGraph<T> extends DirectedGraph<T> {
  private Iterator<T> generator;
  private Iterable<T> vertexSpace;

  public GenerativeDirectedGraph(Iterable<T> vertices) {
    this(new HashMap<>(), vertices);
  }

  public GenerativeDirectedGraph(Map<T, Set<T>> adjList, Iterable<T> vertices) {
    super(adjList);
    vertexSpace = vertices;
    generator = vertexSpace.iterator();
  }

  /**
   * @return A new empty DirectedGraph with the same vertex space.
   */
  public GenerativeDirectedGraph<T> copyBase() {
    return new GenerativeDirectedGraph<>(vertexSpace);
  }

  public static GenerativeDirectedGraph<Integer> integerBase() {
    return new GenerativeDirectedGraph<>(new IntegerIterable());
  }

  public T addNode() {
    T node = generator.next();
    adjList.put(node, new HashSet<>());
    return node;
  }

  public void ensureSize(int size) {
    while (adjList.size() < size) {
      this.addNode();
    }
  }
}
