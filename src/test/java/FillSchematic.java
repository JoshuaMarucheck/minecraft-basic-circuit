import misc.SettingsConstants;
import robot.SchematicFiller;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static misc.SettingsConstants.SCHEMATIC_WIDTH_LIMIT;

public class FillSchematic {
  public static void main(String[] args) throws AWTException {
    Scanner sc = new Scanner(System.in);
    System.out.print("Name of schematic: ");
    String line = sc.nextLine();
    if (line.equals("")) {
      System.out.println("Defaulting to 'is_palindrome'");
      line = "is_palindrome";
    }

    new SchematicFiller(SCHEMATIC_WIDTH_LIMIT).constructSchematic(SettingsConstants.mcSchematicRoot, Paths.get(line), Paths.get(line + "_out"), line);
  }
}
