package physical2.blocks;

import physical.things.BlockConstant;
import physical2.two.Point2D;

import java.util.Map;

/**
 * from 0,0 to 4,4, approximately
 */

public class SideMapping {
  private static Map<SquareSpecifier, Map<Point2D, BlockConstant>> mappings;

  public static Map<Point2D, BlockConstant> get(SquareSpecifier spec) {
    return mappings.get(spec);
  }


}
