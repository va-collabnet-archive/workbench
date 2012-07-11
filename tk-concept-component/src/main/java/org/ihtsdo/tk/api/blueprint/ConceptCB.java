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
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;
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
    private List<DescriptionCAB> fsnCABs = new ArrayList<DescriptionCAB>();
    private List<DescriptionCAB> prefCABs = new ArrayList<DescriptionCAB>();
    private List<DescriptionCAB> descCABs = new ArrayList<DescriptionCAB>();
    private List<RelationshipCAB> relCABs = new ArrayList<RelationshipCAB>();
    private List<MediaCAB> mediaCABs = new ArrayList<MediaCAB>();
    private ConceptAttributeAB conAttr;
    private int usRefexNid = SnomedMetadataRfx.getUS_DIALECT_REFEX_NID();
    private int gbRefexNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
    private Collection<UUID> parents = new TreeSet<UUID>() {

        @Override
        public boolean add(UUID e) {
            boolean result = super.add(e);
            computeComponentUuid();
            return result;
        }

        @Override
        public boolean addAll(Collection<? extends UUID> clctn) {
            boolean result = super.addAll(clctn);
            computeComponentUuid();
            return result;
        }

        @Override
        public boolean remove(Object obj) {
            boolean result = super.remove(obj);
            computeComponentUuid();
            return result;
        }

        @Override
        public boolean removeAll(Collection<?> clctn) {
            boolean result = super.removeAll(clctn);
            computeComponentUuid();
            return result;
        }
    };

    public Collection<UUID> getParents() {
        return parents;
    }

    public ConceptCB(String fullySpecifiedName,
            String preferredName,
            LANG_CODE langCode,
            UUID isaTypeUuid,
            UUID... parentUuids) throws IOException, InvalidCAB, ContradictionException{
        super(null, null, null);
        this.fsns.add(fullySpecifiedName);
        this.fullySpecifiedName = fullySpecifiedName; //@akf todo: these should be removed when NewConcept, etc. is upated
        this.prefNames.add(preferredName);
        this.preferredName = preferredName; //@akf todo: these should be removed when NewConcept, etc. is upated
        this.lang = langCode.getFormatedLanguageCode();
        this.isaType = isaTypeUuid;
        if (parentUuids != null) {
            this.parents.addAll(Arrays.asList(parentUuids));
        }
        pcs.addPropertyChangeListener(this);
        computeComponentUuid();
    }

    public ConceptCB(List<String> fullySpecifiedNames,
            List<String> preferredNames,
            LANG_CODE langCode,
            UUID isaTypeUuid,
            UUID... parentUuids) throws IOException, InvalidCAB, ContradictionException{
        super(null, null, null);
        this.fsns = fullySpecifiedNames;
        this.prefNames = preferredNames;
        this.lang = langCode.getFormatedLanguageCode();
        this.isaType = isaTypeUuid;
        if (parentUuids != null) {
            this.parents.addAll(Arrays.asList(parentUuids));
        }
        pcs.addPropertyChangeListener(this);
        computeComponentUuid();
    }

    public ConceptCB(ConceptVersionBI conceptVersion, UUID newConceptUuid) throws IOException, ContradictionException, InvalidCAB {
        super(null, conceptVersion, conceptVersion.getViewCoordinate());
        pcs.addPropertyChangeListener(this);
        UUID uuid = getComponentUuid();
        ConceptAttributeAB conAttrBp = conceptVersion.getConceptAttributesActive().makeBlueprint(conceptVersion.getViewCoordinate());
        for (DescriptionVersionBI dv : conceptVersion.getDescriptionsFullySpecifiedActive()) {
            fsns.add(dv.getText());
            DescriptionCAB fsnBp = dv.makeBlueprint(conceptVersion.getViewCoordinate());
            fsnCABs.add(fsnBp);
            descCABs.add(fsnBp);
            fsnBp.getAnnotationBlueprintsFromOriginal();
        }
        for (DescriptionVersionBI dv : conceptVersion.getDescriptionsPreferredActive()) {
            prefNames.add(dv.getText());
            DescriptionCAB prefBp = dv.makeBlueprint(conceptVersion.getViewCoordinate());
            prefCABs.add(prefBp);
            descCABs.add(prefBp);
            prefBp.getAnnotationBlueprintsFromOriginal();
        }
        for (DescriptionVersionBI dv : conceptVersion.getDescriptionsActive()) {
            if (conceptVersion.getDescriptionsFullySpecifiedActive().contains(dv) || conceptVersion.getDescriptionsPreferredActive().contains(dv)) {
                continue;
            }
            DescriptionCAB descBp = dv.makeBlueprint(conceptVersion.getViewCoordinate());
            descCABs.add(descBp);
            descBp.getAnnotationBlueprintsFromOriginal();
        }
        for (RelationshipVersionBI rv : conceptVersion.getRelationshipsSourceActive()) {
            if (rv.getCharacteristicNid() == SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid()
                    || rv.getCharacteristicNid() == SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid()
                    || rv.getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
                continue;
            }
            RelationshipCAB relBp = rv.makeBlueprint(conceptVersion.getViewCoordinate());
            relCABs.add(relBp);
            relBp.getAnnotationBlueprintsFromOriginal();
        }
        for (MediaVersionBI mv : conceptVersion.getMediaActive()) {
            MediaCAB mediaBp = mv.makeBlueprint(conceptVersion.getViewCoordinate());
            mediaCABs.add(mediaBp);
            mediaBp.getAnnotationBlueprintsFromOriginal();
        }
        this.setComponentUuid(newConceptUuid);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
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

    public final void computeComponentUuid() throws RuntimeException {
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
        for (DescriptionCAB descBp : getDescriptionCABs()) {
            descBp.setConceptUuid(getComponentUuid());
            descBp.recomputeUuid();
        }
        for (RelationshipCAB relBp : getRelationshipCABs()) {
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
        computeComponentUuid();
    }

    /**
     * Adds an fsn.
     *
     * @param fullySpecifiedNameBlueprint blueprint of fsn
     * @param dialect language code of fsn dialect
     */
    public void addFullySpecifiedName(DescriptionCAB fullySpecifiedNameBlueprint, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        fsns.add(fullySpecifiedNameBlueprint.getText());
        addFullySpecifiedNameDialectRefexes(fullySpecifiedNameBlueprint, dialect);
        this.recomputeUuid();
    }

    private void addFullySpecifiedNameDialectRefexes(DescriptionCAB fullySpecifiedNameBlueprint, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        RefexCAB usAnnot;
        RefexCAB gbAnnot;
        if (dialect == LANG_CODE.EN) {
            usAnnot = new RefexCAB(TK_REFEX_TYPE.CID,
                    fullySpecifiedNameBlueprint.getComponentUuid(),
                    usRefexNid, null, null);
            usAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());

            gbAnnot = new RefexCAB(TK_REFEX_TYPE.CID,
                    fullySpecifiedNameBlueprint.getComponentUuid(),
                    gbRefexNid, null, null);
            gbAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            fullySpecifiedNameBlueprint.addAnnotationBlueprint(usAnnot);
            fullySpecifiedNameBlueprint.addAnnotationBlueprint(gbAnnot);
        } else if (dialect == LANG_CODE.EN_US) {
            usAnnot = new RefexCAB(TK_REFEX_TYPE.CID,
                    fullySpecifiedNameBlueprint.getComponentUuid(),
                    usRefexNid, null, null);
            usAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            fullySpecifiedNameBlueprint.addAnnotationBlueprint(usAnnot);
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
     * @param newFullySpecifiedName text to be updated
     * @param fullySpecifiedNameBlueprint blueprint of fsn
     * @param dialect language code of fsn dialect, leave null if dialect isn't
     * changing
     */
    public void updateFullySpecifiedName(String newFullySpecifiedName, DescriptionCAB fullySpecifiedNameBlueprint, LANG_CODE dialect) throws
            NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        String oldText = fullySpecifiedNameBlueprint.getText();
        fsns.remove(oldText);
        fsns.add(newFullySpecifiedName);
        this.recomputeUuid();
        fullySpecifiedNameBlueprint.setText(newFullySpecifiedName);
        if (dialect != null) {
            List<RefexCAB> annotationBlueprints = fullySpecifiedNameBlueprint.getAnnotationBlueprints();
            for (RefexCAB annot : annotationBlueprints) {
                if (annot.getRefexCollectionNid() == usRefexNid || annot.getRefexCollectionNid() == gbRefexNid) {
                    annotationBlueprints.remove(annot);
                }
            }
            fullySpecifiedNameBlueprint.replaceAnnotationBlueprints(annotationBlueprints);
            addFullySpecifiedNameDialectRefexes(fullySpecifiedNameBlueprint, dialect);
        }
    }

    public UUID getIsaType() {
        return isaType;
    }

    public void setIsaType(UUID isaTypeUuid) {
        this.isaType = isaTypeUuid;
        computeComponentUuid();
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
        computeComponentUuid();
    }

    public String getPreferredName() { //@akf todo : update to use set when NewConcept, etc. has been updated
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
        computeComponentUuid();
    }

    /**
     * Adds a new preferred name.
     *
     * @param perferredNameBlueprint blueprint of pref name
     * @param dialect language code of pref name dialect
     */
    public void addPreferredName(DescriptionCAB perferredNameBlueprint, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        prefNames.add(perferredNameBlueprint.getText());
        this.recomputeUuid();
        addPreferredNameDialectRefexes(perferredNameBlueprint, dialect);
    }

    private void addPreferredNameDialectRefexes(DescriptionCAB preferredBlueprint, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        RefexCAB usAnnot;
        RefexCAB gbAnnot;
        if (dialect == LANG_CODE.EN) {
            usAnnot = new RefexCAB(TK_REFEX_TYPE.CID,
                    preferredBlueprint.getComponentUuid(),
                    usRefexNid, null, null);
            usAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());

            gbAnnot = new RefexCAB(TK_REFEX_TYPE.CID,
                    preferredBlueprint.getComponentUuid(),
                    gbRefexNid, null, null);
            gbAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            preferredBlueprint.addAnnotationBlueprint(usAnnot);
            preferredBlueprint.addAnnotationBlueprint(gbAnnot);
        } else if (dialect == LANG_CODE.EN_US) {
            usAnnot = new RefexCAB(TK_REFEX_TYPE.CID,
                    preferredBlueprint.getComponentUuid(),
                    usRefexNid, null, null);
            usAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            preferredBlueprint.addAnnotationBlueprint(usAnnot);
        } else if (dialect == LANG_CODE.EN_GB) {
            gbAnnot = new RefexCAB(TK_REFEX_TYPE.CID,
                    preferredBlueprint.getComponentUuid(),
                    gbRefexNid, null, null);
            gbAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            preferredBlueprint.addAnnotationBlueprint(gbAnnot);
        } else {
            throw new InvalidCAB("Dialect not supported: " + dialect.getFormatedLanguageCode());
        }
    }

    /**
     * Updates an existing preferred name.
     *
     * @param newPreferredName text to be updated
     * @param preferredNameBlueprint blueprint of pref name
     * @param dialect language code of pref name dialect, leave null if dialect
     * isn't changing
     */
    public void updatePreferredName(String newPreferredName, DescriptionCAB preferredNameBlueprint, LANG_CODE dialect) throws
            NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        String oldText = preferredNameBlueprint.getText();
        prefNames.remove(oldText);
        prefNames.add(newPreferredName);
        this.recomputeUuid();
        preferredNameBlueprint.setText(newPreferredName);
        if (dialect != null) {
            List<RefexCAB> annotationBlueprints = preferredNameBlueprint.getAnnotationBlueprints();
            for (RefexCAB annot : annotationBlueprints) {
                if (annot.getRefexCollectionNid() == usRefexNid || annot.getRefexCollectionNid() == gbRefexNid) {
                    annotationBlueprints.remove(annot);
                }
            }
            preferredNameBlueprint.replaceAnnotationBlueprints(annotationBlueprints);
            addPreferredNameDialectRefexes(preferredNameBlueprint, dialect);
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

    public DescriptionCAB makeFullySpecifiedNameCAB() throws IOException, InvalidCAB, ContradictionException {
        //get rf1/rf2 concepts
        UUID fsnUuid = null;
        if (Ts.get().usesRf2Metadata()) {
            fsnUuid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid();
        } else {
            fsnUuid = SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid();
        }
        return new DescriptionCAB(
                getComponentUuid(),
                fsnUuid,
                LANG_CODE.getLangCode(lang),
                getFullySpecifiedName(),
                isInitialCaseSensitive());
    }

    public DescriptionCAB makePreferredCAB() throws IOException, InvalidCAB, ContradictionException {
        //get rf1/rf2 concepts
        UUID synUuid = null;
        if (Ts.get().usesRf2Metadata()) {
            synUuid = SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid();
        } else {
            synUuid = SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid();
        }
        return new DescriptionCAB(
                getComponentUuid(),
                synUuid, //from PREFERRED
                LANG_CODE.getLangCode(lang),
                getPreferredName(),
                isInitialCaseSensitive());
    }

    public List<RelationshipCAB> getParentCABs() throws IOException, InvalidCAB, ContradictionException {
        List<RelationshipCAB> parentCabs =
                new ArrayList<RelationshipCAB>(getParents().size());
        for (UUID parentUuid : parents) {
            RelationshipCAB parent = new RelationshipCAB(
                    getComponentUuid(),
                    isaType,
                    parentUuid,
                    0,
                    TkRelationshipType.STATED_HIERARCHY);
            parentCabs.add(parent);
        }
        return parentCabs;
    }

    public List<DescriptionCAB> getFullySpecifiedNameCABs() throws IOException, InvalidCAB, ContradictionException {
        if (fsnCABs.isEmpty()) {
            fsnCABs.add(makeFullySpecifiedNameCAB());
        }
        return fsnCABs;
    }

    public List<DescriptionCAB> getPreferredNameCABs() throws IOException, InvalidCAB, ContradictionException {
        if (prefCABs.isEmpty()) {
            prefCABs.add(makePreferredCAB());
        }
        return prefCABs;
    }

    public List<DescriptionCAB> getDescriptionCABs() {
        return descCABs;
    }

    public List<RelationshipCAB> getRelationshipCABs() throws IOException, InvalidCAB, ContradictionException {
        List<RelationshipCAB> parentCABs = getParentCABs();
        for (RelationshipCAB parentBp : parentCABs) {
            if(!relCABs.contains(parentBp)){
                 relCABs.add(parentBp);
            }
        }
        return relCABs;
    }

    public List<MediaCAB> getMediaCABs() {
        return mediaCABs;
    }

    public ConceptAttributeAB getConceptAttributteAB() {
        return conAttr;
    }

    public void addFullySpecifiedNameCAB(DescriptionCAB fullySpecifiedNameBlueprint) {
        fsnCABs.add(fullySpecifiedNameBlueprint);
    }

    public void addPreferredNameCAB(DescriptionCAB preferredNameBlueprint) {
        prefCABs.add(preferredNameBlueprint);
    }

    public void addDescriptionCAB(DescriptionCAB descriptionBlueprint) {
        descCABs.add(descriptionBlueprint);
    }

    public void setRelationshipCAB(RelationshipCAB relationshipBlueprint) {
        relCABs.add(relationshipBlueprint);
    }

    public void addMediaCAB(MediaCAB mediaBlueprint) {
        mediaCABs.add(mediaBlueprint);
    }

    public void setConceptAttributeAB(ConceptAttributeAB conceptAttributeBlueprint) {
        this.conAttr = conceptAttributeBlueprint;
    }
}
