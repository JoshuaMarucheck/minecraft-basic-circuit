package physical;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Point3DCollection implements Iterable<Point3D> {
  private HashSet<Point3D> points;

  public Point3DCollection() {
    points = new HashSet<>();
  }

  public Point3DCollection(Iterator<Point3D> iter) {
    points = new HashSet<>();
    while (iter.hasNext()) {
      points.add(iter.next());
    }
  }

  public boolean add(Point3D p) {
    return points.add(p);
  }

  public boolean remove(Point3D p) {
    return points.remove(p);
  }

  public Point3DCollection copy() {
    Point3DCollection collection = new Point3DCollection();
    for (Point3D p : this) {
      collection.add(p);
    }
    return collection;
  }

  public boolean contains(Point3D point) {
    return points.contains(point);
  }

  /**
   * O(1)
   *
   * @param point The point to check against.
   * @return Whether this Point3DCollection is adjacent to the specified point.
   * (Returns {@code false} if the point is inside this blob.)
   */
  public boolean isAdjacentTo(Point3D point) {
    if (contains(point)) {
      return false;
    }
    for (Iterator<Point3D> it = point.adjacentPoints(); it.hasNext(); ) {
      Point3D p = it.next();
      if (contains(p)) {
        return true;
      }
    }
    return false;
  }

  public int size() {
    return points.size();
  }

  /**
   * @return Whether this blob is a single connected component or not.
   */
  public boolean isSingleBlob() {
    if (size() == 0) {
      return true;
    } else {
      Point3D p = this.iterator().next();
      HashMap<Point3D, Integer> signal = propagateSignal(p);
      return signal.size() == size() - 1;
    }
  }

  /**
   * @return {@code true} if this blob could have an output block at the point specified
   * (i.e. this is not the only point connecting two disparate parts of redstone blob)
   */
  public boolean isEdgePoint(Point3D point) {
    if (!contains(point)) {
      throw new RuntimeException("Testing edgeness on a point not in this blob");
    }
    remove(point);

    Point3D p = null;
    for (Iterator<Point3D> it = point.adjacentPoints(); it.hasNext(); ) {
      Point3D q = it.next();
      if (contains(q)) {
        p = q;
        break;
      }
    }
    if (p == null) {
      // point was the only point in this collection
      // Or else point is some solitary node out in the middle of nowhere.
      boolean r = size() == 0;
      add(point);
      return r;
    }

    HashMap<Point3D, Integer> signal = propagateSignal(p);
    boolean r = signal.size() == size() - 1;
    add(point);
    return r;
  }

  /**
   * @return A map from point to the signal distance from the specified point to that point
   */
  public HashMap<Point3D, Integer> propagateSignal(Point3D point) {
    int i = 0;
    HashMap<Point3D, Integer> distanceMap = new HashMap<>();
    distanceMap.put(point, i);

    HashSet<Point3D> layer = new HashSet<>();
    layer.add(point);

    while (!layer.isEmpty()) {
      HashSet<Point3D> newLayer = new HashSet<>();
      for (Point3D layerPoint : layer) {
        distanceMap.put(layerPoint, i);
        for (Iterator<Point3D> it = layerPoint.adjacentPoints(); it.hasNext(); ) {
          Point3D p = it.next();
          if (!distanceMap.containsKey(p)) {
            newLayer.add(p);
          }
        }
        layer = newLayer;
        i++;
      }
    }

    return distanceMap;
  }

  public Point3D[] toArray() {
    return points.toArray(new Point3D[0]);
  }

  /**
   * @return An iterator over points adjacent to this blob.
   */
  public Iterator<Point3D> adjacentIterator() {
    return new AdjIterForCollection();
  }

  /**
   * Edge points are points which, when removed, do not split the blob into multiple pieces
   *
   * @return An iterator over edge points of this blob.
   */
  public Iterator<Point3D> edgeIterator() {
    return new EdgeIter();
  }

  @Override
  public Iterator<Point3D> iterator() {
    return points.iterator();
  }

  private class EdgeIter implements Iterator<Point3D> {
    Iterator<Point3D> points;
    Point3D next;

    EdgeIter() {
      points = iterator();
      next = null;
      prepNext();
    }

    private void prepNext() {
      if (points.hasNext()) {
        do {
          next = points.next();
        } while (points.hasNext() && !isEdgePoint(next));
      }
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public Point3D next() {
      Point3D p = next;
      prepNext();
      return p;
    }
  }

  /**
   * An iterator over all points adjacent to this Point3DCollection
   */
  private class AdjIterForCollection implements Iterator<Point3D> {
    Iterator<Point3D> myPointIter;
    Iterator<Point3D> subPointIter;
    Point3D next;

    public AdjIterForCollection() {
      myPointIter = iterator();
      if (myPointIter.hasNext()) {
        subPointIter = myPointIter.next().adjacentPoints();
      }
      prepNext();
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    private void prepNext() {
      while (myPointIter.hasNext()) {
        while (subPointIter.hasNext()) {
          Point3D p = subPointIter.next();
          if (isAdjacentTo(p)) {
            next = p;
            return;
          }
        }
        subPointIter = myPointIter.next().adjacentPoints();
      }
      next = null;
    }

    @Override
    public Point3D next() {
      Point3D p = next;
      prepNext();
      return p;
    }
  }
}
