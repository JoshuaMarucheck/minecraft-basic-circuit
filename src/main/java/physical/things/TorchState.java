package physical.things;

/**
 * Represents if this torch is an input or an output
 * to the blob it's connected to by the edge containing this TorchState.
 */
public enum TorchState {
  NONE, INPUT, OUTPUT;
}
