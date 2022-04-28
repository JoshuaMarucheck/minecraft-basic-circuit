import circuit.Circuit;
import circuit.DebugCircuit;
import circuit.Pair;
import circuit.preconstructed.CircuitCollection;
import circuit.preconstructed.LowLevelCircuitGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import static nbt.Constants.circuitRoot;

public class DefaultCircuitTest {
  public static void main(String[] args) throws Exception {
    System.out.println("Constructing tests");

    LowLevelCircuitGenerator gen = LowLevelCircuitGenerator.canonicalGenerator;

    CircuitCollection cc4 = LowLevelCircuitGenerator.defaultNamedCircuits();
    cc4.addAll(gen.operators(4));

    cc4.registerCircuit("add1", gen.addition(1));
    cc4.registerCircuit("add2", gen.addition(2));

    cc4.registerCircuit("any4", LowLevelCircuitGenerator.any(4));
    cc4.registerCircuit("all4", LowLevelCircuitGenerator.all(4));

    cc4.getOrLoad(new File(circuitRoot + "specialized/xor3.txt"));
    cc4.getOrLoad(new File(circuitRoot + "specialized/atLeast2.txt"));

    CircuitCollection cc64 = LowLevelCircuitGenerator.defaultNamedCircuits();
    cc64.addAll(gen.operators(64));

    cc64.getOrLoad(new File(circuitRoot + "specialized/constant5.txt"));
    cc64.getOrLoad(new File(circuitRoot + "final/is_palindrome.txt"));

    CircuitTestCollection[] ctcs = new CircuitTestCollection[]{
        new CircuitTestCollection(cc4, new CircuitTest[]{
            new CircuitTest("not", "0", "1"),
            new CircuitTest("not", "1", "0"),

            new CircuitTest("and", "00", "0"),
            new CircuitTest("and", "10", "0"),
            new CircuitTest("and", "01", "0"),
            new CircuitTest("and", "11", "1"),

            new CircuitTest("or", "00", "0"),
            new CircuitTest("or", "10", "1"),
            new CircuitTest("or", "01", "1"),
            new CircuitTest("or", "11", "1"),

            new CircuitTest("xor", "00", "0"),
            new CircuitTest("xor", "10", "1"),
            new CircuitTest("xor", "01", "1"),
            new CircuitTest("xor", "11", "0"),

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

            new CircuitTest("|", "1111 0000", "1111"),
            new CircuitTest("|", "0000 0000", "0000"),
            new CircuitTest("|", "1001 0100", "1101"),
            new CircuitTest("|", "1101 1000", "1101"),
            new CircuitTest("|", "0011 0101", "0111"),

            new CircuitTest("&", "1111 0000", "0000"),
            new CircuitTest("&", "1111 1111", "1111"),
            new CircuitTest("&", "1001 0100", "0000"),
            new CircuitTest("&", "1101 1000", "1000"),
            new CircuitTest("&", "0011 0101", "0001"),
            new CircuitTest("&", "0111 1110", "0110"),

            new CircuitTest("^", "1111 0000", "1111"),
            new CircuitTest("^", "1111 1111", "0000"),
            new CircuitTest("^", "0000 0000", "0000"),
            new CircuitTest("^", "1001 0100", "1101"),
            new CircuitTest("^", "1101 1000", "0101"),
            new CircuitTest("^", "0011 0101", "0110"),
            new CircuitTest("^", "0111 1110", "1001"),

            new CircuitTest("!", "0000", "1"),
            new CircuitTest("!", "0111", "0"),
            new CircuitTest("!", "1000", "0"),
            new CircuitTest("!", "0100", "0"),
            new CircuitTest("!", "0010", "0"),
            new CircuitTest("!", "0001", "0"),
            new CircuitTest("!", "0110", "0"),
            new CircuitTest("!", "1100", "0"),
            new CircuitTest("!", "1110", "0"),
            new CircuitTest("!", "1010", "0"),

            new CircuitTest("<<1", "1010", "0101"),
            new CircuitTest("<<2", "1010", "0010"),
            new CircuitTest(">>1", "1010", "0100"),
            new CircuitTest(">>2", "1010", "1000"),
        }),
        new CircuitTestCollection(gen.operators(8), new CircuitTest[]{
            // our numbers are little-endian, but shifting is big-endian
            new CircuitTest("<<2", "00101100", "00001011"),
            new CircuitTest("<<4", "00101100", "00000010"),

            new CircuitTest(">>2", "00101100", "10110000"),
            new CircuitTest(">>4", "00101100", "11000000"),

            new CircuitTest("+", "00101100 01000101", "01101011"),
        }),
        new CircuitTestCollection(cc64, new CircuitTest[]{
            new CircuitTest("constant5", "", "101000"),

            new CircuitTest("is_palindrome", "00001111 01010011 10101100 00000000 00000000 00110101 11001010 11110000", "1"),
            new CircuitTest("is_palindrome", "00001111 01010011 11001010 11110000 00000000 00000000 00000000 00000000", "0"),}),
    };

    System.out.println("Running tests...");

    for (CircuitTestCollection tests : ctcs) {
      for (CircuitTest test : tests) {
        try {
          runCircuitTest(test.getCircuit(), test.input, test.expectedOutput);
        } catch (Exception e) {
          throw new RuntimeException("On test " + test.toString(), e);
        }
        try {
          runCircuitTest(test.getCircuit().trim(), test.input, test.expectedOutput);
        } catch (Exception e) {
          throw new RuntimeException("On trimmed test " + test.toString(), e);
        }
      }
    }

    System.out.println("Tests complete");
  }

  private static void runCircuitTest(Circuit circuit, boolean[] input, boolean[] expectedOutput) throws UnitTestFailException {
    if (circuit == null) {
      throw new UnitTestFailException("Missing circuit!");
    }
    if (circuit instanceof DebugCircuit) {
      ArrayList<Pair<String, boolean[]>> debugMap = ((DebugCircuit) circuit).simulateDebug(input);
      ArrayList<Pair<String, String>> readableDebugMap = new ArrayList<Pair<String, String>>();
      for (Pair<String, boolean[]> pair : debugMap) {
        String s = pair.getFirst();
        boolean[] output = pair.getSecond();
        readableDebugMap.add(new Pair<String, String>(s, boolArrToStr(output)));
      }
      System.out.print("");
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
    int spaces = 0;
    for (char c : s.toCharArray()) {
      if (c == ' ') {
        spaces++;
      }
    }

    boolean[] r = new boolean[s.length() - spaces];
    int index = 0;
    for (char c : s.toCharArray()) {
      switch (c) {
        case '0':
          r[index] = false;
          index++;
          break;
        case '1':
          r[index] = true;
          index++;
          break;
        case ' ':
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

  private static class CircuitTestCollection implements Iterable<CircuitTest> {
    private CircuitTest[] tests;

    CircuitTestCollection(CircuitCollection cc, CircuitTest[] tests) {
      this.tests = tests;
      for (CircuitTest test : tests) {
        test.circuit = cc.get(test.circuitName);
      }
    }

    public Iterator<CircuitTest> iterator() {
      return new ArrayIterator<CircuitTest>(tests);
    }
  }

  private static class ArrayIterator<T> implements Iterator<T> {
    private T[] arr;
    int i;

    public ArrayIterator(T[] arr) {
      this.arr = arr;
      this.i = 0;
    }

    public boolean hasNext() {
      return i < arr.length;
    }

    public T next() {
      T t = arr[i];
      i++;
      return t;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  /*
   * Recall that long numbers are little-endian!
   */

  private static class CircuitTest {
    String circuitName;
    boolean[] input;
    boolean[] expectedOutput;
    Circuit circuit;

    CircuitTest(String circuitName, boolean[] input, boolean[] output) {
      this.circuitName = circuitName;
      this.input = input;
      this.expectedOutput = output;
      this.circuit = null;
    }

    CircuitTest(String circuitName, String input, String output) {
      this(circuitName, parseBoolString(input), parseBoolString(output));
    }

    public String toString() {
      return circuitName + ": " + boolArrToStr(input) + " -> " + boolArrToStr(expectedOutput);
    }

    public Circuit getCircuit() {
      return circuit;
    }
  }
}
