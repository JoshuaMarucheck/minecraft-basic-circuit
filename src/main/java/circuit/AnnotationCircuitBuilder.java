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
  protected int[] circuitPositionOffsets;
  protected HashMap<Circuit, int[]> positionMaps;

  public AnnotationCircuitBuilder() {
    super();
    inputCircuits = new ArrayList<Integer>();
    outputCircuits = new ArrayList<Integer>();
    circuitPositionOffsets = null;
    positionMaps = null;
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

  /**
   * Slow. Maybe don't use?
   *
   * @return true if this node is both an input to a subcircuit and has some output plugged into it inside this builder
   */
  private boolean isPluggedInput(int circuitId, int localNodeId) {
    int plugSize = getPluggedInputSize(circuitId);
    if (plugSize > 0) {
      int inputIdx = circuits.get(circuitId).getInputIndex(localNodeId);
      return inputIdx != -1 && inputIdx < plugSize;
    } else {
      return false;
    }
  }

  static int circuitMemorySize(Circuit circuit) {
    return circuit.size() - circuit.inputSize();
  }

  private int[] calculateMemoryPositionMap(int circuitId) {
    // Don't allocate a position for plugged inputs
    Circuit circuit = circuits.get(circuitId);
    int[] positionMap = new int[circuit.size()];

    int index = 0;
    for (int i = 0; i < circuit.size(); i++) {
      // Skip if plugged input
      // Since plugged inputs are passed on to other subcircuits.

      if (isPluggedInput(circuitId, i)) {
        positionMap[i] = -1;
      } else {
        positionMap[i] = index;
        index++;
      }
    }

    return positionMap;
  }

  /**
   * @param circuitId
   * @return The number of bits that this sub-circuit gets from other sub-circuits (as opposed to being inputs to the full circuit)
   */
  private int getPluggedInputSize(int circuitId) {
    int runningOffset = 0;

    AnnotatedCircuit circuit = (AnnotatedCircuit) circuits.get(circuitId);
    ArrayList<Integer> circuitInput = circuitInputs.get(circuitId);

    for (int circuitInputId : circuitInput) {
      runningOffset += circuits.get(circuitInputId).outputSize();
    }

    return runningOffset;
  }

  private static int definedPositions(int[] mappedPositions) {
    int r = 0;
    for (int i : mappedPositions) {
      if (i != -1) {
        r++;
      }
    }
    return r;
  }

  /**
   * Called by toCircuit().
   *
   * @return The total number of bits that the circuit needs in order to be represented
   */
  private int initOffsetsAndPosMap() {
    // Note we're allowing constant values, so we want to make sure to add memory
    // for any circuit input which isn't explicitly taking from an output.
    circuitPositionOffsets = new int[circuits.size()];
    positionMaps = new HashMap<Circuit, int[]>();
    int runningOffset = 0;

    /*
     * the value at offsets[i] is the sum of the number of non-(-1) values in all position maps of circuits before i.
     */
    for (int circuitId = 0; circuitId < circuits.size(); circuitId++) {
      circuitPositionOffsets[circuitId] = runningOffset;
      Circuit circuit = circuits.get(circuitId);
      int circuitSize = circuitMemorySize(circuit);

      ArrayList<Integer> circuitInput = circuitInputs.get(circuitId);
      if (circuitInput.size() != ((AnnotatedCircuit) circuit).getMultibitInputCount()) {
        assert (inputCircuits.contains(circuitId));
        assert (circuit.inputSize() - getPluggedInputSize(circuitId) > 0);
        circuitSize += circuit.inputSize() - getPluggedInputSize(circuitId);
      }

      int[] memoryMap = calculateMemoryPositionMap(circuitId);
      positionMaps.put(circuit, memoryMap);
      runningOffset += circuitSize;

      if (definedPositions(memoryMap) != circuitSize) {
        throw new RuntimeException("Memory map size doesn't match circuit size!");
      }
    }
    return runningOffset;
  }

  /**
   * Takes a given node within a sub-circuit, and finds its position in the overall circuit.
   * Note inputs and outputs are merged using this function.
   * <p>
   * Requires that initOffsetsAndPosMap() has been called after
   * all circuits have been registered. (This is done automatically by toCircuit().)
   *
   * @param circuitId   The id of the circuit that the node is in.
   * @param localNodeId The id of the node with respect to the specified circuit.
   * @return The position in full-circuit memory of the specified node
   */
  protected int calculatePositionOfNode(int circuitId, int localNodeId) {
    int mappedPos;
    int offset;

    Circuit circuit = circuits.get(circuitId);
    int inputIdx = circuit.getInputIndex(localNodeId);

    if (inputIdx != -1 && !inputCircuits.contains(circuitId)) {
      // Useless init so the compiler doesn't complain
      mappedPos = -1;
      offset = -1;
      /* If a circuit has a node which is
       * both input and output (and is not
       * input to the circuit as a whole),
       * then when we find that circuit as
       * an input, we have to recurse on
       * that circuit to find its input.
       */
      inputSearch:
      while (inputIdx != -1 && !inputCircuits.contains(circuitId)) {
        // localNodeId is an input to this circuit, meaning we have to go find that input
        // That input is an output from another sub-circuit, hopefully
        int runningInputSize = 0;

        for (int inputCircuitId : circuitInputs.get(circuitId)) {
          Circuit inputCircuit = circuits.get(inputCircuitId);
          if (inputIdx < runningInputSize + inputCircuit.outputSize()) {
            int posInInputCircuit = inputCircuit.getOutput(inputIdx - runningInputSize);
            mappedPos = positionMaps.get(inputCircuit)[posInInputCircuit];
            offset = circuitPositionOffsets[inputCircuitId];
            assert (mappedPos != -1);
            assert (circuitId != inputCircuitId);
            localNodeId = posInInputCircuit;
            circuitId = inputCircuitId;
            circuit = circuits.get(circuitId);
            inputIdx = circuit.getInputIndex(localNodeId);
            continue inputSearch;
          }
          runningInputSize += inputCircuit.outputSize();
        }

        throw new RuntimeException("Input to circuit not found");
      }
      assert (offset != -1);
    } else {
      mappedPos = positionMaps.get(circuit)[localNodeId];
      offset = circuitPositionOffsets[circuitId];
      assert (mappedPos != -1);
    }

    if (mappedPos + offset < 0) {
      throw new IllegalStateException("Something weird happened... (Did you remember to coat your inputs with identities?)");
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
    int totalSize = initOffsetsAndPosMap();
    builder.ensureSize(totalSize);

    for (int circuitI = 0; circuitI < circuits.size(); circuitI++) {
      Circuit circuit = circuits.get(circuitI);
      for (Iterator<Edge> edges = circuit.getEdges(); edges.hasNext(); ) {
        Edge edge = edges.next();

        int start = edge.getStart();
        int end = edge.getEnd();

        int mappedStart = calculatePositionOfNode(circuitI, start);
        int mappedEnd = calculatePositionOfNode(circuitI, end);

        if (mappedStart == mappedEnd) {
          throw new RuntimeException("Attempted to build self loop");
        }

        if (mappedStart >= totalSize || mappedEnd >= totalSize) {
          throw new RuntimeException("Edge is not within the graph!");
        }

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
        builder.registerInput(circuitPositionOffsets[inputCircuit] + posMap[inCircuit.getInput(j)]);
      }
      inputSizes[i] = inCircuit.inputSize();
    }

    int[] outputSizes = new int[outputCircuits.size()];
    for (int i = 0; i < outputCircuits.size(); i++) {
      int outputCircuit = outputCircuits.get(i);
      Circuit outCircuit = circuits.get(outputCircuit);
      for (int j = 0; j < outCircuit.outputSize(); j++) {
        // Careful; if a given output is also an input, it belongs to a different circuit
        int outputPos = calculatePositionOfNode(outputCircuit, outCircuit.getOutput(j));
        builder.registerOutput(outputPos);
      }
      outputSizes[i] = outCircuit.outputSize();
    }

    return new AnnotatedCircuit(builder.toCircuit(), inputSizes, outputSizes);
  }
}
