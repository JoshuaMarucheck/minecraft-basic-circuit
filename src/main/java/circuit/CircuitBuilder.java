package circuit;

import com.sun.istack.internal.NotNull;
import graph.Edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Circuits are basically just directed graphs.
 */
public class CircuitBuilder {
  protected ArrayList<Circuit> circuits;
  /**
   * A map from the id of a circuit to the ids of its inputs
   */
  protected Map<Integer, ArrayList<Integer>> circuitInputs;
  private int inputCircuit;
  private int outputCircuit;

  public CircuitBuilder() {
    circuits = new ArrayList<>();
    circuitInputs = new HashMap<>();
  }

  /**
   * This function is @NotNull.
   *
   * Note that Circuits are supposed to be immutable.
   * Hence, if multiple copies of the same circuit object are added, they will be treated as different instances.
   *
   * @param circuit
   * @return The id used by this CircuitBuilder to refer to the given circuit.
   */
  public int addCircuit(@NotNull Circuit circuit) {
    circuits.add(circuit);
    int id = circuits.size() - 1;
    circuitInputs.put(id, new ArrayList<Integer>());
    return id;
  }

  /**
   * The order in which you plug circuits into a given circuit matters, since that's the order that inputs are taken in.
   *
   * @param fromCircuit
   * @param toCircuit
   */
  public void plugCircuit(Integer fromCircuit, Integer toCircuit) {
    circuitInputs.get(toCircuit).add(fromCircuit);
  }

  /**
   * Only one circuit can be registered as input at a time.
   *
   * @param circuit
   */
  public void registerAsInput(int circuit) {
    inputCircuit = circuit;
  }

  /**
   * Only one circuit can be registered as output at a time.
   *
   * @param circuit
   */
  public void registerAsOutput(int circuit) {
    outputCircuit = circuit;
  }

  private static int circuitMemorySize(Circuit circuit) {
    return circuit.size() - circuit.inputSize();
  }

  public Circuit getCircuit(int id) {
    return circuits.get(id);
  }

  public Circuit toCircuit() {
    SimpleCircuitBuilder builder = new SimpleCircuitBuilder();
    /*
     * How many nodes are there?
     * Each circuit's inputs may be shared with other circuits.
     * However, each circuit has unique outputs.
     * So count outputs, not inputs.
     */

    Integer[] offsets = new Integer[circuits.size()];
    int runningOffset = 0;

    for (int i = 0; i < circuits.size(); i++) {
      offsets[i] = runningOffset;
      Circuit circuit = circuits.get(i);
      runningOffset += circuitMemorySize(circuit);
    }
    int totalSize = runningOffset;

    builder.ensureSize(totalSize);

    for (int circuitI = 0; circuitI < circuits.size(); circuitI++) {
      int offset = offsets[circuitI];
      Circuit circuit = circuits.get(circuitI);
      for (Iterator<Edge<Integer>> it = circuit.getEdges(); it.hasNext(); ) {
        Edge<Integer> edge = it.next();

        int start = edge.getStart();
        int end = edge.getEnd();

        int inputIdx = circuit.getInputIndex(start);

        inputSearch:
        if (inputIdx != -1) {
          // start is an input to this circuit, meaning we have to go find that input
          int runningInputSize = 0;

          for (int inC : circuitInputs.get(circuitI)) {
            Circuit inputCircuit = circuits.get(inC);
            if (inputIdx < runningInputSize + inputCircuit.outputSize()) {
              start = inputCircuit.getOutput(inputIdx - runningInputSize);
              break inputSearch;
            }
            runningInputSize += inputCircuit.outputSize();
          }

          throw new RuntimeException("Input to circuit not found");
        }

        builder.addEdge(start + offset, end + offset);
      }
    }

    Circuit inCircuit = circuits.get(inputCircuit);
    for (int i = 0; i < inCircuit.inputSize(); i++) {
      builder.registerInput(offsets[inputCircuit] + inCircuit.getInput(i));
    }

    Circuit outCircuit = circuits.get(outputCircuit);
    for (int i = 0; i < outCircuit.outputSize(); i++) {
      builder.registerOutput(offsets[outputCircuit] + outCircuit.getOutput(i));
    }

    return builder.toCircuit();
  }

}
