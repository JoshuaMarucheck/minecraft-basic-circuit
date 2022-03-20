package circuit.preconstructed;

import circuit.AnnotatedCircuit;
import circuit.AnnotationCircuitBuilder;
import circuit.Pair;
import circuit.iterators.FilterIterator;
import circuit.preconstructed.exceptions.MissingCircuitDependencyException;
import circuit.preconstructed.exceptions.StrictCheckException;
import tokens.Tokenizer;

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
  private String[] inputNames;
  private String[] outputNames;
  private HashMap<String, String> nameMapping;
  private HashMap<String, BitCollection> bitCollections;
  private Iterator<String> lines;
  private AnnotationCircuitBuilder circuitBuilder;
  private boolean strict;


  private GateFileParser(Map<String, AnnotatedCircuit> namedCircuits, Iterator<String> lines, boolean strict) {
    this.namedCircuits = namedCircuits;
    this.inputNames = null;
    this.outputNames = null;
    this.nameMapping = new HashMap<String, String>();
    this.bitCollections = new HashMap<String, BitCollection>();
    this.lines = new LineFilterIterator(lines);
    this.circuitBuilder = new AnnotationCircuitBuilder();
    this.strict = strict;
  }

  /**
   * Generally, {@code createIdentityCircuits} mode requires {@code strict} mode.
   * <p>
   * As a side effect, it populates {@code bitCollections}, {@code nameMapping} and {@code inputNames} with the inputs.
   */
  private void parseInitialBoundaryLine(String line, boolean createIdentityCircuits) {
    String[] arr = line.split("\\s+");
    this.inputNames = arr;

    if (createIdentityCircuits) {
      for (String input : arr) {
        int colon = input.indexOf(':');
//      int offset = -1;
        int circuitId;
        int length;
        String varName;

        try {
          varName = input.substring(colon + 1);
          length = Integer.parseInt(input.substring(0, colon));
        } catch (IndexOutOfBoundsException e) {
          varName = input;
          length = -1;
        } catch (NumberFormatException e) {
          varName = input;
          length = -1;
        }

        AnnotatedCircuit ac = LowLevelCircuitGenerator.identity(length);
        circuitId = circuitBuilder.addCircuit(ac);
        circuitBuilder.registerAsInput(circuitId);

        BitCollection bc = new BitCollection(circuitId, length);
        makeAlias(varName, bc);
      }
    }
  }


  private Pair<String[], HashMap<String, BitCollection>> parseFinalBoundaryLine(String line) {
    HashMap<String, BitCollection> r = new HashMap<String, BitCollection>();
    String[] arr = line.split("\\s+");

    for (String input : arr) {
      int colon = input.indexOf(':');
//      int offset = -1;
      int length;
      String varName;

      try {
        varName = input.substring(colon + 1);
        length = Integer.parseInt(input.substring(0, colon));
      } catch (IndexOutOfBoundsException e) {
        varName = input;
        length = -1;
      } catch (NumberFormatException e) {
        varName = input;
        length = -1;
      }

      BitCollection bc = getLocal(varName);
      r.put(varName, bc);
      if (length != -1 && bc.length != length) {
        throw new StrictCheckException("Length of output doesn't match prediction");
      }
    }

    return new Pair<String[], HashMap<String, BitCollection>>(arr, r);
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
      throw new MissingCircuitDependencyException(mainCircuitName);
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
        BitCollection bc = getLocal(token);
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

    Pair<String[], HashMap<String, BitCollection>> finish = parseFinalBoundaryLine(line);
    this.outputNames = finish.getFirst();
    HashMap<String, BitCollection> outputBitCollections = finish.getSecond();
    if (this.outputNames.length != 1) {
      if (this.outputNames.length >= 2 && this.outputNames[1].equals("=")) {
        throw new IllegalArgumentException("File ended with an assignment instead of a return value");
      } else if (this.outputNames.length == 0) {
        throw new IllegalStateException("It shouldn't be possible for an empty line to reach this point...");
      } else {
        throw new UnsupportedOperationException("All files must have exactly one output for now");
      }
    }
    for (String output : this.outputNames) {
      BitCollection bc = getLocal(output);
      assert !strict || bc.length == outputBitCollections.get(output).length;
      circuitBuilder.registerAsOutput(bc.circuitId);
    }

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
