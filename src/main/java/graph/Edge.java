package graph;

import java.util.Iterator;

public class Edge<T> implements Iterable<T> {
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

  @Override
  public Iterator<T> iterator() {
    return new DualIterator();
  }

  private class DualIterator implements Iterator<T> {
    private int i;

    DualIterator() {
      i = 0;
    }

    @Override
    public boolean hasNext() {
      return i < 2;
    }

    @Override
    public T next() {
      switch (i++) {
        case 0:
          return start;
        case 1:
          return end;
        default:
          throw new IllegalStateException();
      }
    }
  }
}
