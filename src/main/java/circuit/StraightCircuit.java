package circuit;

import graph.TriState;
import graph.TwoWayDirectedGraph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static graph.TriState.*;

/**
 * Ideally immutable
 * <p>
 * Note that nodes are blobs of redstone dust (optionally with repeaters). edges are redstone torches.
 * <p>
 * A node is true if there is any redstone torch point to it which is on. A redstone torch is on if its source is false.
 * Hence, A node is true iff any of its inputs is false
 */
public class StraightCircuit {
  private TwoWayDirectedGraph redstone;
  private int inputStartIdx;
  private int inputEndIdx;
  private int outputStartIdx;
  private int outputEndIdx;

  public StraightCircuit(TwoWayDirectedGraph redstone, int inputStartIdx, int inputEndIdx, int outputStartIdx, int outputEndIdx) {
    this.redstone = redstone;
    this.inputStartIdx = inputStartIdx;
    this.inputEndIdx = inputEndIdx;
    this.outputStartIdx = outputStartIdx;
    this.outputEndIdx = outputEndIdx;
  }

  private boolean isInputIndex(int i) {
    return inputStartIdx <= i && i < inputEndIdx;
  }

  private boolean isOutputIndex(int i) {
    return outputStartIdx <= i && i < outputEndIdx;
  }

  public int inputSize() {
    return inputEndIdx - inputStartIdx;
  }

  public int outputSize() {
    return outputEndIdx - outputStartIdx;
  }

  public boolean[] simulate(boolean[] input) {
    if (input.length != this.inputSize()) {
      throw new UnsupportedOperationException();
    }

    TriState[] state = new TriState[redstone.size()];
    Arrays.fill(state, UNKNOWN);

    Stack<Pair<Integer, Boolean>> assertStateStack = new Stack<Pair<Integer, Boolean>>();
    for (int i : redstone.inputs()) {
      if (!this.isInputIndex(i)) {
        assertStateStack.push(new Pair<Integer, Boolean>(i, false));
      }
    }

    for (int i = 0; i < input.length; i++) {
      boolean b = input[i];
      assertStateStack.push(new Pair<Integer, Boolean>(this.inputStartIdx + i, b));
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

    boolean[] r = new boolean[this.outputSize()];
    for (int i = 0; i < this.outputSize(); i++) {
      switch (state[i + this.outputStartIdx]) {
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
}
