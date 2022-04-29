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

import static physical2.blocks.SideMapping.*;

public class AbsolutePhysical3DMap2 {
  private BlockConstant[][][] blocks;
  private Function<Point3D, Point3D> scale3;
  /**
   * In zoomed out coords
   */
  private Bounds validPositions;

  /**
   * @param b Path bounds
   */
  public AbsolutePhysical3DMap2(Bounds b) {
    validPositions = b;
    Offset offset = new Offset(b.getLower().negate());

    Function<Point3D, Point3D> simpleScale = p -> new Point3D(X_SCALE * p.getX(), mapY(p.getY()), mapZ(p.getZ()));
    scale3 = simpleScale.compose(offset);

    Point3D offsetUpper = scale3.apply(b.getUpper()).translate(X_BUFFER, Y_BUFFER, 0);
    // Translate by 1 in each direction to capture the outermost edge
    // Translate by an extra 1 in z to capture the circuit output
    offsetUpper = offsetUpper.translate(1, 1, 2);
    blocks = new BlockConstant[offsetUpper.getX()][offsetUpper.getY()][offsetUpper.getZ()];
  }

  public Point3D size() {
    return new Point3D(blocks.length, blocks[0].length, blocks[0][0].length);
  }

  public static int mapY(int y) {
    return 1 + Y_SCALE * y;
  }

  /**
   * Adds an extra 1 for each repeater that needs to be placed
   * Also adds 1 to give space for input
   */
  public static int mapZ(int z) {
    z = z * 2;
    z += 1;
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
      BlockConstant redstone = mod(z, 8) == 6 ? BlockConstant.REPEATER_Z : BlockConstant.REDSTONE;
      putBlockRaw(new Point3D(lo.getX(), lo.getY(), z), redstone);
      putBlockRaw(new Point3D(lo.getX(), lo.getY() - 1, z), BlockConstant.REDSTONE_BASE);
    }
  }

  public void putInput(Point2D xy) {
    putBlockRaw(
        scale3.apply(new Point3D(xy.getX(), xy.getY(), 0)).translate(0, -1, -1),
        BlockConstant.CIRCUIT_INPUT
    );
  }

  public void putOutput(Point2D xy) {
    putBlockRaw(
        scale3.apply(new Point3D(xy.getX(), xy.getY(), maxZ())).translate(0, 0, 1),
        BlockConstant.REDSTONE
    );
    putBlockRaw(
        scale3.apply(new Point3D(xy.getX(), xy.getY(), maxZ())).translate(0, -1, 1),
        BlockConstant.CIRCUIT_OUTPUT
    );
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
      Point3D basePoint = scale3.apply(to3D(p, z));
      Map<Point2D, BlockConstant> square = pair.getSecond();
      for (Point2D localPos : square.keySet()) {
        putBlockRaw(basePoint.translate(localPos.getX(), localPos.getY(), 0), square.get(localPos));
      }
    }
  }

  public void putBlock(Point3D p, BlockConstant bc) {
    putBlockRaw(scale3.apply(p), bc);
  }

  public void putBlockRaw(Point3D p, BlockConstant bc) {
    BlockConstant oldBc = blocks[p.getX()][p.getY()][p.getZ()];

    if (oldBc == null || oldBc == BlockConstant.EMPTY) {
      putBlockRawUnsafe(p, bc);
    } else if (oldBc != bc) {
      System.err.println("Block type mismatch overlap at " + p);
      putBlockRawUnsafe(p, BlockConstant.ERROR);
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
    return validPositions.getUpper().getZ();
  }
}
