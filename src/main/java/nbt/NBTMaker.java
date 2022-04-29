package nbt;

import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.io.CompressionType;
import dev.dewy.nbt.tags.collection.CompoundTag;
import physical.things.BlockConstant;
import physical.things.Bounds;
import physical.things.Point3D;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static nbt.BlockIdConstants.*;
import static nbt.Constants.nbtRoot;

public class NBTMaker {
  public static final Nbt NBT = new Nbt();


  /**
   * @param blocks Ordered XYZ
   * @return {@code null} if the specified range has no blocks other than {@code BlockConstant.EMPTY}
   */
  public static CompoundTag subrangeToNbt(BlockConstant[][][] blocks, Bounds b) throws IOException, SNBTParser.SNBTParseException {
    CompoundTag tag = baseTag();

    Point3D size = b.size();

    tag.putShort("Width", (short) size.getX());
    tag.putShort("Height", (short) size.getY());
    tag.putShort("Length", (short) size.getZ());

    byte[] data = flattenSubrange(blocks, b, palette);
    if (data == null) {
      return null;
    }

    tag.putByteArray("BlockData", data);

    return tag;
  }

  /**
   * @param blocks Ordered XYZ
   */
  public static CompoundTag toNbt(BlockConstant[][][] blocks) throws IOException, SNBTParser.SNBTParseException {
    CompoundTag tag = baseTag();

    tag.putShort("Width", (short) blocks.length);
    tag.putShort("Height", (short) blocks[0].length);
    tag.putShort("Length", (short) blocks[0][0].length);

    tag.putByteArray("BlockData", flatten(blocks, palette));

    return tag;
  }

  private static CompoundTag baseTag() throws IOException, SNBTParser.SNBTParseException {
    CompoundTag tag = (CompoundTag) SNBTParser.fromFile(Paths.get(nbtRoot).resolve("base_tag.json").toFile());
    tag.put(paletteTag);
    tag.put(paletteSize);
    return tag;
  }

  /**
   * @param blocks Ordered XYZ, where we need YZX
   */
  private static byte[] flattenSubrange(BlockConstant[][][] blocks, Bounds b, Map<BlockConstant, Byte> palette) {
    int xLo = b.getLower().getX();
    int yLo = b.getLower().getY();
    int zLo = b.getLower().getZ();
    int xHi = b.getUpper().getX() + 1;
    int yHi = b.getUpper().getY() + 1;
    int zHi = b.getUpper().getZ() + 1;

    int xWidth = xHi - xLo;
    int yWidth = yHi - yLo;
    int zWidth = zHi - zLo;

    byte[] r = new byte[xWidth * yWidth * zWidth];
    int i = 0;

    boolean hasContent = false;
    for (int y = yLo; y < yHi; y++) {
      for (int z = zLo; z < zHi; z++) {
        for (int x = xLo; x < xHi; x++) {
          BlockConstant bc = blocks[x][y][z];
          if (bc == null) {
            bc = BlockConstant.EMPTY;
          }
          if (bc != BlockConstant.EMPTY) {
            hasContent = true;
          }
          Byte id = palette.get(bc);
          if (id == null) {
            throw new IllegalArgumentException("BlockConstant without id: " + blocks[x][y][z]);
          }
          r[i++] = id;
        }
      }
    }

    if (hasContent) {
      return r;
    } else {
      return null;
    }
  }

  /**
   * @param blocks Ordered XYZ, where we need YZX
   */
  private static byte[] flatten(BlockConstant[][][] blocks, Map<BlockConstant, Byte> palette) {
    return flattenSubrange(blocks, arrayBounds(blocks), palette);
  }

  public static Bounds arrayBounds(BlockConstant[][][] blocks) {
    int xWidth = blocks.length;
    int yWidth = blocks[0].length;
    int zWidth = blocks[0][0].length;
    return Bounds.make(new Point3D(0, 0, 0), new Point3D(xWidth - 1, yWidth - 1, zWidth - 1));
  }

  /**
   * Compresses using gzip
   */
  public static void toFile(CompoundTag tag, File file) throws IOException {
    NBT.toFile(tag, file, CompressionType.GZIP);
  }
}
