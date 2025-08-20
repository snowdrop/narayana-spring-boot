gpg --local-user amunozhe@redhat.com --armor --detach-sign --status-fd 1 --batch --no-tty --output /Users/auri/Code/narayana-spring-boot/narayana-spring-boot-core/target/narayana-spring-boot-core-3.4.1.jar.asc /Users/auri/Code/narayana-spring-boot/narayana-spring-boot-core/target/narayana-spring-boot-core-3.4.1.jar


<profile>
      <id>release</id>
<!--      <distributionManagement>-->
<!--        <snapshotRepository>-->
<!--          <id>ossrh</id>-->
<!--          <url>https://oss.sonatype.org/content/repositories/snapshots</url>-->
<!--        </snapshotRepository>-->
<!--        <repository>-->
<!--          <id>ossrh</id>-->
<!--          <url>https://oss.sonatype.org/service/local/staging/deploy/maven2/</url>-->
<!--        </repository>-->
<!--      </distributionManagement>-->
      <build>
        <plugins>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-source-plugin</artifactId>
            <executions>
              <execution>
                <id>attach-sources</id>
                <goals>
                  <goal>jar-no-fork</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-javadoc-plugin</artifactId>
            <executions>
              <execution>
                <id>attach-javadocs</id>
                <goals>
                  <goal>jar</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-gpg-plugin</artifactId>
            <version>${maven-gpg-plugin.version}</version>
            <!--
              Needed to bypass the encoding of the passphrase when task is executed by a job and not a human
              Syntax could change with future releases of the plugin (after 3.0.0)
            -->
            <configuration>
              <useAgent>true</useAgent>
              <passphrase>${env.GPG_PASSPHRASE}</passphrase>
<!--              <gpgArguments>-->
<!--                <arg>&#45;&#45;batch</arg>-->
<!--                <arg>&#45;&#45;pinentry-mode</arg>-->
<!--                <arg>loopback</arg>-->
<!--              </gpgArguments>-->
              <keyname>amunozhe@redhat.com</keyname>
            </configuration>
            <executions>
              <execution>
                <id>sign-artifacts</id>
                <phase>verify</phase>
                <goals>
                  <goal>sign</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
<!--          <plugin>-->
<!--            <groupId>org.apache.maven.plugins</groupId>-->
<!--            <artifactId>maven-release-plugin</artifactId>-->
<!--            <configuration>-->
<!--              <autoVersionSubmodules>true</autoVersionSubmodules>-->
<!--              <useReleaseProfile>false</useReleaseProfile>-->
<!--              <releaseProfiles>release</releaseProfiles>-->
<!--              <goals>deploy</goals>-->
<!--            </configuration>-->
<!--          </plugin>-->
          <plugin>
            <groupId>org.sonatype.central</groupId>
            <artifactId>central-publishing-maven-plugin</artifactId>
            <version>${central-publishing-maven-plugin.version}</version>
            <extensions>true</extensions>
            <configuration>
              <publishingServerId>central</publishingServerId>
              <autoPublish>false</autoPublish>
<!--              <waitUntil>published</waitUntil>-->
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>

    MAVEN_GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}

    -Dgpg.passphrase=