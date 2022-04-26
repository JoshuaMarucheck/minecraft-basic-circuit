package misc;

import java.util.Iterator;
import java.util.function.Function;

public class FuncMapIterator<T, R> implements Iterator<R> {
  private Iterator<T> iter;
  private Function<T, R> f;

  public FuncMapIterator(Function<T, R> function, Iterator<T> iter) {
    this.iter = iter;
    this.f = function;
  }

  @Override
  public boolean hasNext() {
    return iter.hasNext();
  }

  @Override
  public R next() {
    return f.apply(iter.next());
  }
}
