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
package org.dwfa.vodb.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalAcePosition;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;

public class Path implements I_Path {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    int conceptId;

    Set<I_Position> origins;

    public Path(int conceptId, List<I_Position> origins) {
        super();
        this.conceptId = conceptId;
        if (origins != null) {
            this.origins = new HashSet<I_Position>(origins);
        } else {
            this.origins = new HashSet<I_Position>(0);
        }
    }

    public boolean equals(I_Path another) {
        return (conceptId == another.getConceptId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (I_Path.class.isAssignableFrom(obj.getClass())) {
            return equals((I_Path) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return conceptId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_Path#getConceptId()
     */
    public int getConceptId() {
        return conceptId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_Path#getOrigins()
     */
    public Collection<I_Position> getOrigins() {
        return Collections.unmodifiableSet(origins);
    }

    public Set<I_Position> getInheritedOrigins() {
        HashSet<I_Position> inheritedOrigins = new HashSet<I_Position>();
        for (I_Position origin : this.origins) {
            inheritedOrigins.addAll(origin.getPath().getInheritedOrigins());
            inheritedOrigins.add(origin);
        }
        return inheritedOrigins;
    }

    public Set<I_Position> getNormalisedOrigins() {
        return getNormalisedOrigins(null);
    }

    public Set<I_Position> getNormalisedOrigins(Collection<I_Path> paths) {
        final Set<I_Position> inheritedOrigins = getInheritedOrigins();
        if (paths != null) {
            for (I_Path path : paths) {
                if (path != this) {
                    inheritedOrigins.addAll(path.getInheritedOrigins());
                }
            }
        }
        Set<I_Position> normalisedOrigins = new HashSet<I_Position>(inheritedOrigins);
        for (I_Position a : inheritedOrigins) {
            for (I_Position b : inheritedOrigins) {
                if ((a.getPath().getConceptId() == b.getPath().getConceptId()) && (a.getVersion() < b.getVersion())) {
                    normalisedOrigins.remove(a);
                }
            }
        }
        return normalisedOrigins;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_Path#getMatchingPath(int)
     */
    public I_Path getMatchingPath(int pathId) {
        if (conceptId == pathId) {
            return this;
        }
        for (I_Position origin : origins) {
            if (origin.getPath().getMatchingPath(pathId) != null) {
                return origin.getPath();
            }
        }
        return null;
    }

    public static String toHtmlString(I_Path path) throws IOException, TerminologyException {
        StringBuffer buff = new StringBuffer();
        buff.append("<html><font color='blue' size='+1'><u>");
        I_GetConceptData cb = Terms.get().getConcept(path.getConceptId());
        buff.append(cb.getInitialText());
        buff.append("</u></font>");
        if (path != null) {
            for (I_Position origin : path.getOrigins()) {
                buff.append("<br>&nbsp;&nbsp;&nbsp;Origin: ");
                buff.append(origin);
            }
        }
        return buff.toString();
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.vodb.types.I_Path#convertIds(org.dwfa.ace.api.I_MapNativeToNative
     * )
     */
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        conceptId = jarToDbNativeMap.get(conceptId);
        for (I_Position origin : origins) {
            origin.getPath().convertIds(jarToDbNativeMap);
        }
    }

    public UniversalAcePath getUniversal() throws IOException, TerminologyException {
             List<UniversalAcePosition> universalOrigins = new ArrayList<UniversalAcePosition>(origins.size());
            for (I_Position position : origins) {
                universalOrigins.add(new UniversalAcePosition(Terms.get().nativeToUuid(
                    position.getPath().getConceptId()), Terms.get().convertToThickVersion(position.getVersion())));
            }
            return new UniversalAcePath(Terms.get().nativeToUuid(conceptId), universalOrigins);
    }

    public static void writePath(ObjectOutputStream out, I_Path p) throws IOException {
    	List<UUID> uuids = Terms.get().nativeToUuid(p.getConceptId());
    	if (uuids.size() > 0) {
    		out.writeObject(Terms.get().nativeToUuid(p.getConceptId()));
    	} else {
    		throw new IOException("no uuids for component: " + p);
    	}
    	out.writeInt(p.getOrigins().size());
    	for (I_Position origin : p.getOrigins()) {
    		Position.writePosition(out, origin);
    	}
    }

    @SuppressWarnings("unchecked")
    public static I_Path readPath(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int pathId;
        try {
            List<UUID> pathIdList = (List<UUID>) in.readObject();
            if (Terms.get().hasId(pathIdList)) {
                pathId = Terms.get().uuidToNative(pathIdList);
            } else {
                pathId = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.localize().getNid();
            }

        } catch (TerminologyException e) {
            IOException newEx = new IOException();
            newEx.initCause(e);
            throw newEx;
        }
        int size = in.readInt();
        List<I_Position> origins = new ArrayList<I_Position>(size);
        for (int i = 0; i < size; i++) {
            origins.add(Position.readPosition(in));
        }
        return new Path(pathId, origins);
    }

    public static Set<I_Path> readPathSet(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        Set<I_Path> positions = new HashSet<I_Path>(size);
        for (int i = 0; i < size; i++) {
            try {
                positions.add(readPath(in));
            } catch (IOException ex) {
                if (NoMappingException.class.isAssignableFrom(ex.getCause().getClass())) {
                    AceLog.getAppLog().alertAndLogException(ex);
                } else {
                    throw ex;
                }
            }
        }
        return positions;
    }

    public static void writePathSet(ObjectOutputStream out, Set<I_Path> viewPositions) throws IOException {
        out.writeInt(viewPositions.size());
        for (I_Path p : viewPositions) {
            writePath(out, p);
        }
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        try {
            I_GetConceptData cb = Terms.get().getConcept(getConceptId());
            buff.append(cb.getInitialText());
        } catch (IOException e) {
            buff.append(e.getMessage());
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            buff.append(e.getMessage());
            AceLog.getAppLog().alertAndLogException(e);
		}
        return buff.toString();
    }

    public String toHtmlString() throws IOException, TerminologyException {
        return Path.toHtmlString(this);
    }

    public void addOrigin(I_Position position, I_ConfigAceFrame config) throws TerminologyException {
        assert this.origins.contains(position) == false: "Attempt to add duplicate origin to path: " +
            this.toString() + " duplicate origin: " + position;
        this.origins.add(position);
        Terms.get().writePathOrigin(this, position, config);
    }
    
    public void remmoveOrigin(I_Position position, I_ConfigAceFrame config) throws TerminologyException {
        assert this.origins.contains(position) == false: "Attempt to remove origin that is not in path: " +
            this.toString() + " erroneous origin: " + position;
        this.origins.remove(position);
        Terms.get().removeOrigin(this, position, config);
    }
}
