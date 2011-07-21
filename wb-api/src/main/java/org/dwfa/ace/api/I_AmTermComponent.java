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
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;

public interface I_AmTermComponent extends ComponentBI {

    /**
     * @return The terminology component's identifier
     */
    @Override
    public int getNid();

    /**
     * 
     * @param viewPosition The position items should be promoted from.
     * @param pomotionPaths The path to promote items to.
     * @param allowedStatus Only promote items that have one of these status
     *            values.
     * @return true if there are any promotions that require commitment.
     * @throws IOException
     * @throws TerminologyException
     */
    public boolean promote(I_TestComponent test, I_Position viewPosition, PathSetReadOnly pomotionPaths, 
            NidSetBI allowedStatus, Precedence precedence)
            throws IOException, TerminologyException;

    public boolean promote(PositionBI viewPosition, PathSetReadOnly pomotionPaths, 
            NidSetBI allowedStatus, Precedence precedence)
            throws IOException, TerminologyException;

}
