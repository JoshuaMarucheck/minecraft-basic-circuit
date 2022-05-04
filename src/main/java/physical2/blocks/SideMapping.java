package physical2.blocks;

import circuit.Pair;
import physical.things.BlockConstant;
import physical2.one.BiSide;
import physical2.two.Point2D;
import physical2.two.Side;

import java.util.*;
import java.util.function.Function;

import static physical.things.BlockConstant.*;
import static physical2.two.Side.*;

/**
 * from 0,0 to 4,4, approximately
 */

public class SideMapping {
  public static final int X_SCALE = 5;
  public static final int Y_SCALE = 5;

  public static void main(String[] args) {
  }

  /**
   * The amount of buffer needed at the top of the map to allow for redstone above the top mapped center points.
   * 0 Indicates only including up through the wire
   */
  public static final int Y_BUFFER = 3;
  public static final int X_BUFFER = 6;
//  public static final int

  private static Map<SquareSpecifier, Map<Point2D, BlockConstant>> singleMappings = defaultSingleMappings();
  private static Map<Pair<SquareSpecifier, SquareSpecifier>, Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>>> doubleMappings = defaultDoubleMappings();

  static {
    for (SquareSpecifier spec : singleMappings.keySet()) {
      if (!testSingle(spec, singleMappings.get(spec))) {
        System.out.println(visualizePointSet(getPoweredSet(spec, singleMappings.get(spec))));
        throw new IllegalStateException("Invalid single mapping " + spec);
//        System.err.println("Invalid single mapping " + spec);
      }
    }
  }

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
        "......",
        "ssssss",
        "!"}
    ));
    r.put(new SquareSpecifier(LEFT, RIGHT, null, null, true), parseStringMap(new String[]{
        "..>...",
        "ssssss",
        "!"}
    ));
    r.put(new SquareSpecifier(RIGHT, LEFT, null, null, false), parseStringMap(new String[]{
        "......",
        "ssssss",
        "!"}
    ));
    r.put(new SquareSpecifier(RIGHT, LEFT, null, null, true), parseStringMap(new String[]{
        "..<...",
        "ssssss",
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

    r.put(new SquareSpecifier(RIGHT, DOWN, null, null, false), parseStringMap(new String[]{
        "   p..",
        "   rss",
        "!   ",
        "  ~."}
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
        "!  .Ts.",
        "  T. s"}
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
        return AIR;
      case ' ':
        return EMPTY;
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
        if (bc != EMPTY) {
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

  /*
   *
   * Testing!
   *
   */

  private static String visualizePointSet(Set<Point2D> items) {
    if (items == null) {
      return "(null set)";
    }
    if (items.isEmpty()) {
      return "";
    }
    int minX, minY;
    {
      Point2D p = items.iterator().next();
      minX = p.getX();
      minY = p.getY();
    }

    for (Point2D p : items) {
      minX = Math.min(p.getX(), minX);
      minY = Math.min(p.getY(), minY);
    }

    int finalMinX = minX;
    int finalMinY = minY;
    Function<Point2D, Point2D> offset = p -> new Point2D(p.getX() - finalMinX, p.getY() - finalMinY);

    ArrayList<ArrayList<Character>> strAssembler = new ArrayList<>();

    for (Point2D point : items) {
      Point2D p = offset.apply(point);

      while (p.getY() >= strAssembler.size()) {
        strAssembler.add(new ArrayList<>());
      }
      ArrayList<Character> row = strAssembler.get(p.getY());
      while (p.getX() >= row.size()) {
        row.add(' ');
      }

      row.set(p.getX(), '*');
    }

    StringBuilder sb = new StringBuilder();
    for (int y = strAssembler.size() - 1; y >= 0; y--) {
      ArrayList<Character> row = strAssembler.get(y);
      for (Character c : row) {
        sb.append(c);
      }
      sb.append('\n');
    }
    sb.delete(sb.length() - 1, sb.length());
    return sb.toString();
  }

  /**
   * @return The position of that input or output, or {@code null} on an invalid combination of values.
   */
  private static Point2D getRedstonePosition(Side side, BiSide edge) {
    if ((side == null) == (edge == null)) {
      return null;
    }

    if (side == null) {
      switch (edge) {
        case LEFT:
          return new Point2D(0, 0);
        case RIGHT:
          return new Point2D(X_SCALE, 0);
      }
    }
    switch (side) {
      case UP:
        return new Point2D(3, Y_SCALE - 1);
      case DOWN:
        return new Point2D(3, -1);
      case LEFT:
        return new Point2D(0, 2);
      case RIGHT:
        return new Point2D(X_SCALE, 2);
    }

    throw new IllegalStateException("bug");
  }

  private static boolean testSingle(SquareSpecifier spec, Map<Point2D, BlockConstant> map) {
    Point2D output = getRedstonePosition(spec.getSide2(), spec.getBiSideEnd());
    Set<Point2D> powered = getPoweredSet(spec, map);
    if (powered == null) {
      return false;
    }
    return powered.contains(output);
  }

  /**
   * @return The set containing all powered points in this circuit square, or {@code null} on an error
   */
  private static Set<Point2D> getPoweredSet(SquareSpecifier spec, Map<Point2D, BlockConstant> map) {
    Map<Point2D, BlockConstant> corners = new HashMap<>();
    for (BiSide side : BiSide.values()) {
      Point2D p = getRedstonePosition(null, side);
      if (p == null) {
        throw new IllegalStateException("bug");
      }
      corners.put(p, BlockConstant.REDSTONE);
      corners.put(p.translate(0, -1), BlockConstant.REDSTONE_BASE);

      corners.put(p.translate(0, Y_SCALE), BlockConstant.REDSTONE);
      corners.put(p.translate(0, Y_SCALE - 1), BlockConstant.REDSTONE_BASE);
    }

    for (Point2D p : corners.keySet()) {
      if (map.containsKey(p) && map.get(p) != corners.get(p)) {
        return null;
      }
    }

    Point2D input = getRedstonePosition(spec.getSide1(), spec.getBiSideStart());
    Point2D output = getRedstonePosition(spec.getSide2(), spec.getBiSideEnd());

    if (input == null || output == null) {
      throw new IllegalArgumentException("Invalid SquareSpecifier " + spec);
    }

    Set<Point2D> poweredBlocks = new HashSet<>();
    Stack<Point2D> powerStack = new Stack<>();

    class BlockChecker {
      public void pushIfBlock(BlockConstant bc, Point2D p) {
        if (map.get(p) == bc) {
          powerStack.push(p);
        }
      }

      public void pushIfPowerable(Point2D p) {
        BlockConstant bc = map.get(p);
        if (bc == DOWN_PISTON || bc == REDSTONE || bc == REDSTONE_BASE || bc == REDSTONE_TORCH_BASE) {
          powerStack.push(p);
        }
      }

      public void stronglyPower(Point2D p) {
        pushIfPowerable(p);
        BlockConstant bc = map.get(p);
        if (bc == REDSTONE_BASE || bc == REDSTONE_TORCH_BASE) {
          powerStack.push(p);
          BlockConstant right = map.get(p.translate(1, 0));
          if (right == REPEATER_X || right == REDSTONE || right == DOWN_PISTON) {
            powerStack.push(p.translate(1, 0));
          }
          BlockConstant left = map.get(p.translate(-1, 0));
          if (left == REPEATER_X_ || left == REDSTONE || left == DOWN_PISTON) {
            powerStack.push(p.translate(-1, 0));
          }
          BlockConstant down = map.get(p.translate(0, -1));
          if (down == REDSTONE || down == DOWN_PISTON) {
            powerStack.push(p.translate(0, -1));
          }
          BlockConstant up = map.get(p.translate(0, 1));
          if (up == REDSTONE) {
            powerStack.push(p.translate(0, 1));
          }
        }
      }
    }
    BlockChecker blockChecker = new BlockChecker();
    blockChecker.pushIfBlock(REDSTONE, input);

    while (!powerStack.isEmpty()) {
      Point2D p = powerStack.pop();
      if (!map.containsKey(p)) {
        continue;
      }
      if (poweredBlocks.contains(p)) {
        continue;
      }
      poweredBlocks.add(p);


      switch (map.get(p)) {
        case REDSTONE: {
          BlockConstant above = map.get(p.translate(0, 1));
          if (above == null || above == EMPTY || above == AIR || above == BlockConstant.TOP_SLAB) {
            // signal flows up diagonally
            blockChecker.pushIfBlock(REDSTONE, p.translate(1, 1));
            blockChecker.pushIfBlock(REDSTONE, p.translate(-1, 1));
          }

          blockChecker.pushIfBlock(REPEATER_X_, p.translate(-1, 0));
          blockChecker.pushIfBlock(REPEATER_X, p.translate(1, 0));
          blockChecker.pushIfPowerable(p.translate(-1, 0));
          blockChecker.pushIfPowerable(p.translate(1, 0));
          blockChecker.pushIfPowerable(p.translate(0, -1));

          BlockConstant right = map.get(p.translate(1, 0));
          if (right == null || right == EMPTY || right == AIR) {
            blockChecker.pushIfBlock(REDSTONE, p.translate(1, -1));
          }

          BlockConstant left = map.get(p.translate(-1, 0));
          if (left == null || left == EMPTY || left == AIR) {
            blockChecker.pushIfBlock(REDSTONE, p.translate(-1, -1));
          }

          break;
        }
        case REDSTONE_BASE:
        case REDSTONE_TORCH_BASE: {
          blockChecker.pushIfBlock(REDSTONE_TORCH, p.translate(0, 1));
          blockChecker.pushIfBlock(REDSTONE_WALL_TORCH_LEFT, p.translate(1, 0));
          blockChecker.pushIfBlock(REDSTONE_WALL_TORCH_RIGHT, p.translate(-1, 0));

          blockChecker.pushIfBlock(DOWN_PISTON, p.translate(0, -1));
          break;
        }

        case REDSTONE_TORCH:
        case REDSTONE_WALL_TORCH_LEFT:
        case REDSTONE_WALL_TORCH_RIGHT: {
          blockChecker.pushIfBlock(REDSTONE, p.translate(1, 0));
          blockChecker.pushIfBlock(REDSTONE, p.translate(-1, 0));
          blockChecker.stronglyPower(p.translate(0, 1));
          blockChecker.pushIfBlock(REDSTONE, p.translate(0, -1));

          blockChecker.pushIfBlock(REPEATER_X_, p.translate(-1, 0));
          blockChecker.pushIfBlock(REPEATER_X, p.translate(1, 0));
          break;
        }

        case DOWN_PISTON: {
          if (map.get(p.translate(0, -1)) != REDSTONE_BLOCK) {
            System.err.println("Error: block below DOWN_PISTON is not REDSTONE_BLOCK");
            return poweredBlocks;
          }
          BlockConstant twoDown = map.get(p.translate(0, -2));
          if (!(twoDown == null || twoDown == EMPTY || twoDown == AIR)) {
            System.err.println("Error: block below REDSTONE_BLOCK is not empty space");
            return poweredBlocks;
          }

          blockChecker.pushIfBlock(REDSTONE, p.translate(0, -3));
          blockChecker.pushIfBlock(REDSTONE, p.translate(1, -2));
          blockChecker.pushIfBlock(REDSTONE, p.translate(-1, -2));

          blockChecker.pushIfBlock(REPEATER_X_, p.translate(-1, -2));
          blockChecker.pushIfBlock(REPEATER_X, p.translate(1, -2));
          break;
        }

        case REPEATER_X_:
          blockChecker.stronglyPower(p.translate(-1, 0));
          blockChecker.pushIfBlock(REPEATER_X_, p.translate(-1, 0));
          break;
        case REPEATER_X:
          blockChecker.stronglyPower(p.translate(1, 0));
          blockChecker.pushIfBlock(REPEATER_X, p.translate(1, 0));
          break;

        case EMPTY:
        case AIR:
        case REDSTONE_BLOCK:
        case TOP_SLAB:
          break;
        case REPEATER_Z:
        case REPEATER_Z_:
        case CIRCUIT_INPUT:
        case CIRCUIT_OUTPUT:
        case ERROR:
          throw new IllegalArgumentException("Invalid BlockConstant in mapping " + map.get(p));
      }

    }

    return poweredBlocks;
  }


  /**
   * @return {@code true} if the mapping obeys all invariants, false otherwise
   */
  private static boolean testDouble(Pair<SquareSpecifier, SquareSpecifier> specPair) {
    Pair<Map<Point2D, BlockConstant>, Map<Point2D, BlockConstant>> mappingPair = doubleMappings.get(specPair);

    if (!testSingle(specPair.getFirst(), mappingPair.getFirst())) {
      return false;
    }
    return testSingle(specPair.getSecond(), mappingPair.getSecond());
  }
}
