package nbt;

import dev.dewy.nbt.api.Tag;
import dev.dewy.nbt.tags.TagType;
import dev.dewy.nbt.tags.array.ArrayTag;
import dev.dewy.nbt.tags.array.ByteArrayTag;
import dev.dewy.nbt.tags.array.IntArrayTag;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.collection.ListTag;
import dev.dewy.nbt.tags.primitive.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class SNBTParser {

  public static Tag fromFile(File file) throws SNBTParseException, IOException {
    String content = readFile(file);
    return parse(content);
  }

  private static String readFile(File file) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader reader = new BufferedReader(new FileReader(file));

    String line = reader.readLine();
    while (line != null) {
      sb.append(line);
      line = reader.readLine();
    }

    return sb.toString();
  }

  public static Tag parse(String nbt) throws SNBTParseException {
    SNBTTokenizer tokens = new SNBTTokenizer(nbt);
    Tag tag = parseTag(tokens);
    if (tokens.hasNext()) {
      throw new SNBTParseException("Leftover content", tokens);
    }
    if (tag.getTypeId() == TagType.COMPOUND.getId() && tag.getName() == null) {
      tag.setName("root");
    }
    return tag;
  }

  /**
   * @param tokens A tokenizer immediately after the first curly bracket has been taken.
   */
  private static CompoundTag parseCompoundTag(SNBTTokenizer tokens) throws SNBTParseException {
    CompoundTag r = new CompoundTag();

    while (tokens.hasNext()) {
      String subtagName;
      switch (tokens.next()) {
        default:
          subtagName = tokens.next();
          break;

        case "\"":
          subtagName = grabString(tokens);
          break;

        case "}":
          return r;
      }
      if (!tokens.assertNext(":")) {
        throw new SNBTParseException("Missing colon after tag name", tokens);
      }
      Tag tag = parseTag(tokens);
      r.put(subtagName, tag);

      Character peek = tokens.peekChar();
      if (peek == ',') {
        if (!tokens.assertNext(",")) {
          throw new IllegalStateException("glitch");
        }
      } else if (peek != '}') {
        throw new SNBTParseException("Missing comma after subtag", tokens);
      }
    }

    throw new SNBTParseException("Missing end brackets; incomplete tag", tokens);
  }

  /**
   * Parse any single item
   *
   * @param tokens Placed so the next item drawn is the start of the new tag
   */
  private static Tag parseTag(SNBTTokenizer tokens) throws SNBTParseException {
    if (!tokens.hasNext()) {
      throw new SNBTParseException("Thing to parse has no content", tokens);
    }
    String token = tokens.next();
    switch (token) {
      case "{":
        return parseCompoundTag(tokens);
      case "[":
        return parseListOrArray(tokens);
      case "\"":
        return parseString(tokens);
      default:
        return parseNumber(token, tokens);
    }
  }

  private static IntArrayTag parseIntArray(SNBTTokenizer tokens) throws SNBTParseException {
    ArrayList<Integer> list = new ArrayList<>();
    ListIter iter = new ListIter(tokens);
    while (iter.hasNext()) {
      Tag tag = iter.next();
      Integer i;
      try {
        i = (Integer) tag.getValue();
      } catch (NullPointerException | ClassCastException e) {
        throw new SNBTParseException("Value in int array is not int", tokens);
      }
      list.add(i);
    }
    return new IntArrayTag(list);
  }

  private static ByteArrayTag parseByteArray(SNBTTokenizer tokens) throws SNBTParseException {
    ArrayList<Byte> list = new ArrayList<>();
    ListIter iter = new ListIter(tokens);
    while (iter.hasNext()) {
      Tag tag = iter.next();
      Byte b;
      try {
        b = (Byte) tag.getValue();
      } catch (NullPointerException | ClassCastException e) {
        throw new SNBTParseException("Value in int array is not int", tokens);
      }
      list.add(b);
    }
    return new ByteArrayTag(list);
  }

  private static ArrayTag parseArray(SNBTTokenizer tokens) throws SNBTParseException {
    String item = tokens.next();
    if (!tokens.assertNext(";")) {
      throw new SNBTParseException("Missing semicolon after array identifier", tokens);
    }
    switch (item) {
      case "I":
        return parseIntArray(tokens);
      case "B":
        return parseByteArray(tokens);
      default:
        throw new SNBTParseException("Unrecognized array type " + item, tokens);
    }
  }

  private static class ListIter implements Closeable {
    private SNBTTokenizer tokens;
    private boolean done;

    /**
     * @param tokens A tokenizer which just had the square bracket and any starting list identifiers removed
     *               (i.e. it should not contain the "I;" or "B;" at the start)
     */
    ListIter(SNBTTokenizer tokens) {
      this.tokens = tokens;
      done = false;
    }

    public boolean hasNext() {
      if (done) {
        return false;
      }
      boolean next = tokens.peekChar() != ']';
      if (!next) {
        tokens.next();
        done = true;
      }
      return next;
    }

    public Tag next() throws SNBTParseException {
      Tag tag = parseTag(tokens);
      if (tokens.peekChar() == ',') {
        tokens.next();
      } else if (tokens.peekChar() != ']') {
        throw new SNBTParseException("Missing comma after list entry", tokens);
      }
      return tag;
    }

    public void close() {
      if (!tokens.assertNext("]")) {
        throw new IllegalStateException();
      }
    }
  }

  private static ListTag<Tag> parseList(SNBTTokenizer tokens) throws SNBTParseException {
    ListTag<Tag> r = new ListTag<>();

    ListIter iter = new ListIter(tokens);


    while (iter.hasNext()) {
      r.add(iter.next());
    }

    return r;
  }

  /**
   * Placed so that the starting square bracket is removed
   */
  private static Tag parseListOrArray(SNBTTokenizer tokens) throws SNBTParseException {
    if (Objects.equals(tokens.peekToken(1), ";")) {
      return parseArray(tokens);
    } else {
      return parseList(tokens);
    }
  }

  /**
   * Parses the top token as a number-type tag
   */
  private static Tag parseNumber(String numberToken, SNBTTokenizer tokens) throws SNBTParseException {
    if (numberToken.length() == 0) {
      throw new IllegalArgumentException();
    }
    int len = numberToken.length();
    try {

      String clippedToken = numberToken.substring(0, len - 1);
      switch (Character.toLowerCase(numberToken.charAt(len - 1))) {
        case 'b':
          return new ByteTag(Byte.parseByte(clippedToken));
        case 's':
          return new ShortTag(Short.parseShort(clippedToken));
        case 'i':
          return new IntTag(Integer.parseInt(clippedToken));
        case 'l':
          return new LongTag(Long.parseLong(clippedToken));
        case 'f':
          return new FloatTag(Float.parseFloat(clippedToken));
        case 'd':
          return new DoubleTag(Double.parseDouble(clippedToken));
        default:
          try {
            return new IntTag(Integer.parseInt(numberToken));
          } catch (NumberFormatException e) {
            return new DoubleTag(Double.parseDouble(numberToken));
          }
      }
    } catch (NumberFormatException ignored) {
    }
    throw new SNBTParseException("Number not recognized: " + numberToken, tokens);
  }

  private static StringTag parseString(SNBTTokenizer tokens) throws SNBTParseException {
    return new StringTag(grabString(tokens));
  }

  /**
   * @param tokens A tokenizer immediately after the first quote has been removed.
   */
  private static String grabString(SNBTTokenizer tokens) {
    String json = tokens.getString();
    int start = tokens.getPos();
    int end = findStringEnd(json, start);
    tokens.setPos(end + 1);

    return json.substring(start, end);
  }

  private static class SNBTTokenizer implements Iterator<String> {
    private String json;
    /**
     * The position of the start of the next thing to yield
     * (or maybe before that if there's whitespace)
     */
    private int pos;
    private static final boolean[] specialChars = makeSpecialChars();

    private static boolean[] makeSpecialChars() {
      boolean[] r = new boolean[128];
      Arrays.fill(r, false);
      r['"'] = true;
      r['{'] = true;
      r['}'] = true;
      r['['] = true;
      r[']'] = true;
      r[':'] = true;
      r[';'] = true;
      r[','] = true;
      return r;
    }

    public void setPos(int pos) {
      this.pos = pos;
    }

    public int getPos() {
      return this.pos;
    }

    public String getString() {
      return this.json;
    }

    private static boolean isSpecialChar(char c) {
      return specialChars[c];
    }

    SNBTTokenizer(String json) {
      this.json = json;
      pos = 0;
    }

    private void findNext() {
      while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
        pos++;
      }
    }

    /**
     * @return {@code true} iff the next token matches {@code next}
     */
    public boolean assertNext(String next) {
      return hasNext() && next().equals(next);
    }

    public String peekToken(int i) {
      int oldPos = pos;
      for (int k = 0; k < i; k++) {
        next();
      }
      String r = next();
      pos = oldPos;
      return r;
    }

    public Character peekChar() {
      return peekChar(0);
    }

    public Character peekChar(int i) {
      findNext();
      if (pos + i < json.length()) {
        return json.charAt(pos + i);
      } else {
        return null;
      }
    }

    @Override
    public boolean hasNext() {
      findNext();
      return pos < json.length();
    }

    @Override
    public String next() {
      findNext();
      int start = pos;
      if (isSpecialChar(json.charAt(pos))) {
        pos++;
        return String.valueOf(json.charAt(start));
      }

      int end = pos;
      while (!isSpecialChar(json.charAt(end)) && !Character.isWhitespace(json.charAt(end))) {
        end++;
      }
      pos = end;
      return json.substring(start, end);
    }
  }

  /**
   * @param start The position of the character immediately after the first quote
   * @return the position of the end quote
   */
  private static int findStringEnd(String s, int start) {
    while (start < s.length() && s.charAt(start) != '"') {
      if (s.charAt(start) == '\\') {
        start++;
      }
      start++;
    }
    return start;
  }

  public static class SNBTParseException extends Exception {
    private String parseTarget;
    private int index;

    SNBTParseException(String err, SNBTTokenizer tokens) {
      super(err + " (at index " + tokens.getPos() + ")");
      parseTarget = tokens.getString();
      index = tokens.getPos();
    }

    @Override
    public String toString() {
      return super.toString() + "\n" + parseTarget + "\n" + buffer(index - 1) + "^";
    }
  }

  private static String buffer(int i) {
    StringBuilder sb = new StringBuilder();
    for (int k = 0; k < i; k++) {
      sb.append(' ');
    }
    return sb.toString();
  }
}
