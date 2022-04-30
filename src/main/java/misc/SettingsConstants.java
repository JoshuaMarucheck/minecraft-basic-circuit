package misc;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SettingsConstants {
  /**
   * Various resource paths
   */
  public static String root = "./src/main/resources/";
  public static String circuitRoot = root + "circuits/";
  public static String nbtRoot = root + "nbt/";

  /**
   * The folder where schematics are kept for WorldEdit
   */
  public static Path mcSchematicRoot = Paths.get(System.getProperty("user.home")).resolve("Library/Application Support/minecraft/config/worldedit/schematics/");

  /**
   * The number of milliseconds to wait after pressing or releasing a key.
   */
  public static final int KEY_PRESS_PAUSE = 50;

  /**
   * The height of the world (i.e. the maximum height of a full schematic)
   */
  public static final int WORLD_HEIGHT = 256;

  /**
   * The maximum length of a single schematic file on any given axis.
   */
  public final static int SCHEMATIC_WIDTH_LIMIT = 32;
}
