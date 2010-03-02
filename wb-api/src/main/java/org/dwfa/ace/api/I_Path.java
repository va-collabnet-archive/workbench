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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.tapi.TerminologyException;

public interface I_Path {

    public int getConceptId();

    public List<I_Position> getOrigins();

    /**
     * Get all origins and origin of origins, etc., for this path.
     */
    public Set<I_Position> getInheritedOrigins();

    /**
     * Similar to {@link #getInheritedOrigins()} however superseded origins
     * (where there
     * is more than one origin for the same path but with an earlier version)
     * should be
     * excluded.
     */
    public Set<I_Position> getNormalisedOrigins();

    /**
     * Similar to {@link #getNormalisedOrigins()} however additional peer paths
     * can be provided.
     * This provides a normalised set of origins for this path along with the
     * origins of the
     * additional paths provided.
     */
    public Set<I_Position> getNormalisedOrigins(Collection<I_Path> paths);

    public I_Path getMatchingPath(int pathId);

    public void convertIds(I_MapNativeToNative jarToDbNativeMap);

    public UniversalAcePath getUniversal() throws IOException, TerminologyException;

    public String toHtmlString() throws IOException, TerminologyException;

    /**
     * Add an origin position to a path.
     * If the origin already exists it should be ignored.
     * The the origin already exists with a different version/time position it
     * should be updated.
     * 
     * @param position The position (a point of time on a path) to be added as
     *            an origin
     * @throws TerminologyException If unable to complete
     */
    public void addOrigin(I_Position position, I_ConfigAceFrame config) throws TerminologyException;
}
