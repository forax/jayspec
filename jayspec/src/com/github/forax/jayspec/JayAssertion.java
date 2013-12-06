package com.github.forax.jayspec;

import java.util.List;
import java.util.Objects;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

public class JayAssertion {
  private static List<String> assertionErrors;

  public JayAssertion(List<String> assertionErrors) {
    this.assertionErrors = assertionErrors;
  }

  public static class Assert<E> {
    private final E actual;
    
    Assert(E actual) {
      this.actual = actual;
    }
    
    void check(Predicate<? super E> assertion, String text) {
      if (!assertion.test(actual)) {
        assertionErrors.add("Invalid assertion: " + String.format(text, String.valueOf(actual)));
      }
    }
    
    public void isNull() {
      check(a -> a == null, "%s == null");
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
  
  public static class AssertComparable<E extends Comparable<E>> extends Assert<E> {
    AssertComparable(E actual) {
      super(actual);
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
  
  public static class AssertBoolean {
    private final boolean actual;
    
    AssertBoolean(boolean actual) {
      this.actual = actual;
    }
    
    @FunctionalInterface
    interface BooleanPredicate {
      boolean test(boolean t);
    }
    
    private void check(BooleanPredicate assertion, String text) {
      if (!assertion.test(actual)) {
        assertionErrors.add("Invalid assertion: " + String.format(text, String.valueOf(actual)));
      }
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
  
  public static class AssertInt {
    private final int actual;
    
    AssertInt(int actual) {
      this.actual = actual;
    }
    
    private void check(IntPredicate assertion, String text) {
      if (!assertion.test(actual)) {
        assertionErrors.add("Invalid assertion: " + String.format(text, String.valueOf(actual)));
      }
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
  
  public static class AssertLong {
    private final long actual;
    
    AssertLong(long actual) {
      this.actual = actual;
    }
    
    private void check(LongPredicate assertion, String text) {
      if (!assertion.test(actual)) {
        assertionErrors.add("Invalid assertion: " + String.format(text, String.valueOf(actual)));
      }
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
  
  public static class AssertFloat {
    private final float actual;
    
    AssertFloat(float actual) {
      this.actual = actual;
    }
    
    @FunctionalInterface
    interface FloatPredicate {
      boolean test(float t);
    }
    
    private void check(FloatPredicate assertion, String text) {
      if (!assertion.test(actual)) {
        assertionErrors.add("invalid assertion " + String.format(text, String.valueOf(actual)));
      }
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
  
  public static class AssertDouble {
    private final double actual;
    
    AssertDouble(double actual) {
      this.actual = actual;
    }
    
    private void check(DoublePredicate assertion, String text) {
      if (!assertion.test(actual)) {
        assertionErrors.add("Invalid assertion: " + String.format(text, String.valueOf(actual)));
      }
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

  public AssertBoolean that(boolean actual) {
    return new AssertBoolean(actual);
  }
  
  public AssertInt that(int actual) {
    return new AssertInt(actual);
  }
  
  public AssertLong that(long actual) {
    return new AssertLong(actual);
  }
  
  public AssertFloat that(float actual) {
    return new AssertFloat(actual);
  }
  
  public AssertDouble that(double actual) {
    return new AssertDouble(actual);
  }

  public <E> Assert<E> that(E actual) {
    return new Assert<>(actual);
  }
  
  public <E extends Comparable<E>> AssertComparable<E> that(E actual) {
    return new AssertComparable<>(actual);
  }
}
