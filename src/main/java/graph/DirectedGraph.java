package graph;

import java.util.*;

/**
 * A class for representing directed graphs.
 * Nodes are Integers.
 * <p>
 * The objective is to be able to add and remove nodes.
 * <p>
 * ids can be whatever. There's no way we're getting to 2 billion redstone torches.
 */
public class DirectedGraph {
  private ArrayList<Set<Integer>> adjList;

  public DirectedGraph() {
    this.adjList = new ArrayList<Set<Integer>>();
  }

  public DirectedGraph(ArrayList<Set<Integer>> adjList) {
    this.adjList = adjList;
  }

  /**
   * @return The number of nodes in this graph
   */
  public int size() {
    return adjList.size();
  }

  public int addNode() {
    adjList.add(new HashSet<Integer>());
    return adjList.size() - 1;
  }

  ArrayList<Set<Integer>> getAdjList() {
    return adjList;
  }

  public DirectedGraph invert() {
    DirectedGraph graph = new DirectedGraph();
    graph.ensureSize(this.size());
    for (Iterator<Edge> it = this.getEdges(); it.hasNext(); ) {
      Edge edge = it.next();
      graph.addEdge(edge.reverse());
    }
    return graph;
  }

  public void ensureSize(int size) {
    while (adjList.size() < size) {
      this.addNode();
    }
  }

  public void addEdge(Edge edge) {
    adjList.get(edge.getStart()).add(edge.getEnd());
  }

  public Set<Integer> outNeighborhood(int node) {
    return adjList.get(node);
  }

  public boolean nodeHasOutput(int node) {
    return !adjList.get(node).isEmpty();
  }

  public Set<Integer> getNodesWithoutOutput() {
    Set<Integer> r = new HashSet<Integer>();
    for (int i = 0; i < this.size(); i++) {
      if (!this.nodeHasOutput(i)) {
        r.add(i);
      }
    }
    return r;
  }

  /**
   * Determines which nodes are touched if you set certain inputs
   *
   * @param inputs The nodes which are manually fed inputs
   * @return The set of nodes which are hit by propagating forward from inputs
   */
  public Set<Integer> trace(Integer[] inputs) {
    Set<Integer> r = new HashSet<Integer>();
    Stack<Integer> stack = new Stack<Integer>();

    for (Integer i : inputs) {
      stack.push(i);
    }

    while (!stack.isEmpty()) {
      Integer node = stack.pop();
      if (!r.contains(node)) {
        r.add(node);

        for (Integer i : this.outNeighborhood(node)) {
          stack.push(i);
        }
      }
    }

    return r;
  }

  public boolean hasEdge(Edge edge) {
    return adjList.get(edge.getStart()).contains(edge.getEnd());
  }

  public DirectedGraph copy() {
    DirectedGraph dg = new DirectedGraph();
    dg.ensureSize(this.size());
    for (Iterator<Edge> edges = this.getEdges(); edges.hasNext(); ) {
      Edge edge = edges.next();
      dg.addEdge(edge);
    }
    return dg;
  }

  public Iterator<Edge> getEdges() {
    return new EdgeIterator();
  }

  private class EdgeIterator implements Iterator<Edge> {
    private int start;
    private Iterator<Integer> endIter;

    EdgeIterator() {
      start = -1;
      endIter = new EmptyIterator<Integer>();
    }

    private void prepareNext() {
      while (start + 1 < adjList.size() && !endIter.hasNext()) {
        start++;
        endIter = adjList.get(start).iterator();
      }
    }

    public boolean hasNext() {
      prepareNext();
      return endIter.hasNext();
    }

    public Edge next() {
      prepareNext();
      return new Edge(start, endIter.next());
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }


}
