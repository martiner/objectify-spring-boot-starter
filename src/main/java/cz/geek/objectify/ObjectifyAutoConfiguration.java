package cz.geek.objectify;

import com.google.cloud.datastore.DatastoreOptions;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ClassUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@AutoConfiguration
@EnableConfigurationProperties(ObjectifyProperties.class)
public class ObjectifyAutoConfiguration {

    private static final String SECURITY_FILTER_CHAIN_CLASS = "org.springframework.security.web.SecurityFilterChain";

    /** Fallback project id for the emulator, whose project is arbitrary (it is not matched against the client). */
    private static final String DEFAULT_EMULATOR_PROJECT = "test";

    @Bean
    @ConditionalOnMissingBean
    public ObjectifyFactory objectifyFactory(
            ObjectifyProperties props,
            ObjectProvider<ObjectifyEntityProvider> providers,
            ObjectProvider<ObjectifyEntityScanPackages> scanPackagesProvider,
            BeanFactory beanFactory) {

        ObjectifyFactory factory;
        if (props.getPort() == -1) {
            factory = new ObjectifyFactory();
        } else {
            String project = props.getProject().isBlank() ? DEFAULT_EMULATOR_PROJECT : props.getProject();
            factory = new ObjectifyFactory(
                    DatastoreOptions.newBuilder()
                            .setHost("http://localhost:" + props.getPort())
                            .setProjectId(project)
                            .build()
                            .getService()
            );
        }

        Set<Class<?>> entities = new LinkedHashSet<>();

        providers.forEach(provider -> entities.addAll(provider.getEntities()));

        if (props.isEntityScanEnabled()) {
            ObjectifyEntityScanPackages scanPackages = scanPackagesProvider.getIfAvailable();
            List<String> packages;
            if (scanPackages != null) {
                packages = scanPackages.getPackages();
            } else if (AutoConfigurationPackages.has(beanFactory)) {
                packages = AutoConfigurationPackages.get(beanFactory);
            } else {
                packages = List.of();
            }
            entities.addAll(new ObjectifyEntityScanner().scan(packages));
        }

        for (Class<?> cls : entities) {
            factory.register(cls);
        }

        ObjectifyService.init(factory);

        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "objectifyFilter")
    public FilterRegistrationBean<ObjectifyFilter> objectifyFilter(ObjectifyFactory factory, ObjectifyProperties props) {
        FilterRegistrationBean<ObjectifyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ObjectifyFilter(factory));
        registration.addUrlPatterns("/*");
        Integer order = resolveFilterOrder(props);
        if (order != null) {
            registration.setOrder(order);
        }
        return registration;
    }

    private Integer resolveFilterOrder(ObjectifyProperties props) {
        if (props.getFilterOrder() != null) {
            return props.getFilterOrder();
        }
        if (ClassUtils.isPresent(SECURITY_FILTER_CHAIN_CLASS, getClass().getClassLoader())) {
            // Spring Security is on the classpath — run the Objectify session filter before
            // Spring Security's filter chain so the session is open during security processing.
            return SecurityFilterProperties.DEFAULT_FILTER_ORDER - 1;
        }
        return null;
    }
}
