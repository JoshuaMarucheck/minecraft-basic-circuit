package circuit;

import java.util.HashMap;

public class DebugCircuitBuilder extends AnnotationCircuitBuilder {
  private HashMap<Integer, String> localNames;
  private HashMap<Integer, String> globalNames;

  public DebugCircuitBuilder() {
    super();
    localNames = new HashMap<Integer, String>();
    globalNames = new HashMap<Integer, String>();
  }

  public void registerAsDebug(int circuitId, String globalName, String localName) {


    localNames.put(circuitId, localName);
    globalNames.put(circuitId, globalName);
  }

  private HashMap<Integer, int[]> constructOutputPositions() {
    HashMap<Integer, int[]> outputPositions = new HashMap<Integer, int[]>();

    for (Integer circuitId : localNames.keySet()) {
      Circuit circuit = circuits.get(circuitId);
      Integer[] circuitOutputs = circuit.getOutputs();
      int[] outPos = new int[circuitOutputs.length];
      for (int i = 0; i < circuitOutputs.length; i++) {
        outPos[i] = calculatePositionOfNode(circuitId, circuitOutputs[i]);
      }

      outputPositions.put(circuitId, outPos);
    }

    return outputPositions;
  }

  public DebugCircuit toCircuit() {
    AnnotatedCircuit circuit = super.toCircuit();

    return new DebugCircuit(circuit, globalNames, localNames, constructOutputPositions());
  }

}
