package org.ihtsdo.tk.api.test;

import java.util.HashMap;

/**
 * A common store for references to generate test objects (artefacts).
 * 
 * This assists in the storing and retrieval of test artefacts, such as generated ids, component objects, etc, 
 * rather than needing to define member variables or pass references for each.
 */
public class TestArtefact {

    private static HashMap<String, Object> artefacts = new HashMap<>(); 
    
    public static void set(String key, Object obj) {
        artefacts.put(key, obj);
    }
    
    public static <T> T get(String key, Class<T> type) {
        Object obj = artefacts.get(key);
        if (obj == null) throw new RuntimeException("No test artefact available: " + key);
        return type.cast(obj);
    }
}
