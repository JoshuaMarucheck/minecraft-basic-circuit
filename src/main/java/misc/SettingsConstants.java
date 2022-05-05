package misc;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SettingsConstants {
  public static final String fileExtension = ".schematic";
  /**
   * Various resource paths
   */
  public static final Path root = Paths.get("src").resolve("main").resolve("resources");
  public static final Path circuitRoot = root.resolve("circuits");
  public static final Path nbtRoot = root.resolve("nbt");

  public static final String OS = System.getProperty("os.name").toLowerCase();


  /**
   * The minecraft directory
   */
  public static final Path mcRoot;
  /**
   * The local path from the minecraft directory to the worldedit schematics directory
   */
  public static final Path mcSchematicLocalPath = Paths.get("config").resolve("worldedit").resolve("schematics");
  /**
   * The folder where schematics are kept for WorldEdit
   */
  public static final Path mcSchematicRoot;

  static {
    if (OS.contains("win")) {
      // Windows
      mcRoot = Paths.get(System.getProperty("user.home")).resolve("AppData").resolve("Roaming").resolve(".minecraft");
    } else if (OS.contains("mac")) {
      // MacOS
      mcRoot = Paths.get(System.getProperty("user.home")).resolve("Library").resolve("Application Support").resolve("minecraft");
    } else {
      // Assume it's linux, probably
      mcRoot = Paths.get(System.getProperty("user.home")).resolve(".minecraft");
    }
    mcSchematicRoot = mcRoot.resolve(mcSchematicLocalPath);
  }

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
  public static final int SCHEMATIC_WIDTH_LIMIT = 32;
}
