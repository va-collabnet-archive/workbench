package com.sun.jini.mahalo;

import net.jini.core.transaction.server.TransactionConstants;

import com.sun.jini.mahalo.log.CannotRecoverException;
import com.sun.jini.mahalo.log.LogRecord;
/**
 * A <code>LogRecord</code> which encapsulates a generic
 * transactional logging information.
 *
 *
 */
interface LocalTxnLogRecord extends TransactionConstants, LogRecord {
    void recover(LocalTxnManagerTransaction tmt) throws CannotRecoverException;
}
