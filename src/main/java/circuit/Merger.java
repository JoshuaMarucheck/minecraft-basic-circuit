package circuit;

import java.util.*;

/**
 * A class representing the merging of items.
 * Gives a canonical representation of type {@code T} to
 * each item merged, and can tell you which items have been merged
 * with a given item.
 * <p>
 * Uses {@code null} to represent deletion,
 * and so you probably shouldn't attempt to
 * merge {@code null} in this collection
 * unless you don't intend to delete things.
 */
public class Merger<T> {

  private Map<T, T> mappings;
  /**
   * Map from an item to the set of all things that accumulate into that item.
   * <p>
   * Invariant: Endpoints in {@code mappings} paths are the only items which are keys in {@code accumulations},
   * and the collection they are mapped to contains exactly those things which have a path forward to it.
   */
  private Map<T, Collection<T>> accumulations;

  public Merger() {
    mappings = new HashMap<>();
    accumulations = new HashMap<>();
  }

  /**
   * Merges {@code item} with {@code null}.
   */
  public void delete(T item) {
    T mapping = getMapping(item);
    if (mapping != null) {
      putMappingUnsafe(mapping, null);
    }
  }

  /**
   * Makes it so that {@code a} and {@code b} have the same canonical representation (see {@code getMapping}).
   * This means that if either {@code a} or {@code b} have been merged with other nodes or deleted,
   * all nodes involved will be merged/deleted.
   * <p>
   * Generally maps {@code a} to {@code b}, so if it's more important
   * that one or the other be the canonical representation, put that one
   * in {@code b}. Note that this is not a guarantee that it will map to {@code b},
   * because {@code b} might already be mapped to something else.
   */
  public void merge(T a, T b) {
    a = getMapping(a);
    b = getMapping(b);
    if (!Objects.equals(a, b)) {
      if (a == null) {
        putMappingUnsafe(b, null);
      } else {
        putMappingUnsafe(a, b);
      }
    }
  }

  /**
   * Both inputs should be values which have already gone through {@code getMapping}.
   */
  private void putMappingUnsafe(T from, T to) {
    mappings.put(from, to);
    ensureAccumulation(to).addAll(getAccumulationUnsafe(from));
    accumulations.remove(from);
  }

  /**
   * Returns a set containing only the canonical representations of
   * everything in {@code items}, not including any {@code null} values
   * unless {@code includeDeleted == true}.
   */
  public Set<T> reduce(Iterable<T> items, boolean includeDeleted) {
    Set<T> r = new HashSet<>();
    for (T t : items) {
      T mapped = getMapping(t);
      if (mapped != null || includeDeleted) {
        r.add(mapped);
      }
    }
    return r;
  }

  private Collection<T> ensureAccumulation(T item) {
    if (!accumulations.containsKey(item)) {
      Set<T> set = new HashSet<>();
      set.add(item);
      accumulations.put(item, set);
    }
    return accumulations.get(item);
  }

  private Collection<T> getAccumulationUnsafe(T item) {
    if (accumulations.containsKey(item)) {
      return Collections.unmodifiableCollection(accumulations.get(item));
    } else {
      return Collections.singleton(item);
    }
  }

  /**
   * The returned collection is an unmodifiable view of the underlying map.
   *
   * @return The collection of things which share a canonical representation with this item.
   */
  public Collection<T> getAccumulation(T item) {
    return getAccumulationUnsafe(getMapping(item));
  }

  /**
   * Follows cycles in {@code mappings}, starting from {@code item}, until an end point is reached.
   * <p>
   * O(1) amt
   * <p>
   * Not safe with loops in {@code mappings}.
   *
   * @return The mapping, with {@code null} being returned if the node is deleted.
   */
  public T getMapping(T item) {
    if (mappings.containsKey(item)) {
      ArrayList<T> ts = new ArrayList<>();

      while (mappings.containsKey(item)) {
        ts.add(item);
        item = mappings.get(item);
      }

      for (T t : ts) {
        putMappingUnsafe(t, item);
      }
    }
    return item;
  }

  /**
   * Whether or not the item is its own canonical representation.
   * That is, this item is the one which stands for the group it is a part of.
   */
  public boolean isSelfMapped(T item) {
    return Objects.equals(getMapping(item), item);
  }

  public boolean isDeleted(T item) {
    return getMapping(item) == null;
  }

  private T mapOrDefault(T val) {
    return mappings.getOrDefault(val, val);
  }
}
