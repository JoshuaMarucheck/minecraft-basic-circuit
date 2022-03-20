package circuit.preconstructed.exceptions;

public class StrictCheckException extends RuntimeException {
  public StrictCheckException() {
    super();
  }

  public StrictCheckException(String message) {
    super(message);
  }

  public StrictCheckException(Throwable cause) {
    super(cause);
  }

  public StrictCheckException(String message, Throwable cause) {
    super(message, cause);
  }
}
