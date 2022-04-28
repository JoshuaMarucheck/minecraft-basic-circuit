package physical2;

import circuit.AnnotatedCircuit;
import circuit.preconstructed.CircuitCollection;
import dev.dewy.nbt.tags.collection.CompoundTag;
import nbt.NBTMaker;
import nbt.SNBTParser;
import physical2.blocks.BlockDrawer;
import physical2.blocks.PathAccumulator;
import physical2.tiny.DefaultLegalPositions;
import physical2.tiny.VariableSignalPosMapAnnotated;
import physical2.tiny.XIter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static nbt.Constants.root;

/**
 * Creates a not space efficient redstone setup representing the given circuit.
 */
public class SimplifiedPhysicalCircuitPipeline {
  public static void circuitToSchematic(CircuitCollection cc, String name, boolean verbose) throws IOException {
    circuitToSchematic(cc.get(name).trim(), Paths.get(root).resolve("schematic").resolve(name + ".schematic").toFile(), verbose);
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
    PathAccumulator pathAccumulator = PathAccumulator.makeLinear(sigPosMap, circuit.getGraph());
    print("Drawing blocks", verbose);
    BlockDrawer blockDrawer = new BlockDrawer(pathAccumulator);
    print("Size: " + blockDrawer.size(), verbose);
    print("Building tag", verbose);
    CompoundTag tag;
    try {
      tag = NBTMaker.toNbt(blockDrawer.getBlocks());
    } catch (SNBTParser.SNBTParseException e) {
      throw new IOException("Invalid NBT tag", e);
    }
    print("Writing to file", verbose);
    NBTMaker.toFile(tag, outFile);
  }

  private static void print(String s, boolean verbose) {
    if (verbose) {
      System.out.println(s);
    }
  }
}
