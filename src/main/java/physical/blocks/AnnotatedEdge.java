package physical.blocks;

import circuit.Pair;
import graph.Edge;
import physical.things.BlockConstant;
import physical.things.Point3D;
import physical.things.TorchState;
import physical.transforms.Offset;

import java.util.*;
import java.util.function.Function;

import static physical.things.TorchState.NONE;

/**
 * Immutable, ideally
 */
public class AnnotatedEdge implements Iterable<Pair<Point3D, BlockConstant>> {
  // From 0,0 to some other point
  private Edge<Point3D> connection;

  private TorchState firstTorch;
  private TorchState secondTorch;

  // Other points which must be empty for this connection to be valid
  private Set<Point3D> emptyPoints;

  // How to make this thing physical
  // These are allowed to overlap with other things, as long as all blocks specified are identical
  private Map<Point3D, BlockConstant> points;


  public AnnotatedEdge(TorchState firstTorch, TorchState secondTorch, Edge<Point3D> connection, Set<Point3D> emptyPoints, Map<Point3D, BlockConstant> points) {
    if (firstTorch != NONE && secondTorch != NONE) {
      throw new IllegalArgumentException("Only one torch is allowed per AnnotatedEdge");
    }

    this.connection = connection;
    this.emptyPoints = emptyPoints;
    this.points = points;
    this.firstTorch = firstTorch;
    this.secondTorch = secondTorch;
  }

  public Point3D getTargetPoint() {
    return connection.getEnd();
  }

  public TorchState getFirstTorchState() {
    return firstTorch;
  }

  public TorchState getSecondTorchState() {
    return secondTorch;
  }

  public Iterator<Point3D> emptyPoints() {
    return emptyPoints.iterator();
  }

  /**
   * @return An iterator over the blocks to fill in
   */
  public Iterator<Pair<Point3D, BlockConstant>> iterator() {
    return new BlockIterator();
  }

  private class BlockIterator implements Iterator<Pair<Point3D, BlockConstant>> {
    private Iterator<Point3D> pointIter;

    BlockIterator() {
      pointIter = points.keySet().iterator();
    }

    @Override
    public boolean hasNext() {
      return pointIter.hasNext();
    }

    @Override
    public Pair<Point3D, BlockConstant> next() {
      Point3D p = pointIter.next();
      return new Pair<>(p, points.get(p));
    }
  }

  /**
   * @param rotation A rotation function, ideally, since this object represents things at different scales.
   */
  public AnnotatedEdge rotate(Function<Point3D, Point3D> rotation) {
    HashSet<Point3D> newEmptyPoints = new HashSet<>();
    for (Point3D p : emptyPoints) {
      newEmptyPoints.add(rotation.apply(p));
    }
    Map<Point3D, BlockConstant> newPoints = new HashMap<>();
    for (Point3D p : points.keySet()) {
      newPoints.put(rotation.apply(p), points.get(p));
    }
    return new AnnotatedEdge(
        firstTorch,
        secondTorch,
        new Edge<>(
            rotation.apply(connection.getStart()),
            rotation.apply(connection.getEnd())
        ),
        newEmptyPoints,
        newPoints
    );
  }

  /**
   * Creates an annotated edge which represents approaching this object from the other end of the given connection.
   * <p>
   * Applies a translation.
   */
  public AnnotatedEdge swapPerspective() {
    Function<Point3D, Point3D> offset = new Offset(connection.getEnd().negate());
    HashSet<Point3D> newEmptyPoints = new HashSet<>();
    for (Point3D p : emptyPoints) {
      newEmptyPoints.add(offset.apply(p));
    }
    Map<Point3D, BlockConstant> newPoints = new HashMap<>();
    for (Point3D p : points.keySet()) {
      newPoints.put(offset.apply(p), points.get(p));
    }
    return new AnnotatedEdge(
        secondTorch,
        firstTorch,
        new Edge<>(offset.apply(connection.getStart()), offset.apply(connection.getEnd())),
        newEmptyPoints,
        newPoints
    );
  }
}
