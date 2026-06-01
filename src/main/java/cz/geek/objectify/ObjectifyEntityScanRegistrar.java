package cz.geek.objectify;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

class ObjectifyEntityScanRegistrar implements ImportBeanDefinitionRegistrar {

    static final String BEAN_NAME = "objectifyEntityScanPackages";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attrs = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(ObjectifyEntityScan.class.getName()));
        if (attrs == null) {
            return;
        }

        Set<String> packages = new LinkedHashSet<>();
        packages.addAll(Arrays.asList(attrs.getStringArray("basePackages")));
        for (Class<?> cls : attrs.getClassArray("basePackageClasses")) {
            packages.add(cls.getPackage().getName());
        }

        if (!packages.isEmpty()) {
            addPackages(registry, packages);
        }
    }

    private static void addPackages(BeanDefinitionRegistry registry, Set<String> newPackages) {
        if (registry.containsBeanDefinition(BEAN_NAME)) {
            BeanDefinition bd = registry.getBeanDefinition(BEAN_NAME);
            ConstructorArgumentValues.ValueHolder holder =
                    bd.getConstructorArgumentValues().getIndexedArgumentValue(0, String[].class);
            Set<String> merged = new LinkedHashSet<>();
            if (holder != null) {
                merged.addAll(Arrays.asList((String[]) holder.getValue()));
            }
            merged.addAll(newPackages);
            bd.getConstructorArgumentValues().clear();
            bd.getConstructorArgumentValues().addIndexedArgumentValue(0, merged.toArray(new String[0]));
        } else {
            GenericBeanDefinition bd = new GenericBeanDefinition();
            bd.setBeanClass(ObjectifyEntityScanPackages.class);
            bd.getConstructorArgumentValues().addIndexedArgumentValue(0, newPackages.toArray(new String[0]));
            bd.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(BEAN_NAME, bd);
        }
    }
}
