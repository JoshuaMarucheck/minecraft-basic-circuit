package circuit;

import com.sun.istack.internal.NotNull;

import java.util.HashMap;

public class DebugCircuitBuilder extends AnnotationCircuitBuilder {
  private HashMap<Integer, String> localNames;
  private HashMap<Integer, String> globalNames;
  private HashMap<Integer, Integer[]> outputPositions;
  ;

  public DebugCircuitBuilder() {
    super();
    localNames = new HashMap<Integer, String>();
    globalNames = new HashMap<Integer, String>();
    outputPositions =  new HashMap<Integer, Integer[]>();
  }

  public void registerAsDebug(int circuitId, String globalName, String localName) {
    Circuit circuit = circuits.get(circuitId);

    localNames.put(circuitId, localName);
    globalNames.put(circuitId, globalName);
    outputPositions.put(circuitId, circuit.getOutputs());
  }

  public DebugCircuit toCircuit() {
    AnnotatedCircuit circuit = super.toCircuit();

    return new DebugCircuit(circuit, globalNames, localNames, outputPositions);
  }

}
