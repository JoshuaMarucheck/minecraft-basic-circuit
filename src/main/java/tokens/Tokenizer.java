package tokens;

import java.util.Iterator;

public class Tokenizer {
  private char[] specialChars = new char[]{'(', ')'};
  private char[] wideSeparatorChars = new char[]{' ', '\r', '\n'};

  public void setSpecialChars(char[] specialChars) {
    this.specialChars = specialChars;
  }

  public void setWideSeparatorChars(char[] wideSeparatorChars) {
    this.wideSeparatorChars = wideSeparatorChars;
  }

  public Iterator<String> tokenize(String s) {
    return new TokenIterator(s);
  }

  private static boolean contains(char[] chars, char c) {
    for (char d : chars) {
      if (c == d) {
        return true;
      }
    }
    return false;
  }

  private class TokenIterator implements Iterator<String> {
    private int index;
    private String str;

    TokenIterator(String s) {
      this.str = s;
      index = 0;
      prepNext();
    }

    private void prepNext() {
      while (index < str.length() && contains(wideSeparatorChars, str.charAt(index))) {
        index++;
      }
    }

    public boolean hasNext() {
      return index < str.length();
    }

    public String next() {
      int start = index;
      if (contains(specialChars, str.charAt(index))) {
        index++;
        prepNext();
        return Character.toString(str.charAt(start));
      }

      while (index < str.length() && !contains(wideSeparatorChars, str.charAt(index)) && !contains(specialChars, str.charAt(index))) {
        index++;
      }

      String r = str.substring(start, index);
      prepNext();
      return r;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
