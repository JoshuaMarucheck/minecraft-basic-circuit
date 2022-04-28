package nbt;

import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.primitive.IntTag;
import physical.things.BlockConstant;

import java.util.HashMap;
import java.util.Map;

public class BlockIdConstants {

  public static void main(String[] args) {
  }

  public static final Map<BlockConstant, Byte> palette;
  public static final CompoundTag paletteTag;
  public static final IntTag paletteSize;

  static {
    Map<BlockConstant, String> blockIds = makeDefaultMap();
    Map<String, Byte> paletteMap = makePaletteMap(blockIds);
    palette = joinMap(blockIds, paletteMap);
    paletteTag = makePaletteTag(paletteMap);
    paletteSize = new IntTag("PaletteMax", paletteTag.size());
  }

  private static <A, B, C> Map<A, C> joinMap(Map<A, B> aToB, Map<B, C> bToC) {
    Map<A, C> r = new HashMap<>();
    for (A a : aToB.keySet()) {
      r.put(a, bToC.get(aToB.get(a)));
    }
    return r;
  }

  private static <T> Map<T, Byte> makePaletteMap(Map<?, T> map) {
    Map<T, Byte> r = new HashMap<>();
    byte b = 0;
    for (Object key : map.keySet()) {
      T value = map.get(key);
      if (!r.containsKey(value)) {
        r.put(value, b++);
        if (b == -1) {
          throw new IllegalStateException("Too many block types in the palette");
        }
      }
    }
    return r;
  }

  private static CompoundTag makePaletteTag(Map<String, Byte> paletteMap) {
    CompoundTag tag = new CompoundTag("Palette");
    for (String t : paletteMap.keySet()) {
      tag.put(t, new IntTag(paletteMap.get(t)));
    }
    return tag;
  }

  private static Map<BlockConstant, String> makeDefaultMap() {
    Map<BlockConstant, String> r = new HashMap<>();
    r.put(BlockConstant.AIR, "air");
    r.put(BlockConstant.REDSTONE_TORCH_BASE, "redstone_lamp");
    r.put(BlockConstant.REDSTONE_TORCH, "redstone_torch");
    r.put(BlockConstant.REDSTONE, "redstone_wire");
    r.put(BlockConstant.REDSTONE_BASE, "stone");
    r.put(BlockConstant.EMPTY, "air");
    r.put(BlockConstant.DOWN_PISTON, "sticky_piston[facing=down]");
    r.put(BlockConstant.REDSTONE_BLOCK, "redstone_block");
    r.put(BlockConstant.REPEATER_X, "repeater[facing=west]");
    r.put(BlockConstant.REPEATER_X_, "repeater[facing=east]");
    r.put(BlockConstant.REPEATER_Z, "repeater[facing=north]");
    r.put(BlockConstant.REPEATER_Z_, "repeater[facing=south]");
    r.put(BlockConstant.TOP_SLAB, "stone_slab[type=top]");
    r.put(BlockConstant.CIRCUIT_INPUT, "lever[face=wall,facing=north]");
    r.put(BlockConstant.CIRCUIT_OUTPUT, "redstone_lamp");
    r.put(BlockConstant.ERROR, "gilded_blackstone");
    r.put(BlockConstant.REDSTONE_WALL_TORCH_RIGHT, "redstone_wall_torch[facing=west]");
    r.put(BlockConstant.REDSTONE_WALL_TORCH_LEFT, "redstone_wall_torch[facing=east]");
    return r;
  }
}
