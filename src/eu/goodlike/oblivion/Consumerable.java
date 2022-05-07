package eu.goodlike.oblivion;

import java.util.Deque;
import java.util.Iterator;

/**
 * Iterable for {@link Deque}s which consumes elements as it goes.
 * To prevent the consumption, the loop must be broken manually.
 */
public final class Consumerable<T> implements Iterable<T> {

  public static <T> Consumerable<T> whileConsuming(Deque<T> deque) {
    return new Consumerable<>(deque);
  }

  @Override
  public Iterator<T> iterator() {
    return new Consumerator();
  }

  public Consumerable(Deque<T> deque) {
    this.deque = deque;
  }

  private final Deque<T> deque;

  private final class Consumerator implements Iterator<T> {
    @Override
    public boolean hasNext() {
      return !deque.isEmpty();
    }

    @Override
    public T next() {
      return deque.removeFirst();
    }

    @Override
    public void remove() {
      // done automatically
    }
  }

}
