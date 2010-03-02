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

import java.util.List;

import org.dwfa.ace.api.I_AmTuple;

public interface I_ExtendByRefVersion extends I_ExtendByRefPart, I_AmTuple {

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

    public void addVersion(I_ExtendByRefPart part);

    public int getComponentId();

    public int getMemberId();

    public int getRefsetId();

    public int getTypeId();

    public List<? extends I_ExtendByRefPart> getVersions();

    public I_ExtendByRef getCore();

    public I_ExtendByRefPart getMutablePart();

}
