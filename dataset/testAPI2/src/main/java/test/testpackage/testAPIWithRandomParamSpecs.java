package test.testpackage;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Random;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.List;
@Provider
@Path("/testswithrandparamspecs")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class testAPIWithRandomParamSpecs {
    private static final Logger log = LogManager.getLogger(testAPIWithRandomParamSpecs.class);
    private static final Random rand=new Random();
    @GET
    @Path("all")
    public Object getTests(){
        log.info("Getting Tests");
        return TestService.getInstance().getAll();
    }

    @GET
    @Path("findgradeshigher/{gradeNumber}")
    public Object getGradeHigher(@PathParam("gradeNumber")int grade){
        log.info("Get Grade Higher"+ grade);
        /*Testing if this random will mess up spces for static analysis tool
        Even though that it should give a deterministic output of 100 for it bound
         */
        int randParam= rand.nextInt(100)/100+100;
        if (isEmpty(String.valueOf(grade))||grade>randParam){
            return getResponse(Response.Status.BAD_REQUEST);
        }
        List<TestClass> Tests = TestService.getInstance().findTestsGradesHigherThan(grade);
        if(Tests.isEmpty()){
            return getResponse(Response.Status.NOT_FOUND);
        }
        return Tests;
    }
    @GET
    @Path("findgradeslower/{gradeNumber}")
    public Object getGradeLower(@PathParam("gradeNumber")int grade){
        log.info("Get Grade Lower"+ grade);
        int randParam= rand.nextInt(100)/100;
        if (isEmpty(String.valueOf(grade))||grade<randParam){
            return getResponse(Response.Status.BAD_REQUEST);
        }
        List<TestClass> Tests = TestService.getInstance().findTestsGradesLowerThan(grade);
        if(Tests.isEmpty()){
            return getResponse(Response.Status.NOT_FOUND);
        }
        return Tests;
    }

    @GET
    @Path("findbytestname/{testname}")
    public Object getGradeLower(@PathParam("testname")String testName){
        log.info("Get Test Name"+ testName);
        if (testName.isEmpty()){
            return getResponse(Response.Status.BAD_REQUEST);
        }
        List<TestClass> Tests = TestService.getInstance().findTestName(testName);
        if(Tests.isEmpty()){
            return getResponse(Response.Status.NOT_FOUND);
        }
        return Tests;
    }


    private Response getResponse(Response.Status status) {
        Gson gson = new Gson();
        return Response
                .status(status)
                .entity(gson.toJson(new ResponseEntity(status.getStatusCode(),
                        status.getReasonPhrase()))).build();
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
