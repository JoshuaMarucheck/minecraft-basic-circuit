package graph;

import java.util.Iterator;

public class IntegerIterable implements Iterable<Integer> {
  @Override
  public Iterator<Integer> iterator() {
    return new SimpleIntegerIterator();
  }

  private static class SimpleIntegerIterator implements Iterator<Integer> {
    private int next;

    @Override
    public boolean hasNext() {
      return next >= 0;
    }

    @Override
    public Integer next() {
      return next++;
    }
  }
}