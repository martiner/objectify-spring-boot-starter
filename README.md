# objectify-spring-boot-starter

Spring Boot auto-configuration for [Objectify](https://github.com/objectify/objectify) (Cloud Datastore).

Targets Spring Boot 4.x.

## Usage

Add the dependency:

```xml
<dependency>
    <groupId>cz.geek</groupId>
    <artifactId>objectify-spring-boot-starter</artifactId>
    <version>0.2.0</version>
</dependency>
```

The starter auto-configures an `ObjectifyFactory` bean and registers a servlet filter that opens a per-request Objectify session. Both `factory.ofy()` (injected factory bean) and the static `ObjectifyService.ofy()` work — they share the same factory instance.

## Configuration

| Property | Default | Description |
|---|---|---|
| `objectify.port` | `-1` | Datastore emulator port. `-1` = production. |
| `objectify.project` | `` | GCP project ID. For the emulator it is optional and defaults to `test` (the emulator does not match it against the client); in production the ambient GCP project is used. |
| `objectify.filter-enabled` | `true` | Register the Objectify servlet filter. Set to `false` to disable it — useful when you want to manage sessions yourself (e.g. call `factory.begin()` around units of work, or register your own filter). |
| `objectify.filter-order` | auto | Servlet filter order. When Spring Security is on the classpath this defaults to just before Spring Security's filter chain (`SecurityFilterProperties.DEFAULT_FILTER_ORDER - 1`); otherwise unordered. Set explicitly to override. |
| `objectify.entity-scan-enabled` | `true` | Enable classpath scanning for `@Entity` classes. Set to `false` to rely solely on `ObjectifyEntityProvider` beans. |

## Registering entities

There are three ways to register entities. They combine additively: programmatic providers (option 3) are always registered, and classpath scanning (option 1 *or* 2) adds to them.

### Option 1 — Automatic classpath scan (zero config)

By default the starter scans the application's base package (determined by `@SpringBootApplication`) for classes annotated with `@com.googlecode.objectify.annotation.Entity`. If your entities live under the application package, you need to do nothing.

### Option 2 — `@ObjectifyEntityScan` (custom packages)

Use this when entities live outside the application's base package. The annotation **replaces** the default package scan — it does not add to it, so list every package that contains entities, including the application's own base package if needed:

```java
@ObjectifyEntityScan(basePackages = {"com.example", "com.example.entities"})
@SpringBootApplication
public class MyApp { ... }
```

To skip classpath scanning entirely (e.g. for faster startup) and rely solely on option 3, set `objectify.entity-scan-enabled=false`.

### Option 3 — Programmatic provider

Register entity classes explicitly via an `ObjectifyEntityProvider` Spring bean:

```java
@Component
public class MyEntities implements ObjectifyEntityProvider {
    @Override
    public Collection<Class<?>> getEntities() {
        return List.of(MyEntity.class, OtherEntity.class);
    }
}
```

> **Note:** If you define your own `ObjectifyFactory` bean, the auto-configuration backs off entirely (including entity registration and `ObjectifyService.init`). Your bean takes full responsibility.

## Testing — `@ObjectifyTest` slice

`@ObjectifyTest` is a test slice in the spirit of `@DataJpaTest`: it disables full
auto-configuration and applies only `ObjectifyAutoConfiguration`, giving you a ready-to-use
`ObjectifyFactory` bean (entities scanned, `ObjectifyService` initialized). Regular
`@Component` / `@Service` / `@Controller` beans are **not** loaded, so the context stays
small and your application's other configuration (security, mail, …) is not required.

Entities are discovered automatically from the base package of your application's
`@SpringBootConfiguration` — exactly like `@DataJpaTest` finds `@Entity` classes.

The slice support lives in the main artifact but its compile dependencies
(`spring-boot-test`, `spring-test`, JUnit) are `optional`; you get them on the test
classpath via `spring-boot-starter-test`.

```java
@ObjectifyTest
class MyEntityRepositoryTest {

    @Autowired
    ObjectifyFactory factory;

    @Test
    void savesAndLoads() {
        try (Closeable ignored = factory.begin()) {
            Key<MyEntity> key = factory.ofy().save().entity(new MyEntity("a")).now();
            assertThat(factory.ofy().load().key(key).now()).isNotNull();
        }
    }
}
```

It works the same with Kotest (constructor injection via `SpringAutowireConstructorExtension`):

```kotlin
@ObjectifyTest
class MyEntityRepositoryIT(factory: ObjectifyFactory) : FreeSpec({
    val repo = MyEntityRepository(factory)
    "loads by id" {
        factory.begin().use { /* ... repo.load(id) ... */ }
    }
})
```

Point the test at the emulator with `objectify.port` (via the `properties` attribute or
the environment); `objectify.project` is optional and defaults to `test`.

Your own repository/`@Service` beans are not in the slice by default. Pull in specific
ones when you need them injected:

```java
@ObjectifyTest(includeFilters = @Filter(type = ASSIGNABLE_TYPE, classes = MyEntityRepository.class))
```

or with `@Import(MyEntityRepository.class)` — otherwise just instantiate them with the
injected factory.

## Releasing

```bash
./mvnw release:prepare
./mvnw release:perform
```
