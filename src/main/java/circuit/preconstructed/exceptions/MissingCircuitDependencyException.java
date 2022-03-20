package circuit.preconstructed.exceptions;

public class MissingCircuitDependencyException extends Exception {
  private String circuitName;

  public MissingCircuitDependencyException(String circuitName) {
    super("Missing circuit dependency: \"" + circuitName + "\"");
    this.circuitName = circuitName;
  }

  public String getCircuitName() {
    return circuitName;
  }
}
