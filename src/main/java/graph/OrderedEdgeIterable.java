package graph;

import java.util.*;

/**
 * Guarantees that each item in the graph has all its children yielded before any of its outgoing edges.
 *
 * @param <T> graph node type
 */
public class OrderedEdgeIterable<T> implements Iterable<Edge<T>> {
  private TwoWayDirectedGraph<T> graph;

  public OrderedEdgeIterable(TwoWayDirectedGraph<T> graph) {
    this.graph = graph;
  }

  @Override
  public Iterator<Edge<T>> iterator() {
    return new OrderedEdgeIterator<>(graph);
  }

  /**
   * ITerates over edges in the graph, maintaining the invariant that if edge A has a path forward in the graph to edge B, then B is yielded before A.
   * The behavior is undefined in the case that the graph contains a loop.
   *
   * @param <T> the graph node type
   */
  private static class OrderedEdgeIterator<T> implements Iterator<Edge<T>> {
    TwoWayDirectedGraph<T> graph;
    Iterator<Edge<T>> localIter;
    Set<Edge<T>> handled;
    Iterator<Edge<T>> graphIter;

    public OrderedEdgeIterator(TwoWayDirectedGraph<T> graph) {
      this.graph = graph;
      handled = new HashSet<>();
      localIter = new EmptyIterator<>();
      graphIter = graph.getEdges();
    }

    /**
     * Assumes: graphIter.hasNext()
     * <p>
     * Forcibly updates localIter. Should probably not be called if localIter.hasNext().
     */
    private void nextLocalIter() {
      Stack<Edge<T>> handleStack = new Stack<>();
      handleStack.add(graphIter.next());
      Queue<Edge<T>> iterQueue = new LinkedList<>();

      while (!handleStack.isEmpty()) {
        Edge<T> edge = handleStack.pop();
        if (handled.contains(edge)) {
          iterQueue.add(edge);
        } else {
          handled.add(edge);
          handleStack.push(edge);
          for (T node : graph.inNeighborhood(edge.getStart())) {
            handleStack.push(new Edge<>(node, edge.getStart()));
          }
        }
      }

      localIter = iterQueue.iterator();
    }

    /**
     * Ensures: localIter.hasNext() || !graphIter.hasNext()
     */
    private void prepNext() {
      while (!localIter.hasNext() && graphIter.hasNext()) {
        nextLocalIter();
      }
    }

    @Override
    public boolean hasNext() {
      prepNext();
      return localIter.hasNext();
    }

    @Override
    public Edge<T> next() {
      prepNext();
      return localIter.next();
    }
  }

}
