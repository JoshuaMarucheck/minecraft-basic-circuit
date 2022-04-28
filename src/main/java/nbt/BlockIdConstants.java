package nbt;

import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.primitive.ByteTag;
import dev.dewy.nbt.tags.primitive.IntTag;
import physical.things.BlockConstant;

import java.util.HashMap;
import java.util.Map;

public class BlockIdConstants {

  public static final Map<BlockConstant, Byte> palette;
  public static final CompoundTag paletteTag;
  public static final IntTag paletteSize;

  static {
    Map<BlockConstant, String> blockIds = makeDefaultMap();
    palette = makeDefaultPallete(blockIds);
    paletteTag = makePaletteTag(blockIds, palette);
    paletteSize = new IntTag("PaletteMax", palette.size());
  }

  private static <T> CompoundTag makePaletteTag(Map<T, String> blockIds, Map<T, Byte> palette) {
    CompoundTag tag = new CompoundTag("Palette");
    for (T t : blockIds.keySet()) {
      tag.put(blockIds.get(t), new ByteTag(palette.get(t)));
    }
    return tag;
  }

  private static <T> Map<T, Byte> makeDefaultPallete(Map<T, ?> blockIds) {
    Map<T, Byte> r = new HashMap<>();
    byte b = 0;
    for (T t : blockIds.keySet()) {
      r.put(t, b++);
    }
    return r;
  }

  private static Map<BlockConstant, String> makeDefaultMap() {
    Map<BlockConstant, String> r = new HashMap<>();
    r.put(BlockConstant.AIR, "air");
    r.put(BlockConstant.REDSTONE_TORCH_BASE, "redstone_lamp");
    r.put(BlockConstant.REDSTONE_TORCH, "redstone_torch");
    r.put(BlockConstant.REDSTONE, "redstone_wire");
    r.put(BlockConstant.REDSTONE_BASE, "stone");
    r.put(BlockConstant.EMPTY, "air");
    r.put(BlockConstant.DOWN_PISTON, "piston[facing=down]");
    r.put(BlockConstant.REDSTONE_BLOCK, "redstone_block");
    r.put(BlockConstant.REPEATER_X, "piston[facing=west]");
    r.put(BlockConstant.REPEATER_X_, "piston[facing=east]");
    r.put(BlockConstant.REPEATER_Z, "piston[facing=south]");
    r.put(BlockConstant.REPEATER_Z_, "piston[facing=north]");
    r.put(BlockConstant.TOP_SLAB, "stone_slab[type=top]");
    return r;
  }
}
