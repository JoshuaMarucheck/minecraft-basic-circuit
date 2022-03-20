package circuit;

import circuit.preconstructed.exceptions.StrictCheckException;
import graph.Edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Circuits are basically just directed graphs.
 */
public class AnnotationCircuitBuilder extends CircuitBuilder {
  private ArrayList<Integer> inputCircuits;
  private ArrayList<Integer> outputCircuits;

  public AnnotationCircuitBuilder() {
    super();
    inputCircuits = new ArrayList<Integer>();
    outputCircuits = new ArrayList<Integer>();
  }

  /**
   * The order in which you plug circuits into a given circuit matters, since that's the order that inputs are taken in.
   *
   * @param fromCircuit
   * @param toCircuit
   */
  public void plugCircuit(Integer fromCircuit, Integer toCircuit) throws StrictCheckException {
    ArrayList<Integer> targetCircuitInputs = circuitInputs.get(toCircuit);
    int currentInputIdx = targetCircuitInputs.size();
    if (currentInputIdx >= ((AnnotatedCircuit) circuits.get(toCircuit)).getMultibitInputCount()) {
      throw new StrictCheckException("Too many circuit inputs!");
    }
    if (circuits.get(fromCircuit).outputSize() != ((AnnotatedCircuit) circuits.get(toCircuit)).getMultibitInputSize(currentInputIdx)) {
      throw new StrictCheckException("Circuit input size didn't match other circuit's output size");
    }
    targetCircuitInputs.add(fromCircuit);
  }

  /**
   * Now many circuits can be registered as input at a time!
   * <p>
   * Order of registration matters.
   *
   * @param circuit
   */
  public void registerAsInput(int circuit) {
    if (circuit < 0) {
      throw new IllegalArgumentException("Circuit id " + circuit);
    }
    inputCircuits.add(circuit);
  }

  /**
   * Now many circuits can be registered as output at a time!
   * Though it will fail at other points if you try to do so.
   * <p>
   * Order of registration matters.
   *
   * @param circuit
   */
  public void registerAsOutput(int circuit) {
    if (circuit < 0) {
      throw new IllegalArgumentException("Circuit id " + circuit);
    }
    outputCircuits.add(circuit);
  }

  private static int circuitMemorySize(Circuit circuit) {
    return circuit.size() - circuit.inputSize();
  }

  private int[] calculateMemoryPositionMap(Circuit circuit) {
    int[] positionMap = new int[circuit.size()];

    int index = 0;
    for (int i = 0; i < circuit.size(); i++) {
      // Skip if input and not output
      if (circuit.getInputIndex(i) != -1 && circuit.getOutputIndex(i) == -1) {
        positionMap[i] = -1;
      } else {
        positionMap[i] = index;
        index++;
      }
    }

    return positionMap;
  }

  /**
   *
   * @param circuitId
   * @return The number of bits that this sub-circuit gets from other sub-circuits (as opposed to being inputs to the full circuit)
   */
  private int getPluggedInputSize(int circuitId) {
    int runningOffset = 0;

    AnnotatedCircuit circuit = (AnnotatedCircuit) circuits.get(circuitId);
    ArrayList<Integer> circuitInput = circuitInputs.get(circuitId);

    if (circuitInput.size() != circuit.getMultibitInputCount()) {
      for (int circuitInputId : circuitInput) {
        runningOffset += circuits.get(circuitInputId).outputSize();
      }
    }
    return runningOffset;
  }

  /**
   * Takes a given node within a sub-circuit, and finds its position in the overall circuit.
   * Note inputs and outputs are merged using this function.
   *
   * @param circuitId   The id of the circuit that the node is in.
   * @param localNodeId The id of the node with respect to the specified circuit.
   * @return The position in full-circuit memory of the specified node
   */
  private int calculatePositionOfNode(int circuitId, int localNodeId, int[] circuitPositionOffsets, HashMap<Circuit, int[]> positionMaps) {
    // TODO figure out why self loops are forming
    Circuit circuit = circuits.get(circuitId);

    int mappedPos;
    int offset;

    int inputIdx = circuit.getInputIndex(localNodeId);

    inputSearch:
    if (inputIdx != -1) {
      // localNodeId is an input to this circuit, meaning we have to go find that input
      // That input is an output from another sub-circuit, hopefully
      int runningInputSize = 0;

      for (int inputCircuitId : circuitInputs.get(circuitId)) {
        Circuit inputCircuit = circuits.get(inputCircuitId);
        if (inputIdx < runningInputSize + inputCircuit.outputSize()) {
          int posInInputCircuit = inputCircuit.getOutput(inputIdx - runningInputSize);
          mappedPos = positionMaps.get(inputCircuit)[posInInputCircuit];
          offset = circuitPositionOffsets[inputCircuitId];
          break inputSearch;
        }
        runningInputSize += inputCircuit.outputSize();
      }

      throw new RuntimeException("Input to circuit not found");
    } else {
      mappedPos = positionMaps.get(circuit)[localNodeId];
      offset = circuitPositionOffsets[circuitId];
    }

    return mappedPos + offset;
  }


  public AnnotatedCircuit toCircuit() {
    SimpleCircuitBuilder builder = new SimpleCircuitBuilder();
    /*
     * How many nodes are there?
     * Each circuit's inputs may be shared with other circuits.
     * However, each circuit has unique outputs.
     * So count outputs, not inputs.
     */

    // Note we're allowing constant values, so we want to make sure to add memory
    // for any circuit input which isn't explicitly taking from an output.
    int[] offsets = new int[circuits.size()];
    int runningOffset = 0;

    for (int i = 0; i < circuits.size(); i++) {
      offsets[i] = runningOffset;
      Circuit circuit = circuits.get(i);
      runningOffset += circuitMemorySize(circuit);

      ArrayList<Integer> circuitInput = circuitInputs.get(i);
      if (circuitInput.size() != ((AnnotatedCircuit) circuit).getMultibitInputCount()) {
        runningOffset += circuit.inputSize() - getPluggedInputSize(i);
      }
    }
    int totalSize = runningOffset;

    builder.ensureSize(totalSize);

    HashMap<Circuit, int[]> positionMaps = new HashMap<Circuit, int[]>();
    for (Circuit circuit : circuits) {
      positionMaps.put(circuit, calculateMemoryPositionMap(circuit));
    }

    for (int circuitI = 0; circuitI < circuits.size(); circuitI++) {
      Circuit circuit = circuits.get(circuitI);
      for (Iterator<Edge> edges = circuit.getEdges(); edges.hasNext(); ) {
        Edge edge = edges.next();

        int start = edge.getStart();
        int end = edge.getEnd();

        int mappedStart = calculatePositionOfNode(circuitI, start, offsets, positionMaps);
        int mappedEnd = calculatePositionOfNode(circuitI, end, offsets, positionMaps);

        builder.addEdge(mappedStart, mappedEnd);
      }
    }

    // Since these are inputs to the overall circuit, calculatePositionOfNode() won't work here
    int[] inputSizes = new int[inputCircuits.size()];
    for (int i = 0; i < inputCircuits.size(); i++) {
      int inputCircuit = inputCircuits.get(i);
      Circuit inCircuit = circuits.get(inputCircuit);
      int[] posMap = positionMaps.get(inCircuit);
      for (int j = 0; j < inCircuit.inputSize(); j++) {
        builder.registerInput(offsets[inputCircuit] + posMap[inCircuit.getInput(j)]);
      }
      inputSizes[i] = inCircuit.inputSize();
    }

    int[] outputSizes = new int[outputCircuits.size()];
    for (int i = 0; i < outputCircuits.size(); i++) {
      int outputCircuit = outputCircuits.get(i);
      Circuit outCircuit = circuits.get(outputCircuit);
      for (int j = 0; j < outCircuit.outputSize(); j++) {
        // Careful; if a given output is also an input, it belongs to a different circuit
        int outputPos = calculatePositionOfNode(outputCircuit, outCircuit.getOutput(j), offsets, positionMaps);
        builder.registerOutput(outputPos);
      }
      outputSizes[i] = outCircuit.outputSize();
    }

    return new AnnotatedCircuit(builder.toCircuit(), inputSizes, outputSizes);
  }
}
