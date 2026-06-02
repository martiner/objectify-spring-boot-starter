package cz.geek.objectify.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * {@code @SpringBootConfiguration} for slice tests. Its package
 * ({@code cz.geek.objectify.test}) is used as the entity scan base package, so
 * {@link SampleEntity} is discovered automatically by {@code @ObjectifyTest}.
 */
@SpringBootApplication
public class TestApplication {
}
