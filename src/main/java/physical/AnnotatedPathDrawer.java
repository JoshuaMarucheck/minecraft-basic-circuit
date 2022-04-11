package physical;

import circuit.Pair;
import physical.things.Bounded;
import physical.things.Bounds;
import physical.things.Point3D;
import physical.transforms.Offset;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class AnnotatedPathDrawer implements Iterable<Pair<AnnotatedEdge, Point3D>>, Bounded {
  private final HashSet<Point3D> emptyPoints;
  private final HashSet<Point3D> filledPoints;
  private final Set<Pair<AnnotatedEdge, Point3D>> connections;

  public AnnotatedPathDrawer(HashSet<Point3D> filledPoints, HashSet<Point3D> emptyPoints) {
    this.emptyPoints = emptyPoints;
    this.filledPoints = filledPoints;
    connections = new HashSet<>();
  }

  public AnnotatedPathDrawer() {
    this(new HashSet<>(), new HashSet<>());
  }

  public void addFilledPoint(Point3D p) {
    filledPoints.add(p);
  }

  public void addEmptyPoint(Point3D p) {
    emptyPoints.add(p);
  }

  public void addConnection(AnnotatedEdge edge, Point3D offset) {
    Offset func = new Offset(offset);
    filledPoints.add(offset);
    filledPoints.add(func.apply(edge.getTargetPoint()));
    connections.add(new Pair<>(edge, offset));
  }

  public Bounds bounds() {
    return Bounds.make(filledPoints).mergePoints(emptyPoints);
  }

  public Iterator<Pair<AnnotatedEdge, Point3D>> iterator() {
    return connections.iterator();
  }

  public Iterator<Point3D> filledPointIterator() {
    return filledPoints.iterator();
  }

  public Iterator<Point3D> emptyPointIterator() {
    return emptyPoints.iterator();
  }

  public boolean isFilledPoint(Point3D p) {
    return filledPoints.contains(p);
  }

  public boolean isEmptyPoint(Point3D p) {
    return emptyPoints.contains(p);
  }

  public AnnotatedPathDrawer transform(Function<Point3D, Point3D> f) {
    AnnotatedPathDrawer r = new AnnotatedPathDrawer();
    for (Point3D p : filledPoints) {
      r.addFilledPoint(f.apply(p));
    }
    for (Point3D p : emptyPoints) {
      r.addEmptyPoint(f.apply(p));
    }
    for (Pair<AnnotatedEdge, Point3D> pair : connections) {
      AnnotatedEdge edge = pair.getFirst();
      Point3D point = pair.getSecond();
      r.addConnection(
          edge,
          f.apply(point)
      );
    }
    return r;
  }
}
