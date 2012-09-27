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
package org.dwfa.ace.api.cs;

import java.io.File;
import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.utypes.I_AmChangeSetObject;
import org.dwfa.tapi.TerminologyException;

/**
 * This interface provides the methods necessary to validate
 * a change set, and to allow selective import of change set
 * contents according to aribtrary criterion.
 * 
 * @author kec
 * 
 */
public interface I_ValidateChangeSetChanges {

    /**
     * Determine if a change set is validated for input. This method provides
     * the opportunity for a validator class to open the file, examine the
     * contents, and compare it
     * with the content within the provided <code>I_TermFactory</code> object to
     * determine if a change set is
     * validated for processing. If validated, individual changes in the change
     * set will be processed via
     * the <code>validateChange</code> method, where individual items can be
     * filtered for processing.
     * 
     * @return true if the change set is validated for processing.
     * 
     * @param tf the
     * @return true if change set is validated for processing.
     * @throws IOException
     */
    public boolean validateFile(File csFile, I_TermFactory tf) throws IOException, TerminologyException;

    /**
     * Determine if an individual component within a change set is validated for
     * input.
     * 
     * @param csObj The change set object to validate.
     * @return true if the <code>csObj</code> is validated for import into the
     *         <code>I_TermFactory</code>
     * @throws IOException
     * @throws TerminologyException
     */
    public boolean validateChange(I_AmChangeSetObject csObj, I_TermFactory tf) throws IOException, TerminologyException;

}
