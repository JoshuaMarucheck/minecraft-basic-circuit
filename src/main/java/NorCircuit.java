import circuit.preconstructed.CircuitCollection;
import circuit.preconstructed.LowLevelCircuitGenerator;
import circuit.preconstructed.exceptions.MissingCircuitDependencyException;
import robot.SchematicFiller;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static misc.SettingsConstants.*;
import static physical2.SimplifiedPhysicalCircuitPipeline.circuitToSchematic;

public class NorCircuit {
  public static void main(String[] args) throws Exception {
    Scanner sc = new Scanner(System.in);
    flower: while (true) {
      System.out.println("What are you here to do? ([c]ompile, [p]aste)");
      String line = sc.nextLine();
      switch (line.toLowerCase()) {
        case "c":
        case "compile":
          compile(sc);
          break flower;
        case "p":
        case "paste":
          paste(sc);
          break flower;
        default:
          System.err.println("Unrecognized option \"" + line + "\"");
      }
    }
  }

  private static void compile(Scanner sc) throws IOException, MissingCircuitDependencyException {
    LowLevelCircuitGenerator gen = LowLevelCircuitGenerator.canonicalGenerator;
    CircuitCollection cc64 = LowLevelCircuitGenerator.defaultNamedCircuits();

    System.out.println("Enter the name of a circuit file to add it.");
    System.out.println("Enter an int to add common low level operators with that bit length.");

    String line;
    do {
      System.out.print("Add dependency: ");
      line = sc.nextLine();
      if (!line.equals("")) {
        try {
          int i = Integer.parseInt(line);
          cc64.addAll(gen.operators(i));
        } catch (NumberFormatException ignored) {
          try {
            cc64.getOrLoad(circuitRoot.resolve(line).toFile());
          } catch (IOException e1) {
            e1.printStackTrace();
          } catch (MissingCircuitDependencyException e1) {
            System.err.println(e1.getMessage());
          }
        }
      }
    } while (!line.equals(""));

    System.out.println();
    System.out.println("Now just state the name of the file(s) (no filepath, no extension).");
    do {
      System.out.print("Compile target: ");
      line = sc.nextLine();
      if (!line.equals("")) {
        if (cc64.contains(line)) {
          try {
            circuitToSchematic(cc64, line, true);
            System.out.println();
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          String err = "There is no circuit called \"" + line + "\"";
          if (line.indexOf('/') != -1) {
            err += " (did you remember to remove the filepath?)";
          }
          if (line.indexOf('.') != -1) {
            err += " (did you remember to remove the extension?)";
          }
          System.err.println(err);
        }
      }
    } while (!line.equals(""));
  }

  /**
   * Looks in the Minecraft directory under config/worldedit/schematics for schematics to match against.
   */
  private static void paste(Scanner sc) throws AWTException {
    System.out.print("Name of schematic (without extension): ");
    String line = sc.nextLine();
    if (line.equals("")) {
      System.out.println("Defaulting to 'is_palindrome'");
      line = "is_palindrome";
    }

    Path minecraftLoc = mcRoot;
    Path schematicLocalPath = mcSchematicLocalPath;
    Path schematicRoot = minecraftLoc.resolve(schematicLocalPath);
    if (!schematicRoot.toFile().exists()) {

      if (!minecraftLoc.toFile().exists()) {
        System.err.println("Minecraft directory not found. Where is minecraft on you computer?");
        minecraftLoc = Paths.get(sc.nextLine());
        if (!minecraftLoc.toFile().exists()) {
          throw new IllegalArgumentException("Minecraft directory not found");
        }
        schematicRoot = minecraftLoc.resolve(schematicLocalPath);
      }

      if (!schematicRoot.toFile().exists()) {
        System.err.println("Schematic directory not found. Where is it relative to the minecraft directory?");
        schematicLocalPath = Paths.get(sc.nextLine());
        schematicRoot = minecraftLoc.resolve(schematicLocalPath);
        if (!schematicRoot.toFile().exists()) {
          throw new IllegalArgumentException("Schematic directory not found");
        }
      }
    }
    File targetFile = schematicRoot.resolve(line).toFile();
    File targetSingleFile = schematicRoot.resolve(line + fileExtension).toFile();

    while (!(targetSingleFile.exists() || targetFile.exists()) || line.indexOf(' ') != -1) {
      if (line.indexOf(' ') != -1) {
        System.err.println("Commands cannot handle files with spaces.");
        if (targetSingleFile.exists() || targetFile.exists()) {
          System.err.println("Please rename the schematic file to something without spaces");
        }
      } else {
        System.err.println("That schematic does not exist");
      }

      line = sc.nextLine();
      targetFile = schematicRoot.resolve(line).toFile();
      targetSingleFile = schematicRoot.resolve(line + fileExtension).toFile();
    }
    if (!targetFile.exists()) {
      targetFile = targetSingleFile;
    }
    if (targetFile.isDirectory()) {
      new SchematicFiller(SCHEMATIC_WIDTH_LIMIT).constructSchematic(schematicRoot, Paths.get(line), Paths.get(line + "_out"), line);
    } else if (!targetSingleFile.isDirectory()) {
      System.out.println("You can paste that one manually. Run the following two commands in order:");
      System.out.println("//schem load " + line);
      System.out.println("//paste");
    }
  }
}
