/*
 * Created on Mar 7, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.jini;

import java.util.Date;

import net.jini.core.transaction.server.TransactionManager;

/**
 * @author kec
 *
 */
public interface I_TransactionPart {

    public void commit(TransactionManager mgr, long id, Date commitDate);

    public void abort(TransactionManager mgr, long id);

}
