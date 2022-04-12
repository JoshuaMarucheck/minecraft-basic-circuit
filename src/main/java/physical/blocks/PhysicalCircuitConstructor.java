package physical.blocks;

import circuit.Pair;
import physical.things.Bounds;
import physical.things.Point3D;
import physical.transforms.Offset;
import physical.transforms.Scale;

import java.util.*;
import java.util.function.Function;

/**
 * Some basic limits to remember:
 * <p>
 * We're spreading the redstone out so that we don't need to worry about adjacency,
 * only overlapping. However, this means we get a distance of 8 (taxicab) along the
 * longest path in any single path.
 * <p>
 * Paths cannot go straight up or straight down. Going up or down in a step consumes
 * two blocks (but reduces power level by the same amount as a horizontal step.)
 * <p>
 * You can use A* to draw paths, but note:
 * - all circuits should be left open (or rather, there should be loss for going adjacent to unnecessary collections)
 * - Collections are required to overlap on the edges which connect them
 * - a node with any input is required to be more than one cube large (i.e. it cannot just be the edge node, since torches don't like to power them)
 */
public class PhysicalCircuitConstructor {
  /**
   * Map a blob ID to its AnnotatedPathDrawer
   */
  private ArrayList<AnnotatedPathDrawer> redstoneDustBlobs;

  /**
   * Map a redstone spot to the blob it belongs to.
   * <p>
   * If it's an edge, then it belongs to the blob it comes from.
   * (The blob it goes to is in edgePoints.)
   */
  private Map<Point3D, Integer> consumedPoints;

  /**
   * Set of points which cannot contain redstone
   */
  private Set<Point3D> emptyPoints;

  /**
   * Maps a torch spot to the id of the redstone blob which it outputs to.
   * (Note that the id of the blob which it comes from is in consumedPoints.)
   */
  private Map<Point3D, Integer> edgePoints;

  public PhysicalCircuitConstructor() {
    redstoneDustBlobs = new ArrayList<>();
    consumedPoints = new HashMap<>();
    edgePoints = new HashMap<>();
    emptyPoints = new HashSet<>();
  }

  private void putConsumedPoint(Point3D p, Integer blobId) {
    if (blobId == null) {
      if (!consumedPoints.containsKey(p)) {
        consumedPoints.put(p, null);
      }
    } else {
      if (consumedPoints.containsKey(p) && consumedPoints.get(p) != null && !Objects.equals(consumedPoints.get(p), blobId)) {
        throw new IllegalArgumentException("Constructor already contained point");
      }
      consumedPoints.put(p, blobId);
    }
  }

  private void putEdgePoint(Point3D p, int blobId) {
    Integer edgeOutput = edgePoints.get(p);
    if (edgeOutput != null && edgeOutput != blobId) {
      throw new IllegalArgumentException("Connections between two blobs do not match!");
    }
    edgePoints.put(p, blobId);
  }

  public boolean isEdgePoint(Point3D p) {
    return edgePoints.containsKey(p);
  }

  /**
   * Requires that p is an edge point.
   *
   * @return The id of the redstone blob which this edge feeds into
   */
  private Integer getEdgeAsInputToBlob(Point3D p) {
    return edgePoints.get(p);
  }

  /**
   * Requires that p is an edge point.
   *
   * @return The id of the redstone blob which this edge feeds from
   */
  private Integer getEdgeAsOutputToBlob(Point3D p) {
    return consumedPoints.get(p);
  }

  /**
   * @param blob                A redstone blob
   * @return The id of the blob of redstone added.
   */
  public int addRedstoneBlob(AnnotatedPathDrawer blob) {
    int newBlobId = redstoneDustBlobs.size();
    for (Iterator<Point3D> it = blob.emptyPointIterator(); it.hasNext(); ) {
      Point3D p = it.next();
      if (consumedPoints.containsKey(p)) {
        throw new IllegalArgumentException("Empty point overlaps with consumed point");
      }
      emptyPoints.add(p);
    }
    for (Iterator<Point3D> it = blob.filledPointIterator(); it.hasNext(); ) {
      Point3D p = it.next();
      if (emptyPoints.contains(p)) {
        throw new IllegalArgumentException("Consumed point overlaps with empty point");
      }
      if (blob.isInput(p)) {
        putEdgePoint(p, newBlobId);
        // Mark it as consumed, but don't say by what.
        putConsumedPoint(p, null);
      } else {
        putConsumedPoint(p, newBlobId);
      }
    }

    redstoneDustBlobs.add(blob);
    return newBlobId;
  }

  public Bounds bounds() {
    return Bounds.makeFromBounds(redstoneDustBlobs);
  }

  /**
   * Requires that this constructor is not empty.
   *
   * @return A block map
   */
  public AbsolutePhysical3DMap toRedstoneSpace() {
    Bounds originalBounds = bounds();
    Function<Point3D, Point3D> translation = new Offset(originalBounds.getLower().negate());
    Function<Point3D, Point3D> scaledTranslation = translation.compose(new Scale(2)).compose(new Offset(0, 1, 0));

    // newBounds does not include the bottom block in y, which will only ever have stone.
    Bounds newBounds = originalBounds.transform(scaledTranslation);

    AbsolutePhysical3DMap r = new AbsolutePhysical3DMap(newBounds.getUpper());

    for (int blobID = 0; blobID < redstoneDustBlobs.size(); blobID++) {
      AnnotatedPathDrawer baseBlob = redstoneDustBlobs.get(blobID);
      for (Pair<AnnotatedEdge, Point3D> connection : baseBlob) {
        r.writeEdge(connection.getFirst(), new Offset(connection.getSecond()));
      }
    }

    return r;
  }
}
