# objectify-spring-boot-starter

Spring Boot auto-configuration for [Objectify](https://github.com/objectify/objectify) (Cloud Datastore).

Targets Spring Boot 4.x.

## Usage

Add the dependency:

```xml
<dependency>
    <groupId>cz.geek</groupId>
    <artifactId>objectify-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

The starter auto-configures an `ObjectifyFactory` bean and registers a servlet filter that opens a per-request Objectify session. Both `factory.ofy()` (injected factory bean) and the static `ObjectifyService.ofy()` work — they share the same factory instance.

## Configuration

| Property | Default | Description |
|---|---|---|
| `objectify.port` | `-1` | Datastore emulator port. `-1` = production. |
| `objectify.project` | `` | GCP project ID (required for emulator). |
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

## Releasing

```bash
./mvnw release:prepare
./mvnw release:perform
```
