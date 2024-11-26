
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */


package test.testpackage;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import javax.ws.rs.ext.Provider;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Provider
@Path("/test")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class testRestAPI {
    private static final Logger log = LogManager.getLogger(testRestAPI.class);

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
        if (isEmpty(String.valueOf(grade))){
            return getResponse(Status.BAD_REQUEST);
        }
        List<TestClass> Tests = TestService.getInstance().findTestsGradesHigherThan(grade);
        if(Tests.isEmpty()){
            return getResponse(Status.NOT_FOUND);
        }
        return Tests;
    }
    @GET
    @Path("findgradeslower/{gradeNumber}")
    public Object getGradeLower(@PathParam("gradeNumber")int grade){
        log.info("Get Grade Lower"+ grade);
        if (isEmpty(String.valueOf(grade))){
            return getResponse(Status.BAD_REQUEST);
        }
        List<TestClass> Tests = TestService.getInstance().findTestsGradesLowerThan(grade);
        if(Tests.isEmpty()){
            return getResponse(Status.NOT_FOUND);
        }
        return Tests;
    }

    @GET
    @Path("findbytestname/{testname}")
    public Object getGradeLower(@PathParam("testname")String testName){
        log.info("Get Test Name"+ testName);
        if (testName.isEmpty()){
            return getResponse(Status.BAD_REQUEST);
        }
        List<TestClass> Tests = TestService.getInstance().findTestName(testName);
        if(Tests.isEmpty()){
            return getResponse(Status.NOT_FOUND);
        }
        return Tests;
    }


    private Response getResponse(Status status) {
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
