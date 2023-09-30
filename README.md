# yascribe Java client

This project implements the Java client of the codeless testing tool [yascribe](https://yascribe.com).

Please, use Java 17 or higher to launch yascribe client.

Please, consult the [yascribe documentation](https://yascribe.com/index.php/documentation) about how to write test plans.

## Setup

In Maven, add the following dependency in your pom.xml :
```xml
<dependency>
    <groupId>com.miriya.miri.scribe</groupId>
    <artifactId>yascribe-client</artifactId>
    <version>1.0</version>
    <scope>test</scope>
</dependency>
```

### Configuring the test environment

Create the `scribe.config.properties` file at the root of your project : 
```properties
## Scribe server configuration
# the server name
com.miriya.miri.scribe.server-name=localhost
# the server port
com.miriya.miri.scribe.server-port=9090

## Project configuration
# Project directory
com.miriya.miri.scribe.project-dir=./
# Scribe directory
com.miriya.miri.scribe.yascribe-dir=yascribe
# Report directory
com.miriya.miri.scribe.report-dir=yascribe/reports

## Test configuration
# Browser name: Chrome, Firefox, Edge, for example
com.miriya.miri.scribe.stylus.browser-name=Chrome
```

Replace `localhost` and `9090` by yascribe server name and port.<br>
For the evaluation server of yascribe host name and port are : 
- eval.yascribe.com
- 9090

`com.miriya.miri.scribe.yascribe-dir` is the folder where test plans (`.feature` files) are stored.

`com.miriya.miri.scribe.report-dir` is the folder where test reports will be generated.

`com.miriya.miri.scribe.stylus.browser-name` : specify one or several browsers to run tests.<br>
In the case of multi-browsers, use a `,` as separator character.<br>
If using the evaluation server then only Chrome, Firefox and Edge are available.

## Launching test plans

### Starter class

A starter class is required to launch test plans with IDE launchers (such as IntelliJ launcher).

Create a test class (in `src/test/java/`) and annotate it with `@ScribeTest`, for example : 
```java
@ScribeTest
public class ScribeStarter {
}
```

To launch specific test plans and not all test plans, specify the test plan files via the annotation : 
```java
@ScribeTest(files = { "test-plan-2.feature", "test-plan-4.feature" })
public class ScribeStarter {
}
```

Finally, start the tests via the IDE launcher or via Maven.