package physical2.blocks;

import circuit.Pair;
import physical2.two.Point2D;
import physical2.two.Side;

import java.util.Map;

public class BlockDrawer {
  PathDrawer<?> drawer;
  AbsolutePhysical3DMap2 blocks;

  public BlockDrawer(PathDrawer<?> drawer) {
    this.drawer = drawer;
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
}
