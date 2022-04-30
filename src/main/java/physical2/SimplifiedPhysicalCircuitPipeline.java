package physical2;

import circuit.AnnotatedCircuit;
import circuit.preconstructed.CircuitCollection;
import dev.dewy.nbt.tags.collection.CompoundTag;
import misc.SettingsConstants;
import nbt.NBTMaker;
import nbt.SNBTParser;
import physical.things.Point3D;
import physical2.blocks.BlockDrawer;
import physical2.blocks.PathAccumulator;
import physical2.tiny.DefaultLegalPositions;
import physical2.tiny.VariableSignalPosMapAnnotated;
import physical2.tiny.XIter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static misc.SettingsConstants.root;
import static misc.SettingsConstants.SCHEMATIC_WIDTH_LIMIT;

/**
 * Creates a not space efficient redstone setup representing the given circuit.
 */
public class SimplifiedPhysicalCircuitPipeline {

  public static void circuitToSchematic(CircuitCollection cc, String name, boolean verbose) throws IOException {
    circuitToSchematic(cc.get(name).trim(), Paths.get(root).resolve("schematic").resolve(name + SettingsConstants.fileExtension).toFile(), verbose);
  }

  public static void circuitToSchematic(AnnotatedCircuit circuit, File outFile, boolean verbose) throws IOException {
    VariableSignalPosMapAnnotated sigPosMap = new VariableSignalPosMapAnnotated(circuit, new DefaultLegalPositions());

    for (int i = 0; i < circuit.getMultibitInputCount(); i++) {
      sigPosMap.placeInput(i, new XIter(0, i));
    }
    for (int i = 0; i < circuit.getMultibitOutputCount(); i++) {
      sigPosMap.placeOutput(i, new XIter(0, i + circuit.getMultibitInputCount()));
    }

    print("Accumulating path", verbose);
    PathAccumulator pathAccumulator = PathAccumulator.makeQuadratic(sigPosMap, circuit.getGraph());
    print("Drawing blocks", verbose);
    BlockDrawer blockDrawer = new BlockDrawer(pathAccumulator);
    print("Size: " + blockDrawer.size(), verbose);
    print("Building tag", verbose);
    Map<Point3D, CompoundTag> tags;
    try {
      tags = SchematicSplitter.makeTags(blockDrawer);
    } catch (SNBTParser.SNBTParseException e) {
      throw new IOException("Invalid NBT tag", e);
    }
    if (tags.size() == 1) {
      print("Writing to file", verbose);
      NBTMaker.toFile(tags.get(tags.keySet().iterator().next()), outFile);
    } else {
      print("Too big for one file; splitting into " + tags.size() + " labelled files in shape " + SchematicSplitter.size(blockDrawer) + " with skip size " + SCHEMATIC_WIDTH_LIMIT, verbose);
      int dot = outFile.getName().lastIndexOf('.');
      String filePrefix = outFile.getName().substring(0, dot);
      Path parent = outFile.toPath().getParent().resolve(filePrefix);
      parent.toFile().mkdir();
      for (Point3D pos : tags.keySet()) {
        String fileName = pos.toStringWithoutSpaces() + SettingsConstants.fileExtension;
        File file = parent.resolve(fileName).toFile();
        NBTMaker.toFile(tags.get(pos), file);
      }
    }
  }

  private static void print(String s, boolean verbose) {
    if (verbose) {
      System.out.println(s);
    }
  }
}
