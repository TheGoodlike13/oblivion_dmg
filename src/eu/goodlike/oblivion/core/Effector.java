package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;
import eu.goodlike.oblivion.core.effector.Arrow;
import eu.goodlike.oblivion.core.effector.Bow;
import eu.goodlike.oblivion.core.effector.Melee;
import eu.goodlike.oblivion.core.effector.Spell;
import eu.goodlike.oblivion.core.effector.Staff;
import eu.goodlike.oblivion.core.method.Poison;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

/**
 * Bundle of effects associated with a single item or spell.
 * Equivalent to any in-game weapon, ammo, poison or spell.
 * <p/>
 * Each effect has a unique type and should be processed in iteration order.
 * <p/>
 * Effectors are ordered the same as their {@link Category}.
 * However, unlike {@link Category}, their equality is not consistent with this ordering.
 * This is because there can exist multiple different effectors of the same {@link Category}
 * which do not have an explicit order between themselves.
 * As a result, avoid {@link SortedSet} and similar collections, prefer {@link List} and sort explicitly.
 * <p/>
 * Two effector objects are only equal if they represent the exact same item or spell.
 * Object identity is usually sufficient to achieve this.
 */
public interface Effector extends Iterable<EffectText>, Comparable<Effector> {

  /**
   * Factory for instances of effectors.
   * Usually describes the {@link Category} of the created effectors.
   * <p/>
   * All possible categories are defined in this class.
   */
  interface Factory<E extends Effector> {
    Category<Melee> MELEE  = new Category<>("MELEE", Melee::new);
    Category<Bow>   BOW    = new Category<>("BOW", Bow::new);
    Category<Arrow> ARROW  = new Category<>("ARROW", Arrow::new);
    Poison          POISON = Poison.getInstance();
    Category<Spell> SPELL  = new Category<>("SPELL", Spell::new);
    Category<Staff> STAFF  = new Category<>("STAFF", Staff::new);

    List<Category> ALL_IN_ORDER = ImmutableList.of(ARROW, MELEE, POISON, BOW, SPELL, STAFF);

    default E create(EffectText... effects) {
      return create(Arrays.asList(effects));
    }

    default E create(List<EffectText> effects) {
      return create(null, effects);
    }

    /**
     * The label is optional and can be null. It's entirely cosmetic.
     *
     * @throws StructureException if any of the given effects are duplicate
     */
    E create(String label, List<EffectText> effects);
  }

  Category<?> getCategory();

  Method getMethod();

  /**
   * When hitting a target multiple times with the same effector (e.g. spell), effects tend to not stack.
   * Instead, their durations are refreshed.
   * The id from this method allows us to identify effects which should be overridden rather than stacked.
   * <p/>
   * Result is undefined if the effect did not come from this effector.
   *
   * @param effect effect which we must uniquely identify
   * @return id which uniquely identifies the given effect
   */
  Effect.Id toId(EffectText effect);

  /**
   * Creates a copy of this effector with the same label.
   * The copy should not be equal to this effector.
   */
  Effector copy();

  /**
   * Creates a copy of this effector with a different label.
   * The copy should not be equal to this effector.
   */
  Effector copy(String label);

  /**
   * @return formatted description of this effector, usually containing the category and its label, if any;
   * use {@link #toString} for a full description.
   */
  String getName();

  @Override
  default int compareTo(Effector other) {
    return ORDER.compare(this, other);
  }

  Comparator<Effector> ORDER = Comparator.comparing(Effector::getCategory);

}
