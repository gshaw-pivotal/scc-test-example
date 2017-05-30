Experiments with spring cloud contract testing and using json schema validation.

This repo is the gradle version of [scc-test-example-maven](https://github.com/gshaw-pivotal/scc-test-example-maven) which is built using maven.

A good resource for information about spring cloud contract testing can be found at the [Spring Cloud Contract page](https://cloud.spring.io/spring-cloud-contract/spring-cloud-contract.html).

## Getting started ##
```
    git clone https://github.com/gshaw-pivotal/scc-test-example.git
```

The `run.sh` script will build the service, run all tests (including the spring cloud contract tests), build the stubs, publish said stubs to your local maven and then start the stub-server using the stubs just published.

The endpoints of the service being stubbed will then be available at `http://localhost:6565/{endpoint}`.

## Repo Structure ##

There are two main modules in this repo.

### server ###

This module holds the 'real' service (eg. the production code). It is written no differently then how you would write any other spring boot application.

The key importance is in the testing side.

A ContractBase class is required to specify which controllers are to be stubbed / mocked. In this repo, this is the responsibility of the `ContractVerifierBase` class.

```
    public class ContractVerifierBase {
    
        @Before
        public void setup() {
            RestAssuredMockMvc.standaloneSetup(new up each controller class here as a param);
        }
    }
```

What if a controller that is going to be stubbed has parameters in its constructor? The you need to include those parameters here like you would anywhere else. However, no production code needs to be moved to the stub-server. Thus, our UsersController takes a UsersServices and its entry in the above setup would be

```
    ...new UsersController(new UsersServices())...
```

Our ProjectsController takes no parameters (has no dependencies) and thus is simply `...new ProjectsController()...`

The contract test specifications need to be located in the test resources, under a subfolder called 'contracts'. These contracts are written using groovy script.

A contract test specification follows the following format:

```
    package contracts
    
    org.springframework.cloud.contract.spec.Contract.make {
        request {
            method 'GET'
            url '/users'
        }
        response {
            status 200
            body("""[{"id": 1234, "name": "thename"}, {"id": 4567, "name": "anothername"}]""")
        }
    }
```

where the request segment specifies the format and structure of the request going to the service and the response segment specifying the format and structure of the response coming out of the service back to the consumer.

For an endpoint to be present in the stub-server there must be a contract for that endpoint. For example, in our codebase we have `/projects` and `/projects/{id}` endpoints (where {id} is an integer number). However, we only have a contract written for the `/projects` endpoint.

Thus if you were to start the stub-server and make a request to `/projects/{id}` (replace {id} with any integer number) you will get a 404 response as the stub-server is not stubbing that endpoint. This is due to there being no contract for that endpoint specified in the `test/resources/contracts`.

To verify that the service conforms to the contracts and to build the stubs jar, you can run

```
    ./buildstubs.sh
```

from the root directory of the repo. This script will run the tests (including the contracts, build a stubs jar and publish to your local maven).

The build.gradle for this module needs to include the following:

```
    buildscript {
        ext {
            springBootVersion = '1.5.3.RELEASE'
        }
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
            classpath "io.spring.gradle:dependency-management-plugin:0.6.0.RELEASE"
            classpath "org.springframework.cloud:spring-cloud-contract-gradle-plugin:1.0.2.RELEASE"
        }
    }
    
    apply plugin: 'groovy'
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'spring-cloud-contract'
    apply plugin: 'maven-publish'
    
    group = 'io.pivotal'
    version = '0.0.1-SNAPSHOT'
    
    sourceCompatibility = 1.8
    targetCompatibility = 1.8
    
    repositories {
        mavenLocal()
        mavenCentral()
    }
    
    dependencyManagement {
        imports {
            mavenBom 'org.springframework.cloud:spring-cloud-contract-dependencies:1.0.2.RELEASE'
        }
    }
    
    contracts {
        packageWithBaseClasses = 'io.pivotal'
    }
    
    dependencies {
        compile('org.springframework.boot:spring-boot-starter-web')
        testCompile('org.springframework.boot:spring-boot-starter-test')
        testCompile('org.springframework.cloud:spring-cloud-starter-contract-verifier')
    }
```

with the key points being;

1. the buildscript has dependencies on the `io.spring.gradle:dependency-management-plugin` and the `org.springframework.cloud:spring-cloud-contract-gradle-plugin`.
2. we require plugins to include `io.spring.dependency-management`, `spring-cloud-contract` & `maven-publish` to support the execution of contract tests, building and publishing of stubs.
3. we require a `dependencyManagement` section that has a mavenBom import for `org.springframework.cloud:spring-cloud-contract-dependencies`.
4. test dependencies on `org.springframework.cloud:spring-cloud-starter-contract-verifier`.
5. a `contracts` section that specifies a `packageWithBaseClasses` key with a value that points to the package where the ContractBase class is located.

### fake-server ###

This module holds the stub-server that consumers can test against. It stands up as a its own spring boot application, completely separate from the 'real' service.

The build gradle for the stub-server needs to include the following

```
    buildscript {
        ext {
            springBootVersion = '1.5.3.RELEASE'
        }
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
        }
    }
    
    apply plugin: 'java'
    apply plugin: 'org.springframework.boot'
    
    group = 'io.pivotal'
    version = '0.0.1-SNAPSHOT'
    
    sourceCompatibility = 1.8
    targetCompatibility = 1.8
    
    repositories {
        mavenCentral()
    }
    
    dependencies {
        compile('org.springframework.boot:spring-boot-starter-web')
        compile('org.springframework.cloud:spring-cloud-starter-contract-stub-runner')
        compile('org.springframework.cloud:spring-cloud-stream-test-support')
    }
    
    dependencyManagement {
        imports {
            mavenBom 'org.springframework.cloud:spring-cloud-contract-dependencies:1.0.2.RELEASE'
            mavenBom 'org.springframework.cloud:spring-cloud-dependencies:Camden.SR3'
        }
    }
```

with the key parts being;

1. compile dependencies need to include `org.springframework.cloud:spring-cloud-starter-contract-stub-runner` & `org.springframework.cloud:spring-cloud-stream-test-support`.
2. a `dependencyManagement` segment which includes 2 mavenBom imports for `org.springframework.cloud:spring-cloud-contract-dependencies` & `org.springframework.cloud:spring-cloud-dependencies`.

The stub-server can be started with

```
    ./startstubserver.sh
```

from the root directory of the repo. This stands up a spring boot application that will respond to requests with responses specified in the contracts located in the 'server' module.

To configure the stub-server, a `EnableStubRunnerServer` annotation was added to the stub-server application class:

```
    @SpringBootApplication
    @EnableStubRunnerServer
    public class StubServerApplication {
        public static void main(String[] args) {
            SpringApplication.run(StubServerApplication.class, args);
        }
    }
```

And in its application.properties the following were set:

```
    # Tell the StubRunnerServer to use the Local Maven repo when looking for the stubs
    stubrunner.workOffline=true 
    
    # Tell the StubRunnerServer to look for the Maven stub jar under 'io.pivotal:server' and start on 6565
    stubrunner.ids=io.pivotal:server:+:stubs:6565
    
    # Tell the overarching SpringBoot app to use a random port, so it get's out of the way of other applications
    server.port=0
```

this tells the stubrunner to use the local maven repo, which library / jar in said repo contains the stubs and which port to run the stubs on (in this case 6565). The actual stub-server itself runs on a random port and can be queried with the following get request

```
    htttp://localhost:<randomport>/stubs
```

to report what library / jar it is running and the port it is running them on.

### Consumer vs Producer ###

In our previous contract specification above, we have used fixed values for the response. However, fixed values are not always useful.

Spring cloud contract testing has the concept of consumer and producer. A consumer is anything that uses the service (eg. makes a request to our service and expects a response). A producer is the service that is being called and in the case of spring cloud contract testing is the service that is being stubbed.

By using these we can introduce variable values into the contract specifications with limitations.

By using consumer(your value here) in the request you allow consumers to send a request with any value that matches the form of the request to the stub-server. However, on the response side consumers will get a concrete value to test against.

You can use producer(your value here) in the response, while in the request a concrete value must be used as you need to send a real request to the service.

See [Spring cloud contract consumer and producer](https://cloud.spring.io/spring-cloud-contract/spring-cloud-contract.html#_what_is_this_value_consumer_producer) for more information.

The following contract specification uses the idea of a consumer and a producer.

```
    package contracts
    
    import org.springframework.cloud.contract.spec.Contract
    
    Contract.make {
        request {
            method 'GET'
            //Consumers can be any value, producers must be a real fixed value on the request side
            url value(consumer(regex('/users/[0-9]{1,}')), producer('/users/99'))
        }
        response {
            status 200
            body([
                //Consumers must be a real fixed value, producers can be any value on the response side
                id: value(consumer('123'), producer(regex('[0-9]{1,}'))),
                name: 'a_single_user'
            ])
        }
    }
```

Thus for the above example, a consumer testing against the stub-server would be able send a request to `/users/{id}` where {id} is any integer number with at least one digit (eg. 123, 3, 77 etc) and expect to get a fixed response (which would contain an id key with the value of 123). For the producer side, the contract test would be executed by sending a request to `/users/99` and the test would expect a response from the service to contain an id key with any integer value of at least one digit, but the actual value of the id key would not matter.

### One URL, multiple contracts ###

What if we have an endpoint like `/users/{id}` but we want different responses based on the id value passed? An simple example of this case is for an id that is not present in the service (eg. user not found). To make this happen we need at least two contract specifications, one that returns a user's details for any id and a second that returns a 'not found' for a specific id.

This can be done with `priority` in the contract specification. This allows a contract specification to override another. Priority is set with an integer value, with a value of '1' indicating this contract specification has the highest priority.

For this example we have two contract specifications for the `/user/{id}` endpoint like so

```
    package contracts
    
    import org.springframework.cloud.contract.spec.Contract
    
    Contract.make {
        priority 1
        request {
            method 'GET'
            url '/users/1'
        }
        response {
            status 200
            body(
                error: 'No user found'
            )
        }
    }
```

and

```
    package contracts
    
    import org.springframework.cloud.contract.spec.Contract
    
    Contract.make {
        priority 2
        request {
            method 'GET'
            //Consumers can be any value, producers must be a real fixed value on the request side
            url value(consumer(regex('/users/[0-9]{1,}')), producer('/users/99'))
        }
        response {
            status 200
            body([
                //Consumers must be a real fixed value, producers can be any value on the response side
                id: value(consumer('123'), producer(regex('[0-9]{1,}'))),
                name: 'a_single_user'
            ])
        }
    }
```

The first specification (found in `shouldReturnNoUserFound.groovy`) specifies that if a request with an id of 1 is received then the service is to respond with the error message 'No user found'. This specification has a priority of 1.

The second specification (found in `shouldReturnTheUser.groovy`) specifies that if the consumer sends a request with any valid id, then a user is to be returned. This specification has a priority of 2.

Normally, the second contract specification would handle a request to `/user/1` and respond with a user, but because the first contract specification has a higher priority it is used and thus the 'No user found' message is returned.

## Json Schema validation ##

An alternative sometimes used for contract testing is an approach that uses a Json schema to perform validation.

Under this approach (assuming a spring boot application) one or more test classes are created using the `RunWith` and `SpringBootTest` annotations. Tests are written which make requests to the application and the responses returned are validated against a defined Json schema.

Examples of this can be seen in the `test/java/io/pivotal/validation` directory for our server, where there are two test classes, one to test the users endpoints and a second to test the projects endpoints. The Json schema specifications to go with these tests can be found in `main/resources/json` directory.

It should be noted that there are limitations with using this approach to contract test.

1. The Json schema specifications only apply to the reponses from the service, there is no contract agreement to enforce how a consumer should structure / present its request to the service. Obviously if the consumer sends a request that the service can not handle, some kind of error / exception will occur, but this would be discovered after the fact (when the consumer tries to integrate with the service).
2. There is no stub-server for consumers to test against.
