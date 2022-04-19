package circuit;

import graph.Edge;
import graph.TriState;
import graph.TwoWayDirectedGraph;

import java.util.*;

import static graph.TriState.*;

/**
 * Ideally immutable
 * <p>
 * Note that nodes are blobs of redstone dust (optionally with repeaters). edges are redstone torches.
 * <p>
 * A node is true if there is any redstone torch point to it which is on. A redstone torch is on if its source is false.
 * Hence, A node is true iff any of its inputs is false
 */
public class Circuit {
  protected TwoWayDirectedGraph<Integer> redstone;
  protected Integer[] inputs;
  protected Integer[] outputs;


  public Circuit(TwoWayDirectedGraph<Integer> redstone, Integer[] inputs, Integer[] outputs) {
    this.redstone = redstone;
    this.inputs = inputs;
    this.outputs = outputs;

    String err = this.isValid();
    if (err != null) {
      throw new IllegalStateException("Attempted to construct invalid circuit: " + err);
    }
  }

  /**
   * @return An error message, or {@code null} if there are no errors
   */
  private String isValid() {
    if (redstone == null || inputs == null || outputs == null) {
      return "A field is null!";
    }
    for (Integer i : inputs) {
      if (i == null) {
        return "An input is null!";
      }
      if (i < 0 || redstone.size() <= i) {
        return "An input is out of bounds!";
      }
    }
    for (Integer i : outputs) {
      if (i == null) {
        return "An output is null!";
      }
      if (i < 0 || redstone.size() <= i) {
        return "An output is out of bounds!";
      }
    }

    Iterator<Edge<Integer>> edges = getEdges();
    while (edges.hasNext()) {
      Edge edge = edges.next();
      if (edge.getStart().equals(edge.getEnd())) {
        return "A self loop exists";
        // TODO: check for loops in general?
      }
    }
    return null;
  }

  public TwoWayDirectedGraph<Integer> getGraph() {
    return redstone;
  }
//  public Circuit copy() {
//    Integer[] inputsCopy = new Integer[inputs.length];
//    System.arraycopy(inputs, 0, inputsCopy, 0, inputs.length);
//
//    Integer[] outputsCopy = new Integer[outputs.length];
//    System.arraycopy(outputs, 0, outputsCopy, 0, outputs.length);
//
//    return new Circuit(redstone, inputsCopy, outputsCopy);
//  }

  /**
   * @return The index of the input node with id i, or -1 if i is not the id of an input node.
   */
  int getInputIndex(int i) {
    for (int j = 0; j < inputs.length; j++) {
      if (i == inputs[j]) {
        return j;
      }
    }
    return -1;
  }

  /**
   * @return The index of the output node with id i, or -1 if i is not the id of an output node.
   */
  int getOutputIndex(int i) {
    for (int j = 0; j < outputs.length; j++) {
      if (i == outputs[j]) {
        return j;
      }
    }
    return -1;
  }

  /**
   * @return The number of multibit inputs this circuit has.
   */
  public int inputSize() {
    return inputs.length;
  }

  /**
   * @return The number of multibit outputs this circuit has.
   */
  public int outputSize() {
    return outputs.length;
  }

  /**
   * @return The id of the output node at index i
   */
  public Integer getInput(int i) {
    return inputs[i];
  }

  /**
   * @return The id of the output node at index i
   */
  public Integer getOutput(int i) {
    return outputs[i];
  }

  private static Integer[] copyIntegerArr(Integer[] arr) {
    Integer[] r = new Integer[arr.length];
    System.arraycopy(arr, 0, r, 0, arr.length);
    return r;
  }

  /**
   * @return The ids of all input nodes in order
   */
  public Integer[] getInputs() {
    return copyIntegerArr(inputs);
  }

  /**
   * @return The ids of all output nodes in order
   */
  public Integer[] getOutputs() {
    return copyIntegerArr(outputs);
  }

  public int size() {
    return redstone.size();
  }

  public Iterator<Edge<Integer>> getEdges() {
    return redstone.getEdges();
  }

  public boolean[] simulate(boolean[] input) {
    if (input.length != this.inputSize()) {
      throw new UnsupportedOperationException("Invalid input length: Got " + input.length + " bits when the circuit needed " + this.inputSize() + "!");
    }

    Stack<Pair<Integer, Boolean>> assertStateStack = new Stack<>();

    for (int i = 0; i < input.length; i++) {
      boolean b = input[i];
      assertStateStack.push(new Pair<>(this.inputs[i], b));
    }

    TriState[] state = simulateFull(assertStateStack);

    boolean[] r = new boolean[this.outputSize()];
    for (int i = 0; i < this.outputSize(); i++) {
      switch (state[outputs[i]]) {
        case UNKNOWN:
          throw new RuntimeException("Somehow, the state of a node is unknown after a trace.");
        case TRUE:
          r[i] = true;
          break;
        case FALSE:
          r[i] = false;
          break;
      }
    }

    return r;

  }

  /**
   * @return The nodes which aren't inputs to the network and which don't have inputs
   */
  public Set<Integer> emptyInputs() {
    Set<Integer> untouchedInputs = redstone.inputs();
    untouchedInputs.removeAll(Arrays.asList(inputs));
    return untouchedInputs;
  }

  /**
   * Propagates signals along the circuit to nodes of provable state.
   * <p>
   * Fills in known empty inputs with FALSE so you don't have to. (See {@code emptyInputs()}  )
   * <p>
   * See {@code simulateRaw()}.
   */
  public TriState[] simulateFull(Stack<Pair<Integer, Boolean>> assertStateStack) {
    for (Integer i : emptyInputs()) {
      assertStateStack.push(new Pair<>(i, false));
    }

    return simulateRaw(assertStateStack);
  }

  /**
   * Propagates signals along the circuit to nodes of provable state.
   * That is, given the inputs, any state associated with a node will be its state,
   * where UNKNOWN means changing an unset input may change the state of that node.
   * <p>
   * Technically, you could probably prove more about certain points having constant values
   * (e.g. "A or not A" is always true, but if A is unknown
   * then this algorithm will mark the overall as unknown as well.)
   * <p>
   * You have to fill in all known states yourself.
   * Consider adding all of {@code emptyInputs()} as {@code false}.
   */
  public TriState[] simulateRaw(Stack<Pair<Integer, Boolean>> assertStateStack) {

    TriState[] state = new TriState[redstone.size()];
    Arrays.fill(state, UNKNOWN);

    while (!assertStateStack.isEmpty()) {
      Set<Integer> checkStateSet = new HashSet<>();

      while (!assertStateStack.isEmpty()) {
        Pair<Integer, Boolean> item = assertStateStack.pop();
        Integer i = item.getFirst();
        Boolean b = item.getSecond();

        if (b) {
          state[i] = TRUE;
          checkStateSet.addAll(redstone.outNeighborhood(i));
        } else {
          state[i] = FALSE;
          for (Integer j : redstone.outNeighborhood(i)) {
            assertStateStack.push(new Pair<>(j, true));
          }
        }
      }

      checkFlag:
      for (Integer checkNode : checkStateSet) {
        for (Integer in : redstone.inNeighborhood(checkNode)) {
          switch (state[in]) {
            case UNKNOWN:
              continue checkFlag;
            case TRUE:
              break;
            case FALSE:
              assertStateStack.push(new Pair<>(checkNode, true));
              continue checkFlag;
          }
        }
        // Only TRUEs, so this node is FALSE
        assertStateStack.push(new Pair<>(checkNode, false));
      }
    }

    return state;
  }

  public Circuit trim() {
    return trimWithMapping().getFirst();
  }

  /**
   * Does not modify the circuit in place.
   * <p>
   * Removes extraneous features from the circuit.
   * Specifically, it looks for:
   * - two redstone torches in a row (that don't connect input and output,
   * just to keep inputs distinct from each other and outputs distinct from each other)
   * - TODO: doesn't do this perfectly, since it doesn't check for it after the next item
   * - torches which don't feed from input or to output
   */
  public Pair<Circuit, Map<Integer, Integer>> trimWithMapping() {
    // All nodes remaining at the end should be both either affected by input or affect output.
    ArrayList<Edge<Integer>> nodesToMerge = new ArrayList<>();

    // We want to start out by deleting all nodes which don't affect the output of the network at all
    Set<Integer> nodesToDelete = new HashSet<>(redstone.nodes());
    Set<Integer> affectsOutput = redstone.traceBackward(outputs);
    nodesToDelete.removeAll(affectsOutput);

    for (int node = 0; node < redstone.size(); node++) {
      if (redstone.outNeighborhood(node).size() == 1 && redstone.inNeighborhood(node).size() == 1) {
        Integer inputNode = unit(redstone.inNeighborhood(node));
        Integer outputNode = unit(redstone.outNeighborhood(node));

        if (!(contains(outputs, outputNode) && contains(inputs, inputNode))) {
          nodesToMerge.add(new Edge<>(inputNode, outputNode));
          nodesToDelete.add(node);
        }
      }
    }

    /* Removal of constant nodes
     *
     * If only knowing this much asserts that a state is not UNKNOWN, then that state is a constant
     * Ideally, we would have no constants at all
     * If something is FALSE, then all its outputs are TRUE (and hence not UNKNOWN)
     * TRUE items are useless, since they don't affect the output of anything they connect to.
     * Hence, delete all marked items.
     */
    Stack<Pair<Integer, Boolean>> assertStateStack = new Stack<>();
    for (Integer i : emptyInputs()) {
      assertStateStack.push(new Pair<>(i, false));
    }
    TriState[] constantState = simulateRaw(assertStateStack);
    for (int node = 0; node < size(); node++) {
      switch (constantState[node]) {
        case UNKNOWN:
          break;
        case TRUE:
        case FALSE:
          nodesToDelete.add(node);
          break;
      }
    }

    /*
     * Compile node mappings from merges and deletes:
     *   Change merging to node mapping
     *   -1 represents deletion
     */
    HashMap<Integer, Integer> nodeMapping = new HashMap<>();
    for (Integer node : nodesToDelete) {
      nodeMapping.put(node, -1);
    }
    for (Edge<Integer> nodePair : nodesToMerge) {
      nodeMapping.put(getMapping(nodeMapping, nodePair.getStart()), nodePair.getEnd());
    }

    int newSize = size() - nodesToDelete.size() + nodesToMerge.size();
    SimpleCircuitBuilder scb = new SimpleCircuitBuilder();
    scb.ensureSize(newSize);

    Iterator<Edge<Integer>> edges = redstone.getEdges();
    while (edges.hasNext()) {
      Edge<Integer> edge = edges.next();

      int start = getMapping(nodeMapping, edge.getStart());
      int end = getMapping(nodeMapping, edge.getEnd());

      if (start != -1 && end != -1) {
        scb.addEdge(start, end);
      }
    }

    for (int i : inputs) {
      scb.registerInput(getMapping(nodeMapping, i));
    }
    for (int i : outputs) {
      scb.registerOutput(getMapping(nodeMapping, i));
    }

    return new Pair<>(scb.toCircuit(), nodeMapping);
  }

  /**
   * Follows cycles in {@code map}, starting from {@code val}, until an end point is reached.
   * <p>
   * O(1) amt
   */
  private static <V> V getMapping(Map<V, V> map, V val) {
    if (map.containsKey(val)) {
      ArrayList<V> vs = new ArrayList<>();

      while (map.containsKey(val)) {
        vs.add(val);
        val = map.get(val);
      }

      for (V v : vs) {
        map.put(v, val);
      }
    }
    return val;
  }

  private static boolean contains(Integer[] arr, int x) {
    for (int i : arr) {
      if (i == x) {
        return true;
      }
    }
    return false;
  }

  private static Integer unit(Set<Integer> ints) {
    if (ints.size() != 1) {
      throw new IllegalArgumentException("Set is not a unitary set!");
    }
    return ints.iterator().next();
  }
}
