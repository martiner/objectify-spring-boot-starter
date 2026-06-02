package cz.geek.objectify.test;

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.util.Closeable;
import cz.geek.objectify.ObjectifyTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link ObjectifyTest @ObjectifyTest} boots a minimal context which
 * exposes a configured {@link ObjectifyFactory}, scans entities from the
 * {@code @SpringBootConfiguration} base package, and can round-trip against the emulator.
 */
@ObjectifyTest
class ObjectifyTestSliceIT {

	@Autowired
	private ObjectifyFactory factory;

	@Test
	void exposesFactoryThatPersistsScannedEntity() {
		Long id;
		try (Closeable ignored = factory.begin()) {
			id = factory.ofy().save().entity(new SampleEntity()).now().getId();
		}
		try (Closeable ignored = factory.begin()) {
			SampleEntity loaded = factory.ofy().load().type(SampleEntity.class).id(id).now();
			assertThat(loaded).isNotNull();
			assertThat(loaded.getId()).isEqualTo(id);
		}
	}
}
