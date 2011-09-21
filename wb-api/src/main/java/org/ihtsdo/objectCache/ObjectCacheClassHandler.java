package org.ihtsdo.objectCache;

import org.dwfa.ace.log.AceLog;


public class ObjectCacheClassHandler {
	
	public static Object getInstClass(String Classname) {
		Object Ob = null;

		if (ObjectCache.INSTANCE.get(Classname) != null) {
			// log.error("Classname found in cache "+Classname);
			Ob = (Object) ObjectCache.INSTANCE.get(Classname);
		}

		if (ObjectCache.INSTANCE.get(Classname) == null) {
			// log.error("Classname not found in cache "+Classname);
			try {
				Ob = instantiateClass(Classname);
				checkState(Classname, Ob);
			} catch (Exception Ex) {
				AceLog.getAppLog().severe("Error instantiating a class called "
								+ Classname, Ex);
			}
			ObjectCache.INSTANCE.put(Classname, Ob);
		}
		return Ob;
	}

	/** Instantiates a class from it's (String) Classname */
	private static Object instantiateClass(String className)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException {
		//AceLog.getAppLog().info("instantiateClass called className = " + className);
		if (className == null)
			return null;
		return Class.forName(className).newInstance();
	}

	
	/** Checks to see that a class has been loaded correctly */
	private static void checkState(String interfaceName, Object interfaceObject) {
		if (interfaceObject == null)
			throw new IllegalStateException("Name of class that implements "
					+ interfaceName + " not set.");
	}
	

}
