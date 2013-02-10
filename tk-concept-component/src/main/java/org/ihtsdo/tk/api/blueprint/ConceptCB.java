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
import static org.ihtsdo.tk.api.blueprint.IdDirective.GENERATE_HASH;
import static org.ihtsdo.tk.api.blueprint.IdDirective.GENERATE_REFEX_CONTENT_HASH;
import static org.ihtsdo.tk.api.blueprint.IdDirective.GENERATE_RANDOM;
import static org.ihtsdo.tk.api.blueprint.IdDirective.PRESERVE;
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
        public boolean remove(Object o) {
            boolean result = super.remove(o);
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
            LANG_CODE lang,
            UUID isaType,
            IdDirective idDirective,
            UUID... parents) throws IOException, InvalidCAB, ContradictionException {
        super(null, null, null, idDirective, RefexDirective.EXCLUDE);
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
        computeComponentUuid();
    }

    public ConceptCB(List<String> fullySpecifiedNames,
            List<String> preferredNames,
            LANG_CODE lang,
            UUID isaType,
            IdDirective idDirective,
            UUID... parents) throws IOException, InvalidCAB, ContradictionException {
        super(null, null, null, idDirective, RefexDirective.EXCLUDE);
        this.fsns = fullySpecifiedNames;
        this.prefNames = preferredNames;
        this.lang = lang.getFormatedLanguageCode();
        this.isaType = isaType;
        if (parents != null) {
            this.parents.addAll(Arrays.asList(parents));
        }
        pcs.addPropertyChangeListener(this);
        computeComponentUuid();
    }

    public ConceptCB(ConceptVersionBI cv, UUID newConceptUuid,
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
        super(null, cv, cv.getViewCoordinate(), idDirective, refexDirective);
        pcs.addPropertyChangeListener(this);
        UUID uuid = getComponentUuid();
//        ConceptCB conceptCAB = cv.makeBlueprint();
        ConAttrAB conAttrBp = cv.getConAttrsActive().makeBlueprint(cv.getViewCoordinate(), idDirective, refexDirective);
        for (DescriptionVersionBI dv : cv.getFsnDescsActive()) {
            fsns.add(dv.getText());
            DescCAB fsnBp = dv.makeBlueprint(cv.getViewCoordinate(), idDirective, refexDirective);
            fsnCABs.add(fsnBp);
            descCABs.add(fsnBp);
            fsnBp.getAnnotationBlueprintsFromOriginal();
        }
        for (DescriptionVersionBI dv : cv.getPrefDescsActive()) {
            prefNames.add(dv.getText());
            DescCAB prefBp = dv.makeBlueprint(cv.getViewCoordinate(), idDirective, refexDirective);
            prefCABs.add(prefBp);
            descCABs.add(prefBp);
            prefBp.getAnnotationBlueprintsFromOriginal();
        }
        for (DescriptionVersionBI dv : cv.getDescsActive()) {
            if (cv.getFsnDescsActive().contains(dv) || cv.getPrefDescsActive().contains(dv)) {
                continue;
            }
            DescCAB descBp = dv.makeBlueprint(cv.getViewCoordinate(), idDirective, refexDirective);
            descCABs.add(descBp);
            descBp.getAnnotationBlueprintsFromOriginal();
        }
        for (RelationshipVersionBI rv : cv.getRelsOutgoingActive()) {
            if (rv.getCharacteristicNid() == SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid()
                    || rv.getCharacteristicNid() == SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid()
                    || rv.getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
                continue;
            }
            RelCAB relBp = rv.makeBlueprint(cv.getViewCoordinate(), idDirective, refexDirective);
            relCABs.add(relBp);
            relBp.getAnnotationBlueprintsFromOriginal();
        }
        for (MediaVersionBI mv : cv.getMediaActive()) {
            MediaCAB mediaBp = mv.makeBlueprint(cv.getViewCoordinate(), idDirective, refexDirective);
            mediaCABs.add(mediaBp);
            mediaBp.getAnnotationBlueprintsFromOriginal();
        }
        this.setComponentUuid(newConceptUuid);
    }

    @Override
    public void setComponentUuid(UUID componentUuid) {
        setConceptComponentsConceptUuid(componentUuid);
        super.setComponentUuid(componentUuid); 
    }

    @Override
    public void setComponentUuidNoRecompute(UUID componentUuid) {
        setConceptComponentsConceptUuid(componentUuid);
        super.setComponentUuidNoRecompute(componentUuid); //To change body of generated methods, choose Tools | Templates.
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

    public final void computeComponentUuid() throws RuntimeException {
        switch (idDirective) {
            case GENERATE_HASH:
            case GENERATE_REFEX_CONTENT_HASH:
                StringBuilder sb = new StringBuilder();
                List<String> descs = new ArrayList<String>();
                descs.addAll(fsns);
                descs.addAll(prefNames);
                java.util.Collections.sort(descs);
                for (String desc : descs) {
                    sb.append(desc);
                }
                try {
                    setComponentUuid(
                            UuidT5Generator.get(conceptSpecNamespace,
                            sb.toString()));
                } catch (NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
                break;
                
            case GENERATE_RANDOM:
            case GENERATE_RANDOM_CONCEPT_REST_HASH:
                setComponentUuid(UUID.randomUUID());
                break;
                
            case PRESERVE_CONCEPT_REST_HASH:
            case PRESERVE:
                default:
                    // nothing to generate. 
                

        }
    }

    @Override
    public void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException,
            IOException, InvalidCAB, ContradictionException {
        switch (idDirective) {
            case GENERATE_HASH:
            case GENERATE_REFEX_CONTENT_HASH:
                computeComponentUuid();
                break;
            case GENERATE_RANDOM:
            case GENERATE_RANDOM_CONCEPT_REST_HASH:
                setComponentUuidNoRecompute(UUID.randomUUID());
                break;

            case PRESERVE_CONCEPT_REST_HASH:
            case PRESERVE:
            default:
            // nothing to do...

        }


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
        computeComponentUuid();
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
                    usRefexNid, null, null, idDirective, refexDirective);
            usAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());

            gbAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    fsnBp.getComponentUuid(),
                    gbRefexNid, null, null, idDirective, refexDirective);
            gbAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            fsnBp.setAnnotationBlueprint(usAnnot);
            fsnBp.setAnnotationBlueprint(gbAnnot);
        } else if (dialect == LANG_CODE.EN_US) {
            usAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    fsnBp.getComponentUuid(),
                    usRefexNid, null, null, idDirective, refexDirective);
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
                    usRefexNid, null, null, idDirective, refexDirective);
            usAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());

            gbAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    prefBp.getComponentUuid(),
                    gbRefexNid, null, null, idDirective, refexDirective);
            gbAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            prefBp.setAnnotationBlueprint(usAnnot);
            prefBp.setAnnotationBlueprint(gbAnnot);
        } else if (dialect == LANG_CODE.EN_US) {
            usAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    prefBp.getComponentUuid(),
                    usRefexNid, null, null, idDirective, refexDirective);
            usAnnot.put(RefexCAB.RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            prefBp.setAnnotationBlueprint(usAnnot);
        } else if (dialect == LANG_CODE.EN_GB) {
            gbAnnot = new RefexCAB(TK_REFSET_TYPE.CID,
                    prefBp.getComponentUuid(),
                    gbRefexNid, null, null, idDirective, refexDirective);
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

    public DescCAB makeFsnCAB(IdDirective idDirective) throws IOException, InvalidCAB, ContradictionException {
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
                isInitialCaseSensitive(), idDirective);
    }

    public DescCAB makePreferredCAB(IdDirective idDirective) throws IOException, InvalidCAB, ContradictionException {
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
                isInitialCaseSensitive(), idDirective);
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
                    TkRelType.STATED_HIERARCHY, idDirective);
            parentCabs.add(parent);
        }
        return parentCabs;
    }

    public List<DescCAB> getFsnCABs() throws IOException, InvalidCAB, ContradictionException {
        if (fsnCABs.isEmpty()) {
            fsnCABs.add(makeFsnCAB(idDirective));
        }
        return fsnCABs;
    }

    public List<DescCAB> getPrefCABs() throws IOException, InvalidCAB, ContradictionException {
        if (prefCABs.isEmpty()) {
            prefCABs.add(makePreferredCAB(idDirective));
        }
        return prefCABs;
    }

    public List<DescCAB> getDescCABs() {
        return descCABs;
    }

    public List<RelCAB> getRelCABs() throws IOException, InvalidCAB, ContradictionException {
        List<RelCAB> parentCABs = getParentCABs();
//        if (relCABs.size() != parentCABs.size()) {
//            relCABs.clear();
        for (RelCAB parentBp : parentCABs) {
            if (!relCABs.contains(parentBp)) {
                relCABs.add(parentBp);
            }
//            }
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

    private void setConceptComponentsConceptUuid(UUID componentUuid) {
        for (DescCAB descCAB: descCABs) {
            descCAB.setConceptUuid(componentUuid);
        }
        for (DescCAB descCAB: fsnCABs) {
            descCAB.setConceptUuid(componentUuid);
        }
        for (RelCAB relCAB: relCABs) {
            relCAB.setSourceUuid(componentUuid);
        }
        for (MediaCAB mediaCAB: mediaCABs) {
            mediaCAB.setConceptUuid(componentUuid);
        }
        if (conAttr != null) {
            conAttr.setComponentUuid(componentUuid);
        }
    }
}
