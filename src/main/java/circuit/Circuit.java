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

  private boolean isCircuitEndpoint(Integer node) {
    return contains(inputs, node) || contains(outputs, node);
  }

  /**
   * @return {@code true} if the merge occurred, {@code false} otherwise.
   */
  private boolean attemptMerge(GraphMerger<Integer> merger, Integer node1, Integer node2) {
    boolean oneUnsafe = isCircuitEndpoint(node1);
    boolean twoUnsafe = isCircuitEndpoint(node2);
    if (!(oneUnsafe && twoUnsafe)) {
      if (oneUnsafe) {
        merger.mergeNodes(node2, node1);
      } else {
        merger.mergeNodes(node1, node2);
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Does not modify the circuit in place.
   * <p>
   * Removes extraneous features from the circuit.
   * Specifically, it looks for:
   * - TODO: two redstone torches in a row (that don't connect input and output,
   * just to keep inputs distinct from each other and outputs distinct from each other)
   * - TODO: doesn't do this perfectly, since it doesn't check for it after the next item
   * - torches which don't feed from input or to output
   * <p>
   * Keeps all inputs and outputs.
   * <p>
   * Returns:
   * - A Circuit which is functionally equivalent to this one.
   * - A mapping of node ids from the original circuit to the new one. (-1 represents node deletion)
   */
  public Pair<Circuit, int[]> trimWithMapping() {
    // We want to do removal first, then node merging
    // Node merging shouldn't affect node deletion, I think?


    // All nodes remaining at the end should be both either affected by input or affect output.
    // We want to start out by deleting all nodes which don't affect the output of the network at all
    Set<Integer> nodesToDelete = new HashSet<>(redstone.nodes());
    Set<Integer> affectsOutput = redstone.traceBackward(outputs);
    nodesToDelete.removeAll(affectsOutput);

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

    // Just on principle, we can't delete any inputs or outputs (even the constant outputs or the useless inputs)
    nodesToDelete.removeAll(Arrays.asList(inputs));
    for (int output : outputs) {
      nodesToDelete.remove(output);
      if (constantState[output] == TRUE) {
        // All constant outputs must maintain their constant value
        // FALSE constants are fine without input, but TRUE constants need a FALSE input
        boolean trueConstant = false;
        for (int preceedOutputNode : redstone.inNeighborhood(output)) {
          if (constantState[preceedOutputNode] == FALSE) {
            nodesToDelete.remove(preceedOutputNode);
            trueConstant = true;
            break;
          }
        }
        if (!trueConstant) {
          throw new IllegalStateException("TRUE output without FALSE input");
        }
      }
    }

    /*
     * Some invariants on nodeMerges that we need to maintain:
     * - inputs and outputs are never deleted
     * - If an input or an output is involved in a merge, the input or output is the one being merged into
     *   - this means we cannot merge two inputs or an input and an output.
     */
    GraphMerger<Integer> nodeMerges = new GraphMerger<>(getGraph());
    for (Integer node : nodesToDelete) {
      nodeMerges.removeNode(node);
    }

    /* Check for merging
     * This occurs when there is a lonely edge.
     * A lonely edge is where its output has only the one edge outputting to it,
     * and its input as only the one edge taking it as input.
     */

    Stack<Edge<Integer>> edgesToExamineForForwarding = new Stack<>();
    for (Iterator<Edge<Integer>> it = redstone.getEdges(); it.hasNext(); ) {
      edgesToExamineForForwarding.add(it.next());
    }

    while (!edgesToExamineForForwarding.isEmpty()) {
      Edge<Integer> edge = edgesToExamineForForwarding.pop();
      Integer start = edge.getStart();
      Integer end = edge.getEnd();
      if (!nodeMerges.hasEdge(edge)) {
        continue;
      }
      if (isCircuitEndpoint(start) || isCircuitEndpoint(end)) {
        // We should never delete an input or output
        continue;
      }
      if (nodeMerges.inNeighborhood(end).size() != 1 || nodeMerges.outNeighborhood(start).size() != 1) {
        // The edge must be lonely
        continue;
      }
      Set<Integer> outNeigh = nodeMerges.outNeighborhood(end);
      Set<Integer> inNeigh = nodeMerges.inNeighborhood(start);
      if (outNeigh.size() != 1 && inNeigh.size() != 1) {
        // To avoid multiplicative edge growth
        if (!(inNeigh.size() == 2 && outNeigh.size() == 2)) {
          continue;
        }
      }

      for (Integer farBackFrom : inNeigh) {
        for (Integer farForwardTo : outNeigh) {
          edgesToExamineForForwarding.add(new Edge<>(farBackFrom, farForwardTo));
        }
      }

      nodeMerges.forwardEdge(start, end);
    }

    /*
     * Compile node mappings from merges and deletes:
     *   Change merging to node mapping
     *   -1 represents deletion
     *
     * Note this represents a mapping over the old node ids.
     * Node ids need to be compressed to the new size, which will
     * be done in nodeMapping.
     */
    int[] nodeMapping = new int[size()];
    int runningId = 0;
    for (int i = 0; i < size(); i++) {
      if (nodeMerges.isSelfMapped(i)) {
        nodeMapping[i] = runningId++;
      }
    }
    for (int i = 0; i < size(); i++) {
      if (nodeMerges.isDeleted(i)) {
        nodeMapping[i] = -1;
      } else {
        nodeMapping[i] = nodeMapping[nodeMerges.getMapping(i)];
      }
    }

    int newSize = runningId;//size() - nodesToDelete.size();// + nodesToMerge.size();
    SimpleCircuitBuilder scb = new SimpleCircuitBuilder();
    scb.ensureSize(newSize);

    for (Iterator<Edge<Integer>> it = nodeMerges.toDirectedGraphView().getEdges(); it.hasNext(); ) {
      Edge<Integer> edge = it.next();

      int start = nodeMapping[edge.getStart()];
      int end = nodeMapping[edge.getEnd()];

      if (start == -1 || end == -1 || start == end) {
        throw new IllegalStateException("Invalid node graph");
      }

      scb.addEdge(start, end);
    }

    for (int i : inputs) {
      scb.registerInput(nodeMapping[i]);
    }
    for (int i : outputs) {
      scb.registerOutput(nodeMapping[i]);
    }

    return new Pair<>(scb.toCircuit(), nodeMapping);
  }

  private static int mapCompressInt(Map<Integer, Integer> map, int[] compressMap, Integer val) {
    return map.getOrDefault(val, compressMap[val]);
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
