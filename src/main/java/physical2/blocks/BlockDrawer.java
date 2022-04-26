package physical2.blocks;

import physical.things.BlockConstant;
import physical2.two.Point2D;

import java.util.Map;

public class BlockDrawer {
  private AbsolutePhysical3DMap2 blocks;

  public BlockDrawer(PathDrawer<?> drawer) {
    blocks = new AbsolutePhysical3DMap2(drawer.bounds());

    Map<Integer, Map<Point2D, SquareSpecifier>> paths = drawer.getPaths();
    for (Integer z : paths.keySet()) {
      Map<Point2D, SquareSpecifier> layer = paths.get(z);
      for (Point2D p : layer.keySet()) {
        SquareSpecifier sides = layer.get(p);
        blocks.putPlate(z, p, sides);
      }
    }
  }

  /**
   * Plz donut edit
   */
  public BlockConstant[][][] getBlocks() {
    return blocks.getBlocks();
  }
}
