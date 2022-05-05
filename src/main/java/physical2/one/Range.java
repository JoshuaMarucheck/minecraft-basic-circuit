package physical2.one;

import java.util.Iterator;

/**
 * {@code null} is the empty range, since Ranges only ever seem to be used for merging.
 * <p>
 * Inclusive of both endpoints.
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
   * Requires that the iterator not start at {@code Integer.MIN_VALUE}
   */
  @Override
  public Iterator<Integer> iterator() {
    return new IntIterator();
  }

  public boolean includes(int i) {
    return lo <= i && i <= hi;
  }

  private class IntIterator implements Iterator<Integer> {
    private int i;

    public IntIterator() {
      if (lo == Integer.MIN_VALUE) {
        throw new IllegalStateException("Can't iterate over a range starting at Integer.MIN_VALUE");
      }
      i = lo;
    }

    @Override
    public boolean hasNext() {
      return i != Integer.MIN_VALUE && i <= hi;
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

  @Override
  public int hashCode() {
    return Integer.hashCode(lo) + 37 * Integer.hashCode(hi);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Range) {
      return lo == ((Range) obj).lo && hi == ((Range) obj).hi;
    }
    return false;
  }
}
