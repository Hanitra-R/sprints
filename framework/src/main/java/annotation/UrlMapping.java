package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import util.MethodeType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlMapping {
<<<<<<< Updated upstream:FRAMEWORK/src/main/java/mg/hasner/framework/UrlMapping.java
    String value();
=======
    String path();
    MethodeType method();
>>>>>>> Stashed changes:framework/src/main/java/annotation/UrlMapping.java
}
