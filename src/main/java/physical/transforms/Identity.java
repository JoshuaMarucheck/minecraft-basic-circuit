package physical.transforms;

import java.util.function.Function;

public class Identity<T> implements Function<T,T> {
  @Override
  public T apply(T t) {
    return t;
  }
}
