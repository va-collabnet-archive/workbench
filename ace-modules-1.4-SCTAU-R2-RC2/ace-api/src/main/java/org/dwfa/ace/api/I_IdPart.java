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

public interface I_IdPart extends I_AmPart {

    /**
     * @deprecated Use {@link #getStatusId()}
     */
    @Deprecated
    public int getIdStatus();

    /**
     * @deprecated Use {@link #setStatusId(int)}
     */
    @Deprecated
    public void setIdStatus(int idStatus);

    public int getSource();

    public void setSource(int source);

    public Object getSourceId();

    public void setSourceId(Object sourceId);

    public I_IdPart duplicate();

}
