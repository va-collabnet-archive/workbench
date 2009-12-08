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
package org.dwfa.ace.api;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;

/**
 * Provides an abstract method which will be wrapped in a auto-commit database
 * transaction.
 * If any exception is raised the transaction will be rolled back.
 * 
 */
public abstract class TransactionWrapper {

    private String errorMsg;

    public abstract void doInTransaction() throws Exception;

    public void execute() throws TerminologyException {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {
            termFactory.startTransaction();

            doInTransaction();

            termFactory.commitTransaction();

        } catch (Exception e) {
            try {
                termFactory.cancelTransaction();
            } catch (IOException e1) {
                setErrorMsg(getErrorMsg() + "Unable to cancel transaction.");
            }
            throw new TerminologyException(getErrorMsg(), e);
        }
    }

    public String getErrorMsg() {
        return (errorMsg != null) ? errorMsg : "";
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}
