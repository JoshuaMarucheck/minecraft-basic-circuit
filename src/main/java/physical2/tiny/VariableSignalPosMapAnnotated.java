package physical2.tiny;

import circuit.AnnotatedCircuit;
import circuit.DebugCircuit;
import misc.FuncMapIterator;
import misc.IteratorConcatenator;
import physical2.two.Point2D;

import java.util.Iterator;

public class VariableSignalPosMapAnnotated extends VariableSignalPosMap<Integer> {
  protected AnnotatedCircuit circuit;

  public VariableSignalPosMapAnnotated(AnnotatedCircuit circuit, Iterable<Point2D> legalPositions) {
    super(legalPositions);
    this.circuit = circuit;
  }

  public static VariableSignalPosMapAnnotated makeWithDebug(DebugCircuit circuit, Iterable<Point2D> legalPositions) {
    VariableSignalPosMapAnnotated r = new VariableSignalPosMapAnnotated(circuit, legalPositions);
    // TODO: Add labelling
    return r;
  }

  public Iterator<Point2D> inputPosIterator() {
    return new FuncMapIterator<>(this::getPos, new IteratorConcatenator<>(new Iterator<Iterator<Integer>>() {
      private int i = 0;

      @Override
      public boolean hasNext() {
        return i < circuit.getMultibitInputCount();
      }

      @Override
      public Iterator<Integer> next() {
        return circuit.getMultibitInput(i++);
      }
    }));
  }

  public Iterator<Point2D> outputPosIterator() {
    return new FuncMapIterator<>(this::getPos, new IteratorConcatenator<>(new Iterator<Iterator<Integer>>() {
      private int i = 0;

      @Override
      public boolean hasNext() {
        return i < circuit.getMultibitInputCount();
      }

      @Override
      public Iterator<Integer> next() {
        return circuit.getMultibitInput(i++);
      }
    }));
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
