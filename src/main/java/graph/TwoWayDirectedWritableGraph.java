package graph;

public class TwoWayDirectedWritableGraph<T> extends TwoWayDirectedGraph<T> {
  public TwoWayDirectedWritableGraph(DirectedGraph<T> graph) {
    super(graph);
  }

  /**
   * Creates an empty writable two-way graph.
   */
  public TwoWayDirectedWritableGraph() {
    super(new DirectedGraph<>());
  }

  public void addNode(T node) {
    forward.addNode(node);
    backward.addNode(node);
  }

  public void addEdge(Edge<T> edge) {
    forward.addEdge(edge);
    backward.addEdge(edge.reverse());
  }
}
