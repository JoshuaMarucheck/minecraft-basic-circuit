package physical2.tiny;

import circuit.AnnotatedCircuit;
import circuit.DebugCircuit;
import graph.TwoWayDirectedGraph;
import physical2.two.Point2D;

import java.util.Iterator;

public class VariableSignalPosMapAnnotated extends VariableSignalPosMap<Integer> {
  protected AnnotatedCircuit circuit;

  private VariableSignalPosMapAnnotated(TwoWayDirectedGraph<Integer> redstone, Iterable<Point2D> legalPositions) {
    super(redstone, legalPositions);
  }

  public static VariableSignalPosMapAnnotated makeWithDebug(DebugCircuit circuit, Iterable<Point2D> legalPositions) {
    VariableSignalPosMapAnnotated r = makeWithAnnotations(circuit, legalPositions);
    // TODO: Add labelling
    return r;
  }

  public static VariableSignalPosMapAnnotated makeWithAnnotations(AnnotatedCircuit circuit, Iterable<Point2D> legalPositions) {
    VariableSignalPosMapAnnotated r = new VariableSignalPosMapAnnotated(circuit.getGraph(), legalPositions);
    r.circuit = circuit;
    return r;
  }

  public void placeInput(int inputId, Iterator<Point2D> positions) {
    for (Iterator<Integer> it = circuit.getMultibitInput(inputId); it.hasNext(); ) {
      Integer blob = it.next();
      put(blob, positions.next());
    }
  }

  public void placeOutput(int outputId, Iterator<Point2D> positions) {
    for (Iterator<Integer> it = circuit.getMultibitOutput(outputId); it.hasNext(); ) {
      Integer blob = it.next();
      put(blob, positions.next());
    }
  }
}
