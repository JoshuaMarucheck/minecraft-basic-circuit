package nbt;

import physical.things.BlockConstant;

import java.util.HashMap;
import java.util.Map;

public class BlockIdConstants {
  public static byte AIR = 0;
  public static byte STONE = 1;
  public static byte REDSTONE_WIRE = 55;
  public static byte PISTON = 33;
  public static byte GLOWSTONE = 89;
  public static byte REDSTONE_LAMP = 123;
  //  public static byte REDSTONE_BLOCK = 152;
  public static byte REDSTONE_TORCH = 76;


  public static final Map<BlockConstant, Byte> blockIds = makeDefaultMap();

  private static Map<BlockConstant, Byte> makeDefaultMap() {
    Map<BlockConstant, Byte> r = new HashMap<>();
    r.put(BlockConstant.AIR, AIR);
    r.put(BlockConstant.REDSTONE_TORCH_BASE, REDSTONE_LAMP);
    r.put(BlockConstant.REDSTONE_TORCH, REDSTONE_TORCH);
    r.put(BlockConstant.REDSTONE, REDSTONE_WIRE);
    r.put(BlockConstant.REDSTONE_BASE, STONE);
    r.put(BlockConstant.EMPTY, AIR);
    return r;
  }
}
