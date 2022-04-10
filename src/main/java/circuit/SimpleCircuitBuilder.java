package circuit;

import graph.DirectedGraph;
import graph.Edge;
import graph.TwoWayDirectedGraph;

import java.util.ArrayList;

/**
 * The order in which you register inputs matters.
 * Same with the order in which you register outputs.
 */
public class SimpleCircuitBuilder {
  private ArrayList<Integer> inputs;
  private ArrayList<Integer> outputs;
  private DirectedGraph<Integer> redstone;

  public SimpleCircuitBuilder() {
    inputs = new ArrayList<Integer>();
    outputs = new ArrayList<Integer>();
    redstone = DirectedGraph.integerBase();
  }

  public int addNode() {
    return redstone.addNode();
  }

  public void ensureSize(int size) {
    redstone.ensureSize(size);
  }

  public void addEdge(Integer start, Integer end) {
    redstone.addEdge(new Edge<>(start, end));
  }

  public void registerInput(int inputNode) {
    if (inputNode < 0) {
      throw new IllegalArgumentException("Negative node index!");
    }
    inputs.add(inputNode);
  }

  public void registerOutput(int outputNode) {
    if (outputNode < 0) {
      throw new IllegalArgumentException("Negative node index!");
    }
    outputs.add(outputNode);
  }

  public Circuit toCircuit() {
    return new Circuit(new TwoWayDirectedGraph<>(redstone), inputs.toArray(new Integer[0]), outputs.toArray(new Integer[0]));
  }
}
