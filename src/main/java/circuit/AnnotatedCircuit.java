package circuit;

import graph.TwoWayDirectedGraph;

import java.util.Iterator;

public class AnnotatedCircuit extends Circuit {
  private int[] inputSizes;
  private int[] outputSizes;

  public AnnotatedCircuit(TwoWayDirectedGraph redstone, Integer[] inputs, Integer[] outputs, int[] inputSizes, int[] outputSizes) {
    super(redstone, inputs, outputs);
    this.inputSizes = inputSizes;
    this.outputSizes = outputSizes;

    assert sum(inputSizes) == inputs.length;
    assert sum(outputSizes) == outputs.length;
  }

  public AnnotatedCircuit(Circuit circuit, int[] inputSizes, int[] outputSizes) {
    super(circuit.redstone, circuit.inputs, circuit.outputs);
    this.inputSizes = inputSizes;
    this.outputSizes = outputSizes;

    if (sum(inputSizes) != inputs.length
        || sum(outputSizes) != outputs.length
        || outputSizes.length != 1) {
      throw new IllegalArgumentException();
    }
  }

  private static int sum(int[] ints) {
    return partialSum(ints, ints.length);
  }


  private static int partialSum(int[] ints, int count) {
    int r = 0;
    for (int i = 0; i < count; i++) {
      r += ints[i];
    }
    return r;
  }

  /**
   * @param id
   * @return The cumulative size of all multibit inputs with id less than the specified id
   */
  public int getMultibitInputIdx(int id) {
    return partialSum(inputSizes, id);
  }

  /**
   * @param id
   * @return The cumulative size of all multibit outputs with id less than the specified id
   */
  public int getMultibitOutputIdx(int id) {
    return partialSum(outputSizes, id);
  }

  /**
   * @return The number of bits associated with the given multibit input
   */
  public int getMultibitInputSize(int id) {
    return inputSizes[id];
  }

  /**
   * @return The number of bits associated with the given multibit output
   */
  public int getMultibitOutputSize(int id) {
    return outputSizes[id];
  }

  public int getMultibitInputCount() {
    return inputSizes.length;
  }

  public int getMultibitOutputCount() {
    return outputSizes.length;
  }

  /**
   * Returns the bit indices associated with the given multibit input
   */
  public Iterator<Integer> getMultibitInput(int id) {
    int idx = getMultibitInputIdx(id);
    Iterator<Integer> rangeIter = new Range(idx, idx + inputSizes[id]);
    return new IterMap<Integer>(rangeIter, this.inputs);
  }

  /**
   * Returns the bit indices associated with the given multibit output
   */
  public Iterator<Integer> getMultibitOutput(int id) {
    int idx = getMultibitOutputIdx(id);
    Iterator<Integer> rangeIter = new Range(idx, idx + outputSizes[id]);
    return new IterMap<Integer>(rangeIter, this.outputs);
  }

  public AnnotatedCircuit trim() {
    Circuit trimmed = super.trim();
    return new AnnotatedCircuit(trimmed, inputSizes, outputSizes);
  }

  public String toString() {
    return "AnnotatedCircuit: " + getMultibitInputCount() + " Inputs ("
        + inputSize() + " bits), "
        + getMultibitOutputCount() + " Outputs ("
        + outputSize() + " bits), "
        + size() + " bits total";
  }


  private static class Range implements Iterator<Integer> {
    private int i;
    private int end;

    Range(int start, int end) {
      this.i = start;
      this.end = end;
    }

    public boolean hasNext() {
      return this.i > this.end;
    }

    public Integer next() {
      int r = this.i;
      this.i++;
      return r;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private static class IterMap<K> implements Iterator<K> {
    private K[] map;
    private Iterator<Integer> iter;

    IterMap(Iterator<Integer> iter, K[] map) {
      this.iter = iter;
      this.map = map;
    }

    public boolean hasNext() {
      return iter.hasNext();
    }

    public K next() {
      return map[iter.next()];
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
