package physical.things;

/**
 * Some day, fill these in with the actual block ids.
 *
 * EMPTY represents not assigning a block yet, as opposed to AIR, which must be empty for the redstone to work.
 */
public enum BlockConstant {
  EMPTY, AIR, REDSTONE_BASE, REDSTONE, REDSTONE_TORCH_BASE, REDSTONE_TORCH, TOP_SLAB,
  REPEATER_X,REPEATER_Z,REPEATER_X_,REPEATER_Z_, DOWN_PISTON, REDSTONE_BLOCK;

  int toCanonicalBlockId(BlockConstant c) {
    switch (c) {
      case EMPTY:
      case AIR:
        return 0;
      case REDSTONE_BASE:
      case REDSTONE_TORCH_BASE:
        return 1;
      case REDSTONE:
        return 3;
      case REDSTONE_TORCH:
        return 4;
      default:
        throw new IllegalStateException("Invalid BlockConstant " + c.toString());
    }
  }
}
