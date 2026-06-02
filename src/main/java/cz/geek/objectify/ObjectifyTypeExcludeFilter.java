package cz.geek.objectify;

import org.springframework.boot.test.context.filter.annotation.StandardAnnotationCustomizableTypeExcludeFilter;

/**
 * {@code TypeExcludeFilter} for {@link ObjectifyTest @ObjectifyTest}.
 */
public final class ObjectifyTypeExcludeFilter extends StandardAnnotationCustomizableTypeExcludeFilter<ObjectifyTest> {

	ObjectifyTypeExcludeFilter(Class<?> testClass) {
		super(testClass);
	}

}
