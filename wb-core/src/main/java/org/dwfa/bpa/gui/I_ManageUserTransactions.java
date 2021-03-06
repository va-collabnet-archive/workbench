/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Created on Jan 30, 2006
 */
package org.dwfa.bpa.gui;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import javax.transaction.Transaction;

public interface I_ManageUserTransactions {

    public void addActiveTransactionListener(PropertyChangeListener listener);

    public void removeActiveTransactionListener(PropertyChangeListener listener);

    public void addUncommittedComponentsListener(PropertyChangeListener listener);

    public void removeUncommittedComponentsListener(PropertyChangeListener listener);

    public void commitActiveTransaction();

    public Transaction getActiveTransaction() throws IOException;

    public boolean isTransactionActive();

    public void setActiveTransaction(Transaction activeTransaction);

    public void setTransactionDuration(long transactionDuration);

    public Collection<?> getUncommittedComponents();

    public void abortActiveTransaction();
}
