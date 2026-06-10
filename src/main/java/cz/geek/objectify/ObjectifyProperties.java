package cz.geek.objectify;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "objectify")
public class ObjectifyProperties {

    /** Datastore emulator port. {@code -1} means production (no emulator). */
    private int port = -1;

    /**
     * GCP project ID. For the emulator this is optional and defaults to {@code test};
     * in production the ambient GCP project is used.
     */
    private String project = "";

    /**
     * Servlet filter order. When Spring Security is on the classpath this defaults to
     * just before Spring Security's filter chain
     * ({@code spring.security.filter.order - 1}); otherwise unordered.
     * Set explicitly to override.
     */
    private Integer filterOrder;

    /** Whether to register the Objectify servlet filter. Defaults to {@code true}. */
    private boolean filterEnabled = true;

    /** Whether to scan the classpath for {@code @Entity} classes. Defaults to {@code true}. */
    private boolean entityScanEnabled = true;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public Integer getFilterOrder() {
        return filterOrder;
    }

    public void setFilterOrder(Integer filterOrder) {
        this.filterOrder = filterOrder;
    }

    public boolean isFilterEnabled() {
        return filterEnabled;
    }

    public void setFilterEnabled(boolean filterEnabled) {
        this.filterEnabled = filterEnabled;
    }

    public boolean isEntityScanEnabled() {
        return entityScanEnabled;
    }

    public void setEntityScanEnabled(boolean entityScanEnabled) {
        this.entityScanEnabled = entityScanEnabled;
    }
}
