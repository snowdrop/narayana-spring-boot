[![Version](https://img.shields.io/maven-central/v/dev.snowdrop/narayana-spring-boot-parent?logo=apache-maven&style=for-the-badge)](https://search.maven.org/artifact/dev.snowdrop/narayana-spring-boot-parent)
[![GitHub Actions Status](<https://img.shields.io/github/actions/workflow/status/snowdrop/narayana-spring-boot/test.yml?branch=main&logo=GitHub&style=for-the-badge>)](https://github.com/snowdrop/narayana-spring-boot/actions/workflows/test.yml)
[![License](https://img.shields.io/github/license/snowdrop/narayana-spring-boot?style=for-the-badge&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)

# Narayana Spring Boot

Narayana is a popular open source JTA transaction manager implementation supported by Red Hat.
You can use the `narayana-spring-boot-starter` starter to add the appropriate Narayana dependencies to your project.
Spring Boot automatically configures Narayana and post-processes your beans to ensure that startup and shutdown ordering
is correct.

```xml
<dependency>
    <groupId>dev.snowdrop</groupId>
    <artifactId>narayana-spring-boot-starter</artifactId>
    <version>RELEASE</version>
</dependency>
```

By default, Narayana transaction logs are written to a `transaction-logs` directory in your application home directory
(the directory in which your application jar file resides). You can customize the location of this directory by setting
a `narayana.log-dir` property in your application.properties file. Properties starting with `narayana` can also be used
to customize the Narayana configuration. See the
[NarayanaProperties](narayana-spring-boot-core/src/main/java/dev/snowdrop/boot/narayana/core/properties/NarayanaProperties.java)
Javadoc for complete details.

> Only a limited number of Narayana configuration options are exposed via `application.properties`. For a more complex
configuration you can provide a `jbossts-properties.xml` file. To get more details, please, consult
Narayana project [documentation](http://narayana.io/docs/project/index.html).

> To ensure that multiple transaction managers can safely coordinate the same resource managers, each Narayana instance
must be configured with a unique ID. By default, this ID is set to 1. To ensure uniqueness in production, you should
configure the `narayana.node-identifier` property with a different value for each instance of your application. This value
must not exceed a length of 28 bytes. To ensure that the value is shortened to a valid length by hashing with SHA-224 and encoding
with base64, configure `narayana.shorten-node-identifier-if-necessary` property to true. Be aware, this may result in duplicate
strings which break the uniqueness that is mandatory for safe transaction usage!

# Batch application

If you are running your Spring Boot application as a batch program, you'll have to explicitly call exit (`SIGTERM`) on your application to proper shutdown.
This is needed because of Narayana is running periodic recovery in a non-daemon background thread.

This could be achieved with the following code example:
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.exit(SpringApplication.run(Application.class, args));
    }
}
```

# Using databases

By default, [Narayana Transactional driver](https://www.narayana.io/docs/api/com/arjuna/ats/jdbc/TransactionalDriver.html)
is used to enlist a relational database to a JTA transaction which provides a basic `javax.transaction.xa.XAResource`
enlistment and recovery as well as a simple pooling mechanism which is disabled as default. See
[TransactionalDriverProperties](narayana-spring-boot-core/src/main/java/dev/snowdrop/boot/narayana/core/properties/TransactionalDriverProperties.java)
for more details.

> Be aware that Narayana Transactional driver automatically set transaction isolation level to `java.sql.Connection.TRANSACTION_SERIALIZABLE`,
which might change default behaviour of the used database system!
For example, [Oracle Database](narayana-spring-boot-starter-it/src/test/resources/oracle-initscript.sql)

## Add pooling

If you need a more sophisticated connection management, we advise you to use [agroal-spring-boot-starter](https://agroal.github.io)
which provides connection pooling and many other features. To enable Agroal add the following dependency to your application configuration:
```xml
<dependency>
    <groupId>io.agroal</groupId>
    <artifactId>agroal-spring-boot-starter</artifactId>
    <version>2.x.x</version>
</dependency>
```

All Agroal configuration properties described in its [documentation](https://agroal.github.io/docs.html)

## Examples

For common database management systems, there are unit tests implemented with [testcontainers](https://testcontainers.com/)
to demonstrate the usage and the basic default configuration of `narayana-spring-boot-starter` in two use cases with
single `java.sql.Connection` (`<xxx>GenericRecoveryIT`) and pooled `java.sql.Connection` (`<xxx>PooledRecoveryIT`) managed
`java.sql.DataSource`.

Have a look at the following
[test cases](narayana-spring-boot-starter-it/src/test/java/dev/snowdrop/boot/narayana/testcontainers) for details.

# Using messaging brokers

This Narayana starter supports two ways to enlist a messaging broker to a JTA transaction: plain connection
factory and MessagingHub pooled connection factory.

By default, [Narayana Connection Proxy](https://www.narayana.io/docs/api/org/jboss/narayana/jta/jms/ConnectionFactoryProxy.html)
around the JMS connection factory is used which provides a basic XAResource enlistment and recovery.

## Add pooling

If you need a more sophisticated connection management, you can enable MessagingHub support which provides connection pooling
and many other features. To enable MessagingHub add the following dependency and property to you application configuration:
```xml
<dependency>
    <groupId>org.messaginghub</groupId>
    <artifactId>pooled-jms</artifactId>
</dependency>
```
```properties
narayana.messaginghub.enabled=true
```

All MessagingHub configuration properties described in its [documentation](https://github.com/messaginghub/pooled-jms/blob/master/pooled-jms-docs/Configuration.md)
are mapped with a prefix `narayana.messaginghub`. So for example if you'd like to set a max connections pool size to 10,
you could do that by adding this entry to your application configuration:
```properties
narayana.messaginghub.maxConnections=10
```

# Release Process

This repository uses an automated two-step process for releasing artifacts to Maven Central.

## Step 1: Prepare the Release Version

Before creating a GitHub Release, you need to manually prepare the release version:

1. **Update the version in `pom.xml`** files:
   - Change from `X.Y.Z-SNAPSHOT` to `X.Y.Z` (remove the `-SNAPSHOT` suffix)
   - Example: `1.2.3-SNAPSHOT` → `1.2.3`

2. **Commit and push to main**:
   ```bash
   git add pom.xml
   git commit -m "chore: prepare release X.Y.Z"
   git push origin main
   ```

## Step 2: Create a GitHub Release

1. Go to the [Releases section](../../releases) in GitHub.
2. Click **"Draft a new release"**.
3. In the **Tag version** field, enter the release version (e.g., `1.2.3`).
   - ⚠️ **Important**: The tag must match the version in `pom.xml`.
4. Fill in the **Release title** and **description**.
5. Click **"Publish release"**.

## What Happens Next?

### Automated Step 1: Publish to Maven Central

The `Publish package to the Maven Central Repository` is triggered. This workflow will automatically:
- Checkout the code at the release tag
- Verify that the `pom.xml` version matches the release tag
- Verify that the version is not a `-SNAPSHOT`
- Publish the release to Maven Central

### Automated Step 2: Bump to Next SNAPSHOT Version

Once the publish workflow completes successfully, the `Manual Version Bump to Next SNAPSHOT` is triggered. This workflow will automatically:
- Calculate the next `-SNAPSHOT` version (e.g., `1.2.4-SNAPSHOT`)
- Create a new branch called `bump-version-X.Y.Z-SNAPSHOT`
- Update the `pom.xml` with the new version
- Open a Pull Request with the version bump

## Step 3: Merge the Version Bump PR

1. Review the automatically created Pull Request.
2. If everything looks correct, **merge the PR**.
3. Your `main` branch will now be at the next `-SNAPSHOT` version, ready for development.

## Complete Flow Diagram

```
1. Manual: Update pom.xml (1.2.3-SNAPSHOT → 1.2.3)
   ↓
2. Manual: Commit and push to main
   ↓
3. Manual: Create GitHub Release with tag v1.2.3
   ↓
4. Automated: Publish workflow verifies and publishes 1.2.3 to Maven Central
   ↓
5. Automated: Bump-version workflow creates PR for 1.2.4-SNAPSHOT
   ↓
6. Manual: Review and merge the PR
   ↓
7. Done: main branch is now at 1.2.4-SNAPSHOT
```

## Important Notes

- ⚠️ The tag created in the GitHub release **must match** the version in `pom.xml` (e.g., tag `v1.2.3` for version `1.2.3`).
- ⚠️ The version in `pom.xml` must **not** contain `-SNAPSHOT` when creating the release.
- ✅ The publish workflow includes safety checks to prevent publishing incorrect versions.
- ✅ The PR for the version bump will only be created if the publish workflow succeeds.
- 🔧 Both workflows can also be triggered manually via `workflow_dispatch` if needed.


## Snapshot & debug release job (Post-Sonatype Migration)

This workflow is designed to release a snapshot on Maven Central and to validate under the hood that signing an artifact is working
It helps verify that critical aspects of the release process—such as Maven settings, credentials, GPG key import and usage, and artifact signing—are functioning correctly.

⚠️ Note: This workflow is not intended for production releases.
It is triggered manually via workflow_dispatch and can be reused for debugging or validation before running a full release.

Key steps in this job:

- Sets up the JDK and configures settings.xml to authenticate with Maven Central.
- Verifies that the GPG key has been correctly imported by signing a dummy file.
- Executes a mvn deploy using the release profile and GPG signing.
- Validates that the generated artifacts are properly signed.

