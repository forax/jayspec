package com.github.forax.jayspec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
    
    void check(Predicate<? super E> assertion, Supplier<String> textSupplier){
      checker.check(actual, assertion, textSupplier);
    }
    void check(Predicate<? super E> assertion, String text){
      check(assertion, () -> text);
    }
    
    
    
    Checker delegateChecker(Function<String,String> function) {
      class DelegateChecker {
        DelegateChecker() {
          // not public
        }
        <T> void delegate(T actual, Predicate<? super T> predicate, Supplier<String> text) {
          check(a -> predicate.test(actual), () -> function.apply(text.get()));
        }
      }
      
      return new DelegateChecker()::delegate;
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
    
    @FunctionalInterface
    public interface ToObjectFunction<T, R> extends Serializable { R applyAsObject(T t); }
    @FunctionalInterface
    public interface ToBooleanFunction<T> extends Serializable { boolean applyAsBoolean(T t); }
    @FunctionalInterface
    public interface ToIntFunction<T> extends Serializable { int applyAsInt(T t); }
    @FunctionalInterface
    public interface ToLongFunction<T> extends Serializable { long applyAsLong(T t); }
    @FunctionalInterface
    public interface ToFloatFunction<T> extends Serializable { float applyAsFloat(T t); }
    @FunctionalInterface
    public interface ToDoubleFunction<T> extends Serializable { double applyAsDouble(T t); }
    
    public <R> Assert<R> get(ToObjectFunction<? super E, ? extends R> mapper) {
      //FIXME use diamond when Eclipse will support it
      return new Assert<R>(mapper.applyAsObject(actual), delegateChecker(s -> asMethodName(s, mapper)));
    }
    public AssertBoolean getBoolean(ToBooleanFunction<? super E> mapper) {
      return new AssertBoolean(mapper.applyAsBoolean(actual), delegateChecker(s -> asMethodName(s, mapper)));
    }
    public AssertInt getInt(ToIntFunction<? super E> mapper) {
      return new AssertInt(mapper.applyAsInt(actual), delegateChecker(s -> asMethodName(s, mapper)));
    }
    public AssertLong getInt(ToLongFunction<? super E> mapper) {
      return new AssertLong(mapper.applyAsLong(actual), delegateChecker(s -> asMethodName(s, mapper)));
    }
    public AssertFloat getFloat(ToFloatFunction<? super E> mapper) {
      return new AssertFloat(mapper.applyAsFloat(actual), delegateChecker(s -> asMethodName(s, mapper)));
    }
    public AssertDouble getDouble(ToDoubleFunction<? super E> mapper) {
      return new AssertDouble(mapper.applyAsDouble(actual), delegateChecker(s -> asMethodName(s, mapper)));
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
    
    public void isEqualTo(K key, V value) {
      SimpleImmutableEntry<K, V> entry = new SimpleImmutableEntry<>(key, value);
      check(a -> Objects.equals(a, entry), "%s equals " + entry);
    }
    public void isNotEqualTo(K key, V value) {
      SimpleImmutableEntry<K, V> entry = new SimpleImmutableEntry<>(key, value);
      check(a -> !Objects.equals(a, entry), "%s not equals " + entry);
    }
    public Assert<K> key() {
      return new Assert<>(actual.getKey(), delegateChecker(s -> "key of " + s));
    }
    public Assert<V> value() {
      return new Assert<>(actual.getValue(), delegateChecker(s -> "value of " + s));
    }
  }
  
  public static class AssertCollection<T, E extends Collection<T>, A extends Assert<T>> extends Assert<E> {
    final BiFunction<? super T, Checker, ? extends A> elementMapper;
    
    AssertCollection(E actual, Checker checker, BiFunction<? super T, Checker, ? extends A> mapper) {
      super(actual, checker);
      this.elementMapper = mapper;
    }
    
    public void isEmpty() {
      check(a -> a.isEmpty(), "%s is empty");
    }
    public AssertInt size() {
      return new AssertInt(actual.size(), delegateChecker(s -> "size of " + s));
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
      return elementMapper.apply(actual.iterator().next(), delegateChecker(s -> "first of " + s));
    }
  }
  
  public static class AssertList<T, E extends List<T>, A extends Assert<T>> extends AssertCollection<T, E, A> {
    AssertList(E actual, Checker checker, BiFunction<? super T, Checker, ? extends A> elementMapper) {
      super(actual, checker, elementMapper);
    }
    
    public A get(int index) {
      return elementMapper.apply(actual.get(index), delegateChecker(s -> "get object at " + s));
    }
    public AssertInt indexOf(Object object) {
      return new AssertInt(actual.indexOf(object), delegateChecker(s -> "index of " + s));
    }
    public AssertInt lastIndexOf(Object object) {
      return new AssertInt(actual.indexOf(object), delegateChecker(s -> "last index of " + s));
    }
    public A last() {
      return elementMapper.apply(actual.listIterator(actual.size()).previous(), delegateChecker(s -> "last " + s));
    }
  }
  
  public static class AssertNavigableSet<T, E extends NavigableSet<T>, A extends Assert<T>> extends AssertCollection<T, E, A> {
    AssertNavigableSet(E actual, Checker checker, BiFunction<? super T, Checker, ? extends A> elementMapper) {
      super(actual, checker, elementMapper);
    }
    
    @Override
    public A first() {
      return elementMapper.apply(actual.first(), delegateChecker(s -> "first of " + s));
    }
    public A last() {
      return elementMapper.apply(actual.last(), delegateChecker(s -> "last of " + s));
    }
  }
  
  public static class AssertMap<K, V, E extends Map<K,V>, A extends Assert<K>, C extends AssertCollection<K, ? extends Set<K>, A>> extends Assert<E> {
    final BiFunction<E, Checker, C> keySetMapper;
    
    AssertMap(E actual, Checker checker, BiFunction<E, Checker, C> keySetMapper) {
      super(actual, checker);
      this.keySetMapper = keySetMapper;
    }
    
    public void isEmpty() {
      check(a -> a.isEmpty(), "%s is empty");
    }
    public AssertInt size() {
      return new AssertInt(actual.size(), delegateChecker(s -> "size of " + s));
    }
    public void containsKey(Object o) {
      check(a -> a.containsKey(o), "%s contains " + o);
    }
    public C keySet() {
      return keySetMapper.apply(actual, delegateChecker(s -> "keys of " + s));
    }
    public AssertCollection<V, Collection<V>, Assert<V>> values() {
      return new AssertCollection<>(actual.values(), delegateChecker(s -> "values of " + s), Assert<V>::new);
    }
    public AssertCollection<Map.Entry<K, V>, Set<Map.Entry<K,V>>, AssertEntry<K,V, Map.Entry<K, V>>> entrySet() {
      return new AssertCollection<>(actual.entrySet(), delegateChecker(s -> "entries of " + s), AssertEntry<K,V, Map.Entry<K, V>>::new);
    }
  }
  
  public static class AssertBoolean extends AbstractAssert {
    private final boolean actual;
    
    AssertBoolean(boolean actual, Checker checker) {
      super(checker);
      this.actual = actual;
    }
    
    @FunctionalInterface
    interface BooleanPredicate { boolean test(boolean t); }
    
    private void check(BooleanPredicate predicate, String text){
      checker.check(actual, predicate::test, () -> text);
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
      checker.check(actual, predicate::test, () -> text);
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
      checker.check(actual, predicate::test, () -> text);
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
    interface FloatPredicate { boolean test(float t); }
    
    private void check(FloatPredicate predicate, String text){
      checker.check(actual, predicate::test, () -> text);
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
      checker.check(actual, predicate::test, () -> text);
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
  
  @FunctionalInterface
  public interface Checker {
    <E> void check(E actual, Predicate<? super E> predicate, Supplier<String> textSupplier);
  }
  
  private final Checker checker;
  
  public JayAssertion(Checker checker) {
    this.checker = checker;
  }
  public JayAssertion() {
    this(JayAssertion::checkAssertion);
  }
  
  static String asMethodName(String text, Serializable lambda) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    ObjectOutputStream output;
    try {
      output = new ObjectOutputStream(stream);
      output.writeObject(lambda);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    
    ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
    @SuppressWarnings("unchecked")
    HashMap<String, Object> map = (HashMap<String, Object>)new SerializationDecoder().decode(buffer);
    String format = "%s" + '.' + (String)map.get("implMethodName") + (String)map.get("implMethodSignature");
    return text.replace("%s", format);
  }
  
  private static <E> void checkAssertion(E actual, Predicate<? super E> predicate, Supplier<String> textSupplier) {
    if (!predicate.test(actual)) {
      throw new AssertionError("Invalid assertion, " + String.format(textSupplier.get(), actual));
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
