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
  protected TwoWayDirectedGraph redstone;
  protected Integer[] inputs;
  protected Integer[] outputs;


  public Circuit(TwoWayDirectedGraph redstone, Integer[] inputs, Integer[] outputs) {
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

    Iterator<Edge> edges = getEdges();
    while (edges.hasNext()) {
      Edge edge = edges.next();
      if (edge.getStart().equals(edge.getEnd())) {
        return "A self loop exists";
        // TODO: check for loops in general?
      }
    }
    return null;
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

  public Iterator<Edge> getEdges() {
    return redstone.getEdges();
  }

  public boolean[] simulate(boolean[] input) {
    if (input.length != this.inputSize()) {
      throw new UnsupportedOperationException("Invalid input length: Got " + input.length + " bits when the circuit needed " + this.inputSize() + "!");
    }

    Stack<Pair<Integer, Boolean>> assertStateStack = new Stack<Pair<Integer, Boolean>>();

    for (int i = 0; i < input.length; i++) {
      boolean b = input[i];
      assertStateStack.push(new Pair<Integer, Boolean>(this.inputs[i], b));
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

  public TriState[] simulateFull(Stack<Pair<Integer, Boolean>> assertStateStack) {

    TriState[] state = new TriState[redstone.size()];
    Arrays.fill(state, UNKNOWN);

    Set<Integer> initialAsserts = new HashSet<Integer>();

    for (Pair<Integer, Boolean> item : assertStateStack) {
      initialAsserts.add(item.getFirst());
    }

    for (int i : redstone.inputs()) {
      if (!initialAsserts.contains(i)) {
        assertStateStack.push(new Pair<Integer, Boolean>(i, false));
      }
    }

    while (!assertStateStack.isEmpty()) {
      Set<Integer> checkStateSet = new HashSet<Integer>();

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
            assertStateStack.push(new Pair<Integer, Boolean>(j, true));
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
              assertStateStack.push(new Pair<Integer, Boolean>(checkNode, true));
              continue checkFlag;
          }
        }
        // Only TRUEs, so this node is FALSE
        assertStateStack.push(new Pair<Integer, Boolean>(checkNode, false));
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
    ArrayList<Integer> nodesToDelete = new ArrayList<Integer>();
    ArrayList<Edge> nodesToMerge = new ArrayList<Edge>();
//    ArrayList<Edge> edgesToAdd = new ArrayList<Edge>();

    for (int node = 0; node < redstone.size(); node++) {
      if (redstone.outNeighborhood(node).size() == 1 && redstone.inNeighborhood(node).size() == 1) {
        Integer inputNode = unit(redstone.inNeighborhood(node));
        Integer outputNode = unit(redstone.outNeighborhood(node));

        if (!(contains(outputs, outputNode) && contains(inputs, inputNode))) {
          nodesToMerge.add(new Edge(inputNode, outputNode));
          nodesToDelete.add(node);
        }
      }
    }

    // All nodes should be both either affected by input or affect output.
    // The only exception are constant nodes,

    HashSet<Integer> allNodes = new HashSet<Integer>();
    for (int i = 0; i < size(); i++) {
      allNodes.add(i);
    }

    Set<Integer> affectedByInput = redstone.traceForward(inputs);
    Set<Integer> affectsOutput = redstone.traceBackward(outputs);

    Set<Integer> duallyAffected = new HashSet<Integer>(affectedByInput);
    duallyAffected.retainAll(affectsOutput);

    Set<Integer> untouchedInputs = new HashSet<Integer>(allNodes);
    untouchedInputs.removeAll(duallyAffected);
    untouchedInputs.retainAll(redstone.inputs());

    Stack<Pair<Integer, Boolean>> assertStateStack = new Stack<Pair<Integer, Boolean>>();
    for (Integer i : untouchedInputs) {
      assertStateStack.push(new Pair<Integer, Boolean>(i, false));
    }

    // If only knowing this much asserts that a state is not UNKNOWN, then that state is a constant
    // Ideally, we would have no constants at all
    // If something is FALSE, then all its outputs are TRUE
    // TRUE items are useless, since they don't affect the output of anything they connect to
    // Hence, delete all marked items.
    TriState[] constantState = simulateFull(assertStateStack);
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

    HashMap<Integer, Integer> nodeMapping = new HashMap<Integer, Integer>();
    // -1 represents deletion
    for (Integer node : nodesToDelete) {
      nodeMapping.put(node, -1);
    }

    for (Edge nodePair : nodesToMerge) {
      nodeMapping.put(nodePair.getStart(), nodePair.getEnd());
    }

    int newSize = size() - nodesToDelete.size() + nodesToMerge.size();
    SimpleCircuitBuilder scb = new SimpleCircuitBuilder();
    scb.ensureSize(newSize);

    Iterator<Edge> edges = redstone.getEdges();
    while (edges.hasNext()) {
      Edge edge = edges.next();

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

    return new Pair<Circuit, Map<Integer, Integer>>(scb.toCircuit(), nodeMapping);
  }

  private static <V> V getMapping(Map<V, V> map, V val) {
    if (map.containsKey(val)) {
      ArrayList<V> vs = new ArrayList<V>();

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
