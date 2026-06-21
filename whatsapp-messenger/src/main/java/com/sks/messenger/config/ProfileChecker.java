package com.sks.messenger.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ProfileChecker implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProfileChecker.class);

    private final Environment env;

    public ProfileChecker(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String[] active = env.getActiveProfiles();
        if (active == null || active.length == 0) {
            active = env.getDefaultProfiles();
        }
        log.info("Active Spring profiles: {}", Arrays.toString(active));

        for (String p : active) {
            if ("mysql".equalsIgnoreCase(p)) {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    log.info("MySQL driver available on classpath");
                } catch (ClassNotFoundException e) {
                    String msg = "MySQL profile is active but the MySQL JDBC driver (com.mysql.cj.jdbc.Driver) is not on the classpath. "
                            + "Either add the driver dependency to your build (mysql-connector-java) or run the application with the 'dev' profile for H2: -Dspring.profiles.active=dev";
                    log.error(msg, e);
                    throw new IllegalStateException(msg, e);
                }
            }
        }
    }
}
