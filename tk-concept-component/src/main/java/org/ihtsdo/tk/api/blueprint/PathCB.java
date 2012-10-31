/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api.blueprint;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

/**
 *
 * @author akf
 */
public class PathCB {
    private ConceptCB pathBp;
    private RefexCAB pathRefsetBp;
    private RefexCAB pathOriginRefsetBp;
    private RefexCAB pathOriginRefsetPathForPathAsOriginBp;
    private Collection<ConceptChronicleBI> origins = new TreeSet<ConceptChronicleBI>();
    
    public PathCB(ConceptCB pathBp, RefexCAB pathRefsetBp,
            RefexCAB pathOriginRefsetBp,
            RefexCAB pathOriginRefsetPathForPathAsOriginBp,
            ConceptChronicleBI... origins){
        this.pathBp = pathBp;
        this.pathRefsetBp = pathRefsetBp;
        this.pathOriginRefsetBp = pathOriginRefsetBp;
        this.pathOriginRefsetPathForPathAsOriginBp = pathOriginRefsetPathForPathAsOriginBp;
        if (origins != null) {
            this.origins.addAll(Arrays.asList(origins));
        }
    }

    public Collection<ConceptChronicleBI> getOrigins() {
        return origins;
    }

    public ConceptCB getPathBp() {
        return pathBp;
    }

    public RefexCAB getPathOriginRefsetBp() {
        return pathOriginRefsetBp;
    }

    public RefexCAB getPathRefsetBp() {
        return pathRefsetBp;
    }

    public RefexCAB getPathAsOriginBp() {
        return pathOriginRefsetPathForPathAsOriginBp;
    }

    public void setPathAsOriginBp(RefexCAB pathOriginRefsetPathAsOriginBp) {
        this.pathOriginRefsetPathForPathAsOriginBp = pathOriginRefsetPathAsOriginBp;
    }
}
