package org.axolotlj.RemoteHealth.config.filt.base;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Objects;

import org.axolotlj.RemoteHealth.config.PropertiesManager;

public abstract class BaseFiltersConfig {

	protected PropertiesManager propertiesManager;

	public BaseFiltersConfig(Path configPath) {
		propertiesManager = new PropertiesManager(configPath);
		loadProperties();
	}

	public void loadProperties() {
		for (Field field : this.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(ConfigProperty.class)) {
				ConfigProperty cp = field.getAnnotation(ConfigProperty.class);
				String key = cp.value();
				String value = propertiesManager.get(key);
				if (value != null && !value.isEmpty()) {
					try {
						field.setAccessible(true);
						if (field.getType() == int.class)
							field.setInt(this, Integer.parseInt(value));
						else if (field.getType() == double.class)
							field.setDouble(this, Double.parseDouble(value));
						else if (field.getType() == boolean.class)
							field.setBoolean(this, Boolean.parseBoolean(value));
						else if (field.getType() == String.class)
							field.set(this, value);
					} catch (Exception e) {
						System.err.println("Error cargando propiedad " + key + ": " + e.getMessage());
					}
				}
			}
		}
	}

	public void saveProperties() {
		for (Field field : this.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(ConfigProperty.class)) {
				ConfigProperty cp = field.getAnnotation(ConfigProperty.class);
				try {
					field.setAccessible(true);
					Object value = field.get(this);
					propertiesManager.set(cp.value(), Objects.toString(value, ""));
				} catch (Exception e) {
					System.err.println("Error guardando propiedad " + cp.value() + ": " + e.getMessage());
				}
			}
		}
		propertiesManager.saveAll();
	}
}
