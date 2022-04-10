package graph;

public class Edge<T> {
  private T start;
  private T end;

  public Edge(T start, T end) {
    this.start = start;
    this.end = end;
  }

  public T getStart() {
    return start;
  }

  public T getEnd() {
    return end;
  }

  public Edge<T> reverse() {
    return new Edge<>(end, start);
  }

  public String toString() {
    return "Edge: " + start + " -> " + end;
  }
}
