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

    Map<Integer, Map<Point2D, Pair<Side, Side>>> paths = drawer.getPaths();
    for (Integer z : paths.keySet()) {
      Map<Point2D, Pair<Side, Side>> layer = paths.get(z);
      for (Point2D p : layer.keySet()) {
        Pair<Side, Side> sides = layer.get(p);
        blocks.putPlate(z, p, sides);
      }
    }
  }
}
