import circuit.Circuit;
import circuit.preconstructed.CircuitCollection;
import circuit.preconstructed.LowLevelCircuitGenerator;

import java.io.File;
import java.util.Arrays;

public class DefaultCircuitTest {
  private static String root = "./src/main/resources/circuits/";

  public static void main(String[] args) throws Exception {
    CircuitCollection cc = LowLevelCircuitGenerator.defaultNamedCircuits();
    LowLevelCircuitGenerator gen = LowLevelCircuitGenerator.canonicalGenerator;
    cc.registerCircuit("add1", gen.addition(1));
    cc.registerCircuit("add2", gen.addition(2));

    cc.registerCircuit("any4", LowLevelCircuitGenerator.any(4));
    cc.registerCircuit("all4", LowLevelCircuitGenerator.all(4));

    cc.getOrLoad(new File(root + "specialized/xor3.txt"));
    cc.getOrLoad(new File(root + "specialized/atLeast2.txt"));

    CircuitTest[] tests = new CircuitTest[]{
        new CircuitTest("atLeast2", "000", "0"),
        new CircuitTest("atLeast2", "100", "0"),
        new CircuitTest("atLeast2", "010", "0"),
        new CircuitTest("atLeast2", "110", "1"),
        new CircuitTest("atLeast2", "001", "0"),
        new CircuitTest("atLeast2", "101", "1"),
        new CircuitTest("atLeast2", "011", "1"),
        new CircuitTest("atLeast2", "111", "1"),

        new CircuitTest("xor3", "000", "0"),
        new CircuitTest("xor3", "100", "1"),
        new CircuitTest("xor3", "010", "1"),
        new CircuitTest("xor3", "110", "0"),
        new CircuitTest("xor3", "001", "1"),
        new CircuitTest("xor3", "101", "0"),
        new CircuitTest("xor3", "011", "0"),
        new CircuitTest("xor3", "111", "1"),

        new CircuitTest("xor", "00", "0"),
        new CircuitTest("xor", "10", "1"),
        new CircuitTest("xor", "01", "1"),
        new CircuitTest("xor", "11", "0"),

        new CircuitTest("and", "00", "0"),
        new CircuitTest("and", "10", "0"),
        new CircuitTest("and", "01", "0"),
        new CircuitTest("and", "11", "1"),

        new CircuitTest("or", "00", "0"),
        new CircuitTest("or", "10", "1"),
        new CircuitTest("or", "01", "1"),
        new CircuitTest("or", "11", "1"),

        new CircuitTest("not", "0", "1"),
        new CircuitTest("not", "1", "0"),

        new CircuitTest("any4", "0000", "0"),
        new CircuitTest("any4", "1000", "1"),
        new CircuitTest("any4", "1100", "1"),
        new CircuitTest("any4", "1010", "1"),
        new CircuitTest("any4", "1001", "1"),
        new CircuitTest("any4", "0100", "1"),
        new CircuitTest("any4", "0010", "1"),
        new CircuitTest("any4", "0001", "1"),
        new CircuitTest("any4", "1111", "1"),

        new CircuitTest("all4", "0000", "0"),
        new CircuitTest("all4", "1000", "0"),
        new CircuitTest("all4", "1100", "0"),
        new CircuitTest("all4", "1010", "0"),
        new CircuitTest("all4", "1001", "0"),
        new CircuitTest("all4", "0100", "0"),
        new CircuitTest("all4", "0010", "0"),
        new CircuitTest("all4", "0001", "0"),
        new CircuitTest("all4", "0111", "0"),
        new CircuitTest("all4", "1011", "0"),
        new CircuitTest("all4", "1101", "0"),
        new CircuitTest("all4", "1110", "0"),
        new CircuitTest("all4", "1111", "1"),

        new CircuitTest("add1", "00", "0"), // zero
        new CircuitTest("add1", "01", "1"),
        new CircuitTest("add1", "10", "1"),
        new CircuitTest("add1", "11", "0"), // carry

        new CircuitTest("add2", "0000", "00"), // zero
        new CircuitTest("add2", "0110", "11"), // digits don't interfere with each other
        new CircuitTest("add2", "1010", "01"), // digits can carry
        new CircuitTest("add2", "1011", "00"), // overflow works
        new CircuitTest("add2", "0101", "00"),
        new CircuitTest("add2", "1111", "01"),
    };

    for (CircuitTest test : tests) {
      if (test.circuitName.equals("any4") && Arrays.equals(test.input, parseBoolString("1000"))) {
        System.out.println("hi");
      }
      try {
        runCircuitTest(cc.get(test.circuitName), test.input, test.expectedOutput);
      } catch (Exception e) {
        throw new RuntimeException("On test " + test.toString(), e);
      }
    }
  }

  private static void runCircuitTest(Circuit circuit, boolean[] input, boolean[] expectedOutput) throws UnitTestFailException {
    if (circuit == null) {
      throw new UnitTestFailException("Missing circuit!");
    }
    boolean[] output = circuit.simulate(input);
    if (output.length != expectedOutput.length) {
      throw new UnitTestFailException("Output lengths don't match! Expected " + expectedOutput.length + " bits, got " + output.length + " bits");
    }
    for (int i = 0; i < output.length; i++) {
      if (output[i] != expectedOutput[i]) {
        throw new UnitTestFailException("Outputs don't match at bit " + i + ". Expected " + boolArrToStr(expectedOutput) + ", got " + boolArrToStr(output));
      }
    }
  }


  private static boolean[] parseBoolString(String s) {
    boolean[] r = new boolean[s.length()];
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);

      switch (c) {
        case '0':
          r[i] = false;
          break;
        case '1':
          r[i] = true;
          break;
        default:
          throw new IllegalArgumentException("Boolean string contained digit '" + c + "'; \"" + s + "\"");
      }
    }

    return r;
  }

  private static String boolArrToStr(boolean[] arr) {
    StringBuilder sb = new StringBuilder();
    for (boolean b : arr) {
      if (b) {
        sb.append("1");
      } else {
        sb.append("0");
      }
    }
    return sb.toString();
  }

  /*
   * Recall that long numbers are little-endian!
   */

  private static class CircuitTest {
    String circuitName;
    boolean[] input;
    boolean[] expectedOutput;

    CircuitTest(String circuitName, boolean[] input, boolean[] output) {
      this.circuitName = circuitName;
      this.input = input;
      this.expectedOutput = output;
    }

    CircuitTest(String circuitName, String input, String output) {
      this.circuitName = circuitName;
      this.input = parseBoolString(input);
      this.expectedOutput = parseBoolString(output);
    }

    public String toString() {
      return circuitName + ": " + boolArrToStr(input) + " -> " + boolArrToStr(expectedOutput);
    }
  }

  private static class UnitTestFailException extends Exception {
    UnitTestFailException(String s) {
      super(s);
    }
  }
}
