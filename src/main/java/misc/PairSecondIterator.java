package misc;

import circuit.Pair;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

/**
 * Pairs up items from the first slot with items having gone through the filter applied to the second spot.
 */
public class PairSecondIterator<A, B, C> implements Iterator<Pair<A, B>> {
  private Queue<A> aThings;
  private Queue<B> bThings;
  private Iterator<Pair<A, C>> iter;
  private Iterator<B> filtered;

  public PairSecondIterator(Iterator<Pair<A, C>> iter, Function<Iterator<C>, Iterator<B>> filter) {
    this.iter = iter;
    this.filtered = filter.apply(new IteratorListener());
    aThings = new LinkedList<>();
    bThings = new LinkedList<>();
  }

  @Override
  public boolean hasNext() {
    while ((aThings.isEmpty() || bThings.isEmpty()) && filtered.hasNext()) {
      bThings.add(filtered.next());
    }
    return !(aThings.isEmpty() || bThings.isEmpty());
  }

  @Override
  public Pair<A, B> next() {
    return new Pair<>(aThings.remove(), bThings.remove());
  }

  private class IteratorListener implements Iterator<C> {

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public C next() {
      Pair<A, C> item = iter.next();
      aThings.add(item.getFirst());
      return item.getSecond();
    }
  }
}
