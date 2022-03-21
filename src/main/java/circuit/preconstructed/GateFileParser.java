package circuit.preconstructed;

import circuit.AnnotatedCircuit;
import circuit.AnnotationCircuitBuilder;
import circuit.Pair;
import circuit.iterators.FilterIterator;
import circuit.preconstructed.exceptions.ConstantValueException;
import circuit.preconstructed.exceptions.MissingCircuitDependencyException;
import circuit.preconstructed.exceptions.StrictCheckException;
import tokens.Tokenizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * public facing functions: registerCircuit, parse
 */
public class GateFileParser {
  /**
   * @param lines
   * @param strict
   * @param name   {@code null} if you don't want to register the circuit
   * @return
   * @throws NumberFormatException
   * @throws MissingCircuitDependencyException
   */
  public static AnnotatedCircuit parse(Map<String, AnnotatedCircuit> namedCircuits, Iterator<String> lines, boolean strict, String name) throws NumberFormatException, MissingCircuitDependencyException {
    GateFileParser gp = new GateFileParser(namedCircuits, lines, strict);
    AnnotatedCircuit circuit;
    try {
      circuit = gp.parseFile();
    } catch (RuntimeException e) {
      throw new RuntimeException("For circuit \"" + name + "\"", e);
    }

    return circuit;
  }

  public static AnnotatedCircuit parse(Map<String, AnnotatedCircuit> namedCircuits, String[] lines, boolean strict, String registerName) throws NumberFormatException, MissingCircuitDependencyException {
    return parse(namedCircuits, new ArrayIter<String>(lines), strict, registerName);
  }

  private Map<String, AnnotatedCircuit> namedCircuits;
  private HashMap<String, String> nameMapping;
  private HashMap<String, BitCollection> bitCollections;
  private Iterator<String> lines;
  private AnnotationCircuitBuilder circuitBuilder;
  private boolean strict;


  private GateFileParser(Map<String, AnnotatedCircuit> namedCircuits, Iterator<String> lines, boolean strict) {
    this.namedCircuits = namedCircuits;
    this.nameMapping = new HashMap<String, String>();
    this.bitCollections = new HashMap<String, BitCollection>();
    this.lines = new LineFilterIterator(lines);
    this.circuitBuilder = new AnnotationCircuitBuilder();
    this.strict = strict;
  }

  boolean[] toBoolArr(long constant, int bitLength) throws ConstantValueException {
    boolean[] arr = new boolean[bitLength];
    Arrays.fill(arr, false);

    long val = constant;
    for (int i = 0; i < bitLength && val != 0; i++) {
      arr[i] = val % 2 == 1;
      val /= 2;
    }
    if (val != 0) {
      throw new ConstantValueException(constant, bitLength);
    }
    return arr;
  }

  private Pair<String, Integer> splitToken(String token) {
    int colon = token.indexOf(':');

    int length;
    String varName;

    try {
      varName = token.substring(0, colon);
      length = Integer.parseInt(token.substring(colon + 1));
    } catch (IndexOutOfBoundsException e) {
      varName = token;
      length = -1;
    } catch (NumberFormatException e) {
      varName = token;
      length = -1;
    }

    return new Pair<String, Integer>(varName, length);
  }

  /**
   * Parses a token that is not a function.
   * <p>
   * If {@code isDeclaration} is {@code true}, then it checks that it is not a constant value,
   * and it shadows any old declaration. If {@code false}, it makes sure that it exists as a variable or is a constant.
   * <p>
   * Parses a token that is either the output of another circuit (a variable name) or a constant.
   * If it is a constant, adds a circuit outputting it to the builder, and returns the {@code BitCollection}.
   *
   * @param isDeclaration Whether or not {@code token} is the first in its line (or is an input to the circuit)
   * @param token         The constant string to parse
   * @return The BitCollection of the circuit that outputs this value.
   */
  private BitCollection parseToken(String token, boolean isDeclaration) {
    Pair<String, Integer> spl = splitToken(token);

    String varName = spl.getFirst();
    int length = spl.getSecond();

    long varVal;
    try {
      // varName is maybe a constant?
      int radix = -1;
      if (varName.length() >= 3 && varName.charAt(0) == '0') {
        switch (varName.charAt(1)) {
          case 'x':
            radix = 16;
            break;
          case 'b':
            radix = 2;
            break;
        }
      }
      if (radix != -1) {
        varVal = Integer.parseInt(varName.substring(2), radix);
      } else {
        varVal = Integer.parseInt(varName);
      }
    } catch (NumberFormatException e) {
      // varName is not a constant
      if (isDeclaration) {
        AnnotatedCircuit ac = LowLevelCircuitGenerator.identity(length);
        int circuitId = circuitBuilder.addCircuit(ac);
        circuitBuilder.registerAsInput(circuitId);

        BitCollection bc = new BitCollection(circuitId, length);
        makeAlias(varName, bc);
        return getLocal(varName);
      } else {
        BitCollection bc = getLocal(varName);
        if (bc != null && length != -1 && length != bc.length) {
          throw new IllegalArgumentException("For token " + token + ": bit length is different from before!");
        }
        return bc;
      }
    }

    // varName is a constant, with value varVal
    if (isDeclaration) {
      throw new IllegalArgumentException("For token " + token + ": you cannot set a constant to another value!");
    } else {
      try {
        boolean[] varBoolValue = toBoolArr(varVal, length);

        AnnotatedCircuit ac = LowLevelCircuitGenerator.constant(varBoolValue);
        int circuitId = circuitBuilder.addCircuit(ac);

        BitCollection bc = new BitCollection(circuitId, length);
        makeAlias(varName, bc);
        return bc;
      } catch (ConstantValueException e) {
        throw new RuntimeException("For constant " + token, e);
      }
    }
  }

  /**
   * Generally, {@code createIdentityCircuits} mode requires {@code strict} mode.
   * <p>
   * As a side effect, it populates {@code bitCollections}, {@code nameMapping} and {@code inputNames} with the inputs.
   */
  private void parseInitialBoundaryLine(String line, boolean createIdentityCircuits) {
    String[] arr = line.split("\\s+");

    if (createIdentityCircuits) {
      for (String input : arr) {
        parseToken(input, true);
      }
    }
  }

  private void parseFinalBoundaryLine(String line) {
    String[] outputNames = line.split("\\s+");

    if (outputNames.length != 1) {
      if (outputNames.length >= 2 && outputNames[1].equals("=")) {
        throw new IllegalArgumentException("File ended with an assignment instead of a return value");
      } else if (outputNames.length == 0) {
        throw new IllegalStateException("It shouldn't be possible for an empty line to reach this point...");
      } else {
        throw new UnsupportedOperationException("All files must have exactly one output for now");
      }
    }

    for (String output : outputNames) {
      String trueOutput = splitToken(output).getFirst();

      // parseToken does a length check for us (declared length against previously calculated length)
      BitCollection bc1 = parseToken(output, false);
      if (bc1 == null) {
        throw new IllegalArgumentException("Unrecognised return value: \"" + output + "\"");
      }
      circuitBuilder.registerAsOutput(bc1.circuitId);

      BitCollection bc2 = getLocal(trueOutput);
      assert !strict || bc1.length == bc2.length;
    }
  }

  /**
   * @param varName The name to make an alias for
   * @param bc      The BitCollection to associate with this alias
   * @return The alias
   */
  private String makeAlias(String varName, BitCollection bc) {
    String tempName = varName;

    if (bitCollections.containsKey(tempName)) {
      int i = 0;
      while (bitCollections.containsKey(tempName)) {
        tempName = varName + "_" + i;
        i++;
      }
      nameMapping.put(varName, tempName);
    }

    bitCollections.put(tempName, bc);

    return tempName;
  }

  /**
   * (Returns the one to use to index into bitCollections)
   *
   * @return The global name associated with this local name
   */
  private String getAlias(String varName) {
    if (nameMapping.containsKey(varName)) {
      return nameMapping.get(varName);
    } else {
      return varName;
    }
  }

  /**
   * Queries for the global variable associated with the alias of {@code localName} (if there is one).
   * For setting the values, use {@code makeAlias}
   *
   * @return The BitCollection currently associated with this variable name, or {@code null} if none exists
   */
  private BitCollection getLocal(String localName) {
    return bitCollections.get(this.getAlias(localName));
  }

  /**
   * Parses an expression, going until either hitting a ')' character or the end of stream.
   * Note that sub expressions contained in () are evaluated recursively.
   * That is, start the iterator immediately after any given '(' character.
   *
   * @return the id of the new circuit in {@code circuitBuilder}.
   */
  private int parseExpression(Iterator<String> tokenIter) throws MissingCircuitDependencyException {
    String mainCircuitName = tokenIter.next();
    AnnotatedCircuit mainCircuit = namedCircuits.get(mainCircuitName);
    if (mainCircuit == null) {
      if (tokenIter.hasNext()) {
        // Oops, we can't find that circuit name
        if (mainCircuitName.equals("(")) {
          StringBuilder sb = new StringBuilder("(");
          String token;
          do {
            token = tokenIter.next();
            sb.append(" ").append(token);
          } while (tokenIter.hasNext() && !token.equals(")"));
          throw new IllegalArgumentException("An expression started with a '(' character. (All expressions must start with a function name.)\nFor expression \"" + sb.toString() + "\"");
        } else {
          throw new MissingCircuitDependencyException(mainCircuitName);
        }
      } else {
        // Maybe it's a constant?
        String token = mainCircuitName;
        BitCollection bc = parseToken(token, false);
        if (bc == null) {
          throw new IllegalArgumentException("Constant/variable not recognised: \"" + token + "\"");
        }
        return bc.circuitId;
      }
    }

    int mainCircuitId = circuitBuilder.addCircuit(mainCircuit);

    int inputIdx = 0;


    String token;
    while (tokenIter.hasNext()) {
      token = tokenIter.next();
      if (token.equals(")")) {
        break;
      }
      int subCircuitId;
      if (token.equals("(")) {
        subCircuitId = this.parseExpression(tokenIter);
      } else {
        BitCollection bc = parseToken(token, false);
        if (bc == null) {
          throw new RuntimeException('"' + token + "\" was never assigned a value in bitCollections");
        }
        subCircuitId = bc.getCircuitId();
      }
      circuitBuilder.plugCircuit(subCircuitId, mainCircuitId);
      if (strict &&
          mainCircuit.getMultibitInputSize(inputIdx) != circuitBuilder.getCircuit(subCircuitId).outputSize()) {
        throw new StrictCheckException("Strict check failed! Circuit input and output size don't match!");
      }
      inputIdx++;
    }

    if (strict && inputIdx !=
        mainCircuit.getMultibitInputCount()) {
      throw new StrictCheckException("Strict check failed! Circuit doesn't have enough inputs specified!");
    }

    return mainCircuitId;
  }

  /**
   * A single line takes one of two forms:
   * a -> b
   * a = ...
   *
   * @param line The line to parse
   */
  private void parseLine(String line) throws MissingCircuitDependencyException {
    Iterator<String> tokenIter = new Tokenizer().tokenize(line);
    if (!tokenIter.hasNext()) {
      return;
    }
    String name = tokenIter.next();

    String operator = tokenIter.next();
    if (operator.equals("=")) {
      try {
        int exprId = parseExpression(tokenIter);
        this.makeAlias(name, new BitCollection(exprId, circuitBuilder.getCircuit(exprId).outputSize()));
      } catch (StrictCheckException e) {
        throw new StrictCheckException("On line \"" + line + "\"\n" + e.getMessage());
      }

    } else {
      throw new RuntimeException("Illegal line while parsing file: \"" + line + "\"; Unrecognised operator '" + operator + "'");
    }
  }

  private AnnotatedCircuit parseFile() throws MissingCircuitDependencyException {
    this.parseInitialBoundaryLine(lines.next(), strict);

    String line = lines.next();
    while (lines.hasNext()) {
      this.parseLine(line);
      line = lines.next();
    }

    this.parseFinalBoundaryLine(line);

    return circuitBuilder.toCircuit();
  }

  private static class BitCollection {
    /**
     * The id in circuitBuilder of the circuit which outputs
     * this BitCollection, or -1 if there is no such circuit
     * (in the scope of this file)
     */
    private int circuitId;
    private int length;

    BitCollection(int circuitId, int length) {
      this.circuitId = circuitId;
      this.length = length;
    }

    public int getCircuitId() {
      return circuitId;
    }

    public int getLength() {
      return length;
    }
  }

  private static class ArrayIter<T> implements Iterator<T> {
    private T[] arr;
    private int index;

    ArrayIter(T[] arr) {
      this.arr = arr;
      this.index = 0;
    }

    public boolean hasNext() {
      return index < arr.length;
    }

    public T next() {
      T r = arr[index];
      index++;
      return r;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private static class LineFilterIterator extends FilterIterator<String> {
    public LineFilterIterator(Iterator<String> iter) {
      super(iter);
    }

    protected boolean filter(String s) {
      return !s.equals("\n") && !s.equals("") && !s.startsWith("//");
    }
  }
}
