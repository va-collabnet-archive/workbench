/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.dwfa.queue;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashSet;
import java.util.Set;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 *
 * @author kec
 */
public abstract class QueueTransactionPart implements XAResource {
    int      transactionTimeout = Integer.MAX_VALUE;
    Set<Xid> transactionSet     = new HashSet<>();
    Set<Xid> prepareSet         = new HashSet<>();
    String   resourceName;

    public QueueTransactionPart(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public int hashCode() {
        int hash = 3;

        hash = 13 * hash + ((this.resourceName != null)
                            ? this.resourceName.hashCode()
                            : 0);

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final QueueTransactionPart other = (QueueTransactionPart) obj;

        if ((this.resourceName == null)
            ? (other.resourceName != null)
            : !this.resourceName.equals(other.resourceName)) {
            return false;
        }

        return true;
    }

    public void end(Xid xid, int flags) throws XAException {
        System.out.println(resourceName + " end:" + xid + " flags: " + flags);
    }

    public void forget(Xid xid) throws XAException {
        System.out.println(resourceName + " forget:" + xid);
        transactionSet.remove(xid);
        prepareSet.remove(xid);
    }

    public int getTransactionTimeout() throws XAException {
        System.out.println(resourceName + " getTransactionTimeout");

        return transactionTimeout;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        System.out.println(resourceName + " isSameRM:" + xares);

        return xares.equals(this);
    }

    public int prepare(Xid xid) throws XAException {
        System.out.println(resourceName + " prepare:" + xid);
        prepareSet.add(xid);

        return XA_OK;
    }

    public Xid[] recover(int flag) throws XAException {
        System.out.println(resourceName + " recover:" + flag);

        return prepareSet.toArray(new Xid[prepareSet.size()]);
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        System.out.println(resourceName + " setTransactionTimeout:" + seconds);
        transactionTimeout = seconds;

        return true;
    }

    public void start(Xid xid, int flags) throws XAException {
        System.out.println(resourceName + " start:" + xid + " flags: " + flags);
        transactionSet.add(xid);
    }

    @Override
    public String toString() {
        return resourceName + ", transactionSet=" + transactionSet;
    }
}
