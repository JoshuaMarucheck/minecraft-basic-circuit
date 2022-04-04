package physical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Some basic limits to remember:
 * <p>
 * We're spreading the redstone out so that we don't need to worry about adjacency,
 * only overlapping. However, this means we get a distance of 8 (taxicab) along the
 * longest path in any single Point3DCollection.
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
  private ArrayList<Point3DCollection> redstoneDustBlobs;
  private Map<Point3D, Integer> consumedPoints;
  /**
   * Maps a torch spot to the id of the redstone blob which it outputs to.
   * (Note that the id of the blob which it comes from is in consumedPoints.)
   */
  private Map<Point3D, Integer> edgePoints;

  public PhysicalCircuitConstructor() {
    redstoneDustBlobs = new ArrayList<>();
    consumedPoints = new HashMap<>();
    edgePoints = new HashMap<>();
  }

  private void putConsumedPoint(Point3D p, int blobId) {
    if (consumedPoints.containsKey(p)) {
      throw new IllegalArgumentException("Constructor already contained point");
    }
    consumedPoints.put(p, blobId);
  }

  private void putEdgePoint(Point3D p, int blobId) {
    Integer edgeOutput = edgePoints.get(p);
    if (edgeOutput != null && edgeOutput != blobId) {
      throw new IllegalArgumentException("Connections between two blobs do not match!");
    }
    edgePoints.put(p, blobId);
  }

  /**
   * @param blob                A redstone blob
   * @param intentionalOverlaps A map marking the overlaps of this blob. {@code true} stands for outputs from the given blob, {@code false} stands for inputs.
   * @return The id of the blob of redstone added.
   */
  public int addRedstoneBlob(Point3DCollection blob, Map<Point3D, Boolean> intentionalOverlaps) {
    int newBlobId = redstoneDustBlobs.size();
    for (Point3D p : blob) {
      boolean overlap = consumedPoints.containsKey(p);
      if (overlap != intentionalOverlaps.containsKey(p)) {
        if (overlap) {
          throw new RuntimeException("Overlapping parts");
        } else {
          throw new RuntimeException("Nonoverlap when overlap was expected");
        }
      }
      if (overlap) {
        int fromBlobId = consumedPoints.get(p);
        if (intentionalOverlaps.get(p)) {
          // this outputs into something else
          putConsumedPoint(p, newBlobId);
          putEdgePoint(p, fromBlobId);
        } else {
          // something outputs into this
          putEdgePoint(p, newBlobId);
        }
      } else {
        putConsumedPoint(p, newBlobId);
      }
    }
    redstoneDustBlobs.add(blob);
    return newBlobId;
  }


}
