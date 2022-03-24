import a_star.AStar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AStarTest {
  private static class MazePosition {
    private int x;
    private int y;

    public MazePosition(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public int hashCode() {
      return Integer.hashCode(x) * 31 + Integer.hashCode(y);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof MazePosition) {
        return ((MazePosition) obj).x == x && ((MazePosition) obj).y == y;
      }
      return false;
    }

    @Override
    public String toString() {
      return "(" + x + ", " + y + ")";
    }
  }

  private static class MovesFunc implements Function<MazePosition, Iterable<MazePosition>> {
    private int width;
    private int height;

    public MovesFunc(int[][] maze) {
      height = maze.length;
      if (maze.length != 0) {
        width = maze[0].length;
      }
    }

    @Override
    public Collection<MazePosition> apply(MazePosition pos) {
      ArrayList<MazePosition> r = new ArrayList<>(4);
      int x = pos.x;
      int y = pos.y;
      if (x > 0) {
        r.add(new MazePosition(x - 1, y));
      }
      if (y > 0) {
        r.add(new MazePosition(x, y - 1));
      }
      if (x < width - 1) {
        r.add(new MazePosition(x + 1, y));
      }
      if (y < height - 1) {
        r.add(new MazePosition(x, y + 1));
      }
      return r;
    }
  }

  private static class CostFunction implements BiFunction<MazePosition, MazePosition, Integer> {
    private int[][] arr;

    public CostFunction(int[][] arr) {
      this.arr = arr;
    }

    @Override
    public Integer apply(MazePosition p1, MazePosition p2) {
      return this.arr[p2.y][p2.x];
    }
  }

  private static class BadHeuristic implements Function<MazePosition, Integer> {
    public BadHeuristic() {
    }

    @Override
    public Integer apply(MazePosition mazePosition) {
      return 0;
    }
  }

  /**
   * Assumes the bottom right is the objective
   */
  private static class GoodHeuristic implements Function<MazePosition, Integer> {
    int[] minCostDiagonals;

    public GoodHeuristic(int[][] arr) {
      if (arr.length == 0) {
        minCostDiagonals = new int[0];
        return;
      }

      int height = arr.length;
      int width = arr[0].length;

      ArrayList<Integer> minCosts = new ArrayList<>();
      for (int diag = 0; diag < height + width; diag++) {
        int x = Math.max(0, diag - height);
        {
          int y = diag - x;
          minCosts.add(arr[x][y]);
          x++;
        }
        assert minCosts.size() == diag;
        while (x < Math.min(diag, width)) {
          int y = diag - x;
          minCosts.set(diag - 1, Integer.min(arr[x][y], minCosts.get(diag - 1)));
          x++;
        }
      }

      minCostDiagonals = new int[width + height];
      for (int diag = height + width - 2; diag >= 0; diag--) {
        minCostDiagonals[diag] = minCostDiagonals[diag + 1] + minCosts.get(diag);
      }
    }

    @Override
    public Integer apply(MazePosition pos) {
      return minCostDiagonals[pos.x + pos.y];
    }
  }

  public static void main(String[] args) throws UnitTestFailException {
    // Based on https://projecteuler.net/problem=83
    test(new int[][]{
        new int[]{131, 673, 234, 103, 18},
        new int[]{201, 96, 342, 965, 150},
        new int[]{630, 803, 746, 422, 111},
        new int[]{537, 699, 497, 121, 956},
        new int[]{805, 732, 524, 37, 331},
    }, 2297);
  }

  private static void test(int[][] maze, int trueCost) throws UnitTestFailException {
    CostFunction costFunc = new CostFunction(maze);
    MazePosition goal = new MazePosition(maze[0].length - 1, maze.length - 1);
    AStar<MazePosition> aStar = new AStar<>(new MovesFunc(maze), costFunc, new BadHeuristic());
    aStar.pathFind(new MazePosition[]{new MazePosition(0, 0)}, new HashSet<>(Collections.singletonList(goal)));

    System.out.println(aStar.backtrace(goal));
    int cost = 0;
    MazePosition prevPos = null;
    for (MazePosition pos : aStar.backtrace(goal)) {
      if (prevPos != null) {
        cost += costFunc.apply(pos, prevPos);
      }
      prevPos = pos;
    }

    if (cost != aStar.getPathCost(goal)) {
      throw new UnitTestFailException("cost of steps does not match cached cost-to-goal");
    }
    if (cost + maze[0][0] != trueCost) {
      throw new UnitTestFailException("cost does not match answer key");
    }
  }
}
