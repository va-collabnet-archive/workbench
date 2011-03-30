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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.coordinate.PositionSet;

public interface I_Identify extends I_AmTermComponent {

    public int getNid();

    public List<? extends I_IdPart> getMutableIdParts();
    
    public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet);
    
    public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet, int... authorityNids);
    
    public List<UUID> getUUIDs();

    @Deprecated
    /**
     * @deprecated Use addStringId(), addIntId(), addUuidId, and addLongId() instead.
     */
    public boolean addMutableIdPart(I_IdPart srcId);

    public boolean hasMutableIdPart(I_IdPart newPart);

    public Set<TimePathId> getTimePathSet();

    public List<? extends I_IdVersion> getIdVersions();

    public UniversalAceIdentification getUniversalId() throws IOException, TerminologyException;

    public boolean addStringId(String stringId, int authorityNid, int statusNid, int pathNid, long time);

    public boolean addUuidId(UUID uuidId, int authorityNid, int statusNid, int pathNid, long time);

    public boolean addLongId(Long longId, int authorityNid, int statusNid, int pathNid, long time);

}
