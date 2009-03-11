/*
 * Created on Aug 16, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue;

import net.jini.security.AccessPermission;

/**
 * @author kec
 *
 */
public class QueueServerPermission extends AccessPermission {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param name
	 */
	public QueueServerPermission(String name) {
		super(name);
	}

}
