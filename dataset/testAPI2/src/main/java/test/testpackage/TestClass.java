package test.testpackage;

import java.util.List;
/*TestClass*/
public class TestClass{
    private int grade;
    private String testName;
    private List<Character>multipleChoiceAnswers;

    public TestClass(int thisGrade, String thisTestName, List<Character>thisMultipleChoiceAnswer){
        this.grade=thisGrade;
        this.testName=thisTestName;
        this.multipleChoiceAnswers=thisMultipleChoiceAnswer;
    }

    public int getGrade() {
        return grade;
    }

    public List<Character> getMultipleChoiceAnswers() {
        return multipleChoiceAnswers;
    }

    public String getTestName() {
        return testName;
    }
}