package cz.geek.objectify;

import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.test.context.TestContextBootstrapper;

/**
 * {@link TestContextBootstrapper} for {@link ObjectifyTest @ObjectifyTest} support.
 */
class ObjectifyTestContextBootstrapper extends SpringBootTestContextBootstrapper {

	@Override
	protected String[] getProperties(Class<?> testClass) {
		ObjectifyTest annotation = TestContextAnnotationUtils.findMergedAnnotation(testClass, ObjectifyTest.class);
		return (annotation != null) ? annotation.properties() : null;
	}

}
