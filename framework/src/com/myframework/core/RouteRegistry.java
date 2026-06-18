package com.myframework.core;

import com.myframework.annotations.WebRoute;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class RouteRegistry {
    public static final class Route {
        public final String httpMethod;
        public final String path;
        public final Object instance;
        public final Method method;
        public Route(String httpMethod, String path, Object instance, Method method) {
            this.httpMethod = httpMethod;
            this.path = path;
            this.instance = instance;
            this.method = method;
        }
    }

    private final Map<String, Route> routes = new LinkedHashMap<>(); // key = METHOD + " " + path

    private String key(String httpMethod, String path) {
        return httpMethod.toUpperCase(Locale.ROOT) + " " + normalize(path);
    }

    private String normalize(String path) {
        if (path == null || path.isEmpty()) return "/";
        if (!path.startsWith("/")) path = "/" + path;
        // remove trailing slash except root
        if (path.length() > 1 && path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return path;
    }

    public void register(String httpMethod, String path, Object instance, Method method) {
        routes.put(key(httpMethod, path), new Route(httpMethod.toUpperCase(Locale.ROOT), normalize(path), instance, method));
    }

    public void discoverFrom(Class<?> controllerClass) {
        try {
            Object instance = controllerClass.getDeclaredConstructor().newInstance();
            for (Method m : controllerClass.getDeclaredMethods()) {
                WebRoute wr = m.getAnnotation(WebRoute.class);
                if (wr != null) {
                    m.setAccessible(true);
                    register(wr.method(), wr.value(), instance, m);
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to instantiate controller: " + controllerClass.getName(), e);
        }
    }

    public Route find(String httpMethod, String path) {
        return routes.get(key(httpMethod, path));
    }

    public Collection<Route> all() {
        return routes.values();
    }

    public static void invoke(Route route, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Class<?>[] params = route.method.getParameterTypes();
        if (params.length == 2 && HttpServletRequest.class.isAssignableFrom(params[0]) && HttpServletResponse.class.isAssignableFrom(params[1])) {
            route.method.invoke(route.instance, req, resp);
        } else if (params.length == 1 && HttpServletResponse.class.isAssignableFrom(params[0])) {
            route.method.invoke(route.instance, resp);
        } else if (params.length == 0) {
            Object result = route.method.invoke(route.instance);
            if (result != null) {
                resp.setContentType("text/plain;charset=UTF-8");
                resp.getWriter().print(result.toString());
            }
        } else {
            throw new IllegalStateException("Unsupported handler signature for method: " + route.method);
        }
    }
}
