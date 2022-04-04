package a_star;

import circuit.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @param <S> state type, hashable
 */
public class AStar<S> {
  private Function<S, Iterable<S>> movesFunc;
  private BiFunction<S, S, Integer> costFunc;
  private Function<S, Integer> heuristic;
  private Map<S, Integer> totalCostMap;
  private Map<S, S> backtraceMap;


  public AStar(Function<S, Iterable<S>> movesFunc, BiFunction<S, S, Integer> costFunc, Function<S, Integer> heuristic) {
    this.movesFunc = movesFunc;
    this.costFunc = costFunc;
    this.heuristic = heuristic;
    this.reset();
  }

  public void reset() {
    totalCostMap = new HashMap<>();
    backtraceMap = new HashMap<>();
  }

  public void pathFind(S[] startingPoints, Set<S> goals) {
    PriorityQueue<Pair<Pair<S, S>, Integer>> queue;
    {
      Comparator<Pair<Pair<S, S>, Integer>> c = Comparator.comparingInt(Pair::getSecond);
//      c = c.reversed();
      queue = new PriorityQueue<>(c);
    }
    for (S s : startingPoints) {
      queue.add(new Pair<>(new Pair<>(null, s), 0));
    }
    while (!queue.isEmpty()) {
      Pair<Pair<S, S>, Integer> queueEntry = queue.poll();
      Pair<S, S> states = queueEntry.getFirst();

      Integer runningCost = queueEntry.getSecond();
      S state = states.getSecond();
      S prevState = states.getFirst();

      if (totalCostMap.containsKey(state)) {
        if (totalCostMap.get(state) > runningCost) {
          throw new RuntimeException("Is the heuristic not an underestimate?");
        } else if (
            runningCost > totalCostMap.get(state)
        ) {
          continue;
        }
      }
      backtraceMap.put(state, prevState);
      totalCostMap.put(state, runningCost);

      for (S nextState : movesFunc.apply(state)) {
        Integer stepCost = costFunc.apply(state, nextState);
        if (totalCostMap.containsKey(nextState) && runningCost + stepCost >= totalCostMap.get(nextState)) {
          continue;
        }
        queue.add(new Pair<>(new Pair<>(state, nextState), runningCost + stepCost + heuristic.apply(nextState)));

      }

      if (goals.contains(state)) {
        return;
      }
    }
  }

  public ArrayList<S> backtrace(S goal) {
    ArrayList<S> r = new ArrayList<>();

    S s = goal;
    while (s != null) {
      r.add(s);
      s = backtraceMap.get(s);
    }

    return r;
  }

  public Integer getPathCost(S goal) {
    return totalCostMap.get(goal);
  }
}
