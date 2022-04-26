package physical2.blocks;

import physical.things.BlockConstant;
import physical.things.Bounds;
import physical.things.Point3D;
import physical.transforms.Offset;
import physical2.two.Point2D;

import java.util.Map;
import java.util.function.Function;

public class AbsolutePhysical3DMap2 {
  private BlockConstant[][][] blocks;
  private Function<Point3D, Point3D> scale3;

  public AbsolutePhysical3DMap2(Bounds b) {
    Offset offset = new Offset(b.getLower().negate());

    scale3 = offset.compose(p -> new Point3D(5 * p.getX(), 5 * p.getY(), mapZ(p.getZ())));

    Point3D offsetUpper = scale3.apply(b.getUpper());
    blocks = new BlockConstant[offsetUpper.getX()][offsetUpper.getY()][offsetUpper.getZ()];
  }

  private int mapZ(int z) {
    z = z * 2;
    z += z / 15;
    return z;
  }

  private Point3D to3D(Point2D p, int z) {
    return new Point3D(p.getX(), p.getY(), z);
  }

  /**
   * In tiny coords
   */
  public void putPlate(int z, Point2D localPos, SquareSpecifier spec) {
    Map<Point2D, BlockConstant> square = SideMapping.get(spec);
    for (Point2D p : square.keySet()) {
      putBlock(to3D(p.add(localPos), z), square.get(p));
    }
  }

  public void putBlock(Point3D p, BlockConstant bc) {
    putBlockRaw(scale3.apply(p), bc);
  }

  public void putBlockRaw(Point3D p, BlockConstant bc) {
    BlockConstant oldBc = blocks[p.getX()][p.getY()][p.getZ()];

    if (oldBc == BlockConstant.EMPTY) {
      putBlockRawUnsafe(p, bc);
    } else if (oldBc != bc) {
      throw new IllegalArgumentException("Block type mismatch overlap");
    }
  }

  public void putBlockRawUnsafe(Point3D p, BlockConstant bc) {
    blocks[p.getX()][p.getY()][p.getZ()] = bc;
  }

  /**
   * Plz donut edit
   */
  public BlockConstant[][][] getBlocks() {
    return blocks;
  }
}
