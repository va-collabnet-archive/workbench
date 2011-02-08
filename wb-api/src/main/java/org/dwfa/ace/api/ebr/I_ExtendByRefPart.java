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
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

public interface I_ExtendByRefPart<T extends RefexAnalogBI<T>> 
        extends Comparable<I_ExtendByRefPart<T>>, I_AmPart<T>, RefexVersionBI<T> {

    /**
     * @deprecated Use {@link #getStatusNid()}
     */
    @Deprecated
    public int getStatus();

    /**
     * @deprecated Use {@link #setStatusNid(int)}
     */
    @Deprecated
    public void setStatus(int idStatus);

    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

    public I_ExtendByRefPart<T> duplicate();

    public I_ExtendByRefPart<T> makePromotionPart(PathBI promotionPath);

}
