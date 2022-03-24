package circuit;

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
    return k.toString() + " -> " + v.toString();
  }

  public int hashCode() {
    return 31 * k.hashCode() + v.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof Pair) {
      return ((Pair) obj).k.equals(this.k) && ((Pair) obj).v.equals(this.v);
    }
    return false;
  }
}