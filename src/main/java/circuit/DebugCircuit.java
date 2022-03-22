package circuit;

import graph.TriState;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DebugCircuit extends AnnotatedCircuit {

  /**
   * Sub circuit id to global name
   */
  protected HashMap<Integer, String> globalNames;
  /**
   * global name to sub circuit id
   */
  protected Map<String, Integer> circuitIds;
  /**
   * Sub circuit id to local name
   */
  protected HashMap<Integer, String> localNames;
  /**
   * Sub circuit id to an array of global output positions in order
   */
  protected HashMap<Integer, Integer[]> outputPositions;

  public DebugCircuit(AnnotatedCircuit ac, HashMap<Integer, String> globalNames, HashMap<Integer, String> localNames, HashMap<Integer, Integer[]> outputPositions) {
    super(ac.redstone, ac.inputs, ac.outputs, ac.inputSizes, ac.outputSizes);
    this.globalNames = globalNames;
    this.localNames = localNames;
    this.outputPositions = outputPositions;
    this.circuitIds = reverseMap(globalNames);
  }

  private <K, V> Map<V, K> reverseMap(Map<K, V> map) {
    HashMap<V, K> r = new HashMap<V, K>();
    for (K key : map.keySet()) {
      V val = map.get(key);
      if (r.containsKey(val)) {
        throw new RuntimeException("Overlapping value \"" + val + "\"");
      }
      r.put(val, key);
    }
    return r;
  }

  public String getLocalName(Integer subCircuitId) {
    return localNames.get(subCircuitId);
  }

  public String getGlobalName(Integer subCircuitId) {
    return globalNames.get(subCircuitId);
  }

  public Integer getCircuitId(String outputName) {
    return circuitIds.get(outputName);
  }

  /**
   * @param input The input bits to the circuit
   * @return A map from global variable names to their multibit values
   */
  public HashMap<String, boolean[]> simulateDebug(boolean[] input) {
    if (input.length != this.inputSize()) {
      throw new UnsupportedOperationException("Invalid input length: Got " + input.length + " bits when the circuit needed " + this.inputSize() + "!");
    }

    Stack<Pair<Integer, Boolean>> assertStateStack = new Stack<Pair<Integer, Boolean>>();

    for (int i = 0; i < input.length; i++) {
      boolean b = input[i];
      assertStateStack.push(new Pair<Integer, Boolean>(this.inputs[i], b));
    }

    TriState[] triState = simulateFull(assertStateStack);

    HashMap<String, boolean[]> r = new HashMap<String, boolean[]>();

    for (Integer circuitId : globalNames.keySet()) {
      String globalName = globalNames.get(circuitId);

      Integer[] outPositions = outputPositions.get(circuitId);
      boolean[] subOutput = new boolean[outPositions.length];
      r.put(globalName, subOutput);

      int index = 0;
      for (int outputNode : outPositions) {
        switch (triState[outputNode]) {
          case FALSE:
            subOutput[index] = false;
            break;
          case TRUE:
            subOutput[index] = true;
            break;
          case UNKNOWN:
            throw new IllegalStateException("The circuit was not able to be fully modeled");
        }
        index++;
      }
    }

    return r;
  }
}
