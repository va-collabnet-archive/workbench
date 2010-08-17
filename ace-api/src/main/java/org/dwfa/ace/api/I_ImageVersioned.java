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

import org.dwfa.ace.utypes.UniversalAceImage;
import org.dwfa.tapi.TerminologyException;

public interface I_ImageVersioned extends I_AmVersioned<I_ImagePart> {

    public byte[] getImage();

    public int getImageId();

    public List<I_ImagePart> getVersions();

    public boolean addVersion(I_ImagePart part);

    public String getFormat();

    public int getConceptId();

    public I_ImageTuple getLastTuple();

    public List<I_ImageTuple> getTuples();

    public void convertIds(I_MapNativeToNative jarToDbNativeMap);

    public boolean merge(I_ImageVersioned jarImage);

    public Set<TimePathId> getTimePathSet();

    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            List<I_ImageTuple> returnImages);
    
    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            List<I_ImageTuple> matchingTuples, boolean returnConflictResolvedLatestState)
            throws TerminologyException, IOException;

    public UniversalAceImage getUniversal() throws IOException, TerminologyException;
}
