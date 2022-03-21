package circuit.preconstructed;

import circuit.AnnotatedCircuit;
import circuit.preconstructed.exceptions.MissingCircuitDependencyException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CircuitCollection {
  private Map<String, AnnotatedCircuit> namedCircuits = new HashMap<String, AnnotatedCircuit>();

  public void registerCircuit(String circuitName, AnnotatedCircuit circuit) {
    if (namedCircuits.containsKey(circuitName)) {
      throw new IllegalArgumentException("A circuit called \"" + circuitName + "\" has already been registered!");
    }
    namedCircuits.put(circuitName, circuit);
  }

  Map<String, AnnotatedCircuit> getMap() {
    return namedCircuits;
  }

  public AnnotatedCircuit get(String name) {
    return namedCircuits.get(name);
  }

  public boolean contains(String circuitName) {
    return get(circuitName) != null;
  }

  public void addAll(CircuitCollection other) {
    namedCircuits.putAll(other.namedCircuits);
  }

  public AnnotatedCircuit getOrLoad(File file) throws IOException, MissingCircuitDependencyException {
    String circuitName = getCircuitName(file);
    AnnotatedCircuit circuit = get(circuitName);
    if (circuit == null) {
      circuit = loadFromFile(file, true, true);
    }
    return circuit;
  }

  public void parseAndRegister(Iterator<String> lines, boolean strict, String name) throws MissingCircuitDependencyException {
    AnnotatedCircuit circuit = GateFileParser.parse(namedCircuits, lines, strict, name);
    registerCircuit(name, circuit);
  }

  private String getCircuitName(File file) {
    String fileName = file.getName();
    int dot = fileName.lastIndexOf('.');
    String circuitName;
    if (dot != -1) {
      circuitName = fileName.substring(0, dot);
    } else {
      circuitName = fileName;
    }
    return circuitName;
  }

  public AnnotatedCircuit loadFromFile(File file, boolean strict, boolean register) throws IOException, MissingCircuitDependencyException {
    String circuitName = getCircuitName(file);

    AnnotatedCircuit circuit = GateFileParser.parse(namedCircuits, new LineIterator(new BufferedReader(new FileReader(file))), strict, circuitName);

    if (register) {
      registerCircuit(circuitName, circuit);
    }

    return circuit;
  }

  private static class LineIterator implements Iterator<String> {
    private String nextLine;
    private BufferedReader reader;

    LineIterator(BufferedReader reader) throws IOException {
      nextLine = reader.readLine();
      this.reader = reader;
    }

    public boolean hasNext() {
      return nextLine != null;
    }

    public String next() {
      String r = nextLine;
      try {
        nextLine = reader.readLine();
      } catch (IOException e) {
        throw new RuntimeException("File read failed");
      }
      return r;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
