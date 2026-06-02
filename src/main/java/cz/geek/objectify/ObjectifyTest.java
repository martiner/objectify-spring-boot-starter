package cz.geek.objectify;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.filter.annotation.TypeExcludeFilters;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Annotation for an Objectify test that focuses <strong>only</strong> on Objectify
 * components.
 * <p>
 * Using this annotation disables full auto-configuration and applies only
 * {@link ObjectifyAutoConfiguration}, exposing a ready-to-use {@code ObjectifyFactory}
 * bean (entities scanned, {@code ObjectifyService} initialized). Regular {@code @Component},
 * {@code @Service} and {@code @Controller} beans are <strong>not</strong> scanned; use
 * {@link #includeFilters()} or {@code @Import} to add specific beans.
 * <p>
 * Entities are discovered from the base package of the application's
 * {@code @SpringBootConfiguration}, just like {@code @DataJpaTest} discovers JPA entities.
 * <p>
 * Point the test at the Datastore emulator with {@code objectify.port} (via
 * {@link #properties()} or the environment); {@code objectify.project} is optional and
 * defaults to {@code test}.
 *
 * @see ObjectifyAutoConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@BootstrapWith(ObjectifyTestContextBootstrapper.class)
@ExtendWith(SpringExtension.class)
@OverrideAutoConfiguration(enabled = false)
@TypeExcludeFilters(ObjectifyTypeExcludeFilter.class)
@ImportAutoConfiguration(ObjectifyAutoConfiguration.class)
public @interface ObjectifyTest {

	/**
	 * Properties in form {@literal key=value} that should be added to the Spring
	 * environment before the test runs.
	 * @return the properties to add
	 */
	String[] properties() default {};

	/**
	 * Determines if default filtering should be used with
	 * {@code @SpringBootApplication}. By default no beans are included.
	 * @see #includeFilters()
	 * @see #excludeFilters()
	 * @return if default filters should be used
	 */
	boolean useDefaultFilters() default true;

	/**
	 * A set of include filters which can be used to add otherwise filtered beans to the
	 * application context.
	 * @return include filters to apply
	 */
	Filter[] includeFilters() default {};

	/**
	 * A set of exclude filters which can be used to filter beans that would otherwise be
	 * added to the application context.
	 * @return exclude filters to apply
	 */
	Filter[] excludeFilters() default {};
}
