# Narayana Spring Boot

Narayana is a popular open source JTA transaction manager implementation supported by Red Hat.
You can use the `narayana-spring-boot-starter` starter to add the appropriate Narayana dependencies to your project.
Spring Boot automatically configures Narayana and post-processes your beans to ensure that startup and shutdown ordering
is correct.

By default, Narayana transaction logs are written to a `transaction-logs` directory in your application home directory
(the directory in which your application jar file resides). You can customize the location of this directory by setting
a `narayana.log-dir` or `spring.jta.log-dir` property in your application.properties file. Properties starting with
`narayana` can also be used to customize the Narayana configuration. See the
[NarayanaProperties](narayana-spring-boot-core/src/main/java/me/snowdrop/boot/narayana/core/properties/NarayanaProperties.java)
Javadoc for complete details.

> Only a limited number of Narayana configuration options are exposed via `application.properties`. For a more
more complex configuration you can provide a `jbossts-proeprties.xml` file. To get more details, please, consult
Narayana project [documentation](http://narayana.io/docs/project/index.html).

> To ensure that multiple transaction managers can safely coordinate the same resource managers, each Narayana instance
must be configured with a unique ID. By default, this ID is set to 1. To ensure uniqueness in production, you should
configure the `narayana.transaction-manager-id` or `spring.jta.transaction-manager-id` property with a different value
for each instance of your application.

# Using databases

This Narayana starter supports two ways to enlist a relational database to a JTA transaction: Narayana Transactional
Driver and DBCP2.

By default Narayana Transactional driver is used which provides a basic XAResource enlistment and recovery.

If you need a more sophisticated connection management, you can enable DBCP2 support which provides connection pooling
and many other features. To enable DBCP2 add the following property to you application configuration:
```
narayana.dbcp.enabled=true
```
All DBCP2 configuration properties described in its
[documentation](https://commons.apache.org/proper/commons-dbcp/configuration.html) are mapped with a prefix
`narayana.dbcp`. So for example if you'd like to set an initial pool size to 10, you could do that by adding this entry
to your application configuration:
```
narayana.dbcp.initialSize=10
```  

# Release

Dry run:
```
mvn release:prepare -DdryRun
```

Tag:
```
mvn release:prepare
```

Deploy:
```
mvn release:perform -DskipTests
```