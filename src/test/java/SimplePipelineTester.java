import circuit.preconstructed.CircuitCollection;
import circuit.preconstructed.LowLevelCircuitGenerator;

import java.io.File;

import static misc.SettingsConstants.circuitRoot;
import static physical2.SimplifiedPhysicalCircuitPipeline.circuitToSchematic;

public class SimplePipelineTester {
  public static void main(String[] args) throws Exception {
    LowLevelCircuitGenerator gen = LowLevelCircuitGenerator.canonicalGenerator;
    CircuitCollection cc64 = LowLevelCircuitGenerator.defaultNamedCircuits();
    cc64.addAll(gen.operators(64));

    cc64.getOrLoad(new File(circuitRoot + "specialized/atLeast2.txt"));
    cc64.getOrLoad(new File(circuitRoot + "final/is_palindrome.txt"));

//    circuitToSchematic(cc64, "is_palindrome", true);
    circuitToSchematic(cc64, "atLeast2", true);
  }
}
