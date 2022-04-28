package nbt;

import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.io.CompressionType;
import dev.dewy.nbt.tags.collection.CompoundTag;
import physical.things.BlockConstant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static nbt.BlockIdConstants.blockIds;
import static nbt.Constants.nbtRoot;

public class NBTMaker {
  public static final Nbt NBT = new Nbt();


  /**
   * @param blocks Ordered XYZ
   */
  public static CompoundTag toNbt(BlockConstant[][][] blocks) throws IOException, SNBTParser.SNBTParseException {
    CompoundTag tag = (CompoundTag) SNBTParser.fromFile(Paths.get(nbtRoot).resolve("base_tag.json").toFile());

    tag.putShort("Width", (short) blocks.length);
    tag.putShort("Height", (short) blocks[0].length);
    tag.putShort("Length", (short) blocks[0][0].length);

    tag.putByteArray("Blocks", flatten(blocks, blockIds));

    return tag;
  }

  /**
   * @param blocks Ordered XYZ, where we need YZX
   */
  private static byte[] flatten(BlockConstant[][][] blocks, Map<BlockConstant, Byte> blockIds) {
    int xWidth = blocks.length;
    int yWidth = blocks[0].length;
    int zWidth = blocks[0][0].length;

    byte[] r = new byte[xWidth * yWidth * zWidth];
    int i = 0;

    for (int y = 0; y < yWidth; y++) {
      for (int z = 0; z < zWidth; z++) {
        for (int x = 0; x < xWidth; x++) {
          BlockConstant bc = blocks[x][y][z];
          if (bc == null) {
            bc = BlockConstant.EMPTY;
          }
          Byte id = blockIds.get(bc);
          if (id == null) {
            throw new IllegalArgumentException("BlockConstant without id: " + blocks[x][y][z]);
          }
          r[i++] = id;
        }
      }
    }

    return r;
  }

  /**
   * Compresses using gzip
   */
  public static void toFile(CompoundTag tag, File file) throws IOException {
    NBT.toFile(tag, file, CompressionType.GZIP);
  }
}
