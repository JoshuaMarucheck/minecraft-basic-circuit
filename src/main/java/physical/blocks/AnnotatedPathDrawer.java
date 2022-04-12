package physical.blocks;

import circuit.Pair;
import graph.EmptyIterator;
import physical.things.Bounded;
import physical.things.Bounds;
import physical.things.Point3D;
import physical.things.TorchState;
import physical.transforms.Offset;

import java.util.*;
import java.util.function.Function;

public class AnnotatedPathDrawer implements Iterable<Pair<AnnotatedEdge, Point3D>>, Bounded {
  /**
   * The signal strength at an input cell.
   * <p>
   * The signal strength of an item is the number of steps forward from that item you can take.
   */
  private final int HIGH_STRENGTH = 8;

  private final Set<Point3D> emptyPoints;
  private final Set<Point3D> filledPoints;
  private final Set<Point3D> inputs;
  private final Set<Point3D> outputs;
  private final Set<Pair<AnnotatedEdge, Point3D>> connections;
  private final Map<Point3D, Set<Pair<AnnotatedEdge, Point3D>>> adjList;

  /**
   * Map a redstone spot to the amount further the signal is allowed to extend from it
   */
  private Map<Point3D, Integer> pointStrength;

  public AnnotatedPathDrawer() {
    emptyPoints = new HashSet<>();
    filledPoints = new HashSet<>();
    connections = new HashSet<>();
    pointStrength = new HashMap<>();
    inputs = new HashSet<>();
    outputs = new HashSet<>();
    adjList = new HashMap<>();
  }

  public void addFilledPoint(Point3D p) {
    filledPoints.add(p);
  }

  public void addEmptyPoint(Point3D p) {
    emptyPoints.add(p);
  }

  private void addToAdjList(Point3D p, Pair<AnnotatedEdge, Point3D> edge) {
    if (!adjList.containsKey(p)) {
      adjList.put(p, new HashSet<>());
    }
    adjList.get(p).add(edge);
  }

  private Iterator<Pair<AnnotatedEdge, Point3D>> outNeighborhood(Point3D p) {
    if (adjList.get(p) == null) {
      return new EmptyIterator<>();
    } else {
      return adjList.get(p).iterator();
    }
  }

  /**
   * Determines if the given AnnotatedEdge is legal to add at the given position.
   * <p>
   * Useful for pathfinding around this AnnotatedPathDrawer.
   *
   * @return {@code true} if {@code addConnection(edge, offset)} won't throw an exception.
   */
  public boolean isLegalConnection(AnnotatedEdge edge, Point3D offset) {
    if (edge.getFirstTorchState() == TorchState.OUTPUT || edge.getSecondTorchState() == TorchState.INPUT) {
      return false;
    }

    Offset func = new Offset(offset);
    Point3D translatedTarget = func.apply(edge.getTargetPoint());

    if (emptyPoints.contains(offset) || emptyPoints.contains(translatedTarget)) {
      return false;
    }

    if (edge.getFirstTorchState() == TorchState.INPUT) {
      if (filledPoints.contains(offset)) {
        if (!inputs.contains(offset)) {
          return false;
        }
      }
    }

    if (edge.getSecondTorchState() == TorchState.OUTPUT) {
      if (filledPoints.contains(translatedTarget)) {
        if (!outputs.contains(translatedTarget)) {
          return false;
        }
      }
    }

    for (Iterator<Point3D> it = edge.emptyPoints(); it.hasNext(); ) {
      Point3D emptyPoint = it.next();
      if (filledPoints.contains(emptyPoint)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Assumes edge goes from a point that is already known to an unknown point, unless it starts from an input
   */
  public void addConnection(AnnotatedEdge edge, Point3D offset) {
    if (edge.getFirstTorchState() == TorchState.OUTPUT || edge.getSecondTorchState() == TorchState.INPUT) {
      throw new IllegalArgumentException("Invalid torch direction");
    }

    Offset func = new Offset(offset);
    Point3D translatedTarget = func.apply(edge.getTargetPoint());

    if (emptyPoints.contains(offset) || emptyPoints.contains(translatedTarget)) {
      throw new IllegalArgumentException("Attempted to place block in enforced empty spot");
    }

    // We maybe set the strength of the first point...
    if (edge.getFirstTorchState() == TorchState.INPUT) {
      if (filledPoints.contains(offset)) {
        if (!inputs.contains(offset)) {
          throw new IllegalArgumentException("Position " + offset + " cannot be both an input and not an input");
        }
      } else {
        inputs.add(offset);
        pointStrength.put(offset, HIGH_STRENGTH);
      }
    }

    // But we definitely set the strength of the second point.
    if (edge.getSecondTorchState() == TorchState.OUTPUT) {
      if (filledPoints.contains(translatedTarget)) {
        if (!outputs.contains(translatedTarget)) {
          throw new IllegalArgumentException("Position " + translatedTarget + " cannot be both an output and not an output");
        }
      } else {
        outputs.add(translatedTarget);
        pointStrength.put(translatedTarget, 0);
      }
    } else {
      pointStrength.put(translatedTarget, pointStrength.get(offset) - 1);
    }

    // Now just add all the things to the various collections
    filledPoints.add(offset);
    filledPoints.add(translatedTarget);

    Pair<AnnotatedEdge, Point3D> edgeWithPos = new Pair<>(edge, offset);
    connections.add(edgeWithPos);
    addToAdjList(offset, edgeWithPos);

    for (Iterator<Point3D> it = edge.emptyPoints(); it.hasNext(); ) {
      Point3D emptyPoint = it.next();
      if (filledPoints.contains(emptyPoint)) {
        throw new IllegalArgumentException("Filled point and empty point overlap");
      }
      emptyPoints.add(emptyPoint);
    }
  }

  public Bounds bounds() {
    return Bounds.mergePoints(Bounds.make(filledPoints), emptyPoints);
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

  public Iterator<Point3D> inputIterator() {
    return inputs.iterator();
  }

  public Iterator<Point3D> outputIterator() {
    return outputs.iterator();
  }

  public boolean isFilledPoint(Point3D p) {
    return filledPoints.contains(p);
  }

  public boolean isEmptyPoint(Point3D p) {
    return emptyPoints.contains(p);
  }

  public boolean isInput(Point3D p) {
    return inputs.contains(p);
  }

  public boolean isOutput(Point3D p) {
    return outputs.contains(p);
  }

  private static Pair<AnnotatedEdge, Point3D> transformEdge(Function<Point3D, Point3D> func, Pair<AnnotatedEdge, Point3D> pair) {
    return new Pair<>(pair.getFirst(), func.apply(pair.getSecond()));
  }

  /**
   * We're using an Offset since we can't rotate or scale AnnotatedEdges
   */
  public AnnotatedPathDrawer translate(Offset f) {
    AnnotatedPathDrawer r = new AnnotatedPathDrawer();
    for (Point3D p : filledPoints) {
      r.addFilledPoint(f.apply(p));
    }
    for (Point3D p : emptyPoints) {
      r.addEmptyPoint(f.apply(p));
    }
    for (Point3D p : inputs) {
      r.inputs.add(f.apply(p));
    }
    for (Point3D p : outputs) {
      r.outputs.add(f.apply(p));
    }
    for (Pair<AnnotatedEdge, Point3D> pair : connections) {
      r.connections.add(transformEdge(f, pair));
    }
    for (Point3D p : adjList.keySet()) {
      Set<Pair<AnnotatedEdge, Point3D>> set = new HashSet<>();
      for (Pair<AnnotatedEdge, Point3D> pair : adjList.get(p)) {
        set.add(transformEdge(f, pair));
      }
      r.adjList.put(f.apply(p), set);
    }

    return r;
  }
}
