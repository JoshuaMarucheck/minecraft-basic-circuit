package physical2.blocks;

import physical.things.BlockConstant;
import physical2.one.Range;
import physical2.tiny.VariableSignalPosMap;
import physical2.tiny.VariableSignalPosMapAnnotated;
import physical2.two.Point2D;

import java.util.Iterator;
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

    for (Point2D xy : drawer.rangePositions()) {
      blocks.putForwardSignal(xy, drawer.getZRange(xy));
    }

    VariableSignalPosMap<?> varPosMap = drawer.getVarPosMap();
    if (varPosMap instanceof VariableSignalPosMapAnnotated) {
      for (Iterator<Point2D> it = ((VariableSignalPosMapAnnotated) varPosMap).inputPosIterator(); it.hasNext(); ) {
        Point2D input = it.next();
        blocks.putForwardSignal(input, Range.make(0, drawer.getZRange(input).getLower()));
      }
      for (Iterator<Point2D> it = ((VariableSignalPosMapAnnotated) varPosMap).outputPosIterator(); it.hasNext(); ) {
        Point2D output = it.next();
        blocks.putForwardSignal(output, Range.make(drawer.getZRange(output).getUpper(), blocks.maxZ()));
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
