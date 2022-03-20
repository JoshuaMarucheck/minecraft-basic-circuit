package circuit.iterators;

import java.util.Iterator;

abstract public class FilterIterator<T> implements Iterator<T> {
  /*
   * Invariant: the iterator will have just popped the last valid item, or else it will be out of items.
   */
  private Iterator<T> iter;
  private T nextItem;
  private boolean holdingItem;

  public FilterIterator(Iterator<T> iter) {
    this.iter = iter;
    prepNext();
  }

  public boolean hasNext() {
    return holdingItem;
  }

  public T next() {
    T item = nextItem;
    prepNext();
    return item;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  private void prepNext() {
    holdingItem = false;
    if (iter.hasNext()) {
      do {
        nextItem = iter.next();
      } while (iter.hasNext() && !filter(nextItem));

      if (filter(nextItem)) {
        holdingItem = true;
      }
    }
  }

  /**
   * @param t
   * @return {@code true} iff {@code t} should be passed on, {@code false} otherwise.
   */
  abstract protected boolean filter(T t);
}
