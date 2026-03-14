package aq.project.util.containers;

import org.testcontainers.postgresql.PostgreSQLContainer;

public abstract class Containers {

    public static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer("postgres:18-alpine");
}
