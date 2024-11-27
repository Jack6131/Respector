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
@Path("/testAPIWithFunctionParam")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class testAPIWithFunctionParam {
    private static final Logger log = LogManager.getLogger(testAPIWithFunctionParam.class);
    private static final Random rand=new Random();
    private static int intHolder;
    private int get100(){
        return 100;
    }

    private void setIntHolder(){
        intHolder=0;
    }
    @GET
    @Path("findgradeshigher/{gradeNumber}")
    public Object getGradeHigher(@PathParam("gradeNumber")int grade){
        log.info("Get Grade Higher"+ grade);
        /*
       Calls a function to hold bound
        */
        if (isEmpty(String.valueOf(grade))||grade>get100()){
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
        setIntHolder();
        /*
       seeing how this api interacts with param bound
       that is set by a setting function
        */
        if (isEmpty(String.valueOf(grade))||grade<intHolder){
            return getResponse(Response.Status.BAD_REQUEST);
        }
        List<TestClass> Tests = TestService.getInstance().findTestsGradesLowerThan(grade);
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
