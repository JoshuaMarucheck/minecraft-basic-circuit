package graph;

public class Edge {
  private Integer start;
  private Integer end;

  public Edge(Integer start, Integer end) {
    this.start = start;
    this.end = end;
  }

  public Integer getStart() {
    return start;
  }

  public Integer getEnd() {
    return end;
  }

  public Edge reverse() {
    return new Edge(end, start);
  }

  public String toString() {
    return "Edge: " + start + " -> " + end;
  }
}
