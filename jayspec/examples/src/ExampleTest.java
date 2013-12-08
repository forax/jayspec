import java.util.ArrayList;
import java.util.TreeSet;

import com.github.forax.jayspec.JaySpec;

public interface ExampleTest {
  public static void main(String[] args) {
    new JaySpec() {{
      describe(ArrayList.class, it -> {
        given("an empty list", () -> {
          ArrayList<String> list = new ArrayList<>();
          
          it.should("has a size == 0", verify -> {
            verify.that(list).size().isEqualTo(0);
          });
        });
        
        given("a list of one element", () -> {
          ArrayList<String> list = new ArrayList<>();
          list.add("hello");
          
          it.should("has a size == 1", verify -> {
            verify.that(list).size().isEqualTo(1);
          });
          it.should("gets the item at index 0", verify -> {
            verify.that(list).get(0).isEqualTo("hello");
          });
          it.should("not returns -1 as an index of an item not present", verify -> {
            verify.that(list).indexOf("not hello").isEqualTo(-1);
          });
        });
      });
      
      describe(TreeSet.class, it -> {
        given("a set with one element", () -> {
          TreeSet<String> set = new TreeSet<>();
          set.add("hello");
          
          it.should("first is the element", verify -> {
            verify.that(set).first().isEqualTo("hello");
          });
          it.should("last is the element", verify -> {
            verify.that(set).last().isEqualTo("hello");
          });
          it.should("first element is less than zzzzz", verify -> {
            verify.that(set).first().isLessThan("zzzzz");
          });
        });
      });
      
      
    }}.run();
  }

}
