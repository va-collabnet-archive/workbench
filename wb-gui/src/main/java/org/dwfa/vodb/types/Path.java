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
import java.util.concurrent.CopyOnWriteArraySet;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalAcePosition;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

public class Path implements PathBI, I_Path {
    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    int conceptNid;

    Set<PositionBI> origins;

    public Path(int conceptId, List<? extends PositionBI> origins) {
        super();
        this.conceptNid = conceptId;
        if (origins != null) {
            this.origins = new CopyOnWriteArraySet<PositionBI>(origins);
        } else {
            this.origins = new CopyOnWriteArraySet<PositionBI>();
        }
    }

    public boolean equals(PathBI another) {
        return (conceptNid == another.getConceptNid());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (PathBI.class.isAssignableFrom(obj.getClass())) {
            return equals((PathBI) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return conceptNid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.PathBI#getConceptNid()
     */
    public int getConceptNid() {
        return conceptNid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.PathBI#getOrigins()
     */
    public Collection<? extends PositionBI> getOrigins() {
        return Collections.unmodifiableSet(origins);
    }

    public Set<PositionBI> getInheritedOrigins() {
        HashSet<PositionBI> inheritedOrigins = new HashSet<PositionBI>();
        for (PositionBI origin : this.origins) {
            inheritedOrigins.addAll(origin.getPath().getInheritedOrigins());
            inheritedOrigins.add(origin);
        }
        return inheritedOrigins;
    }

    public Set<PositionBI> getNormalisedOrigins() {
        return getNormalisedOrigins(null);
    }

    public Set<PositionBI> getNormalisedOrigins(Collection<PathBI> paths) {
        final Set<PositionBI> inheritedOrigins = getInheritedOrigins();
        if (paths != null) {
            for (PathBI path : paths) {
                if (path != this) {
                    inheritedOrigins.addAll(path.getInheritedOrigins());
                }
            }
        }
        Set<PositionBI> normalisedOrigins = new HashSet<PositionBI>(inheritedOrigins);
        for (PositionBI a : inheritedOrigins) {
            for (PositionBI b : inheritedOrigins) {
                if ((a.getPath().getConceptNid()) == b.getPath().getConceptNid() && (a.getVersion() < b.getVersion())) {
                    normalisedOrigins.remove(a);
                }
            }
        }
        return normalisedOrigins;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.PathBI#getMatchingPath(int)
     */
    public PathBI getMatchingPath(int pathId) {
        if (conceptNid == pathId) {
            return this;
        }
        for (PositionBI origin : origins) {
            if (origin.getPath().getMatchingPath(pathId) != null) {
                return origin.getPath();
            }
        }
        return null;
    }

    public static String toHtmlString(PathBI path) throws IOException {
        try {
			StringBuffer buff = new StringBuffer();
			buff.append("<html><font color='blue' size='+1'><u>");
			I_GetConceptData cb = Terms.get().getConcept(path.getConceptNid());
			buff.append(cb.getInitialText());
			buff.append("</u></font>");
			if (path != null) {
			    for (PositionBI origin : path.getOrigins()) {
			        buff.append("<br>&nbsp;&nbsp;&nbsp;Origin: ");
			        buff.append(origin);
			    }
			}
			return buff.toString();
		} catch (TerminologyException e) {
			throw new IOException(e);
		}
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
     * org.dwfa.vodb.types.PathBI#convertIds(org.dwfa.ace.api.I_MapNativeToNative
     * )
     */
    @Deprecated
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        throw new UnsupportedOperationException();
    }

    public UniversalAcePath getUniversal() throws IOException, TerminologyException {
             List<UniversalAcePosition> universalOrigins = new ArrayList<UniversalAcePosition>(origins.size());
            for (PositionBI position : origins) {
                universalOrigins.add(new UniversalAcePosition(Terms.get().nativeToUuid(
                    position.getPath().getConceptNid()), Terms.get().convertToThickVersion(position.getVersion())));
            }
            return new UniversalAcePath(Terms.get().nativeToUuid(conceptNid), universalOrigins);
    }

    public static void writePath(ObjectOutputStream out, PathBI p) throws IOException {
    	List<UUID> uuids = Terms.get().nativeToUuid(p.getConceptNid());
    	if (uuids.size() > 0) {
    		out.writeObject(Terms.get().nativeToUuid(p.getConceptNid()));
    	} else {
    		throw new IOException("no uuids for component: " + p);
    	}
    	out.writeInt(p.getOrigins().size());
    	for (PositionBI origin : p.getOrigins()) {
    		Position.writePosition(out, origin);
    	}
    }

    @SuppressWarnings("unchecked")
    public static PathBI readPath(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int pathId;
        try {
            List<UUID> pathIdList = (List<UUID>) in.readObject();
            if (Terms.get().hasId(pathIdList)) {
                pathId = Terms.get().uuidToNative(pathIdList);
            } else {
                pathId = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.localize().getNid();
                AceLog.getAppLog().warning("ReadPath error. " + pathIdList + " missing. Substuting WB Aux ");
            }

        } catch (TerminologyException e) {
            IOException newEx = new IOException();
            newEx.initCause(e);
            throw newEx;
        }
        int size = in.readInt();
        List<PositionBI> origins = new ArrayList<PositionBI>(size);
        for (int i = 0; i < size; i++) {
            origins.add(Position.readPosition(in));
        }
        return new Path(pathId, origins);
    }

    public static Set<PathBI> readPathSet(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        Set<PathBI> positions = new HashSet<PathBI>(size);
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

    public static void writePathSet(ObjectOutputStream out, Set<PathBI> viewPositions) throws IOException {
        out.writeInt(viewPositions.size());
        for (PathBI p : viewPositions) {
            writePath(out, p);
        }
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        try {
            I_GetConceptData cb = Terms.get().getConcept(getConceptNid());
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

    public String toHtmlString() throws IOException {
        return Path.toHtmlString(this);
    }

    public void addOrigin(PositionBI position, I_ConfigAceFrame config) throws TerminologyException {
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

	@Override
	public List<UUID> getUUIDs() {
		try {
			return new ArrayList<UUID>(Terms.get().getUids(conceptNid));
		} catch (TerminologyException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
