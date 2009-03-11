/*
 * Created on Apr 19, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.pwdbreaker;

import java.util.UUID;

import net.jini.core.entry.Entry;


public class PoisonPill implements Entry {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public UUID masterId;

    public PoisonPill() {
    }

    /**
     * @param masterId
     */
    public PoisonPill(UUID masterId) {
        super();
        this.masterId = masterId;
    }
}