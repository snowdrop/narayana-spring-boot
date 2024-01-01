# Narayana Spring Boot

Narayana is a popular open source JTA transaction manager implementation supported by Red Hat.
You can use the `narayana-spring-boot-starter` starter to add the appropriate Narayana dependencies to your project.
Spring Boot automatically configures Narayana and post-processes your beans to ensure that startup and shutdown ordering
is correct.

By default, Narayana transaction logs are written to a `transaction-logs` directory in your application home directory
(the directory in which your application jar file resides). You can customize the location of this directory by setting
a `narayana.log-dir` property in your application.properties file. Properties starting with `narayana` can also be used
to customize the Narayana configuration. See the
[NarayanaProperties](narayana-spring-boot-core/src/main/java/dev/snowdrop/boot/narayana/core/properties/NarayanaProperties.java)
Javadoc for complete details.

> Only a limited number of Narayana configuration options are exposed via `application.properties`. For a more
more complex configuration you can provide a `jbossts-properties.xml` file. To get more details, please, consult
Narayana project [documentation](http://narayana.io/docs/project/index.html).

> To ensure that multiple transaction managers can safely coordinate the same resource managers, each Narayana instance
must be configured with a unique ID. By default, this ID is set to 1. To ensure uniqueness in production, you should
configure the `narayana.transaction-manager-id` property with a different value for each instance of your application.

# Using databases

By default Narayana Transactional driver is used to enlist a relational database to a JTA transaction which provides a basic XAResource enlistment and recovery.

## Add pooling

If you need a more sophisticated connection management, we advice you to use [agroal-spring-boot-starter](https://agroal.github.io)
which provides connection pooling and many other features. To enable Agroal add the following dependency to your application configuration:
```
<dependency>
    <groupId>io.agroal</groupId>
    <artifactId>agroal-spring-boot-starter</artifactId>
    <version>2.x.x</version>
</dependency>
```

All Agroal configuration properties described in its [documentation](https://agroal.github.io/docs.html)

# Using messaging brokers

This Narayana starter supports two ways to enlist a messaging broker to a JTA transaction: plain connection
factory and MessagingHub pooled connection factory.

By default Narayana Connection Proxy around the JMS connection factory is used which provides a basic XAResource enlistment and recovery.

## Add pooling

If you need a more sophisticated connection management, you can enable MessagingHub support which provides connection pooling
and many other features. To enable MessagingHub add the following property to you application configuration:
```
narayana.messaginghub.enabled=true
```
All MessagingHub configuration properties described in its [documentation](https://github.com/messaginghub/pooled-jms/blob/master/pooled-jms-docs/Configuration.md) are mapped with a prefix `narayana.messaginghub`. So for example if you'd like to set an max connections pool size to 10, you could do that by adding this entry to your application configuration:
```
narayana.messaginghub.maxConnections=10
```

# Release

## Manually

Dry run:
```bash
mvn release:prepare -DdryRun
```

Tag:
```bash
mvn release:prepare
```

Deploy:
```bash
mvn release:perform
```
