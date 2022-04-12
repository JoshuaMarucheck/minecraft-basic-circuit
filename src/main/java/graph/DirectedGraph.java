package graph;

import java.util.*;

/**
 * A class for representing directed graphs.
 * Nodes are type T.
 * <p>
 * The objective is to be able to add and remove nodes.
 * <p>
 * ids can be whatever. There's no way we're getting to 2 billion redstone torches.
 * <p>
 * Ideally, generator would be able to just keep generating ids.
 */
public class DirectedGraph<T> {
  protected Map<T, Set<T>> adjList;

  public DirectedGraph() {
    this(new HashMap<>());
  }

  public DirectedGraph(Map<T, Set<T>> adjList) {
    this.adjList = adjList;
  }

  /**
   * @return The number of nodes in this graph
   */
  public int size() {
    return adjList.size();
  }

  public void addNode(T node) {
    adjList.put(node, new HashSet<>());
  }

  private Map<T, Set<T>> getAdjList() {
    return adjList;
  }

  public DirectedGraph<T> invert() {
    DirectedGraph<T> graph = new DirectedGraph<>();
    for (T node : adjList.keySet()) {
      graph.addNode(node);
    }

    for (Iterator<Edge<T>> it = this.getEdges(); it.hasNext(); ) {
      Edge<T> edge = it.next();
      graph.addEdge(edge.reverse());
    }
    return graph;
  }

  /**
   * Probably make sure the graph has both endpoints as nodes first.
   */
  public void addEdge(Edge<T> edge) {
    adjList.get(edge.getStart()).add(edge.getEnd());
  }

  public Set<T> outNeighborhood(T node) {
    return adjList.get(node);
  }

  public boolean nodeHasOutput(T node) {
    return !adjList.get(node).isEmpty();
  }

  public Set<T> getNodesWithoutOutput() {
    Set<T> r = new HashSet<>();
    for (T node : adjList.keySet()) {
      if (!this.nodeHasOutput(node)) {
        r.add(node);
      }
    }
    return r;
  }

  public Set<T> nodes() {
    return adjList.keySet();
  }

  /**
   * Determines which nodes are touched if you set certain inputs
   *
   * @param inputs The nodes which are manually fed inputs
   * @return The set of nodes which are hit by propagating forward from inputs
   */
  public Set<T> trace(T[] inputs) {
    Set<T> r = new HashSet<>();
    Stack<T> stack = new Stack<>();

    for (T i : inputs) {
      stack.push(i);
    }

    while (!stack.isEmpty()) {
      T node = stack.pop();
      if (!r.contains(node)) {
        r.add(node);

        for (T i : this.outNeighborhood(node)) {
          stack.push(i);
        }
      }
    }

    return r;
  }

  public boolean hasEdge(Edge<T> edge) {
    return adjList.get(edge.getStart()).contains(edge.getEnd());
  }

  public DirectedGraph<T> copy() {
    DirectedGraph<T> dg = new DirectedGraph<>();

    for (T node : adjList.keySet()) {
      dg.addNode(node);
    }

    for (Iterator<Edge<T>> edges = this.getEdges(); edges.hasNext(); ) {
      Edge<T> edge = edges.next();
      dg.addEdge(edge);
    }
    return dg;
  }

  public Iterator<Edge<T>> getEdges() {
    return new EdgeIterator();
  }

  private class EdgeIterator implements Iterator<Edge<T>> {
    private T start;
    private Iterator<T> endIter;
    private Iterator<T> startIter;

    EdgeIterator() {
      startIter = adjList.keySet().iterator();
      nextStart();
    }

    private void nextStart() {
      start = startIter.next();
      Set<T> ends = adjList.get(start);
      if (ends == null) {
        endIter = new EmptyIterator<>();
      } else {
        endIter = ends.iterator();
      }
    }

    private void prepareNext() {
      while (adjList.containsKey(start) && !endIter.hasNext()) {
        nextStart();
      }
    }

    public boolean hasNext() {
      prepareNext();
      return endIter.hasNext();
    }

    public Edge<T> next() {
      prepareNext();
      return new Edge<>(start, endIter.next());
    }
  }
}
