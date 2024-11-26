
package test.testpackage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.ArrayList;

public class Test {
    private static final Logger logger =LogManager.getLogger(Test.class);

    public static void main(String[] args) {
        ArrayList<Character> c = new ArrayList<Character>();
        c.add('a');
        TestClass t= new TestClass(10,"Test",c );
        System.out.println(t.getGrade());
        TestService ts= TestService.getInstance();
        System.out.println(ts.findTestName("OddTest").size());
        logger.debug("This is a DEBUG message.");
        logger.info("HELLO");

        logger.warn("This is a WARN message.");
        logger.error("This is an ERROR message.");
        logger.fatal("This is a FATAL message.");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

    }
}
