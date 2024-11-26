
package test.testpackage;


import java.util.List;
import java.util.ArrayList;
import java.util.Random;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/*TestService*/
public class TestService{
    private static final Logger log= LogManager.getLogger(TestService.class);

    private static List<TestClass> listOfTests;
    private TestService() {
        initialize();
    }
    private static class InstanceHolder {
        private static final TestService INSTANCE = new TestService();
    }
    public static TestService getInstance() {
        return InstanceHolder.INSTANCE;
    }
    public List<TestClass> getAll() {
        return listOfTests;
    }

    public List<TestClass> findTestsGradesHigherThan(int testGrade){
        List<TestClass> subTest=new ArrayList<TestClass>();
        for(TestClass test:listOfTests){
            if(test.getGrade()>testGrade){
                subTest.add(test);
            }
        }
        return (List<TestClass>)subTest;
    }

    public List<TestClass> findTestsGradesLowerThan(int testGrade){
        List<TestClass> subTest=new ArrayList<TestClass>();
        for(TestClass test:listOfTests){
            if(test.getGrade()<testGrade){
                subTest.add(test);
            }
        }
        return (List<TestClass>)subTest;
    }

    public  List<TestClass> findTestName(String testName){
        List<TestClass> subTest = new ArrayList<>();
        for(TestClass test:listOfTests){
            if(test.getTestName().equals(testName)){
                subTest.add(test);
            }
        }
        return (List<TestClass>)subTest;
    }
    private void initialize() {
        listOfTests =new ArrayList<>();
        for(int i =0;i<20;i++){
            Random rand= new Random();
            ArrayList<Character> multiple=new ArrayList<>();
            String testName="OddTest";
            for(int p=0;p<10;p++){
                int answer=rand.nextInt(4);
                if(answer==0){
                    multiple.add('a');
                }
                else if(answer==1){
                    multiple.add('b');
                }
                else if(answer==2){
                    multiple.add('c');
                }
                else if(answer==3){
                    multiple.add('d');
                }
            }
            if(i%2==0){
                testName="EvenTest";
            }
            TestClass test=new TestClass(rand.nextInt(101),testName,multiple);
            listOfTests.add(test);
        }
    }

}