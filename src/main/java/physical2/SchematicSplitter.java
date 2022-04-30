package physical2;

import dev.dewy.nbt.tags.collection.CompoundTag;
import misc.SettingsConstants;
import nbt.NBTMaker;
import nbt.SNBTParser;
import physical.things.Bounds;
import physical.things.Point3D;
import physical.transforms.Scale;
import physical2.blocks.BlockDrawer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SchematicSplitter {
  private final static Function<Point3D, Point3D> scaleSize = new Scale(SettingsConstants.SCHEMATIC_WIDTH_LIMIT);

  /**
   * @return map from position zoomed out to Pair(size, tag)
   */
  public static Map<Point3D, CompoundTag> makeTags(BlockDrawer blockDrawer) throws IOException, SNBTParser.SNBTParseException {
    Map<Point3D, CompoundTag> r = new HashMap<>();
    if (tooBig(blockDrawer.size())) {
      Bounds arrayBounds = NBTMaker.arrayBounds(blockDrawer.getBlocks());

      Bounds zoomedOutPointBounds = Bounds.make(new Point3D(0, 0, 0), size(blockDrawer).translate(-1, -1, -1));
      for (Point3D p : zoomedOutPointBounds) {
        Bounds b = Bounds.make(scaleSize.apply(p), scaleSize.apply(p.translate(1, 1, 1)).translate(-1, -1, -1));
        b = Bounds.restrict(b, arrayBounds);
        CompoundTag tag = NBTMaker.subrangeToNbt(blockDrawer.getBlocks(), b);
        if (tag != null) {
          r.put(p, tag);
        }
      }
    } else {
      r.put(new Point3D(0, 0, 0), NBTMaker.toNbt(blockDrawer.getBlocks()));
    }
    return r;
  }

  public static Point3D size(BlockDrawer blockDrawer) {
    Point3D upper = blockDrawer.size();
    return new Point3D(schematicWidth(upper.getX()), schematicWidth(upper.getY()), schematicWidth(upper.getZ()));
  }

  /**
   * Division by SettingsConstants.SCHEMATIC_WIDTH_LIMIT rounded up.
   */
  private static int schematicWidth(int blockWidth) {
    return (blockWidth + SettingsConstants.SCHEMATIC_WIDTH_LIMIT - 1) / SettingsConstants.SCHEMATIC_WIDTH_LIMIT;
  }

  private static boolean tooBig(Point3D upperBound) {
    return upperBound.getX() >= SettingsConstants.SCHEMATIC_WIDTH_LIMIT || upperBound.getY() >= SettingsConstants.SCHEMATIC_WIDTH_LIMIT || upperBound.getZ() >= SettingsConstants.SCHEMATIC_WIDTH_LIMIT;
  }
}
