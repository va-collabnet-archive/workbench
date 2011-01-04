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
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.media.MediaAnalogBI;

public interface I_ImageVersioned<A extends MediaAnalogBI>
        extends I_AmTermComponent, MediaAnalogBI<A> {

    public byte[] getImage();

    public int getImageId();

    public List<? extends I_ImagePart> getMutableParts();

    public boolean addVersion(I_ImagePart part);

    public String getFormat();

    public int getConceptNid();

    public I_ImageTuple getLastTuple();

    public List<? extends I_ImageTuple> getTuples();

    public void convertIds(I_MapNativeToNative jarToDbNativeMap);

    public Set<TimePathId> getTimePathSet();

    public void addTuples(NidSetBI allowedStatus, NidSetBI allowedTypes, 
            PositionSetBI positions, List<I_ImageTuple> returnImages, 
            Precedence precedencePolicy, ContradictionManagerBI contradictionManager);

    public UniversalAceImage getUniversal() throws IOException, TerminologyException;
}
