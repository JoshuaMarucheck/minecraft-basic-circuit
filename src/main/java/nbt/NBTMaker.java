package nbt;

import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.tags.collection.CompoundTag;
import physical.things.BlockConstant;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static nbt.BlockIdConstants.blockIds;
import static nbt.Constants.root;

public class NBTMaker {
  public static final Nbt NBT = new Nbt();


  /**
   * @param blocks Ordered XYZ
   */
  public static CompoundTag toNbt(BlockConstant[][][] blocks) throws IOException {
    CompoundTag tag = NBT.fromFile(Paths.get(root).getParent().resolve("nbt/base_tag.txt").toFile());

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
          r[i++] = blockIds.get(blocks[x][y][z]);
        }
      }
    }

    return r;
  }
}
