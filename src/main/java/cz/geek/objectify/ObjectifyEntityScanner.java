package cz.geek.objectify;

import com.googlecode.objectify.annotation.Entity;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

class ObjectifyEntityScanner {

    private final ClassPathScanningCandidateComponentProvider scanner;

    ObjectifyEntityScanner() {
        scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
    }

    Set<Class<?>> scan(Collection<String> packages) {
        ClassLoader classLoader = scanner.getResourceLoader().getClassLoader();
        Set<Class<?>> classes = new LinkedHashSet<>();
        for (String pkg : packages) {
            scanner.findCandidateComponents(pkg).forEach(bd -> {
                try {
                    classes.add(ClassUtils.forName(bd.getBeanClassName(), classLoader));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Cannot load entity class: " + bd.getBeanClassName(), e);
                }
            });
        }
        return classes;
    }
}
