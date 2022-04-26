package physical2;

import circuit.AnnotatedCircuit;
import circuit.preconstructed.CircuitCollection;
import circuit.preconstructed.LowLevelCircuitGenerator;
import dev.dewy.nbt.tags.collection.CompoundTag;
import nbt.NBTMaker;
import physical2.blocks.BlockDrawer;
import physical2.blocks.PathDrawer;
import physical2.tiny.DefaultLegalPositions;
import physical2.tiny.VariableSignalPosMapAnnotated;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static nbt.Constants.root;
import static nbt.NBTMaker.NBT;

public class SimplifiedPhysicalCircuitPipeline {
  public static void circuitToSchematic(CircuitCollection cc, String name) throws IOException {
    circuitToSchematic(cc.get(name), Paths.get(root).getParent().resolve("schematic_out").resolve(name).toFile());
  }

  public static void circuitToSchematic(AnnotatedCircuit circuit, File outFile) throws IOException {
    VariableSignalPosMapAnnotated sigPosMap = VariableSignalPosMapAnnotated.makeWithAnnotations(circuit, new DefaultLegalPositions());
    PathDrawer pathDrawer = PathDrawer.makeLinear(sigPosMap, circuit.getGraph());
    BlockDrawer blockDrawer = new BlockDrawer(pathDrawer);
    CompoundTag tag = NBTMaker.toNbt(blockDrawer.getBlocks());
    NBT.toFile(tag, outFile);
  }
}
