package cz.geek.objectify;

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.util.Closeable;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

public class ObjectifyFilter implements Filter {

    private final ObjectifyFactory factory;

    public ObjectifyFilter(ObjectifyFactory factory) {
        this.factory = factory;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        try (Closeable ignored = factory.begin()) {
            chain.doFilter(req, resp);
        }
    }
}
