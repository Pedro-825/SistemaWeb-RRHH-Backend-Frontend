package com.rrhh.Shared.prototype;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PrototypeRegistry {

    private final Map<String, Prototype<?>> prototypes = new HashMap<>();

    public void register(String key, Prototype<?> prototype) {
        prototypes.put(key, prototype);
    }

    @SuppressWarnings("unchecked")
    public <T extends Prototype<T>> T createClone(String key) {
        Prototype<?> prototype = prototypes.get(key);
        if (prototype == null) {
            throw new IllegalArgumentException("No existe un prototipo registrado con la clave: " + key);
        }
        return (T) prototype.clonePrototype();
    }
}
