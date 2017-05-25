Experiments with spring cloud contract testing.

## Getting started ##
```
    git clone https://github.com/gshaw-pivotal/scc-test-example.git
```

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