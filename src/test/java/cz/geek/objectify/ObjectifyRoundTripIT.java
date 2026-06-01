package cz.geek.objectify;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectifyRoundTripIT {

    private final int port = Integer.parseInt(System.getProperty("objectify.port", "8484"));

    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ObjectifyAutoConfiguration.class))
                .withPropertyValues("objectify.port=" + port, "objectify.project=test-project")
                .withUserConfiguration(EntityConfig.class);
    }

    @Test
    void saveAndLoad() {
        runner().run(ctx -> {
            assertThat(ctx).hasNotFailed();
            com.googlecode.objectify.ObjectifyFactory factory = ctx.getBean(com.googlecode.objectify.ObjectifyFactory.class);

            Long savedId;
            try (Closeable ignored = factory.begin()) {
                Key<RoundTripEntity> key = ObjectifyService.ofy().save().entity(new RoundTripEntity("hello")).now();
                savedId = key.getId();
            }

            try (Closeable ignored = factory.begin()) {
                RoundTripEntity loaded = ObjectifyService.ofy().load().type(RoundTripEntity.class).id(savedId).now();
                assertThat(loaded).isNotNull();
                assertThat(loaded.value).isEqualTo("hello");
            }
        });
    }

    @Configuration
    static class EntityConfig {
        @Bean
        ObjectifyEntityProvider roundTripEntityProvider() {
            return () -> List.of(RoundTripEntity.class);
        }
    }
}
