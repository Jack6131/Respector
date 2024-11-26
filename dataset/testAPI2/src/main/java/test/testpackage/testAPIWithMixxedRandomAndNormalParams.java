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
@Path("/testAPIWithMixxedRandomAndNormalParams")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class testAPIWithMixxedRandomAndNormalParams {
    private static final Logger log = LogManager.getLogger(testAPIWithMixxedRandomAndNormalParams.class);
    private static final Random rand=new Random();


    @GET
    @Path("findgradeshigher/{gradeNumber}")
    public Object getGradeHigher(@PathParam("gradeNumber")int grade){
        log.info("Get Grade Higher"+ grade);
        /*
       Testing to see if it will get partial bounding of param
         */
        int top=101;
        int randParam= rand.nextInt(100)/100+0;
        if (isEmpty(String.valueOf(grade))||(grade<randParam||grade<top)){
            return getResponse(Response.Status.BAD_REQUEST);
        }
        List<TestClass> Tests = TestService.getInstance().findTestsGradesHigherThan(grade);
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
