package physical2.blocks;

import circuit.Pair;
import physical.things.BlockConstant;
import physical2.one.BiSide;
import physical2.two.Point2D;

import java.util.*;

import static physical2.two.Side.*;

/**
 * from 0,0 to 4,4, approximately
 */

public class SideMapping {
  public static final int X_SCALE = 5;
  public static final int Y_SCALE = 5;

  /**
   * The amount of buffer needed at the top of the map to allow for redstone above the top mapped center points.
   * 0 Indicates only including up through the wire
   */
  public static final int Y_BUFFER = 3;
  public static final int X_BUFFER = 6;
//  public static final int

  private static Map<SquareSpecifier, Map<Point2D, BlockConstant>> singleMappings = defaultSingleMappings();
  private static Map<Pair<SquareSpecifier, SquareSpecifier>, Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>>> doubleMappings = defaultDoubleMappings();

  private static Map<SquareSpecifier, Map<Point2D, BlockConstant>> defaultSingleMappings() {
    Map<SquareSpecifier, Map<Point2D, BlockConstant>> r = new HashMap<>();
    r.put(new SquareSpecifier(null, LEFT, BiSide.RIGHT, null, false), parseStringMap(new String[]{
        "...*",
        "ssss.",
        "!    s.",
        "     s"}
    ));
    r.put(new SquareSpecifier(null, RIGHT, BiSide.LEFT, null, false), parseStringMap(new String[]{
        "  *...",
        " .ssss",
        "!.s",
        "s"}
    ));
    r.put(new SquareSpecifier(LEFT, RIGHT, null, null, false), parseStringMap(new String[]{
        ".....",
        "sssss",
        "!"}
    ));
    r.put(new SquareSpecifier(LEFT, RIGHT, null, null, true), parseStringMap(new String[]{
        "..>..",
        "sssss",
        "!"}
    ));
    r.put(new SquareSpecifier(RIGHT, LEFT, null, null, false), parseStringMap(new String[]{
        ".....",
        "sssss",
        "!"}
    ));
    r.put(new SquareSpecifier(RIGHT, LEFT, null, null, true), parseStringMap(new String[]{
        "..<..",
        "sssss",
        "!"}
    ));
    r.put(new SquareSpecifier(LEFT, UP, null, null, false), parseStringMap(new String[]{
        "   .",
        "~..s",
        ".ss",
        "s",
        "!"}
    ));
    r.put(new SquareSpecifier(LEFT, UP, null, null, true), parseStringMap(new String[]{
        "   .",
        "  .s",
        ".>s",
        "ss",
        "!"}
    ));
    r.put(new SquareSpecifier(UP, LEFT, null, null, false), parseStringMap(new String[]{
        " ~.s",
        "~.s",
        ".s",
        "s",
        "!"}
    ));
    r.put(new SquareSpecifier(RIGHT, UP, null, null, false), parseStringMap(new String[]{
        "   .",
        "  .T",
        "  s...",
        "   sss",
        "!"}
    ));
    r.put(new SquareSpecifier(RIGHT, UP, null, null, true), parseStringMap(new String[]{
        "   .",
        "  .s ~",
        "  s<..",
        "   sss",
        "!"}
    ));


    r.put(new SquareSpecifier(DOWN, LEFT, null, null, false), parseStringMap(new String[]{
        ".",
        "s.~",
        "! s.~",
        "  T."}
    ));
    r.put(new SquareSpecifier(LEFT, DOWN, null, null, false), parseStringMap(new String[]{
        "...p",
        "sssr",
        "!   ~",
        "   ."}
    ));
    r.put(new SquareSpecifier(LEFT, DOWN, null, null, true), parseStringMap(new String[]{
        "...p",
        "sssr",
        "!   ~",
        "   ."}
    ));

    r.put(new SquareSpecifier(DOWN, RIGHT, null, null, false), parseStringMap(new String[]{
        "    ~.",
        "  ~..s",
        "!  .Ts",
        "  T."}
    ));
    r.put(new SquareSpecifier(RIGHT, DOWN, null, null, false), parseStringMap(new String[]{
        "!   ~..",
        "  ~.ss"}
    ));
    r.put(new SquareSpecifier(RIGHT, DOWN, null, null, true), parseStringMap(new String[]{
        "   p..",
        "   rss",
        "!   ~",
        "  ~."}
    ));


    r.put(new SquareSpecifier(null, DOWN, BiSide.LEFT, null, false), parseStringMap(new String[]{
        "!..s}",
        "ss ."}
    ));
    r.put(new SquareSpecifier(null, DOWN, BiSide.RIGHT, null, false), parseStringMap(new String[]{
        "    .",
        "!   {s.",
        "   . s"}
    ));
    r.put(new SquareSpecifier(UP, DOWN, null, null, false), parseStringMap(new String[]{
        "  ~.",
        " ~.s",
        " .s ",
        " sp ",
        "!  r ",
        "   ."}
    ));
    r.put(new SquareSpecifier(UP, DOWN, null, null, true), parseStringMap(new String[]{
        "  ~.",
        " ~.s",
        " .s ",
        " sp ",
        "!  r ",
        "   ."}
    ));
    r.put(new SquareSpecifier(null, UP, BiSide.LEFT, null, false), parseStringMap(new String[]{
        "   .",
        "   s",
        "   *",
        "  .s",
        "!..s",
        "ss"}
    ));
    r.put(new SquareSpecifier(null, UP, BiSide.RIGHT, null, false), parseStringMap(new String[]{
        "   .",
        "   s",
        "   *",
        "   s.",
        "!    s.",
        "     s"}
    ));


    r.put(new SquareSpecifier(null, RIGHT, BiSide.RIGHT, null, false), parseStringMap(new String[]{
        "   s..",
        "   *ss",
        "!   s..",
        "    ss"}
    ));

    r.put(new SquareSpecifier(null, LEFT, BiSide.LEFT, null, false), parseStringMap(new String[]{
        "..s",
        "ss*",
        "!..s",
        "ss"}
    ));

    r.put(new SquareSpecifier(null, LEFT, BiSide.RIGHT, null, false), parseStringMap(new String[]{
        "..*",
        "sss.",
        "!   s..",
        "    ss"}
    ));

    r.put(new SquareSpecifier(null, RIGHT, BiSide.LEFT, null, false), parseStringMap(new String[]{
        "   *..",
        "  .sss",
        "!..s",
        "ss"}
    ));

    r.put(new SquareSpecifier(LEFT, null, null, BiSide.RIGHT, false), parseStringMap(new String[]{
        "...",
        "sss.",
        "!   s..",
        "    ss"}
    ));
    r.put(new SquareSpecifier(LEFT, null, null, BiSide.RIGHT, true), parseStringMap(new String[]{
        ".>.",
        "sss.",
        "!   s..",
        "    ss"}
    ));


    r.put(new SquareSpecifier(LEFT, null, null, BiSide.LEFT, false), parseStringMap(new String[]{
        "..p",
        "ssr",
        "!..~",
        "ss"}
    ));
    r.put(new SquareSpecifier(LEFT, null, null, BiSide.LEFT, true), parseStringMap(new String[]{
        "..p",
        "ssr",
        "!..~",
        "ss"}
    ));


    r.put(new SquareSpecifier(RIGHT, null, null, BiSide.RIGHT, false), parseStringMap(new String[]{
        "   p..",
        "   rss",
        "!   ~..",
        "    ss"}
    ));
    r.put(new SquareSpecifier(RIGHT, null, null, BiSide.RIGHT, true), parseStringMap(new String[]{
        "   p..",
        "   rss",
        "!   ~..",
        "    ss"}
    ));

    r.put(new SquareSpecifier(RIGHT, null, null, BiSide.LEFT, false), parseStringMap(new String[]{
        "   ...",
        "  .sss",
        "!..s",
        "ss"}
    ));
    r.put(new SquareSpecifier(RIGHT, null, null, BiSide.LEFT, true), parseStringMap(new String[]{
        "   .<.",
        "  .sss",
        "!..s",
        "ss"}
    ));

    r.put(new SquareSpecifier(DOWN, UP, null, null, false), parseStringMap(new String[]{
        "   .  ",
        "   T. ",
        "    T.",
        "   ..s",
        "!  .Ts",
        "  T.  "}
    ));
    r.put(new SquareSpecifier(DOWN, UP, null, null, true), parseStringMap(new String[]{
        "   .  ",
        "   T. ",
        "  .>s ",
        "s.Ts  ",
        "! s.   ",
        "  T.  "}
    ));

    r.put(new SquareSpecifier(DOWN, LEFT, null, null, false), parseStringMap(new String[]{
        "",
        "",
        ".",
        "s.",
        "! s.",
        "  T."}
    ));
    r.put(new SquareSpecifier(DOWN, LEFT, null, null, true), parseStringMap(new String[]{
        "",
        "",
        ".<.",
        "ssT.",
        "!  .T",
        "  T."}
    ));


    r.put(new SquareSpecifier(DOWN, RIGHT, null, null, false), parseStringMap(new String[]{
        "    ..",
        "   .ss",
        "!  .T  ",
        "  T.  "}
    ));
    r.put(new SquareSpecifier(DOWN, RIGHT, null, null, true), parseStringMap(new String[]{
        " s}.  ",
        "~* s. ",
        " s.~s.",
        "  T. s",
        "!  .T  ",
        "  T.  "}
    ));

    r.put(new SquareSpecifier(DOWN, null, null, BiSide.LEFT, false), parseStringMap(new String[]{
        "!...",
        "ssT."}
    ));
    r.put(new SquareSpecifier(DOWN, null, null, BiSide.LEFT, true), parseStringMap(new String[]{
        "!.<.",
        "ssT."}
    ));


    r.put(new SquareSpecifier(DOWN, null, null, BiSide.RIGHT, false), parseStringMap(new String[]{
        "!   ~..",
        "   .Ts"}
    ));
    r.put(new SquareSpecifier(DOWN, null, null, BiSide.RIGHT, true), parseStringMap(new String[]{
        "   .>s",
        "!  .Ts",
        "  T. "}
    ));

    r.put(new SquareSpecifier(UP, null, null, BiSide.LEFT, false), parseStringMap(new String[]{
        "  ~.",
        "  .s",
        "  sp",
        "   r",
        "!...",
        "sss"}
    ));
    r.put(new SquareSpecifier(UP, null, null, BiSide.LEFT, true), parseStringMap(new String[]{
        "  ~.",
        "  .s",
        "  sp ",
        "   r",
        "!...",
        "sss"}
    ));

    r.put(new SquareSpecifier(UP, null, null, BiSide.RIGHT, false), parseStringMap(new String[]{
        "  ~.",
        "  .s",
        "  sp",
        "   r",
        "!    ..",
        "    ss"}
    ));
    r.put(new SquareSpecifier(UP, null, null, BiSide.RIGHT, true), parseStringMap(new String[]{
        "  ~.",
        "  .s",
        "  sp ",
        "   r",
        "!    ..",
        "    ss"}
    ));


    r.put(new SquareSpecifier(UP, RIGHT, null, null, false), parseStringMap(new String[]{
        "  ~.  ",
        " p.s  ",
        " rs~..",
        "  ..ss",
        "!  ss  "}
    ));
    r.put(new SquareSpecifier(UP, RIGHT, null, null, true), parseStringMap(new String[]{
        "  ~.  ",
        " p.s  ",
        " rs~..",
        "  ..ss",
        "!  ss  "}
    ));


    r.put(new SquareSpecifier(UP, LEFT, null, null, false), parseStringMap(new String[]{
        "  ~.",
        "~..s",
        ".ss",
        "s",
        "!"}
    ));
    r.put(new SquareSpecifier(UP, LEFT, null, null, true), parseStringMap(new String[]{
        "   .",
        "  .s",
        ".~sp",
        "s.~r",
        "! s.",
        "  s"}
    ));

    return r;
  }

  private static Map<Pair<SquareSpecifier, SquareSpecifier>, Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>>> defaultDoubleMappings() {
    Map<Pair<SquareSpecifier, SquareSpecifier>, Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>>> r = new HashMap<>();


    Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>> diag1 = new Pair<>(parseStringMap(new String[]{
        ".~",
        "s.~",
        "! s."
    }), parseStringMap(new String[]{
        "  s.~",
        "   s.~",
        "    s.",
        "     s",
        "!"
    }));
    r.put(new Pair<>(
        new SquareSpecifier(LEFT, DOWN, null, null, false),
        new SquareSpecifier(UP, RIGHT, null, null, false)
    ), diag1);
    r.put(new Pair<>(
        new SquareSpecifier(RIGHT, UP, null, null, false),
        new SquareSpecifier(DOWN, LEFT, null, null, false)
    ), diag1.swap());


    Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>> diag2 = new Pair<>(parseStringMap(new String[]{
        "    ~.",
        "   ~.s",
        "!  ~.s"
    }), parseStringMap(new String[]{
        " ~.s",
        "~.s",
        ".s",
        "s",
        "!"
    }));
    r.put(new Pair<>(
        new SquareSpecifier(RIGHT, DOWN, null, null, false),
        new SquareSpecifier(UP, LEFT, null, null, false)
    ), diag2);
    r.put(new Pair<>(
        new SquareSpecifier(LEFT, UP, null, null, false),
        new SquareSpecifier(DOWN, RIGHT, null, null, false)
    ), diag2.swap());


    r.put(new Pair<>(
            new SquareSpecifier(null, DOWN, BiSide.RIGHT, null, false),
            new SquareSpecifier(UP, LEFT, null, null, false)
        ),
        new Pair<>(parseStringMap(new String[]{
            "! ~...."
        }), parseStringMap(new String[]{
            " {ssss",
            "~.",
            ".s",
            "s",
            "!"
        }))
    );

    r.put(new Pair<>(
            new SquareSpecifier(null, DOWN, BiSide.LEFT, null, false),
            new SquareSpecifier(UP, RIGHT, null, null, false)
        ),
        new Pair<>(parseStringMap(new String[]{
            "!....~"
        }), parseStringMap(new String[]{
            "ssss}",
            "    .",
            "    s.",
            "     s",
            "!"
        }))
    );


    r.put(new Pair<>(
            new SquareSpecifier(LEFT, UP, null, null, false),
            new SquareSpecifier(DOWN, null, null, BiSide.RIGHT, false)
        ),
        new Pair<>(parseStringMap(new String[]{
            "!  ~...",
        }), parseStringMap(new String[]{
            " ~.sss",
            "~.s",
            ".s",
            "s",
            "!"
        })).swap()
    );
    r.put(new Pair<>(
            new SquareSpecifier(RIGHT, UP, null, null, false),
            new SquareSpecifier(DOWN, null, null, BiSide.LEFT, false)
        ),
        new Pair<>(parseStringMap(new String[]{
            "!...~",
        }), parseStringMap(new String[]{
            "sss.~",
            "   s.~",
            "    s.",
            "     s",
            "!"
        })).swap()
    );

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
      case 'p':
        return BlockConstant.DOWN_PISTON;
      case 'r':
        return BlockConstant.REDSTONE_BLOCK;
      case '{':
        return BlockConstant.REDSTONE_WALL_TORCH_RIGHT;
      case '}':
        return BlockConstant.REDSTONE_WALL_TORCH_LEFT;
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
          interpretationQueue.remove();
          interpretationQueue.remove();
          return r.getFirst();
        }
        // Give up on pairing items
        Map<Point2D, BlockConstant> r0 = singleMappings.get(s0);
        if (r0 == null) {
          throw new IllegalStateException("Unrecognized SquareSpecifier sequence: " + s0 + ", " + s1);
        }
        interpretationQueue.remove();
        return r0;
      } else {
        Map<Point2D, BlockConstant> r0 = singleMappings.get(s0);
        if (r0 == null) {
          throw new IllegalStateException("Unrecognized SquareSpecifier sequence: " + s0 + " [End of Sequence]");
        }
        interpretationQueue.remove();
        return r0;
      }
    }
  }
}
