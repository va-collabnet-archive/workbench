/*
 * Created on Apr 19, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.pwdbreaker;

import java.util.UUID;

import net.jini.core.entry.Entry;


public class CryptResult implements Entry {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public byte[] word = null;

    public UUID masterId;

    public CryptResult() {
    }

    public CryptResult(byte[] word, UUID masterId) {
        this.word = word;
        this.masterId = masterId;
    }
}