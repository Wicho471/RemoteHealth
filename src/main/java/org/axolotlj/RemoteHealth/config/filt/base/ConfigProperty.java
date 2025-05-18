package org.axolotlj.RemoteHealth.config.filt.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Anotación para mapear un campo a una propiedad de configuración.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigProperty {
    String value(); // llave en el archivo properties
}
