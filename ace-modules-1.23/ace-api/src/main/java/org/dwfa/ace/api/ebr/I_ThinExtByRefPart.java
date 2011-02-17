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
package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefPart extends Comparable<I_ThinExtByRefPart>, I_AmPart {

    /**
     * @deprecated Use {@link #getStatusId()}
     */
    @Deprecated
    public int getStatus();

    /**
     * @deprecated Use {@link #setStatusId(int)}
     */
    @Deprecated
    public void setStatus(int idStatus);

    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

    /**
     * @deprecated Use {@link #duplicate()}
     */
    @Deprecated
    public I_ThinExtByRefPart duplicatePart();

    public I_ThinExtByRefPart duplicate();

    public I_ThinExtByRefPart makePromotionPart(I_Path promotionPath);

}
