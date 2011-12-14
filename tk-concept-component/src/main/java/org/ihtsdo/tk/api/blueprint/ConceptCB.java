/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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

import java.beans.PropertyChangeEvent;
import org.ihtsdo.lang.LANG_CODE;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public final class ConceptCB extends CreateOrAmendBlueprint {

    public static final UUID conceptSpecNamespace =
            UUID.fromString("620d1f30-5285-11e0-b8af-0800200c9a66");
    private String fullySpecifiedName;
    private String preferredName;
    private List<String> fsns = new ArrayList<String>();
    private List<String> prefNames = new ArrayList<String>();
    private boolean initialCaseSensitive = false;
    private String lang;
    private UUID isaType;
    private boolean defined;
    private List<DescCAB> fsnCABs = new ArrayList<DescCAB>();
    private List<DescCAB> prefCABs = new ArrayList<DescCAB>();
    private List<DescCAB> descCABs = new ArrayList<DescCAB>();
    private List<RelCAB> relCABs = new ArrayList<RelCAB>();
    private List<MediaCAB> mediaCABs = new ArrayList<MediaCAB>();
    private ConAttrAB conAttr;
    private int usRefexNid = SnomedMetadataRfx.getUS_DIALECT_REFEX_NID();
    private int gbRefexNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
    private Collection<UUID> parents = new TreeSet<UUID>() {

        @Override
        public boolean add(UUID e) {
            boolean result = super.add(e);
            comupteComponentUuid();
            return result;
        }

        @Override
        public boolean addAll(Collection<? extends UUID> clctn) {
            boolean result = super.addAll(clctn);
            comupteComponentUuid();
            return result;
        }

        @Override
        public boolean remove(Object o) {
            boolean result = super.remove(o);
            comupteComponentUuid();
            return result;
        }

        @Override
        public boolean removeAll(Collection<?> clctn) {
            boolean result = super.removeAll(clctn);
            comupteComponentUuid();
            return result;
        }
    };

    public Collection<UUID> getParents() {
        return parents;
    }

    public ConceptCB(String fullySpecifiedName,
            String preferredName,
            LANG_CODE lang,
            UUID isaType,
            UUID... parents) throws IOException, InvalidCAB, ContradictionException{
        super(null, null, null);
        this.fsns.add(fullySpecifiedName);
        this.fullySpecifiedName = fullySpecifiedName; //@akf todo: these should be removed when NewConcept, etc. is upated
        this.prefNames.add(preferredName);
        this.preferredName = preferredName; //@akf todo: these should be removed when NewConcept, etc. is upated
        this.lang = lang.getFormatedLanguageCode();
        this.isaType = isaType;
        if (parents != null) {
            this.parents.addAll(Arrays.asList(parents));
        }
        pcs.addPropertyChangeListener(this);
        comupteComponentUuid();
    }

    public ConceptCB(List<String> fullySpecifiedNames,
            List<String> preferredNames,
            LANG_CODE lang,
            UUID isaType,
            UUID... parents) throws IOException, InvalidCAB, ContradictionException{
        super(null, null, null);
        this.fsns = fullySpecifiedNames;
        this.prefNames = preferredNames;
        this.lang = lang.getFormatedLanguageCode();
        this.isaType = isaType;
        if (parents != null) {
            this.parents.addAll(Arrays.asList(parents));
        }
        pcs.addPropertyChangeListener(this);
        comupteComponentUuid();
    }

    public ConceptCB(ConceptVersionBI cv, UUID newConceptUuid) throws IOException, ContradictionException, InvalidCAB {
        super(null, cv, cv.getViewCoordinate());
        pcs.addPropertyChangeListener(this);
        UUID uuid = getComponentUuid();
//        ConceptCB conceptCAB = cv.makeBlueprint();
        ConAttrAB conAttrBp = cv.getConAttrsActive().makeBlueprint(cv.getViewCoordinate());
        for (DescriptionVersionBI dv : cv.getFsnDescsActive()) {
            fsns.add(dv.getText());
            DescCAB fsnBp = dv.makeBlueprint(cv.getViewCoordinate());
            fsnCABs.add(fsnBp);
            descCABs.add(fsnBp);
            fsnBp.getAnnotationBlueprints();
        }
        for (DescriptionVersionBI dv : cv.getPrefDescsActive()) {
            prefNames.add(dv.getText());
            DescCAB prefBp = dv.makeBlueprint(cv.getViewCoordinate());
            prefCABs.add(prefBp);
            descCABs.add(prefBp);
            prefBp.getAnnotationBlueprints();
        }
        for (DescriptionVersionBI dv : cv.getDescsActive()) {
            if (cv.getFsnDescsActive().contains(dv) || cv.getPrefDescsActive().contains(dv)) {
                continue;
            }
            DescCAB descBp = dv.makeBlueprint(cv.getViewCoordinate());
            descCABs.add(descBp);
            descBp.getAnnotationBlueprints();
        }
        for (RelationshipVersionBI rv : cv.getRelsOutgoingActive()) {
            if (rv.getCharacteristicNid() == SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid()
                    || rv.getCharacteristicNid() == SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid()
                    || rv.getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
                continue;
            }
            RelCAB relBp = rv.makeBlueprint(cv.getViewCoordinate());
            relCABs.add(relBp);
            relBp.getAnnotationBlueprints();
        }
        for (MediaVersionBI mv : cv.getMediaActive()) {
            MediaCAB mediaBp = mv.makeBlueprint(cv.getViewCoordinate());
            mediaCABs.add(mediaBp);
            mediaBp.getAnnotationBlueprints();
        }
        this.setComponentUuid(newConceptUuid);
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        try {
            recomputeUuid();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CreateOrAmendBlueprint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CreateOrAmendBlueprint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CreateOrAmendBlueprint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidCAB ex) {
            Logger.getLogger(CreateOrAmendBlueprint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ContradictionException ex) {
            Logger.getLogger(CreateOrAmendBlueprint.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public final void comupteComponentUuid() throws RuntimeException {
        try {
            StringBuilder sb = new StringBuilder();
            List<String> descs = new ArrayList<String>();
            descs.addAll(fsns);
            descs.addAll(prefNames);
            java.util.Collections.sort(descs);
            for (String desc : descs) {
                sb.append(desc);
            }
            setComponentUuid(
                    UuidT5Generator.get(conceptSpecNamespace,
                    sb.toString()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException,
            IOException, InvalidCAB, ContradictionException {
        for (DescCAB descBp : getDescCABs()) {
            descBp.setConceptUuid(getComponentUuid());
            descBp.recomputeUuid();
        }
        for (RelCAB relBp : getRelCABs()) {
            relBp.setSourceUuid(getComponentUuid());
            relBp.recomputeUuid();
        }
        for (MediaCAB mediaBp : getMediaCABs()) {
            mediaBp.setConceptUuid(getComponentUuid());
            mediaBp.recomputeUuid();
        }
    }

    public String getFullySpecifiedName() {//@akf todo : update to use set when NewConcept, etc. has been updated
        return fullySpecifiedName;
    }

    public void setFullySpecifiedName(String fullySpecifiedName) {
        this.fullySpecifiedName = fullySpecifiedName;
        comupteComponentUuid();
    }

    /**
     * Adds an fsn.
     *
     * @param fsnBp blueprint of fsn
     * @param dialect language code of fsn dialect
     */
    public void addFsn(DescCAB fsnBp, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        fsns.add(fsnBp.getText());
        addFsnDialectRefexes(fsnBp, dialect);
        this.recomputeUuid();
    }

    private void addFsnDialectRefexes(DescCAB fsnBp, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        RefexCAB usAnnot;
        RefexCAB gbAnnot;
        if (dialect == LANG_CODE.EN) {
            usAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    fsnBp.getComponentUuid(),
                    usRefexNid, null, null);
            usAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());

            gbAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    fsnBp.getComponentUuid(),
                    gbRefexNid, null, null);
            gbAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            fsnBp.setAnnotationBlueprint(usAnnot);
            fsnBp.setAnnotationBlueprint(gbAnnot);
        } else if (dialect == LANG_CODE.EN_US) {
            usAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    fsnBp.getComponentUuid(),
                    usRefexNid, null, null);
            usAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            fsnBp.setAnnotationBlueprint(usAnnot);
        } else if (dialect == LANG_CODE.EN_GB) {
            throw new InvalidCAB("<html>Currently FSNs are only allowed for en or en-us. "
                    + "<br>Please add gb dialect variants as preferred terms.");
        } else {
            throw new InvalidCAB("Dialect not supported: " + dialect.getFormatedLanguageCode());
        }
    }

    /**
     * Updates an existing fsn.
     *
     * @param newFsn text to be updated
     * @param fsnBp blueprint of fsn
     * @param dialect language code of fsn dialect, leave null if dialect isn't
     * changing
     */
    public void updateFsn(String newFsn, DescCAB fsnBp, LANG_CODE dialect) throws
            NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        String oldText = fsnBp.getText();
        fsns.remove(oldText);
        fsns.add(newFsn);
        this.recomputeUuid();
        fsnBp.setText(newFsn);
        if (dialect != null) {
            List<RefexCAB> annotationBlueprints = fsnBp.getAnnotationBlueprints();
            for (RefexCAB annot : annotationBlueprints) {
                if (annot.getRefexColNid() == usRefexNid || annot.getRefexColNid() == gbRefexNid) {
                    annotationBlueprints.remove(annot);
                }
            }
            fsnBp.replaceAnnotationBlueprints(annotationBlueprints);
            addFsnDialectRefexes(fsnBp, dialect);
        }
    }

    public UUID getIsaType() {
        return isaType;
    }

    public void setIsaType(UUID isaType) {
        this.isaType = isaType;
        comupteComponentUuid();
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
        comupteComponentUuid();
    }

    public String getPreferredName() { //@akf todo : update to use set when NewConcept, etc. has been updated
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
        comupteComponentUuid();
    }

    /**
     * Adds a new preferred name.
     *
     * @param prefBp blueprint of pref name
     * @param dialect language code of pref name dialect
     */
    public void addPreferredName(DescCAB prefBp, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        prefNames.add(prefBp.getText());
        this.recomputeUuid();
        addPrefNameDialectRefexes(prefBp, dialect);
    }

    private void addPrefNameDialectRefexes(DescCAB prefBp, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        RefexCAB usAnnot;
        RefexCAB gbAnnot;
        if (dialect == LANG_CODE.EN) {
            usAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    prefBp.getComponentUuid(),
                    usRefexNid, null, null);
            usAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());

            gbAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    prefBp.getComponentUuid(),
                    gbRefexNid, null, null);
            gbAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            prefBp.setAnnotationBlueprint(usAnnot);
            prefBp.setAnnotationBlueprint(gbAnnot);
        } else if (dialect == LANG_CODE.EN_US) {
            usAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    prefBp.getComponentUuid(),
                    usRefexNid, null, null);
            usAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            prefBp.setAnnotationBlueprint(usAnnot);
        } else if (dialect == LANG_CODE.EN_GB) {
            gbAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    prefBp.getComponentUuid(),
                    gbRefexNid, null, null);
            gbAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            prefBp.setAnnotationBlueprint(gbAnnot);
        } else {
            throw new InvalidCAB("Dialect not supported: " + dialect.getFormatedLanguageCode());
        }
    }

    /**
     * Updates an existing preferred name.
     *
     * @param newPreferredName text to be updated
     * @param prefBp blueprint of pref name
     * @param dialect language code of pref name dialect, leave null if dialect
     * isn't changing
     */
    public void updatePreferredName(String newPreferredName, DescCAB prefBp, LANG_CODE dialect) throws
            NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        String oldText = prefBp.getText();
        prefNames.remove(oldText);
        prefNames.add(newPreferredName);
        this.recomputeUuid();
        prefBp.setText(newPreferredName);
        if (dialect != null) {
            List<RefexCAB> annotationBlueprints = prefBp.getAnnotationBlueprints();
            for (RefexCAB annot : annotationBlueprints) {
                if (annot.getRefexColNid() == usRefexNid || annot.getRefexColNid() == gbRefexNid) {
                    annotationBlueprints.remove(annot);
                }
            }
            prefBp.replaceAnnotationBlueprints(annotationBlueprints);
            addPrefNameDialectRefexes(prefBp, dialect);
        }
    }

    public boolean isDefined() {
        return defined;
    }

    public void setDefined(boolean defined) {
        this.defined = defined;
    }

    public boolean isInitialCaseSensitive() {
        return initialCaseSensitive;
    }

    public void setInitialCaseSensitive(boolean initialCaseSensitive) {
        this.initialCaseSensitive = initialCaseSensitive;
    }

    public DescCAB makeFsnCAB() throws IOException, InvalidCAB, ContradictionException {
        //get rf1/rf2 concepts
        UUID fsnUuid = null;
        if (Ts.get().usesRf2Metadata()) {
            fsnUuid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid();
        } else {
            fsnUuid = SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid();
        }
        return new DescCAB(
                getComponentUuid(),
                fsnUuid,
                LANG_CODE.getLangCode(lang),
                getFullySpecifiedName(),
                isInitialCaseSensitive());
    }

    public DescCAB makePreferredCAB() throws IOException, InvalidCAB, ContradictionException {
        //get rf1/rf2 concepts
        UUID synUuid = null;
        if (Ts.get().usesRf2Metadata()) {
            synUuid = SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid();
        } else {
            synUuid = SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid();
        }
        return new DescCAB(
                getComponentUuid(),
                synUuid, //from PREFERRED
                LANG_CODE.getLangCode(lang),
                getPreferredName(),
                isInitialCaseSensitive());
    }

    public List<RelCAB> getParentCABs() throws IOException, InvalidCAB, ContradictionException {
        List<RelCAB> parentCabs =
                new ArrayList<RelCAB>(getParents().size());
        for (UUID parentUuid : parents) {
            RelCAB parent = new RelCAB(
                    getComponentUuid(),
                    isaType,
                    parentUuid,
                    0,
                    TkRelType.STATED_HIERARCHY);
            parentCabs.add(parent);
        }
        return parentCabs;
    }

    public List<DescCAB> getFsnCABs() throws IOException, InvalidCAB, ContradictionException {
        if (fsnCABs.isEmpty()) {
            fsnCABs.add(makeFsnCAB());
        }
        return fsnCABs;
    }

    public List<DescCAB> getPrefCABs() throws IOException, InvalidCAB, ContradictionException {
        if (prefCABs.isEmpty()) {
            prefCABs.add(makePreferredCAB());
        }
        return prefCABs;
    }

    public List<DescCAB> getDescCABs() {
        return descCABs;
    }

    public List<RelCAB> getRelCABs() throws IOException, InvalidCAB, ContradictionException {
        if (relCABs.isEmpty()) {
            List<RelCAB> parentCABs = getParentCABs();
            for (RelCAB parentBp : parentCABs) {
                relCABs.add(parentBp);
            }
        }
        return relCABs;
    }

    public List<MediaCAB> getMediaCABs() {
        return mediaCABs;
    }

    public ConAttrAB getConAttrAB() {
        return conAttr;
    }

    public void setFsnCABs(DescCAB fsnBp) {
        fsnCABs.add(fsnBp);
    }

    public void setPrefCABs(DescCAB prefBp) {
        prefCABs.add(prefBp);
    }

    public void setDescCABs(DescCAB descBp) {
        descCABs.add(descBp);
    }

    public void setRelCABs(RelCAB relBp) {
        relCABs.add(relBp);
    }

    public void setMediaCABs(MediaCAB mediaBp) {
        mediaCABs.add(mediaBp);
    }

    public void setConAttrAB(ConAttrAB conAttrBp) {
        this.conAttr = conAttrBp;
    }
}
