package physical2.blocks;

import physical2.one.BiSide;
import physical2.two.Side;

import java.util.Objects;

public class SquareSpecifier {
  private Side s1, s2;
  private boolean repeater;
  private BiSide start, end;

  /**
   * {@code start} and {@code end} are generally encouraged to be {@code null} in the middle of a path.
   */
  public SquareSpecifier(Side s1, Side s2, BiSide start, BiSide end, boolean repeater) {
    this.s1 = s1;
    this.s2 = s2;
    this.repeater = repeater;
    this.start = start;
    this.end = end;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SquareSpecifier) {
      return s1 == ((SquareSpecifier) obj).s1
          && s2 == ((SquareSpecifier) obj).s2
          && repeater == ((SquareSpecifier) obj).repeater
          && start == ((SquareSpecifier) obj).start
          && end == ((SquareSpecifier) obj).end;
    }
    return false;
  }

  public Side getSide1() {
    return s1;
  }

  public Side getSide2() {
    return s2;
  }

  public BiSide getBiSideStart() {
    return start;
  }

  public BiSide getBiSideEnd() {
    return end;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(s1) + 11 * Objects.hashCode(s2) + Objects.hashCode(start) * 113 + Objects.hashCode(end) * 1009 + (repeater ? 7919 : 0);
  }

  @Override
  public String toString() {
    return "(" + s1 + " -> " + s2 + " (start:" + start + ",end:" + end + "), repeater: " + repeater + ")";
  }
}
