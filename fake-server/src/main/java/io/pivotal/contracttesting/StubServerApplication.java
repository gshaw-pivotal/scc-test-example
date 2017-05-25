package io.pivotal.contracttesting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.contract.stubrunner.server.EnableStubRunnerServer;

@SpringBootApplication
@EnableStubRunnerServer
public class StubServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StubServerApplication.class, args);
    }
}
