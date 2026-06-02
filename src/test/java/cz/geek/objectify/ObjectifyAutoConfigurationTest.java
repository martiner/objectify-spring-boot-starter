package cz.geek.objectify;

import com.googlecode.objectify.ObjectifyFactory;
import cz.geek.objectify.other.OtherEntity;
import cz.geek.objectify.test.SampleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectifyAutoConfigurationTest {

    private ApplicationContextRunner baseRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(ObjectifyAutoConfiguration.class))
                .withPropertyValues("objectify.port=8484", "objectify.project=test-project");
    }

    @Test
    void portWithoutProjectFailsWithClearMessage() {
        new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(ObjectifyAutoConfiguration.class))
                .withPropertyValues("objectify.port=8484")
                .run(ctx -> assertThat(ctx).getFailure()
                        .hasMessageContaining("objectify.project"));
    }

    @Test
    void beansAreCreated() {
        baseRunner().run(ctx -> {
            assertThat(ctx).hasSingleBean(ObjectifyFactory.class);
            assertThat(ctx).hasSingleBean(FilterRegistrationBean.class);
        });
    }

    @Test
    void propertiesAreBound() {
        baseRunner()
                .withPropertyValues("objectify.port=8282", "objectify.project=my-project", "objectify.filter-order=10")
                .run(ctx -> {
                    ObjectifyProperties props = ctx.getBean(ObjectifyProperties.class);
                    assertThat(props.getPort()).isEqualTo(8282);
                    assertThat(props.getProject()).isEqualTo("my-project");
                    assertThat(props.getFilterOrder()).isEqualTo(10);
                });
    }

    @Test
    void rule1_entityProviderIsRegistered() {
        baseRunner()
                .withUserConfiguration(SampleEntityProviderConfig.class)
                .run(ctx -> {
                    ObjectifyFactory factory = ctx.getBean(ObjectifyFactory.class);
                    assertThatCode(() -> factory.getMetadata(SampleEntity.class)).doesNotThrowAnyException();
                });
    }

    @Test
    void rule2_defaultPackageScanFindsEntity() {
        baseRunner()
                .withInitializer(appCtx -> AutoConfigurationPackages.register(
                        (BeanDefinitionRegistry) appCtx,
                        "cz.geek.objectify.test"))
                .run(ctx -> {
                    ObjectifyFactory factory = ctx.getBean(ObjectifyFactory.class);
                    assertThatCode(() -> factory.getMetadata(SampleEntity.class)).doesNotThrowAnyException();
                });
    }

    @Test
    void rule2_entityScanAnnotationScansSpecifiedPackage() {
        baseRunner()
                .withUserConfiguration(EntityScanConfig.class)
                .run(ctx -> {
                    ObjectifyFactory factory = ctx.getBean(ObjectifyFactory.class);
                    assertThatCode(() -> factory.getMetadata(OtherEntity.class)).doesNotThrowAnyException();
                });
    }

    @Test
    void rule3_scanDisabledSkipsPackageEntities() {
        baseRunner()
                .withInitializer(appCtx -> AutoConfigurationPackages.register(
                        (BeanDefinitionRegistry) appCtx,
                        "cz.geek.objectify.test"))
                .withPropertyValues("objectify.entity-scan-enabled=false")
                .run(ctx -> {
                    ObjectifyFactory factory = ctx.getBean(ObjectifyFactory.class);
                    assertThatThrownBy(() -> factory.getMetadata(SampleEntity.class))
                            .isInstanceOf(IllegalArgumentException.class);
                });
    }

    @Test
    void rule3_scanDisabledStillRegistersProviderEntities() {
        baseRunner()
                .withUserConfiguration(SampleEntityProviderConfig.class)
                .withPropertyValues("objectify.entity-scan-enabled=false")
                .run(ctx -> {
                    ObjectifyFactory factory = ctx.getBean(ObjectifyFactory.class);
                    assertThatCode(() -> factory.getMetadata(SampleEntity.class)).doesNotThrowAnyException();
                });
    }

    @Test
    void rule2_multipleEntityScanAnnotationsMerge() {
        baseRunner()
                .withUserConfiguration(EntityScanConfig.class, AnotherEntityScanConfig.class)
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    ObjectifyFactory factory = ctx.getBean(ObjectifyFactory.class);
                    assertThatCode(() -> factory.getMetadata(OtherEntity.class)).doesNotThrowAnyException();
                    assertThatCode(() -> factory.getMetadata(SampleEntity.class)).doesNotThrowAnyException();
                });
    }

    @Test
    void filterOrderIsSet() {
        baseRunner()
                .withPropertyValues("objectify.filter-order=5")
                .run(ctx -> {
                    @SuppressWarnings("unchecked")
                    FilterRegistrationBean<ObjectifyFilter> filter =
                            (FilterRegistrationBean<ObjectifyFilter>) ctx.getBean(FilterRegistrationBean.class);
                    assertThat(filter.getOrder()).isEqualTo(5);
                });
    }

    @Test
    void filterDefaultsBeforeSecurityWhenSecurityOnClasspath() {
        // spring-security-web is on the test classpath, so with no explicit order the filter
        // should default to running just before Spring Security's filter chain.
        baseRunner().run(ctx -> {
            @SuppressWarnings("unchecked")
            FilterRegistrationBean<ObjectifyFilter> filter =
                    (FilterRegistrationBean<ObjectifyFilter>) ctx.getBean(FilterRegistrationBean.class);
            assertThat(filter.getOrder())
                    .isEqualTo(SecurityFilterProperties.DEFAULT_FILTER_ORDER - 1);
        });
    }

    @Test
    void explicitFilterOrderOverridesSecurityDefault() {
        baseRunner()
                .withPropertyValues("objectify.filter-order=42")
                .run(ctx -> {
                    @SuppressWarnings("unchecked")
                    FilterRegistrationBean<ObjectifyFilter> filter =
                            (FilterRegistrationBean<ObjectifyFilter>) ctx.getBean(FilterRegistrationBean.class);
                    assertThat(filter.getOrder()).isEqualTo(42);
                });
    }

    @Configuration
    static class SampleEntityProviderConfig {
        @Bean
        ObjectifyEntityProvider sampleEntityProvider() {
            return () -> List.of(SampleEntity.class);
        }
    }

    @Configuration
    @ObjectifyEntityScan(basePackages = "cz.geek.objectify.other")
    static class EntityScanConfig {
    }

    @Configuration
    @ObjectifyEntityScan(basePackages = "cz.geek.objectify.test")
    static class AnotherEntityScanConfig {
    }
}
