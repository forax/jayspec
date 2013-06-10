Lambdas - Welcome to the Java 2nd revolution
============================================

Java 8 will introduce (or introduces it depends if you have already played with [the betas](https://jdk8.java.net/download.html)
or not) a new full featured feature, lambdas.
Instead of explaining [the syntax](http://cr.openjdk.java.net/~briangoetz/lambda/lambda-state-4.html),
[the semantics](http://cr.openjdk.java.net/~briangoetz/lambda/lambda-state-4.html),
how it's integrated with [java.util](http://download.java.net/jdk8/docs/api/java/util/stream/package-summary.html#package_description), yada yada,
Let's take an example and see how lambdas can be used by users and developers of APIs.

Let suppose I want to design a JUnit/TestNG replacement, basically these two libraries are designed to take an existing code
and write unit tests to the code. I want library which is designed in the opposite way, the Test Driven Development way.
I want to first write tests and use them as a specification to write code. Note that this idea is far from new,
it's basically a rip of Ruby's RSpec in Java.

JaySpec
-------

So let me introduce JaySpec, which allows to describe executable tests that can be used as specification.
In language like Scala or Groovy, the parser is enough flexible to be able to parse that text and see it as code,
so they are able to understand/execute code like

    describe ArrayList
      given an empty list
        ArrayList<String> list = new ArrayList<>();
          
        it should have a size == 0
          verify that list.size() is equals to 0
 

In Java, even with the lambda syntax you need to add extra characters.
Here is the equivalent code in Java 8

    describe(ArrayList.class, it -> {
      given("an empty list", () -> {
        ArrayList<String> list = new ArrayList<>();
          
        it.should("have a size == 0", verify -> {
          verify.that(list.size()).isEqualTo(0);
        });
      });
    });

Ok, less readable but not that bad :)
'describe', 'given' and 'should' are methods that takes a textual description and a lambda that correspond to
the executable part of the spec. The lambda acts as way to delay the execution of a code, so the code
above is seen as a tree (with a node 'describe' that contains a node 'given' ...) by JaySpec.
In term of vocabulary, for the rest of this text and for JaySpec,
'describe' defines a spec, 'given' defines an example and 'should' defines a test.

The expressions that you can use after verify.that() is the one provided by the [fest-assert](https://github.com/alexruiz/fest-assert-2.x)
library (I don't want to re-invent the wheel here),
which is a nice library (at least the 2.x version, I've not taken a look to the older versions).

I've cheated a little by saying that the code above was a Java code because I've omitted the class declaration,
so the real executable Java code for our small example is

    public interface ExampleTest {
      public static void main(String[] args) {
        new JaySpec() {{
          describe(ArrayList.class, it -> {
            given("an empty list", () -> {
              ArrayList<String> list = new ArrayList<>();
          
              it.should("have a size == 0", verify -> {
                verify.that(list.size()).isEqualTo(0);
              });
            });
          });
        }}.run();
      }
    }

I use an interface instead of a class at top-level so nobody can create an instance of ExampleTest
(you can have public static method in interface in Java 8).
I abuse of the inner-class syntax (hence the double mustache after new JaySpec())
to be able to use 'describe' or 'given' as if they were functions. There are in fact of methods of the class JaySpec.

What JaySpec does if to take all the tests (the one that starts with given), execute them in parallel (using fork-join) and in this case write the results
in a terminal (the code the execution of the tests and the output are separated so JaySpec can be embedded in any UI easily).

Here is an example of run of a similar but a little more complex code (see [ExampleTest.java](https://github.com/forax/jayspec/blob/master/jayspec/examples/src/ExampleTest.java))

    class java.util.ArrayList
      given a list of one element
        it should have a size == 1
        it should get the item at index 0
        it should not return a valid index for a different item
      given an empty list
        it should have a size == 0


The cool thing is that JaySpec.java is one only file, containing less than 200 lines of code (with no comment).
Because the is not a lot of codes that use lambda out there (at least at the time when I write this lines),
I suppose that it a good idea to explain a little bit the code of [JaySpec.java](https://github.com/forax/jayspec/blob/master/jayspec/src/com/github/forax/jayspec/JaySpec.java).

JaySpec does 3 different things, the first one is to create the tree describing what to execute,
the second one if to run all the examples (in parallel, we have several cores on our computers now)
and at the end, the result (the report) needs to be collected and organized by spec and by example.

Creating the tree
-----------------
Technically, we don't need to maintain the forward links between a spec and all the examples of the spec but
the only the backward links from a test (the things that starts by "it should") to its example and from
an example to its spec.
Unlike in Ruby, there is no scope object in Java, so the standard way to emulate dynamic scope object
is to use thread local variables.

Running the examples in parallel
--------------------------------
This part is really easy because JaySpec uses the new java.util.stream API,
so the code is just:

    examples.parallelStream().flatMap(example -> { 
        ArrayList<R> reportList = new ArrayList<R>();
        // execute each test of the example
        return reportList.stream();
    }).collect(Collectors.toList()); 

That takes all examples, from the previous step, and for each of them in parallel,
it will generate several reports (one by test defined in the examples)
that will be gathered in a List. The underlying implementation of the parallel stream
will use a fork-join to distribute the execution of the tests on all available cores.
Note that each test for one example will be executed sequentially so JaySpec can
test code that use objects that are not thread safe.

Gathering the reports
---------------------
Once the reports are all created, in parallel, they need to be grouped
first by example and then by the spec of the example,
again this is something easy to write with the new stream API:

    Map<Spec, Map<Example, List<Report>>> map = ... .stream().collect(
        Collectors.groupingBy(report -> report.getExample().getSpec(),
            Collectors.groupingBy(Report::getExample)
        ));
        
The [Collector API](http://download.java.net/jdk8/docs/api/java/util/stream/Collectors.html)
is a really powerful but complex, I guess that i will take
a little more time for people to use it than the other parts of the Stream API.

And at the end, the result of each report is printed using the newly introduced
method Map.forEach which is far easier to use that using a for loop on an entrySet
as we used to do before Java 8.

    map.forEach((spec, exampleMap) -> {
      ...
      exampleMap.forEach((example, reports) -> {
        ...
        reports.forEach(report -> {
          ...
        });
      });
    });
  
The last calls to forEach can be also written using a for loop,
but I have not yet determined the lambda syntax should be used or not in this case.

cheers,
Remi
