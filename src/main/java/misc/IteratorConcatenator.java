package misc;

import java.util.Iterator;

public class IteratorConcatenator<T> implements Iterator<T> {
  private Iterator<Iterator<T>> iterIter;
  private Iterator<T> localIter;

  public IteratorConcatenator(Iterator<Iterator<T>> iterIter) {
    this.iterIter = iterIter;
  }

  private void prepNext() {
    while (!localIter.hasNext() && iterIter.hasNext()) {
      localIter = iterIter.next();
    }
  }

  @Override
  public boolean hasNext() {
    prepNext();
    return localIter.hasNext();
  }

  @Override
  public T next() {
    prepNext();
    return localIter.next();
  }
}
