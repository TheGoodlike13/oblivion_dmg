package eu.goodlike.oblivion;

import java.util.Iterator;
import java.util.Map;

/**
 * Neat iterator for maps when entry removal is required.
 * <p/>
 * Normal methods of iteration do not allow changing state.
 * To do so, you have to have access to the underlying iterator.
 * Only that can change the state.
 * Using standard iterators is very bulky, though.
 * This class helps trim down that code.
 */
public final class Neaterator<KEY, VALUE> implements Iterator<Map.Entry<KEY, VALUE>> {

  public interface Action<KEY, VALUE> {
    void accept(Neaterator<KEY, VALUE> neaterator, KEY key, VALUE value);
  }

  public void forEach(Action<KEY, VALUE> action) {
    while (hasNext()) {
      Map.Entry<KEY, VALUE> next = next();
      action.accept(this, next.getKey(), next.getValue());
    }
  }

  @Override
  public boolean hasNext() {
    return actual.hasNext();
  }

  @Override
  public Map.Entry<KEY, VALUE> next() {
    return actual.next();
  }

  @Override
  public void remove() {
    actual.remove();
  }

  public Neaterator(Map<KEY, VALUE> map) {
    this.actual = map.entrySet().iterator();
  }

  private final Iterator<Map.Entry<KEY, VALUE>> actual;

}
