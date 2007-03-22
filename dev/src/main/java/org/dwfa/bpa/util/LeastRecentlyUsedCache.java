/*
 * Created on Feb 25, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author kec
 *
 */
public class LeastRecentlyUsedCache extends LinkedHashMap {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private  int cacheSize;

    /**
	 * @param arg0
	 * @param arg1
	 */
	public LeastRecentlyUsedCache(int cacheSize) {
		super(cacheSize, 0.75f, true);
        this.cacheSize = cacheSize;
	}


    protected boolean removeEldestEntry(Map.Entry eldest) {
       return size() > cacheSize;
    }
}
