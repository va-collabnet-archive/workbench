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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.table.TupleAdder;
import org.dwfa.ace.utypes.UniversalAceImage;
import org.dwfa.ace.utypes.UniversalAceImagePart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;

public class ThinImageVersioned implements I_ImageVersioned {
    public ThinImageVersioned() {
        super();
    }

    private int imageId;

    private String format;

    private byte[] image;

    private int conceptId;

    private List<I_ImagePart> versions;

    public ThinImageVersioned(int nativeId, byte[] image, List<I_ImagePart> versions, String format, int conceptId) {
        super();
        this.imageId = nativeId;
        this.image = image;
        this.versions = versions;
        this.format = format;
        this.conceptId = conceptId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getImage()
     */
    public byte[] getImage() {
        return image;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getImageId()
     */
    public int getImageId() {
        return imageId;
    }

    public int getTermComponentId() {
        return imageId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getVersions()
     */
    public List<I_ImagePart> getVersions() {
        return versions;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.vodb.types.I_ImageVersioned#addVersion(org.dwfa.vodb.types.
     * ThinImagePart)
     */
    public boolean addVersion(I_ImagePart part) {
        return versions.add(part);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getFormat()
     */
    public String getFormat() {
        return format;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getConceptId()
     */
    public int getConceptId() {
        return conceptId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getLastTuple()
     */
    public I_ImageTuple getLastTuple() {
        return new ThinImageTuple(this, versions.get(versions.size() - 1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getTuples()
     */
    public List<I_ImageTuple> getTuples() {
        List<I_ImageTuple> tuples = new ArrayList<I_ImageTuple>();
        for (I_ImagePart p : getVersions()) {
            tuples.add(new ThinImageTuple(this, p));
        }
        return tuples;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.vodb.types.I_ImageVersioned#convertIds(org.dwfa.vodb.jar.
     * I_MapNativeToNative)
     */
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        conceptId = jarToDbNativeMap.get(conceptId);
        imageId = jarToDbNativeMap.get(imageId);
        for (I_ImagePart p : versions) {
            p.convertIds(jarToDbNativeMap);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.vodb.types.I_ImageVersioned#merge(org.dwfa.vodb.types.
     * ThinImageVersioned)
     */
    public boolean merge(I_ImageVersioned jarImage) {
        HashSet<I_ImagePart> versionSet = new HashSet<I_ImagePart>(versions);
        boolean changed = false;
        for (I_ImagePart jarPart : jarImage.getVersions()) {
            if (!versionSet.contains(jarPart)) {
                changed = true;
                versions.add(jarPart);
            }
        }
        return changed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ImageVersioned#getTimePathSet()
     */
    public Set<TimePathId> getTimePathSet() {
        Set<TimePathId> tpSet = new HashSet<TimePathId>();
        for (I_ImagePart p : versions) {
            tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
        }
        return tpSet;
    }

    private class ImageTupleAdder extends TupleAdder<I_ImageTuple, ThinImageVersioned> {

        @Override
        public I_ImageTuple makeTuple(I_AmPart part, ThinImageVersioned core) {
            return new ThinImageTuple(core, (I_ImagePart) part);
        }

    }

    ImageTupleAdder adder = new ImageTupleAdder();

    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            List<I_ImageTuple> matchingTuples) {
        adder.addTuples(allowedStatus, allowedTypes, positions, matchingTuples, true, versions, this);
    }

    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            List<I_ImageTuple> matchingTuples, boolean returnConflictResolvedLatestState)
            throws TerminologyException, IOException {

        List<I_ImageTuple> tuples = new ArrayList<I_ImageTuple>();

        addTuples(null, allowedTypes, positions, tuples);

        if (returnConflictResolvedLatestState) {
            I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
            I_ManageConflict conflictResolutionStrategy;
            if (config == null) {
                conflictResolutionStrategy = new IdentifyAllConflictStrategy();
            } else {
                conflictResolutionStrategy = config.getConflictResolutionStrategy();
            }

            tuples = conflictResolutionStrategy.resolveTuples(tuples);
        }

        if (allowedStatus != null) {
            for (I_ImageTuple descTuple : tuples) {
                // filter by allowed status
                if (allowedStatus.contains(descTuple.getStatusId())) {
                    matchingTuples.add(descTuple);
                }
            }
        } else {
            matchingTuples.addAll(tuples);
        }
    }    
    
    private static Collection<UUID> getUids(int id) throws IOException, TerminologyException {
        return LocalFixedTerminology.getStore().getUids(id);
    }

    public UniversalAceImage getUniversal() throws IOException, TerminologyException {
        UniversalAceImage universal = new UniversalAceImage(getUids(imageId), getImage(),
            new ArrayList<UniversalAceImagePart>(versions.size()), getFormat(), getUids(conceptId));
        for (I_ImagePart part : versions) {
            UniversalAceImagePart universalPart = new UniversalAceImagePart();
            universalPart.setPathId(getUids(part.getPathId()));
            universalPart.setStatusId(getUids(part.getStatusId()));
            universalPart.setTextDescription(part.getTextDescription());
            universalPart.setTypeId(getUids(part.getTypeId()));
            universalPart.setTime(ThinVersionHelper.convert(part.getVersion()));
            universal.addVersion(universalPart);
        }
        return universal;
    }

    public int getNid() {
        return imageId;
    }

    public boolean promote(I_Position viewPosition, Set<I_Path> pomotionPaths, I_IntSet allowedStatus) {
        int viewPathId = viewPosition.getPath().getConceptId();
        Set<I_Position> positions = new HashSet<I_Position>();
        positions.add(viewPosition);
        List<I_ImageTuple> matchingTuples = new ArrayList<I_ImageTuple>();
        addTuples(allowedStatus, null, positions, matchingTuples);
        boolean promotedAnything = false;
        for (I_Path promotionPath : pomotionPaths) {
            for (I_ImageTuple it : matchingTuples) {
                if (it.getPathId() == viewPathId) {
                    I_ImagePart promotionPart = it.getPart().duplicate();
                    promotionPart.setVersion(Integer.MAX_VALUE);
                    promotionPart.setPathId(promotionPath.getConceptId());
                    it.getVersioned().addVersion(promotionPart);
                    promotedAnything = true;
                }
            }
        }
        return promotedAnything;
    }

}
