package org.ihtsdo.objectCache;
/**
 * @author Adam Flinton
 */

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ObjectCache {

	public static Hashtable cache;
	private static final Logger log = Logger.getLogger(ObjectCache.class.getName());

	//private static final Log log = LogFactory.getLog(ObjectCache.class);

	public ObjectCache() {
		super();
		if(cache == null){
			cache = new Hashtable();
		}
	}

	public static Object get(String key) {
		Object o = null;
		try {
			if (getCache().get(key) != null) {
				synchronized (ObjectCache.class) // make thread safe
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

	public static boolean put(String key, Object o) {
		boolean ok = false;
		try {
			synchronized (ObjectCache.class) // make thread safe
			{
				getCache().put(key, o);
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
	public static void clear() {
		getCache().clear();
	}

	public static Hashtable getCache() {
		
		if(cache == null){
			synchronized (ObjectCache.class) // make thread safe
			{
			cache = new Hashtable();
			}
		}
		
		return cache;
	}

	public static void setCache(Hashtable cache) {
		ObjectCache.cache = cache;
	}

	/*
	 * 
	 * @return java.util.Enumeration
	 */
	public static java.util.Enumeration elements() {

		return getCache().elements();
	}

	/**
	 * Remove a specific object from the cache.
	 * 
	 * @param key
	 *            The filename of the object.
	 */
	public static void remove(Object key) {
		getCache().remove(key);
	}

	/**
	 * Insert the method's description here. Creation date: (14/02/02 12:54:30)
	 * 
	 * @return int
	 */
	public static int size() {
		return getCache().size();
	}

	/**
	 * Insert the method's description here. Creation date: (22/07/2002
	 * 16:22:14)
	 */
	public static void debugOC() {

		Enumeration enum1;
		String Key;
		String Value;

		// enum = oc.keys();
		enum1 = ObjectCache.keys();
		while (enum1.hasMoreElements()) {
			Key = (String) enum1.nextElement();
			// Value = (String) oc.get(Key).toString();
			Value = (String) cache.get(Key).toString();
			log.severe("OC Key = " + Key);
			log.severe("OC Value = " + Value);
		}

	}

	/**
	 * Insert the method's description here. Creation date: (14/02/02 12:53:12)
	 * 
	 * @return java.util.Enumeration
	 */
	public static Enumeration keys() {

		return getCache().keys();
	}

}
