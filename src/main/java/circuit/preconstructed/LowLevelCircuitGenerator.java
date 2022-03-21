package circuit.preconstructed;

import circuit.AnnotatedCircuit;
import circuit.AnnotationCircuitBuilder;
import circuit.preconstructed.exceptions.MissingCircuitDependencyException;
import graph.DirectedGraph;
import graph.Edge;
import graph.TwoWayDirectedGraph;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LowLevelCircuitGenerator {
  private static String root = "./src/main/resources/circuits/";
  private CircuitCollection circuitCollection;
  public final static LowLevelCircuitGenerator canonicalGenerator = new LowLevelCircuitGenerator();

  private LowLevelCircuitGenerator() {
    circuitCollection = defaultNamedCircuits();
  }

  public static CircuitCollection defaultNamedCircuits() {
    CircuitCollection cc = new CircuitCollection();

    cc.registerCircuit("not", invert(1));
    cc.registerCircuit("and", and());
    cc.registerCircuit("or", or());
    try {
      cc.loadFromFile(new File(root + "xor.txt"), true, true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return cc;
  }

  public CircuitCollection operators(int bitLength) throws IOException, MissingCircuitDependencyException {
    CircuitCollection cc = new CircuitCollection();

    AnnotatedCircuit xor;
    try {
      xor = circuitCollection.loadFromFile(new File(root + "xor.txt"), true, false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    cc.registerCircuit("+", canonicalGenerator.addition(bitLength));
    // <<, >>, ^, |, &, !

    for (int i = 1; i <= bitLength; i *= 2) {
      cc.registerCircuit("<<" + i, leftShift(i, bitLength));
      cc.registerCircuit(">>" + i, rightShift(i, bitLength));
    }

    cc.registerCircuit("|", extendBitwise(or(), bitLength));
    cc.registerCircuit("&", extendBitwise(and(), bitLength));
    cc.registerCircuit("^", extendBitwise(xor, bitLength));
    cc.registerCircuit("!", logicalNegation(bitLength));

    return cc;
  }

  private static <K, V> Map<K, V> copyMap(Map<K, V> map) {
    HashMap<K, V> r = new HashMap<K, V>();
    for (K key : map.keySet()) {
      r.put(key, map.get(key));
    }
    return r;
  }

  public static AnnotatedCircuit identity(int size) {
    DirectedGraph dg = new DirectedGraph();
    dg.ensureSize(size);

    Integer[] inputs = new Integer[size];
    for (int i = 0; i < size; i++) {
      inputs[i] = i;
    }
    int[] inputSizes = new int[]{size};

    return new AnnotatedCircuit(new TwoWayDirectedGraph(dg), inputs, inputs, inputSizes, inputSizes);
  }

  public static AnnotatedCircuit invert(int size) {
    DirectedGraph dg = new DirectedGraph();
    dg.ensureSize(2 * size);

    Integer[] inputs = new Integer[size];
    Integer[] outputs = new Integer[size];

    for (int i = 0; i < size; i++) {
      inputs[i] = i;
      outputs[i] = i + size;
      dg.addEdge(new Edge(i, i + size));
    }

    int[] inputSizes = new int[]{size};
    int[] outputSizes = new int[]{size};

    return new AnnotatedCircuit(new TwoWayDirectedGraph(dg), inputs, outputs, inputSizes, outputSizes);
  }

  /**
   * Specifies whether the redstone is on or off.
   *
   * @param bits
   * @return A circuit that outputs the specified constant bits, annotated as a single output.
   */
  public static AnnotatedCircuit constant(boolean[] bits) {
    int size = bits.length;

    DirectedGraph dg = new DirectedGraph();
    dg.ensureSize(size);

    Integer[] inputs = new Integer[0];
    Integer[] outputs = new Integer[size];

    for (int i = 0; i < size; i++) {
      outputs[i] = i;
      if (bits[i]) {
        dg.addEdge(new Edge(dg.addNode(), i));
      }
    }

    int[] inputSizes = new int[]{};
    int[] outputSizes = new int[]{size};

    return new AnnotatedCircuit(new TwoWayDirectedGraph(dg), inputs, outputs, inputSizes, outputSizes);
  }

  /**
   * Any bits outside the specified region are assumed to be 0 bits. Bit order is maintained.
   *
   * @return A circuit mapping the input bits to a single item containing only the specified bits.
   */
  public static AnnotatedCircuit getBits(int inputSize, int bitsStart, int bitsEnd) {
    int outputSize = bitsEnd - bitsStart;
    if (outputSize < 0) {
      throw new IllegalArgumentException("bitsEnd comes before bitsStart! (bitsStart=" + bitsStart + ", bitsEnd=" + bitsEnd + ")");
    }

    DirectedGraph dg = new DirectedGraph();
    dg.ensureSize(inputSize);

    Integer[] inputs = new Integer[inputSize];
    Integer[] outputs = new Integer[outputSize];

    for (int i = 0; i < inputSize; i++) {
      inputs[i] = i;
    }

    for (int i = bitsStart; i < bitsEnd; i++) {
      if (0 <= i && i < inputSize) {
        outputs[i - bitsStart] = i;
      } else {
        outputs[i - bitsStart] = dg.addNode();
      }
    }

    int[] inputSizes = new int[]{inputSize};
    int[] outputSizes = new int[]{outputSize};

    return new AnnotatedCircuit(new TwoWayDirectedGraph(dg), inputs, outputs, inputSizes, outputSizes);
  }

  private static int sum(int[] ints) {
    int r = 0;
    for (int i : ints) {
      r += i;
    }
    return r;
  }

  /**
   *
   */
  public static AnnotatedCircuit merge(int[] inputSizes) {
    int outputSize = sum(inputSizes);

    DirectedGraph dg = new DirectedGraph();
    dg.ensureSize(outputSize);

    Integer[] inputs = new Integer[outputSize];
    Integer[] outputs = new Integer[outputSize];

    for (int i = 0; i < outputSize; i++) {
      inputs[i] = i;
      outputs[i] = i;
    }

    int[] outputSizes = new int[]{outputSize};

    return new AnnotatedCircuit(new TwoWayDirectedGraph(dg), inputs, outputs, inputSizes, outputSizes);
  }

  public static AnnotatedCircuit and() {
    DirectedGraph dg = new DirectedGraph();
    dg.ensureSize(4);

    Integer[] inputs = new Integer[]{0, 1};
    Integer[] outputs = new Integer[]{3};

    dg.addEdge(new Edge(0, 2));
    dg.addEdge(new Edge(1, 2));
    dg.addEdge(new Edge(2, 3));

    int[] inputSizes = new int[]{1, 1};
    int[] outputSizes = new int[]{1};

    return new AnnotatedCircuit(new TwoWayDirectedGraph(dg), inputs, outputs, inputSizes, outputSizes);
  }

  /**
   * Takes two 1-bit inputs
   */
  public static AnnotatedCircuit or() {
    DirectedGraph dg = new DirectedGraph();
    dg.ensureSize(5);

    Integer[] inputs = new Integer[]{0, 2};
    Integer[] outputs = new Integer[]{4};

    dg.addEdge(new Edge(0, 1));
    dg.addEdge(new Edge(1, 4));
    dg.addEdge(new Edge(2, 3));
    dg.addEdge(new Edge(3, 4));

    int[] inputSizes = new int[]{1, 1};
    int[] outputSizes = new int[]{1};

    return new AnnotatedCircuit(new TwoWayDirectedGraph(dg), inputs, outputs, inputSizes, outputSizes);
  }

  /**
   * Takes a single n-bit input
   *
   * @return a circuit which outputs true if any input bit is true, and false otherwise
   */
  public static AnnotatedCircuit any(int n) {
    AnnotationCircuitBuilder acb = new AnnotationCircuitBuilder();
    AnnotatedCircuit orCircuit = or();

    int input = acb.addCircuit(identity(n));
    acb.registerAsInput(input);

    // If n=1, then input represents a single bit, and the 'or' of it is just itself
    int orBit = input;
    int prevCombinedBit = acb.addCircuit(getBits(n, 0, 1));
    acb.plugCircuit(input, prevCombinedBit);

    for (int i = 1; i < n; i++) {
      // or together nodes i and i-1

      int nextBit = acb.addCircuit(getBits(n, i, i + 1));
      acb.plugCircuit(input, nextBit);

      orBit = acb.addCircuit(orCircuit);

      acb.plugCircuit(prevCombinedBit, orBit);
      acb.plugCircuit(nextBit, orBit);

      prevCombinedBit = orBit;
    }

    acb.registerAsOutput(orBit);

    return acb.toCircuit();
  }


  /**
   * Takes a single n-bit input
   *
   * @return a circuit which outputs true if all inputs bits are true, and false otherwise
   */
  public static AnnotatedCircuit all(int n) {
    AnnotationCircuitBuilder acb = new AnnotationCircuitBuilder();
    AnnotatedCircuit andCircuit = and();

    int input = acb.addCircuit(identity(n));
    acb.registerAsInput(input);

    // If n=1, then input represents a single bit, and the 'or' of it is just itself
    int andBit = input;
    int prevCombinedBit = acb.addCircuit(getBits(n, 0, 1));
    acb.plugCircuit(input, prevCombinedBit);

    for (int i = 1; i < n; i++) {
      // or together nodes i and i-1

      int nextBit = acb.addCircuit(getBits(n, i, i + 1));
      acb.plugCircuit(input, nextBit);

      andBit = acb.addCircuit(andCircuit);

      acb.plugCircuit(prevCombinedBit, andBit);
      acb.plugCircuit(nextBit, andBit);

      prevCombinedBit = andBit;
    }

    acb.registerAsOutput(andBit);

    return acb.toCircuit();
  }

  /**
   * Treats the value as big-endian (i.e. shifts bits towards the larger-value indices).
   * <p>
   * Logical shift (fills the new values with 0s.
   */
  public static AnnotatedCircuit leftShift(int shiftSize, int bitLength) {
    if (shiftSize < 0 || bitLength < 0) {
      throw new IllegalArgumentException("Bit size and shift length must be positive");
    }
    if (shiftSize > bitLength) {
      throw new IllegalArgumentException("Shift length must be at most bit length");
    }
    AnnotationCircuitBuilder acb = new AnnotationCircuitBuilder();

    int input = acb.addCircuit(identity(bitLength));
    acb.registerAsInput(input);

    int get = acb.addCircuit(getBits(bitLength, 0, bitLength - shiftSize));
    acb.plugCircuit(input, get);

    boolean[] zeros = new boolean[shiftSize];
    Arrays.fill(zeros, false);
    int side = acb.addCircuit(constant(zeros));

    int mergeId = acb.addCircuit(merge(new int[]{shiftSize, bitLength - shiftSize}));
    acb.plugCircuit(side, mergeId);
    acb.plugCircuit(get, mergeId);
    acb.registerAsOutput(mergeId);

    return acb.toCircuit();
  }

  /**
   * Treats the value as big-endian (i.e. shifts bits towards the larger-value indices).
   * <p>
   * Logical shift (fills the new values with 0s.
   */
  public static AnnotatedCircuit rightShift(int shiftSize, int bitLength) {
    if (shiftSize < 0 || bitLength < 0) {
      throw new IllegalArgumentException("Bit size and shift length must be positive");
    }
    if (shiftSize > bitLength) {
      throw new IllegalArgumentException("Shift length must be at most bit length");
    }
    AnnotationCircuitBuilder acb = new AnnotationCircuitBuilder();

    int input = acb.addCircuit(identity(bitLength));
    acb.registerAsInput(input);

    int get = acb.addCircuit(getBits(bitLength, shiftSize, bitLength));
    acb.plugCircuit(input, get);

    boolean[] zeros = new boolean[shiftSize];
    Arrays.fill(zeros, false);
    int side = acb.addCircuit(constant(zeros));

    int mergeId = acb.addCircuit(merge(new int[]{bitLength - shiftSize, shiftSize}));
    acb.plugCircuit(get, mergeId);
    acb.plugCircuit(side, mergeId);
    acb.registerAsOutput(mergeId);

    return acb.toCircuit();
  }

  /**
   * Constructs a circuit which does lots of 1-bit pairwise operations in parallel
   *
   * @param circuit A circuit with two 1-bit inputs and one 1-bit output
   */
  public static AnnotatedCircuit extendBitwise(AnnotatedCircuit circuit, int bitLength) {
    AnnotationCircuitBuilder acb = new AnnotationCircuitBuilder();

    int input1 = acb.addCircuit(identity(bitLength));
    int input2 = acb.addCircuit(identity(bitLength));
    acb.registerAsInput(input1);
    acb.registerAsInput(input2);

    int[] outputSizes = new int[bitLength];
    Arrays.fill(outputSizes, 1);
    int output = acb.addCircuit(merge(outputSizes));
    acb.registerAsOutput(output);

    for (int i = 0; i < bitLength; i++) {
      AnnotatedCircuit get = getBits(bitLength, i, i + 1);

      int bit1 = acb.addCircuit(get);
      int bit2 = acb.addCircuit(get);

      acb.plugCircuit(input1, bit1);
      acb.plugCircuit(input2, bit2);

      int combine = acb.addCircuit(circuit);
      acb.plugCircuit(bit1, combine);
      acb.plugCircuit(bit2, combine);

      acb.plugCircuit(combine, output);
    }

    return acb.toCircuit();
  }

  public static AnnotatedCircuit logicalNegation(int bitLength) {
    AnnotationCircuitBuilder acb = new AnnotationCircuitBuilder();

    int input = acb.addCircuit(identity(bitLength));
    int mid = acb.addCircuit(any(bitLength));
    int output = acb.addCircuit(invert(1));

    acb.plugCircuit(input, mid);
    acb.plugCircuit(mid, output);

    acb.registerAsInput(input);
    acb.registerAsOutput(output);

    return acb.toCircuit();
  }

  /**
   * Assumes two inputs of equal size
   */
  public AnnotatedCircuit addition(int size) throws IOException {
    // first set of nodes is input
    // next set of nodes is output
    // next set of nodes is carry bit for that column

    AnnotatedCircuit xor3;
    AnnotatedCircuit atLeast2;
    try {
      xor3 = circuitCollection.getOrLoad(new File(root + "specialized/xor3.txt"));
      atLeast2 = circuitCollection.getOrLoad(new File(root + "specialized/atLeast2.txt"));
    } catch (MissingCircuitDependencyException e) {
      throw new RuntimeException("Missing a basic logic gate somehow?", e);
    }

    AnnotationCircuitBuilder acb = new AnnotationCircuitBuilder();

    int outputId;
    {
      int[] outputSizes = new int[size];
      Arrays.fill(outputSizes, 1);
      outputId = acb.addCircuit(merge(outputSizes));
      acb.registerAsOutput(outputId);
    }

    int input1Id = acb.addCircuit(identity(size));
    int input2Id = acb.addCircuit(identity(size));
    acb.registerAsInput(input1Id);
    acb.registerAsInput(input2Id);

    int prevCarry = acb.addCircuit(constant(new boolean[]{false}));

    for (int digit = 0; digit < size; digit++) {
      // xor3 is true iff an odd number of its inputs are true
      // i.e. if xor3 is true, then we output a bit at this digit

      int bit1 = acb.addCircuit(getBits(size, digit, digit + 1));
      int bit2 = acb.addCircuit(getBits(size, digit, digit + 1));
      acb.plugCircuit(input1Id, bit1);
      acb.plugCircuit(input2Id, bit2);

      int xor = acb.addCircuit(xor3);
      acb.plugCircuit(bit1, xor);
      acb.plugCircuit(bit2, xor);
      acb.plugCircuit(prevCarry, xor);

      acb.plugCircuit(xor, outputId);

      // Now check for at least two bits being true, so we know whether to carry

      int carry = acb.addCircuit(atLeast2);
      acb.plugCircuit(bit1, carry);
      acb.plugCircuit(bit2, carry);
      acb.plugCircuit(prevCarry, carry);

      prevCarry = carry;
    }

    return acb.toCircuit();
  }
}
