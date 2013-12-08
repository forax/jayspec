package com.github.forax.jayspec;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
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
    public void isSameAs(E element) {
      check(a -> a == element, "%s == " + element);
    }
    public void isNotSameAs(E element) {
      check(a -> a != element, "%s != " + element);
    }
    public void isEqualTo(E element) {
      check(a -> Objects.equals(a, element), "%s equals " + element);
    }
    public void isNotEqualTo(E element) {
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
  
  public static class AssertCollection<T, E extends Collection<T>> extends Assert<E> {
    AssertCollection(E actual, Checker checker) {
      super(actual, checker);
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
    public void containsAll(Object... objects) {
      containsAll(Arrays.asList(objects));
    }
    public void containsAll(Collection<?> objects) {
      check(a -> a.containsAll(objects), "%s contains all" + objects);
    }
    public Assert<T> first() {
      return new Assert<>(actual.iterator().next(), this::checkFirst);
    }
  }
  
  public static class AssertList<T, E extends List<T>> extends AssertCollection<T, E> {
    AssertList(E actual, Checker checker) {
      super(actual, checker);
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
    
    public Assert<T> get(int index) {
      return new Assert<>(actual.get(index), this::checkGet);
    }
    public AssertInt indexOf(Object object) {
      return new AssertInt(actual.indexOf(object), this::checkIndexOf);
    }
    public AssertInt lastIndexOf(Object object) {
      return new AssertInt(actual.indexOf(object), this::checkLastIndexOf);
    }
    public Assert<T> last() {
      return new Assert<>(actual.listIterator(actual.size()).previous(), this::checkLast);
    }
  }
  
  public static class AssertNavigableSet<T, E extends NavigableSet<T>> extends AssertCollection<T, E> {
    AssertNavigableSet(E actual, Checker checker) {
      super(actual, checker);
    }
    
    private <V> void checkFirst(V actual, Predicate<? super V> predicate, String text) {
      check(a -> predicate.test(actual), "first of " + text);
    }
    private <V> void checkLast(V actual, Predicate<? super V> predicate, String text) {
      check(a -> predicate.test(actual), "last of " + text);
    }
    
    @Override
    public Assert<T> first() {
      return new Assert<>(actual.first(), this::checkFirst);
    }
    public Assert<T> last() {
      return new Assert<>(actual.last(), this::checkLast);
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
  
  public <T, E extends Collection<T>> AssertCollection<T, E> that(E actual) {
    return new AssertCollection<>(actual, checker);
  }
  
  public <T, E extends List<T>> AssertList<T, E> that(E actual) {
    return new AssertList<>(actual, checker);
  }
  
  public <T, E extends NavigableSet<T>> AssertNavigableSet<T, E> that(E actual) {
    return new AssertNavigableSet<>(actual, checker);
  }
}
