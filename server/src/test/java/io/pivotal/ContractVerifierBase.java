package io.pivotal;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import io.pivotal.controllers.ProjectsController;
import io.pivotal.controllers.UsersController;
import org.junit.Before;

public class ContractVerifierBase {

    @Before
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(new UsersController(), new ProjectsController());
    }
}
