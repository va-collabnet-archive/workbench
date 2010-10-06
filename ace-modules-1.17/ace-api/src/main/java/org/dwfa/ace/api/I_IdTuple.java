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

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface I_IdTuple extends I_AmTuple {

    public abstract int getNativeId();

    public abstract Set<TimePathId> getTimePathSet();

    public abstract List<UUID> getUIDs();

    public abstract List<I_IdPart> getVersions();

    public abstract boolean hasVersion(I_IdPart newPart);

    public abstract void setNativeId(int nativeId);

    /**
     * @deprecated Use {@link #getStatusId()}
     */
    @Deprecated
    public abstract int getIdStatus();

    public abstract int getSource();

    public abstract Object getSourceId();

    public abstract I_IdVersioned getIdVersioned();

    /**
     * @deprecated Use {@link #duplicate()}
     */
    @Deprecated
    public abstract I_IdPart duplicatePart();

    public I_IdPart duplicate();

    public abstract I_IdPart getPart();

}
