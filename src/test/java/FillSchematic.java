import nbt.Constants;
import robot.SchematicFiller;

import java.awt.*;
import java.io.File;
import java.nio.file.Paths;

import static physical2.SchematicSplitter.MAX_SIZE;

public class FillSchematic {
  private static final File target = Paths.get(Constants.root).resolve("schematic").resolve("is_palindrome").toFile();

  public static void main(String[] args) throws AWTException {
    new SchematicFiller(MAX_SIZE).constructSchematic(target, "is_palindrome");
  }
}
