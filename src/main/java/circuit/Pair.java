package circuit;

import java.util.Objects;

public class Pair<K, V> {
  private K k;
  private V v;

  public Pair(K k, V v) {
    this.k = k;
    this.v = v;
  }

  public K getFirst() {
    return k;
  }

  public V getSecond() {
    return v;
  }

  public String toString() {
    return "(" + k + ", " + v + ")";
  }

  public int hashCode() {
    return 31 * Objects.hashCode(k) + Objects.hashCode(v);
  }

  public boolean equals(Object obj) {
    if (obj instanceof Pair) {
      return Objects.equals(((Pair) obj).k, this.k) && Objects.equals(((Pair) obj).v, this.v);
    }
    return false;
  }

  public Pair<V, K> swap() {
    return new Pair<>(v, k);
  }
}
