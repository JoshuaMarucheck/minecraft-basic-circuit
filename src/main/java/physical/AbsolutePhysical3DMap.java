package physical;

import circuit.Pair;
import physical.things.BlockConstant;
import physical.things.Point3D;
import physical.transforms.Offset;

import java.util.Arrays;

public class AbsolutePhysical3DMap {
  private BlockConstant[][][] blocks;

  public AbsolutePhysical3DMap(Point3D size) {
    this(size.getX(), size.getY(), size.getZ());
  }

  public AbsolutePhysical3DMap(int x, int y, int z) {
    blocks = new BlockConstant[x][y][z];
    for (BlockConstant[][] plane : blocks) {
      for (BlockConstant[] row : plane) {
        Arrays.fill(row, BlockConstant.EMPTY);
      }
    }
  }

  public void writeEdge(AnnotatedEdge edge, Offset offset) {
    for (Pair<Point3D, BlockConstant> pair : edge) {
      Point3D point = offset.apply(pair.getFirst());
      BlockConstant block = pair.getSecond();

      writeBlock(point, block);
    }
  }

  public void writeBlock(Point3D point, BlockConstant block) {
    if (getBlock(point) == BlockConstant.EMPTY) {
      writeBlockUnsafe(point, block);
    } else if (getBlock(point) != block) {
      throw new IllegalArgumentException("Blocks are in conflict with each other at position " + point.toString());
    }
  }

  /**
   * Does no checking against the old value of the array
   */
  private void writeBlockUnsafe(Point3D p, BlockConstant block) {
    blocks[p.getX()][p.getY()][p.getZ()] = block;
  }

  public BlockConstant getBlock(Point3D p) {
    return blocks[p.getX()][p.getY()][p.getZ()];
  }
}
