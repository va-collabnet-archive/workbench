package org.dwfa.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EnumMap implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Map<Enum<?>, Object> map = new HashMap<Enum<?> , Object>();
	
	public Object get(Enum<?> key) {
		return map.get(key);
	}
	
	public EnumMap put(Enum<?> key, Object value) {
		map.put(key, value);
		return this;
	}

}
