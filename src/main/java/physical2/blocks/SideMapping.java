package physical2.blocks;

import circuit.Pair;
import physical.things.BlockConstant;
import physical2.two.Point2D;

import java.util.*;

import static physical2.two.Side.*;

/**
 * from 0,0 to 4,4, approximately
 */

public class SideMapping {
  public static int X_SCALE = 5;
  public static int Y_SCALE = 5;

  private static Map<SquareSpecifier, Map<Point2D, BlockConstant>> singleMappings = defaultSingleMappings();
  private static Map<Pair<SquareSpecifier, SquareSpecifier>, Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>>> doubleMappings = defaultDoubleMappings();

  private static Map<SquareSpecifier, Map<Point2D, BlockConstant>> defaultSingleMappings() {
    Map<SquareSpecifier, Map<Point2D, BlockConstant>> r = new HashMap<>();
    r.put(new SquareSpecifier(LEFT, RIGHT, false), parseStringMap(new String[]{
        ".....",
        "sssss",
        "",
        "!"}
    ));
    r.put(new SquareSpecifier(LEFT, RIGHT, true), parseStringMap(new String[]{
        "..>..",
        "sssss",
        "",
        "!"}
    ));
    r.put(new SquareSpecifier(RIGHT, LEFT, false), parseStringMap(new String[]{
        ".....",
        "sssss",
        "",
        "!"}
    ));
    r.put(new SquareSpecifier(LEFT, RIGHT, true), parseStringMap(new String[]{
        "..<..",
        "sssss",
        "",
        "!"}
    ));
    r.put(new SquareSpecifier(LEFT, UP, false), parseStringMap(new String[]{
        " ~.s",
        "~.s",
        ".s",
        "s",
        "",
        "!"}
    ));
    r.put(new SquareSpecifier(UP, LEFT, false), parseStringMap(new String[]{
        " ~.s",
        "~.s",
        ".s",
        "s",
        "",
        "!"}
    ));
    r.put(new SquareSpecifier(RIGHT, UP, false), parseStringMap(new String[]{
        "  .T",
        "  s..~",
        "   ss.",
        "     s",
        "",
        "!"}
    ));
    r.put(new SquareSpecifier(RIGHT, UP, true), parseStringMap(new String[]{
        "  .T",
        "  s<.~",
        "   ss.",
        "     s",
        "",
        "!"}
    ));


    r.put(new SquareSpecifier(DOWN, LEFT, false), parseStringMap(new String[]{
        ".~",
        "s.~",
        " s.~",
        "!  T."}
    ));
    r.put(new SquareSpecifier(LEFT, DOWN, false), parseStringMap(new String[]{
        "...p",
        "sssr",
        "",
        "!  ~."}
    ));

    r.put(new SquareSpecifier(DOWN, RIGHT, false), parseStringMap(new String[]{
        "   ~..",
        "  ~.ss",
        "  .T",
        "!  T."}
    ));
    r.put(new SquareSpecifier(RIGHT, DOWN, false), parseStringMap(new String[]{
        "   p..",
        "   rss",
        "",
        "~  ~."}
    ));

    return r;
  }

  private static Map<Pair<SquareSpecifier, SquareSpecifier>, Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>>> defaultDoubleMappings() {
    Map<Pair<SquareSpecifier, SquareSpecifier>, Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>>> r = new HashMap<>();
    r.put(new Pair<>(new SquareSpecifier(LEFT, DOWN, false), new SquareSpecifier(DOWN, RIGHT, false)), new Pair<>(parseStringMap(new String[]{
        ".~",
        "s.~",
        "! s."
    }), parseStringMap(new String[]{
        "  s.~",
        "   s.~",
        "    s.",
        "     s",
        "!"
    })));

    r.put(new Pair<>(new SquareSpecifier(RIGHT, DOWN, false), new SquareSpecifier(DOWN, LEFT, false)), new Pair<>(parseStringMap(new String[]{
        "    ~.",
        "   ~.s",
        "!  ~.s"
    }), parseStringMap(new String[]{
        " ~.s",
        "~.s",
        ".s",
        "s",
        "!"
    })));


    return r;
  }

  public static boolean hasSingle(SquareSpecifier spec) {
    return singleMappings.containsKey(spec);
  }

  public static boolean hasDouble(Pair<SquareSpecifier, SquareSpecifier> specPair) {
    return doubleMappings.containsKey(specPair);
  }

  public static Map<Point2D, BlockConstant> getSingle(SquareSpecifier spec) {
    if (!singleMappings.containsKey(spec)) {
      throw new IllegalArgumentException("Illegal SquareSpecifier " + spec.toString());
    }
    return singleMappings.get(spec);
  }

  public static Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>> getDouble(Pair<SquareSpecifier, SquareSpecifier> specPair) {
    if (!doubleMappings.containsKey(specPair)) {
      throw new IllegalArgumentException("Illegal SquareSpecifier pair " + specPair.toString());
    }
    return doubleMappings.get(specPair);
  }

  private static BlockConstant charToBlockConstant(char c) {
    switch (c) {
      case '.':
        return BlockConstant.REDSTONE;
      case '*':
        return BlockConstant.REDSTONE_TORCH;
      case 's':
        return BlockConstant.REDSTONE_BASE;
      case 'S':
        return BlockConstant.REDSTONE_TORCH_BASE;
      case '~':
        return BlockConstant.AIR;
      case ' ':
        return BlockConstant.EMPTY;
      case 'T':
        return BlockConstant.TOP_SLAB;
      case '<':
        return BlockConstant.REPEATER_X_;
      case '>':
        return BlockConstant.REPEATER_X;
      default:
        throw new IllegalArgumentException("Not a valid block character: '" + c + "'");
    }
  }

  private static Map<Point2D, BlockConstant> parseStringMap(String s) {
    return parseStringMap(s.split("\n"));
  }

  /**
   * {@code '!'} marks the center.
   */
  private static Map<Point2D, BlockConstant> parseStringMap(String[] lines) {
    int centerX = -1, centerY = -1;
    flower:
    for (int y = 0; y < lines.length; y++) {
      for (int x = 0; x < lines[y].length(); x++) {
        if (lines[y].charAt(x) == '!') {
          centerX = x;
          centerY = y;
          break flower;
        }
      }
    }

    if (centerX == -1) {
      throw new IllegalArgumentException("No center mark");
    }

    Map<Point2D, BlockConstant> r = new HashMap<>();

    for (int y = 0; y < lines.length; y++) {
      for (int xRaw = 0; xRaw < lines[y].length(); xRaw++) {
        int x;
        if (y == centerY) {
          if (xRaw > centerX) {
            x = xRaw - 1;
          } else if (xRaw == centerX) {
            continue;
          } else {
            x = xRaw;
          }
        } else {
          x = xRaw;
        }

        // Note that we iterate over it upside down, hence negating the y.
        BlockConstant bc = charToBlockConstant(lines[y].charAt(xRaw));
        if (bc != BlockConstant.EMPTY) {
          r.put(new Point2D(x - centerX, centerY - y), bc);
        }
      }
    }

    return r;
  }

  public static Iterator<Map<Point2D, BlockConstant>> iterateOverPath(Iterator<SquareSpecifier> iter) {
    return new SquarePathIterator(iter);
  }

  private static class SquarePathIterator implements Iterator<Map<Point2D, BlockConstant>> {
    private Iterator<SquareSpecifier> iter;
    private Deque<SquareSpecifier> interpretationQueue;
    private Map<Point2D, BlockConstant> next;

    public SquarePathIterator(Iterator<SquareSpecifier> iter) {
      this.iter = iter;
      interpretationQueue = new LinkedList<>();
      next = null;
    }

    private void ensure(int i) {
      while (i >= interpretationQueue.size() && iter.hasNext()) {
        interpretationQueue.add(iter.next());
      }
    }

    private boolean has(int i) {
      ensure(i);
      return i < interpretationQueue.size();
    }

    private SquareSpecifier get(int i) {
      ensure(i);
      if (i < interpretationQueue.size()) {
        Iterator<SquareSpecifier> localIter = interpretationQueue.iterator();
        while (i > 0) {
          localIter.next();
          i--;
        }
        return localIter.next();
      }
      throw new IllegalStateException("i too deep");
    }

    @Override
    public boolean hasNext() {
      return next != null || has(0);
    }

    @Override
    public Map<Point2D, BlockConstant> next() {
      if (next != null) {
        Map<Point2D, BlockConstant> r = next;
        next = null;
        return r;
      }
      SquareSpecifier s0 = get(0);
      if (has(1)) {
        // Attempt to get a pairing item
        SquareSpecifier s1 = get(1);
        Pair<SquareSpecifier, SquareSpecifier> pair = new Pair<>(s0, s1);

        Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>> r = doubleMappings.get(pair);
        if (r != null) {
          next = r.getSecond();
          return r.getFirst();
        }
        // Give up on pairing items
      }

      return singleMappings.get(s0);
    }
  }
}
