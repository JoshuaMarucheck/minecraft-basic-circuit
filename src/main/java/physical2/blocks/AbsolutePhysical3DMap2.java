package physical2.blocks;

import circuit.Pair;
import misc.PairSecondIterator;
import physical.things.BlockConstant;
import physical.things.Bounds;
import physical.things.Point3D;
import physical.transforms.Offset;
import physical2.one.Range;
import physical2.tiny.BentPath;
import physical2.two.Point2D;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static physical2.blocks.SideMapping.X_SCALE;
import static physical2.blocks.SideMapping.Y_SCALE;

public class AbsolutePhysical3DMap2 {
  private BlockConstant[][][] blocks;
  private Function<Point3D, Point3D> scale3;

  public AbsolutePhysical3DMap2(Bounds b) {
    Offset offset = new Offset(b.getLower().negate());

    scale3 = offset.compose(p -> new Point3D(X_SCALE * p.getX(), Y_SCALE * p.getY(), mapZ(p.getZ())));

    Point3D offsetUpper = scale3.apply(b.getUpper());
    blocks = new BlockConstant[offsetUpper.getX()][offsetUpper.getY()][offsetUpper.getZ()];
  }

  public static int mapZ(int z) {
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
  public void putForwardSignal(Point2D xy, Range zRange) {
    Point3D lo = scale3.apply(new Point3D(xy.getX(), xy.getY(), zRange.getLower()));
    Point3D hi = scale3.apply(new Point3D(xy.getX(), xy.getY(), zRange.getUpper()));

    for (int z = lo.getZ(); z < hi.getZ(); z++) {
      BlockConstant redstone = mod(z, 16) == 15 ? BlockConstant.REPEATER_Z : BlockConstant.REDSTONE;
      putBlockRaw(new Point3D(lo.getX(), lo.getY(), z), redstone);
      putBlockRaw(new Point3D(lo.getX(), lo.getY() - 1, z), BlockConstant.REDSTONE_BASE);
    }
  }

  private static int mod(int a, int b) {
    int r = a % b;
    if (r < 0) {
      r += b;
    }
    return r;
  }

    /**
     * In tiny coords
     */
  public void putPath(Integer z, BentPath bp) {
    Iterator<Pair<Point2D, Map<Point2D, BlockConstant>>> squares = new PairSecondIterator<>(bp.iterator(), SideMapping::iterateOverPath);
    for (; squares.hasNext(); ) {
      Pair<Point2D, Map<Point2D, BlockConstant>> pair = squares.next();
      Point2D p = pair.getFirst();
      Map<Point2D, BlockConstant> square = pair.getSecond();
      for (Point2D localPos : square.keySet()) {
        putBlock(to3D(p.add(localPos), z), square.get(localPos));
      }
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

  public int maxZ() {
    return blocks[0][0].length - 1;
  }
}
