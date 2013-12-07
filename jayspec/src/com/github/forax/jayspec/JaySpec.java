package com.github.forax.jayspec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class JaySpec {
  @FunctionalInterface
  public interface AssertionConsumer {
    public void accept(JayAssertion assertion) throws Exception;
  }
  
  @FunctionalInterface
  public interface Behavior {
    public void should(String description, AssertionConsumer assertionConsumer);
  }
  
  @FunctionalInterface
  public interface TestDefinition {
    public void define(Behavior it);
  }
  
  @FunctionalInterface
  public interface Reporter<R> {
    R createReport(Example example, String description, Throwable error);
  }
  
  public static class Spec {
    private final Class<?> declaredClass;
    private final TestDefinition testDefinition;
    
    public Spec(Class<?> declaredClass, TestDefinition testDefinition) {
      this.declaredClass = declaredClass;
      this.testDefinition = testDefinition;
    }
    
    public Class<?> getDeclaredClass() {
      return declaredClass;
    }
    public TestDefinition getTestDefinition() {
      return testDefinition;
    }
    
    @Override
    public String toString() {
      return "Spec of " + declaredClass;
    }
  }
  
  public static class Example {
    private final Spec spec;
    private final String description;
    private final Runnable test;
    
    public Example(Spec spec, String description, Runnable test) {
      this.spec = spec;
      this.description = description;
      this.test = test;
    }
    
    public Spec getSpec() {
      return spec;
    }
    public String getDescription() {
      return description;
    }
    public Runnable getTest() {
      return test;
    }
    
    @Override
    public String toString() {
      return "Example of " + spec.getDeclaredClass()+ ' ' + description;
    }
  }
  
  public static class Report {
    private final Example example;
    private final String description;
    private final Throwable error;
    
    public Report(Example example, String description, Throwable error) {
      this.example = example;
      this.description = description;
      this.error = error;
    }
    
    public Example getExample() {
      return example;
    }
    public String getDescription() {
      return description;
    }
    public Throwable getError() {
      return error;
    }
    
    @Override
    public String toString() {
      return "Report " + description + ' ' + error + " of " + example;
    }
  }
  
  
  private final ArrayList<Spec> specs = new ArrayList<>();
  private final ThreadLocal<Spec> currentSpec = new ThreadLocal<>();
  private final ThreadLocal<List<Example>> currentExampleList = new ThreadLocal<>();
  
  public void describe(Class<?> classToken, TestDefinition testDefinition) {
    specs.add(new Spec(classToken, testDefinition));
  }
  
  public void given(String description, Runnable action) {
    List<Example> exampleList = currentExampleList.get();
    if (exampleList == null) {
      throw new IllegalStateException("given() should be called inside a describe() block");
    }
    exampleList.add(new Example(currentSpec.get(), description, action));
  }
  
  public List<Spec> getSpecs() {
    return specs;
  }
  
  static void stackTraceDiff(Throwable stackTrace, Throwable base) {
    StackTraceElement[] stackElements = stackTrace.getStackTrace();
    if (stackElements.length == 0) {  // an exception with no stack trace
      return;
    }
    StackTraceElement[] baseElements = base.getStackTrace();
    if (stackElements.length < baseElements.length) { // something goes wrong
      return;
    }
    stackTrace.setStackTrace(Arrays.copyOf(stackElements, stackElements.length - baseElements.length));
  }
  
  public <R> List<R> runTest(Reporter<? extends R> reporter) {
    JayAssertion assertion = new JayAssertion();
    ThreadLocal<Example> currentExample = new ThreadLocal<>();
    ThreadLocal<List<R>> currentReportList = new ThreadLocal<>();
    Behavior behavior = (description, consumer) -> {
      Example example = currentExample.get();
      List<R> reportList = currentReportList.get();
      if (example == null || reportList == null) {
        throw new IllegalStateException("should() can only be called in a given() block");
      }
      
      Throwable error;
      try {
        consumer.accept(assertion);
        error = null;
      } catch(Exception|AssertionError e) {
        stackTraceDiff(e, new Throwable());
        error = e;
      }
      reportList.add(reporter.createReport(example, description, error));
    };
    ArrayList<Example> examples = new ArrayList<>();
    currentExampleList.set(examples);
    try {
      specs.forEach(spec -> {
        currentSpec.set(spec);
        spec.getTestDefinition().define(behavior);
      });
    } finally {
      currentSpec.remove();
      currentExampleList.remove();
    }
    
    //FIXME remove explicit type when eclipse will work
    return examples.parallelStream().<R>flatMap(example -> {
      ArrayList<R> reportList = new ArrayList<>();
      currentExample.set(example);
      currentReportList.set(reportList);
      try {
        example.getTest().run();
        return reportList.stream();
      } finally {
        currentExample.remove();
        currentReportList.remove();
      }
    }).collect(Collectors.<R>toList());
  }
  
  public void run() {
    List<Report> totalReports;
    long startTime = System.currentTimeMillis();
    totalReports = runTest(Report::new);
    long endTime = System.currentTimeMillis();
    Map<Spec, Map<Example, List<Report>>> map = totalReports.stream().collect(
        Collectors.groupingBy(report -> report.getExample().getSpec(),
            Collectors.<Report, Example>groupingBy(Report::getExample)
        ));
    
    int[] failures = new int[1];
    map.forEach((spec, exampleMap) -> {
      exampleMap.forEach((example, reports) -> {
        reports.forEach(report -> {
          Throwable error = report.getError();
          if (error != null) {
            System.err.println(spec);
            System.err.println("  " + example.getDescription() +
                " fails to verify that it " + report.getDescription());
            error.printStackTrace();
            failures[0]++;
          }
        });
      });
    });
    
    System.out.println("\nFinished in " + (endTime - startTime) / 1000.0 + " seconds.");
    System.out.println("Among " + totalReports.size() + " report(s), " + failures[0]  + " failed.");
  }
}
