package circuit.preconstructed.exceptions;

public class ConstantValueException extends Exception {
  private long val;
  private int bitLength;

  public ConstantValueException(long val, int bitLength) {
    super("Constant: \"" + val + " does not fit in constant of bit length " + bitLength + "\"");
    this.val = val;
    this.bitLength = bitLength;
  }

  public long getVal() {
    return val;
  }

  public int getBitLength() {
    return bitLength;
  }
}
