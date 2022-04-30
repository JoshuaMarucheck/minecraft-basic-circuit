import circuit.preconstructed.CircuitCollection;
import circuit.preconstructed.LowLevelCircuitGenerator;
import circuit.preconstructed.exceptions.MissingCircuitDependencyException;
import misc.SettingsConstants;
import robot.SchematicFiller;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import static misc.SettingsConstants.*;
import static physical2.SimplifiedPhysicalCircuitPipeline.circuitToSchematic;

public class NorCircuit {
  public static void main(String[] args) throws Exception {
    System.out.println("What are you here to do? (compile, paste)");
    Scanner sc = new Scanner(System.in);
    String line = sc.nextLine();
    switch (line) {
      case "compile":
        compile(sc);
        break;
      case "paste":
        paste(sc);
        break;
      default:
        throw new IllegalArgumentException("Unrecognized input \"" + line + "\"");
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
            cc64.getOrLoad(Paths.get(circuitRoot).resolve(line).toFile());
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

    File targetFile = SettingsConstants.mcSchematicRoot.resolve(line).toFile();
    File targetSingleFile = SettingsConstants.mcSchematicRoot.resolve(line + fileExtension).toFile();

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
      targetFile = SettingsConstants.mcSchematicRoot.resolve(line).toFile();
    }
    if (!targetFile.exists()) {
      targetFile = targetSingleFile;
    }
    if (targetFile.isDirectory()) {
      new SchematicFiller(SCHEMATIC_WIDTH_LIMIT).constructSchematic(SettingsConstants.mcSchematicRoot, Paths.get(line), Paths.get(line + "_out"), line);
    } else if (!targetSingleFile.isDirectory()) {
      System.out.println("You can paste that one manually. Run the following two commands in order:");
      System.out.println("//schem load " + line);
      System.out.println("//paste");
    }
  }
}
