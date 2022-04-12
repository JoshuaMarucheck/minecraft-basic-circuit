package physical.edges;

import circuit.AnnotatedCircuit;
import graph.Edge;
import graph.IntegerIterable;
import physical.things.Point3D;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Note, we are placing edges at positions!
 * <p>
 * The original node type in the base circuit is Integer
 *
 * @param <T> generative node type in torchPlacer
 */
public class TorchEndsPlacer<T> {
  private AnnotatedCircuit circuit;
  private TorchPlacer<Integer, T> torchPlacer;

  public TorchEndsPlacer(AnnotatedCircuit circuit, Iterable<T> gen) {
    this.circuit = circuit;
    torchPlacer = new TorchPlacer<>(circuit.getGraph(), gen);
  }

  public static TorchEndsPlacer<Integer> make(AnnotatedCircuit circuit) {
    return new TorchEndsPlacer<>(circuit, new IntegerIterable());
  }

  /**
   * @param outputID The id of the circuit output to map
   * @param start    The position of the first bit of the output
   * @param step     Probably a translation function.
   */
  public void placeOutput(int outputID, Point3D start, Function<Point3D, Point3D> step) {
    Iterator<Integer> blobIter = circuit.getMultibitOutput(outputID);

    while (blobIter.hasNext()) {
      T torch = torchPlacer.edgeToTorch(new Edge<>(blobIter.next(), null));
      torchPlacer.forcePlaceTorch(torch, start);
      start = step.apply(start);
    }
  }

  public void placeInput(int inputID, Point3D start, Function<Point3D, Point3D> step) {
    Iterator<Integer> blobIter = circuit.getMultibitInput(inputID);

    while (blobIter.hasNext()) {
      T torch = torchPlacer.edgeToTorch(new Edge<>(null, blobIter.next()));
      torchPlacer.forcePlaceTorch(torch, start);
      start = step.apply(start);
    }
  }

  public TorchPlacer getTorchPlacer() {
    return torchPlacer;
  }
}
