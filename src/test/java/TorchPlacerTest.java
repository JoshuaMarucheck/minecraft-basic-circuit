import circuit.AnnotatedCircuit;
import circuit.preconstructed.LowLevelCircuitGenerator;
import physical.edges.TorchEndsPlacer;
import physical.edges.TorchPlacer;
import physical.things.Point3D;
import physical.transforms.Offset;

import java.io.IOException;

public class TorchPlacerTest {
  public static void main(String[] args) throws IOException {
    LowLevelCircuitGenerator gen = LowLevelCircuitGenerator.canonicalGenerator;

    AnnotatedCircuit circuit = gen.addition(4);
    TorchEndsPlacer<Integer> torchEndsPlacer = TorchEndsPlacer.make(circuit);

    int i;
    for (i = 0; i < circuit.getMultibitInputCount(); i++) {
      torchEndsPlacer.placeInput(
          i,
          new Point3D(0, 128 + 2 * i, 0),
          new Offset(0, 0, 2)
      );
    }

    for (int j = 0; j < circuit.getMultibitOutputCount(); j++) {
      torchEndsPlacer.placeOutput(
          j,
          new Point3D(0, 128 + 2 * (i + j), 0),
          new Offset(0, 0, 2)
      );
    }

    TorchPlacer torchPlacer = torchEndsPlacer.getTorchPlacer();
    torchPlacer.placeTorchesRandomly();
  }
}
