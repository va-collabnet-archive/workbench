package org.dwfa.ace.task.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
public class MultiMap implements Map {

	private Map map = new HashMap();

	@Override
	public int size() {
		return this.values().size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.values().contains(value);
	}

	@Override
	public Object get(Object key) {
		return map.get(key);
	}

	@Override
	public Object put(Object key, Object value) {

		List values = null;
		if (this.containsKey(key)) {
			values = (List) this.get(key);
		} else {
			values = new ArrayList();
		}
		values.add(value);
		map.put(key, values);
		return value;
	}

	@Override
	public Object remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map map) {
		map.putAll(map);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set keySet() {
		return map.keySet();
	}

	@Override
	public Collection values() {
		List values = new ArrayList();
		Collection<List> valueList = map.values();
		for (List vals : valueList) {
			values.addAll(vals);
		}
		return values;
	}

	@Override
	public Set entrySet() {
		return map.entrySet();
	}
}