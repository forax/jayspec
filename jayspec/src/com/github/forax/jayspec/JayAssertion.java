package com.github.forax.jayspec;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

public class JayAssertion {
  static class AbstractAssert {
    final Checker checker;

    AbstractAssert(Checker checker) {
      this.checker = checker;
    }
  }
  
  public static class Assert<E> extends AbstractAssert {
    final E actual;
    
    Assert(E actual, Checker checker) {
      super(checker);
      this.actual = actual;
    }
    
    void check(Predicate<? super E> assertion, String text){
      checker.check(actual, assertion, text);
    }
    
    public void isNull() {
      check(a -> a == null, "%s == null");
    }
    public void isNotNull() {
      check(a -> a != null, "%s != null");
    }
    public void isSameAs(Object element) {
      check(a -> a == element, "%s == " + element);
    }
    public void isNotSameAs(Object element) {
      check(a -> a != element, "%s != " + element);
    }
    public void isEqualTo(Object element) {
      check(a -> Objects.equals(a, element), "%s equals " + element);
    }
    public void isNotEqualTo(Object element) {
      check(a -> !Objects.equals(a, element), "%s not equals " + element);
    }
  }
  
  public static class AssertComparable<E extends Comparable<? super E>> extends Assert<E> {
    AssertComparable(E actual, Checker checker) {
      super(actual, checker);
    }
    
    public void isLessThan(E element) {
      check(a -> a.compareTo(element) < 0, "%s < " + element);
    }
    public void isLessOrEqualThan(E element) {
      check(a -> a.compareTo(element) <= 0, "%s <= " + element);
    }
    public void isGreaterThan(E element) {
      check(a -> a.compareTo(element) > 0, "%s > " + element);
    }
    public void isGreaterOrEqualThan(E element) {
      check(a -> a.compareTo(element) >= 0, "%s >= " + element);
    }
    public void isInRange(E first, E last) {
      check(a -> a.compareTo(first) >= 0 && a.compareTo(last) <= 0, "%s in [" + first + ".." + last + ']');
    }
  }
  
  public static class AssertEntry<K, V, E extends Map.Entry<K, V>> extends Assert<E> {
    AssertEntry(E actual, Checker checker) {
      super(actual, checker);
    }
    
    private <T> void checkKey(T actual, Predicate<? super T> predicate, String text) {
      check(a -> predicate.test(actual), "key of " + text);
    }
    private <T> void checkValue(T actual, Predicate<? super T> predicate, String text) {
      check(a -> predicate.test(actual), "value of " + text);
    }
    
    public void isEqualTo(K key, V value) {
      SimpleImmutableEntry<K, V> entry = new SimpleImmutableEntry<>(key, value);
      check(a -> Objects.equals(a, entry), "%s equals " + entry);
    }
    public void isNotEqualTo(K key, V value) {
      SimpleImmutableEntry<K, V> entry = new SimpleImmutableEntry<>(key, value);
      check(a -> !Objects.equals(a, entry), "%s not equals " + entry);
    }
    public Assert<K> key() {
      return new Assert<>(actual.getKey(), this::checkKey);
    }
    public Assert<V> value() {
      return new Assert<>(actual.getValue(), this::checkValue);
    }
  }
  
  public static class AssertCollection<T, E extends Collection<T>, A extends Assert<T>> extends Assert<E> {
    final BiFunction<? super T, Checker, ? extends A> elementMapper;
    
    AssertCollection(E actual, Checker checker, BiFunction<? super T, Checker, ? extends A> mapper) {
      super(actual, checker);
      this.elementMapper = mapper;
    }
    
    private <V> void checkSize(V actual, Predicate<? super V> predicate, String text) {
      check(a -> predicate.test(actual), "size of " + text);
    }
    private <V> void checkFirst(V actual, Predicate<? super V> predicate, String text) {
      check(a -> predicate.test(actual), "first of " + text);
    }
    
    public void isEmpty() {
      check(a -> a.isEmpty(), "%s is empty");
    }
    public AssertInt size() {
      return new AssertInt(actual.size(), this::checkSize);
    }
    public void contains(Object o) {
      check(a -> a.contains(o), "%s contains " + o);
    }
    public final void containsAll(Object... objects) {
      containsAll(Arrays.asList(objects));
    }
    public void containsAll(Collection<?> objects) {
      check(a -> a.containsAll(objects), "%s contains all" + objects);
    }
    public A first() {
      return elementMapper.apply(actual.iterator().next(), this::checkFirst);
    }
  }
  
  public static class AssertList<T, E extends List<T>, A extends Assert<T>> extends AssertCollection<T, E, A> {
    AssertList(E actual, Checker checker, BiFunction<? super T, Checker, ? extends A> elementMapper) {
      super(actual, checker, elementMapper);
    }
    
    private <V> void checkIndexOf(V actual, Predicate<? super V> predicate, String text) {
      check(a -> predicate.test(actual), "index of " + text);
    }
    private <V> void checkLastIndexOf(V actual, Predicate<? super V> predicate, String text) {
      check(a -> predicate.test(actual), "last index of " + text);
    }
    private <V> void checkGet(V actual, Predicate<? super V> predicate, String text) {
      check(a -> predicate.test(actual), "get object at " + text);
    }
    private <V> void checkLast(V actual, Predicate<? super V> predicate, String text) {
      check(a -> predicate.test(actual), "last of " + text);
    }
    
    public A get(int index) {
      return elementMapper.apply(actual.get(index), this::checkGet);
    }
    public AssertInt indexOf(Object object) {
      return new AssertInt(actual.indexOf(object), this::checkIndexOf);
    }
    public AssertInt lastIndexOf(Object object) {
      return new AssertInt(actual.indexOf(object), this::checkLastIndexOf);
    }
    public A last() {
      return elementMapper.apply(actual.listIterator(actual.size()).previous(), this::checkLast);
    }
  }
  
  public static class AssertNavigableSet<T, E extends NavigableSet<T>, A extends Assert<T>> extends AssertCollection<T, E, A> {
    AssertNavigableSet(E actual, Checker checker, BiFunction<? super T, Checker, ? extends A> elementMapper) {
      super(actual, checker, elementMapper);
    }
    
    private <V> void checkFirst(V actual, Predicate<? super V> predicate, String text) {
      check(a -> predicate.test(actual), "first of " + text);
    }
    private <V> void checkLast(V actual, Predicate<? super V> predicate, String text) {
      check(a -> predicate.test(actual), "last of " + text);
    }
    
    @Override
    public A first() {
      return elementMapper.apply(actual.first(), this::checkFirst);
    }
    public A last() {
      return elementMapper.apply(actual.last(), this::checkLast);
    }
  }
  
  public static class AssertMap<K, V, E extends Map<K,V>, A extends Assert<K>, C extends AssertCollection<K, ? extends Set<K>, A>> extends Assert<E> {
    final BiFunction<E, Checker, C> keySetMapper;
    
    AssertMap(E actual, Checker checker, BiFunction<E, Checker, C> keySetMapper) {
      super(actual, checker);
      this.keySetMapper = keySetMapper;
    }
    
    private <T> void checkSize(T actual, Predicate<? super T> predicate, String text) {
      check(a -> predicate.test(actual), "size of " + text);
    }
    private <T> void checkKeySet(T actual, Predicate<? super T> predicate, String text) {
      check(a -> predicate.test(actual), "keys of " + text);
    }
    private <T> void checkValues(T actual, Predicate<? super T> predicate, String text) {
      check(a -> predicate.test(actual), "values of " + text);
    }
    private <T> void checkEntrySet(T actual, Predicate<? super T> predicate, String text) {
      check(a -> predicate.test(actual), "entries of " + text);
    }
    
    public void isEmpty() {
      check(a -> a.isEmpty(), "%s is empty");
    }
    public AssertInt size() {
      return new AssertInt(actual.size(), this::checkSize);
    }
    public void containsKey(Object o) {
      check(a -> a.containsKey(o), "%s contains " + o);
    }
    public C keySet() {
      return keySetMapper.apply(actual, this::checkKeySet);
    }
    public AssertCollection<V, Collection<V>, Assert<V>> values() {
      return new AssertCollection<>(actual.values(), this::checkValues, Assert<V>::new);
    }
    public AssertCollection<Map.Entry<K, V>, Set<Map.Entry<K,V>>, AssertEntry<K,V, Map.Entry<K, V>>> entrySet() {
      return new AssertCollection<>(actual.entrySet(), this::checkEntrySet, AssertEntry<K,V, Map.Entry<K, V>>::new);
    }
  }
  
  public static class AssertBoolean extends AbstractAssert {
    private final boolean actual;
    
    AssertBoolean(boolean actual, Checker checker) {
      super(checker);
      this.actual = actual;
    }
    
    @FunctionalInterface
    interface BooleanPredicate {
      boolean test(boolean t);
    }
    
    private void check(BooleanPredicate predicate, String text){
      checker.check(actual, predicate::test, text);
    }
    
    public void isTrue() {
      check(a -> a == true, "%s == true");
    }
    public void isFalse() {
      check(a -> a == false, "%s == false");
    }
    public void isEqualTo(boolean element) {
      check(a -> a == element, "%s == " + element);
    }
    public void isNotEqualsTo(boolean element) {
      check(a -> a != element, "%s != " + element);
    }
  }
  
  public static class AssertInt extends AbstractAssert  {
    private final int actual;
    
    AssertInt(int actual, Checker checker) {
      super(checker);
      this.actual = actual;
    }
    
    private void check(IntPredicate predicate, String text){
      checker.check(actual, predicate::test, text);
    }
    
    public void isEqualTo(int element) {
      check(a -> a == element, "%s == " + element);
    }
    public void isNotEqualsTo(int element) {
      check(a -> a != element, "%s != " + element);
    }
    public void isLessThan(int element) {
      check(a -> a < element, "%s < " + element);
    }
    public void isLessOrEqualThan(int element) {
      check(a -> a <= element, "%s <= " + element);
    }
    public void isGreaterThan(int element) {
      check(a -> a > element, "%s > " + element);
    }
    public void isGreaterOrEqualThan(int element) {
      check(a -> a >= element, "%s >= " + element);
    }
    public void isInRange(int first, int last) {
      check(a -> a >= first && a <= last, "%s in [" + first + ".." + last + ']');
    }
  }
  
  public static class AssertLong extends AbstractAssert {
    private final long actual;
    
    AssertLong(long actual, Checker checker) {
      super(checker);
      this.actual = actual;
    }
    
    private void check(LongPredicate predicate, String text){
      checker.check(actual, predicate::test, text);
    }
    
    public void isEqualTo(long element) {
      check(a -> a == element, "%s == " + element);
    }
    public void isNotEqualsTo(long element) {
      check(a -> a != element, "%s != " + element);
    }
    public void isLessThan(long element) {
      check(a -> a < element, "%s < " + element);
    }
    public void isLessOrEqualThan(long element) {
      check(a -> a <= element, "%s <= " + element);
    }
    public void isGreaterThan(long element) {
      check(a -> a > element, "%s > " + element);
    }
    public void isGreaterOrEqualThan(long element) {
      check(a -> a >= element, "%s >= " + element);
    }
    public void isInRange(long first, long last) {
      check(a -> a >= first && a <= last, "%s in [" + first + ".." + last + ']');
    }
  }
  
  public static class AssertFloat extends AbstractAssert {
    private final float actual;
    
    AssertFloat(float actual, Checker checker) {
      super(checker);
      this.actual = actual;
    }
    
    @FunctionalInterface
    interface FloatPredicate {
      boolean test(float t);
    }
    
    private void check(FloatPredicate predicate, String text){
      checker.check(actual, predicate::test, text);
    }
    
    public void isEqualTo(float element) {
      check(a -> a == element, "%s == " + element);
    }
    public void isNotEqualsTo(float element) {
      check(a -> a != element, "%s != " + element);
    }
    public void isLessThan(float element) {
      check(a -> a < element, "%s < " + element);
    }
    public void isLessOrEqualThan(float element) {
      check(a -> a <= element, "%s <= " + element);
    }
    public void isGreaterThan(float element) {
      check(a -> a > element, "%s > " + element);
    }
    public void isGreaterOrEqualThan(float element) {
      check(a -> a >= element, "%s >= " + element);
    }
    public void isInRange(float first, float last) {
      check(a -> a >= first && a <= last, "%s in [" + first + ".." + last + ']');
    }
  }
  
  public static class AssertDouble extends AbstractAssert {
    private final double actual;
    
    AssertDouble(double actual, Checker checker) {
      super(checker);
      this.actual = actual;
    }
    
    private void check(DoublePredicate predicate, String text){
      checker.check(actual, predicate::test, text);
    }
    
    public void isEqualTo(double element) {
      check(a -> a == element, "%s == " + element);
    }
    public void isNotEqualsTo(double element) {
      check(a -> a != element, "%s != " + element);
    }
    public void isLessThan(double element) {
      check(a -> a < element, "%s < " + element);
    }
    public void isLessOrEqualThan(double element) {
      check(a -> a <= element, "%s <= " + element);
    }
    public void isGreaterThan(double element) {
      check(a -> a > element, "%s > " + element);
    }
    public void isGreaterOrEqualThan(double element) {
      check(a -> a >= element, "%s >= " + element);
    }
    public void isInRange(double first, double last) {
      check(a -> a >= first && a <= last, "%s in [" + first + ".." + last + ']');
    }
  }
  
  public interface Checker {
    <E> void check(E actual, Predicate<? super E> assertion, String text);
  }
  
  private final Checker checker;
  
  public JayAssertion(Checker checker) {
    this.checker = checker;
  }
  public JayAssertion() {
    this(JayAssertion::checkAssertion);
  }
  
  private static <E> void checkAssertion(E actual, Predicate<? super E> predicate, String text) {
    if (!predicate.test(actual)) {
      throw new AssertionError("Invalid assertion, " + String.format(text, actual));
    }
  }
  
  public AssertBoolean that(boolean actual) {
    return new AssertBoolean(actual, checker);
  }
  
  public AssertInt that(int actual) {
    return new AssertInt(actual, checker);
  }
  
  public AssertLong that(long actual) {
    return new AssertLong(actual, checker);
  }
  
  public AssertFloat that(float actual) {
    return new AssertFloat(actual, checker);
  }
  
  public AssertDouble that(double actual) {
    return new AssertDouble(actual, checker);
  }

  public <E> Assert<E> that(E actual) {
    return new Assert<>(actual, checker);
  }
  
  public <E extends Comparable<E>> AssertComparable<E> that(E actual) {
    return new AssertComparable<>(actual, checker);
  }
  
  public <K, V, E extends Map.Entry<K, V>> AssertEntry<K, V, E> that(E actual) {
    return new AssertEntry<>(actual, checker);
  }
  
  public <T, E extends Collection<T>> AssertCollection<T, E, Assert<T>> that(E actual) {
    return new AssertCollection<>(actual, checker, Assert<T>::new);
  }
  
  public <T extends Comparable<? super T>, E extends Set<T>> AssertCollection<T, E, AssertComparable<T>> that(E actual) {
    return new AssertCollection<>(actual, checker, AssertComparable<T>::new);
  }
  
  public <T, E extends List<T>> AssertList<T, E, Assert<T>> that(E actual) {
    return new AssertList<>(actual, checker, Assert<T>::new);
  }
  
  //FIXME, we only support navigable set with elements that are comparable due to erasure
  // otherwise, it will be typed as a set
  public <T extends Comparable<? super T>, E extends NavigableSet<T>> AssertNavigableSet<T,E,AssertComparable<T>> that(E actual) {
    return new AssertNavigableSet<>(actual, checker, AssertComparable<T>::new);
  }
  
  public <K, V, E extends Map<K,V>> AssertMap<K,V,E,Assert<K>,AssertCollection<K,Set<K>,Assert<K>>> that(E actual) {
    return new AssertMap<>(actual, checker, (map, __) -> new AssertCollection<>(map.keySet(), __, Assert<K>::new));
  }
  
  //FIXME, we only support navigable map with keys that are comparable due to erasure
  // otherwise, it will be typed as a map
  public <K extends Comparable<? super K>, V, E extends NavigableMap<K,V>> AssertMap<K,V,E,AssertComparable<K>, AssertNavigableSet<K,NavigableSet<K>,AssertComparable<K>>> that(E actual) {
    return new AssertMap<>(actual, checker, (map, __) -> new AssertNavigableSet<>(map.navigableKeySet(), __, AssertComparable<K>::new));
  }
}
