package graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * An uneditable set, backed by the contents of another set.
 * If the backing set changes, so does the view.
 * You cannot edit the set through the view.
 * <p>
 * If you want to edit the contents of the set, make a copy first.
 *
 * @param <E> The element type
 */
public class SetView<E> implements Set<E> {
  private Set<E> set;

  public SetView(Set<E> set) {
    this.set = set;
  }

  @Override
  public int size() {
    return set.size();
  }

  @Override
  public boolean isEmpty() {
    return set.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return set.contains(o);
  }

  @Override
  public Iterator<E> iterator() {
    return new ViewIterator<>(set.iterator());
  }

  /**
   * An iterator without editing privileges
   */
  private static class ViewIterator<E> implements Iterator<E> {
    private Iterator<E> iter;

    ViewIterator(Iterator<E> iter) {
      this.iter = iter;
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public E next() {
      return iter.next();
    }
  }

  @Override
  public Object[] toArray() {
    return set.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return set.toArray(a);
  }

  @Override
  public boolean add(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return set.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
}
