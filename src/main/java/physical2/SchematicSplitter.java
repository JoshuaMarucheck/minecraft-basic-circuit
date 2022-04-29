package physical2;

import dev.dewy.nbt.tags.collection.CompoundTag;
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
  public final static int MAX_SIZE = 32;
  private final static Function<Point3D, Point3D> scaleSize = new Scale(MAX_SIZE);

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
    return new Point3D(upper.getX() / MAX_SIZE, upper.getY() / MAX_SIZE, upper.getZ() / MAX_SIZE);
  }

  private static boolean tooBig(Point3D upperBound) {
    return upperBound.getX() >= MAX_SIZE || upperBound.getY() >= MAX_SIZE || upperBound.getZ() >= MAX_SIZE;
  }
}
