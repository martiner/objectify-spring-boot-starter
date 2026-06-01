package cz.geek.objectify;

import java.util.Arrays;
import java.util.List;

class ObjectifyEntityScanPackages {

    private final List<String> packages;

    ObjectifyEntityScanPackages(String... packages) {
        this.packages = Arrays.asList(packages);
    }

    List<String> getPackages() {
        return packages;
    }
}
