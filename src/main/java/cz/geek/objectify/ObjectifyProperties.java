package cz.geek.objectify;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "objectify")
public class ObjectifyProperties {

    private int port = -1;
    private String project = "";
    private Integer filterOrder;
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

    public boolean isEntityScanEnabled() {
        return entityScanEnabled;
    }

    public void setEntityScanEnabled(boolean entityScanEnabled) {
        this.entityScanEnabled = entityScanEnabled;
    }
}
