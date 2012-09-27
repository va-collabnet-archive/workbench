package org.ihtsdo.objectCache;
/**
 * @author Adam Flinton
 */

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum ObjectCache {

	INSTANCE;
	
	private Hashtable<String, Object> cache;
	private final Logger log = Logger.getLogger(ObjectCache.class.getName());

	private ObjectCache() {
		cache = new Hashtable<String, Object>();
	}

	public Object get(String key) {
		Object o = null;
		try {
			if (cache.containsKey(key)){
				synchronized (this) // make thread safe
				{
					o = cache.get(key);
				}
			}
		} catch (Exception E) {
			//log.error("Error in ObjectCache.get key = " + key, E);
			log.log(Level.SEVERE,"Error in ObjectCache.get key = " + key, E);
		}
		return o;
	}

	public boolean put(String key, Object o) {
		boolean ok = false;
		try {
			synchronized (this) // make thread safe
			{
				cache.put(key, o);
				ok = true;
			}

		} catch (Exception E) {
			//log.error("Error in ObjectCache.put key = " + key + "Object = " + o,E);
			log.log(Level.SEVERE,"Error in ObjectCache.put key = " + key + "Object = " + o,E);
			ok = false;
		}

		return ok;
	}

	/**
	 * Clear the cache.
	 */
	public void clear() {
		cache.clear();
	}

	/*
	 * 
	 * @return java.util.Enumeration
	 */
	public Enumeration<Object> elements() {
		return cache.elements();
	}

	/**
	 * Remove a specific object from the cache.
	 * 
	 * @param key
	 *            The filename of the object.
	 */
	public void remove(Object key) {
		cache.remove(key);
	}

	/**
	 * Insert the method's description here. Creation date: (14/02/02 12:54:30)
	 * 
	 * @return int
	 */
	public int size() {
		return cache.size();
	}

	/**
	 * Insert the method's description here. Creation date: (22/07/2002
	 * 16:22:14)
	 */
	public void debugOC() {

		for(Entry<String, Object> entry : cache.entrySet()) {
			log.severe("OC Key = " + entry.getKey());
			log.severe("OC Value = " + entry.getValue());
		}

	}

	/**
	 * Insert the method's description here. Creation date: (14/02/02 12:53:12)
	 * 
	 * @return java.util.Enumeration
	 */
	public Enumeration<String> keys() {
		return cache.keys();
	}

}
