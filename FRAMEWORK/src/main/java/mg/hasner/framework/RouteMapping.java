package mg.hasner.framework;

import java.lang.reflect.Method;

public class RouteMapping {
    private final UrlMethod urlMethod;
    private final Class<?> controllerClass;
    private final Method method;

    public RouteMapping(String url, HttpMethod httpMethod, Class<?> controllerClass, Method method) {
        this.urlMethod = new UrlMethod(url, httpMethod);
        this.controllerClass = controllerClass;
        this.method = method;
    }

    public UrlMethod getUrlMethod() {
        return urlMethod;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getMethod() {
        return method;
    }

    public String getUrl() {
        return urlMethod.getUrl();
    }

    public HttpMethod getHttpMethod() {
        return urlMethod.getMethod();
    }

    @Override
    public String toString() {
        return urlMethod + " → " + controllerClass.getSimpleName() + "." + method.getName() + "()";
    }
}