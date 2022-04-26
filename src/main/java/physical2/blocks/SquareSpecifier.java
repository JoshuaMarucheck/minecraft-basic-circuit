package physical2.blocks;

import physical2.two.Side;

public class SquareSpecifier {
  private Side s1, s2;
  private boolean repeater;

  public SquareSpecifier(Side s1, Side s2, boolean repeater) {
    this.s1 = s1;
    this.s2 = s2;
    this.repeater = repeater;
  }

  public boolean isRepeater() {
    return repeater;
  }

  public Side getSide1() {
    return s1;
  }

  public Side getSide2() {
    return s2;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SquareSpecifier) {
      return s1 == ((SquareSpecifier) obj).s1 && s2 == ((SquareSpecifier) obj).s2 && repeater == ((SquareSpecifier) obj).repeater;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return s1.hashCode() + 10 * s2.hashCode() + (repeater ? 37 : 0);
  }

  @Override
  public String toString() {
    return "(" + s1 + " -> " + s2 + ", repeater: " + repeater + ")";
  }
}
