package physical2.one;

import java.util.Iterator;

/**
 * {@code null} is the empty range, or maybe the full one? I really just need two nullish values.
 */
public class Range implements Iterable<Integer> {
  private int lo, hi;

  private Range(int lo, int hi) {
    this.lo = lo;
    this.hi = hi;
  }

  public int getUpper() {
    return hi;
  }

  public int getLower() {
    return lo;
  }

  public static Range make(int lo, int hi) {
    return new Range(Math.min(lo, hi), Math.max(lo, hi));
  }

  public static Range make(Integer i) {
    if (i == null) {
      return null;
    } else {
      return new Range(i, i);
    }
  }

  public static Range merge(Range r1, Range r2) {
    if (r1 == null) {
      return r2;
    } else if (r2 == null) {
      return r1;
    } else {
      return new Range(Math.min(r1.getLower(), r2.getLower()), Math.max(r1.getUpper(), r2.getUpper()));
    }
  }

  public static Range merge(Range range, Integer p) {
    if (range == null) {
      return new Range(p, p);
    } else if (p == null) {
      return range;
    } else {
      return new Range(Math.min(range.lo, p), Math.max(range.hi, p));
    }
  }

  /**
   * Will not include Integer.MAX_VALUE even if it is part of this Range.
   */
  @Override
  public Iterator<Integer> iterator() {
    return new IntIterator();
  }

  private class IntIterator implements Iterator<Integer> {
    private int i;

    public IntIterator() {
      i = lo;
    }

    @Override
    public boolean hasNext() {
      return i < hi;
    }

    @Override
    public Integer next() {
      return i++;
    }
  }

  @Override
  public String toString() {
    return "Range(" + lo + ", " + hi + ")";
  }
}
