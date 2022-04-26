import circuit.preconstructed.CircuitCollection;
import circuit.preconstructed.LowLevelCircuitGenerator;

import java.io.File;

import static nbt.Constants.root;
import static physical2.SimplifiedPhysicalCircuitPipeline.circuitToSchematic;

public class SimplePipelineTester {
  public static void main(String[] args) throws Exception {
    LowLevelCircuitGenerator gen = LowLevelCircuitGenerator.canonicalGenerator;
    CircuitCollection cc64 = LowLevelCircuitGenerator.defaultNamedCircuits();
    cc64.addAll(gen.operators(64));

    cc64.getOrLoad(new File(root + "final/is_palindrome.txt"));

    circuitToSchematic(cc64, "is_palindrome");
  }
}
