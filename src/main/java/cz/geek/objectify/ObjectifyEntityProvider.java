package cz.geek.objectify;

import java.util.Collection;

public interface ObjectifyEntityProvider {

    Collection<Class<?>> getEntities();
}
