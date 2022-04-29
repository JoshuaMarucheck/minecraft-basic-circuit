import nbt.Constants;
import robot.SchematicFiller;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static physical2.SchematicSplitter.MAX_SIZE;

public class FillSchematic {
  private static final Path parent = Paths.get(Constants.root).resolve("schematic");

  public static void main(String[] args) throws AWTException {
    Scanner sc = new Scanner(System.in);
    System.out.print("Name of schematic: ");
    String line = sc.nextLine();
    if (line.equals("")) {
      System.out.println("Defaulting to 'is_palindrome'");
      line = "is_palindrome";
    }
    new SchematicFiller(MAX_SIZE).constructSchematic(parent.resolve(line).toFile(), line);
  }
}
