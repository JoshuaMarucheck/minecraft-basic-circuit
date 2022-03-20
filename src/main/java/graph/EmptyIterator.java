package graph;

import java.util.Iterator;

public class EmptyIterator<K> implements Iterator<K> {
  public boolean hasNext() {
    return false;
  }

  public K next() {
    throw new UnsupportedOperationException();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
