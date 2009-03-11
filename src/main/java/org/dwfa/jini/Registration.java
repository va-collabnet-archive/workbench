/*
 * Created on Mar 7, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.jini;

import java.io.Serializable;
import java.rmi.MarshalledObject;

import net.jini.core.event.RemoteEventListener;
import net.jini.id.Uuid;

/**
 * @author kec
 *
 */
public class Registration implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected Uuid cookie;
    protected RemoteEventListener listener;
    protected MarshalledObject data;
    protected long expiration;

	/**
	 * @param cookie
	 * @param listener
	 * @param data
	 * @param expiration
	 */
	public Registration(Uuid cookie, RemoteEventListener listener,
			MarshalledObject data, long expiration) {
		super();
		this.cookie = cookie;
		this.listener = listener;
		this.data = data;
		this.expiration = expiration;
	}
	/**
	 * @return Returns the expiration.
	 */
	public long getExpiration() {
		return expiration;
	}
	/**
	 * @param expiration The expiration to set.
	 */
	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}
	/**
	 * @return Returns the cookie.
	 */
	public Uuid getCookie() {
		return cookie;
	}
	/**
	 * @return Returns the data.
	 */
	public MarshalledObject getData() {
		return data;
	}
	/**
	 * @return Returns the listener.
	 */
	public RemoteEventListener getListener() {
		return listener;
	}
	/**
	 * 
	 */
	public Registration() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    protected void cancelled() {
        
    }

}
