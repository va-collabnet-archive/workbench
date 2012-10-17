/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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

// TODO: Auto-generated Javadoc
/**
 * The Class ConceptCB.
 *
 * @author kec
 */
public final class ConceptCB extends CreateOrAmendBlueprint {

    /** The Constant conceptSpecNamespace. */
    public static final UUID conceptSpecNamespace =
            UUID.fromString("620d1f30-5285-11e0-b8af-0800200c9a66");
    
    /** The fully specified name. */
    private String fullySpecifiedName;
    
    /** The preferred name. */
    private String preferredName;
    
    /** The fsns. */
    private List<String> fsns = new ArrayList<String>();
    
    /** The pref names. */
    private List<String> prefNames = new ArrayList<String>();
    
    /** The initial case sensitive. */
    private boolean initialCaseSensitive = false;
    
    /** The lang. */
    private String lang;
    
    /** The isa type. */
    private UUID isaType;
    
    /** The defined. */
    private boolean defined;
    
    /** The fsn ca bs. */
    private List<DescriptionCAB> fsnCABs = new ArrayList<DescriptionCAB>();
    
    /** The pref ca bs. */
    private List<DescriptionCAB> prefCABs = new ArrayList<DescriptionCAB>();
    
    /** The desc ca bs. */
    private List<DescriptionCAB> descCABs = new ArrayList<DescriptionCAB>();
    
    /** The rel ca bs. */
    private List<RelationshipCAB> relCABs = new ArrayList<RelationshipCAB>();
    
    /** The media ca bs. */
    private List<MediaCAB> mediaCABs = new ArrayList<MediaCAB>();
    
    /** The con attr. */
    private ConceptAttributeAB conAttr;
    
    /** The us refex nid. */
    private int usRefexNid = SnomedMetadataRfx.getUS_DIALECT_REFEX_NID();
    
    /** The gb refex nid. */
    private int gbRefexNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
    
    /** The parents. */
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

    /**
     * Gets the parents.
     *
     * @return the parents
     */
    public Collection<UUID> getParents() {
        return parents;
    }

    /**
     * Instantiates a new concept cb.
     *
     * @param fullySpecifiedName the fully specified name
     * @param preferredName the preferred name
     * @param langCode the lang code
     * @param isaTypeUuid the isa type uuid
     * @param parentUuids the parent uuids
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
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

    /**
     * Instantiates a new concept cb.
     *
     * @param fullySpecifiedNames the fully specified names
     * @param preferredNames the preferred names
     * @param langCode the lang code
     * @param isaTypeUuid the isa type uuid
     * @param parentUuids the parent uuids
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
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

    /**
     * Instantiates a new concept cb.
     *
     * @param conceptVersion the concept version
     * @param newConceptUuid the new concept uuid
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException the contradiction exception
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     */
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint#propertyChange(java.beans.PropertyChangeEvent)
     */
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

    /**
     * Compute component uuid.
     *
     * @throws RuntimeException the runtime exception
     */
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint#recomputeUuid()
     */
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

    /**
     * Gets the fully specified name.
     *
     * @return the fully specified name
     */
    public String getFullySpecifiedName() {//@akf todo : update to use set when NewConcept, etc. has been updated
        return fullySpecifiedName;
    }

    /**
     * Sets the fully specified name.
     *
     * @param fullySpecifiedName the new fully specified name
     */
    public void setFullySpecifiedName(String fullySpecifiedName) {
        this.fullySpecifiedName = fullySpecifiedName;
        computeComponentUuid();
    }

    /**
     * Adds an fsn.
     *
     * @param fullySpecifiedNameBlueprint blueprint of fsn
     * @param dialect language code of fsn dialect
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
    public void addFullySpecifiedName(DescriptionCAB fullySpecifiedNameBlueprint, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        fsns.add(fullySpecifiedNameBlueprint.getText());
        addFullySpecifiedNameDialectRefexes(fullySpecifiedNameBlueprint, dialect);
        this.recomputeUuid();
    }

    /**
     * Adds the fully specified name dialect refexes.
     *
     * @param fullySpecifiedNameBlueprint the fully specified name blueprint
     * @param dialect the dialect
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
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
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
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

    /**
     * Gets the isa type.
     *
     * @return the isa type
     */
    public UUID getIsaType() {
        return isaType;
    }

    /**
     * Sets the isa type.
     *
     * @param isaTypeUuid the new isa type
     */
    public void setIsaType(UUID isaTypeUuid) {
        this.isaType = isaTypeUuid;
        computeComponentUuid();
    }

    /**
     * Gets the lang.
     *
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the lang.
     *
     * @param lang the new lang
     */
    public void setLang(String lang) {
        this.lang = lang;
        computeComponentUuid();
    }

    /**
     * Gets the preferred name.
     *
     * @return the preferred name
     */
    public String getPreferredName() { //@akf todo : update to use set when NewConcept, etc. has been updated
        return preferredName;
    }

    /**
     * Sets the preferred name.
     *
     * @param preferredName the new preferred name
     */
    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
        computeComponentUuid();
    }

    /**
     * Adds a new preferred name.
     *
     * @param perferredNameBlueprint blueprint of pref name
     * @param dialect language code of pref name dialect
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
    public void addPreferredName(DescriptionCAB perferredNameBlueprint, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        prefNames.add(perferredNameBlueprint.getText());
        this.recomputeUuid();
        addPreferredNameDialectRefexes(perferredNameBlueprint, dialect);
    }

    /**
     * Adds the preferred name dialect refexes.
     *
     * @param preferredBlueprint the preferred blueprint
     * @param dialect the dialect
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
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
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
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

    /**
     * Checks if is defined.
     *
     * @return <code>true</code>, if is defined
     */
    public boolean isDefined() {
        return defined;
    }

    /**
     * Sets the defined.
     *
     * @param defined the new defined
     */
    public void setDefined(boolean defined) {
        this.defined = defined;
    }

    /**
     * Checks if is initial case sensitive.
     *
     * @return <code>true</code>, if is initial case sensitive
     */
    public boolean isInitialCaseSensitive() {
        return initialCaseSensitive;
    }

    /**
     * Sets the initial case sensitive.
     *
     * @param initialCaseSensitive the new initial case sensitive
     */
    public void setInitialCaseSensitive(boolean initialCaseSensitive) {
        this.initialCaseSensitive = initialCaseSensitive;
    }

    /**
     * Make fully specified name cab.
     *
     * @return the description cab
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
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

    /**
     * Make preferred cab.
     *
     * @return the description cab
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
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

    /**
     * Gets the parent ca bs.
     *
     * @return the parent ca bs
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
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

    /**
     * Gets the fully specified name ca bs.
     *
     * @return the fully specified name ca bs
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
    public List<DescriptionCAB> getFullySpecifiedNameCABs() throws IOException, InvalidCAB, ContradictionException {
        if (fsnCABs.isEmpty()) {
            fsnCABs.add(makeFullySpecifiedNameCAB());
        }
        return fsnCABs;
    }

    /**
     * Gets the preferred name ca bs.
     *
     * @return the preferred name ca bs
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
    public List<DescriptionCAB> getPreferredNameCABs() throws IOException, InvalidCAB, ContradictionException {
        if (prefCABs.isEmpty()) {
            prefCABs.add(makePreferredCAB());
        }
        return prefCABs;
    }

    /**
     * Gets the description ca bs.
     *
     * @return the description ca bs
     */
    public List<DescriptionCAB> getDescriptionCABs() {
        return descCABs;
    }

    /**
     * Gets the relationship ca bs.
     *
     * @return the relationship ca bs
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
    public List<RelationshipCAB> getRelationshipCABs() throws IOException, InvalidCAB, ContradictionException {
        List<RelationshipCAB> parentCABs = getParentCABs();
        for (RelationshipCAB parentBp : parentCABs) {
            if(!relCABs.contains(parentBp)){
                 relCABs.add(parentBp);
            }
        }
        return relCABs;
    }

    /**
     * Gets the media ca bs.
     *
     * @return the media ca bs
     */
    public List<MediaCAB> getMediaCABs() {
        return mediaCABs;
    }

    /**
     * Gets the concept attributte ab.
     *
     * @return the concept attributte ab
     */
    public ConceptAttributeAB getConceptAttributteAB() {
        return conAttr;
    }

    /**
     * Adds the fully specified name cab.
     *
     * @param fullySpecifiedNameBlueprint the fully specified name blueprint
     */
    public void addFullySpecifiedNameCAB(DescriptionCAB fullySpecifiedNameBlueprint) {
        fsnCABs.add(fullySpecifiedNameBlueprint);
    }

    /**
     * Adds the preferred name cab.
     *
     * @param preferredNameBlueprint the preferred name blueprint
     */
    public void addPreferredNameCAB(DescriptionCAB preferredNameBlueprint) {
        prefCABs.add(preferredNameBlueprint);
    }

    /**
     * Adds the description cab.
     *
     * @param descriptionBlueprint the description blueprint
     */
    public void addDescriptionCAB(DescriptionCAB descriptionBlueprint) {
        descCABs.add(descriptionBlueprint);
    }

    /**
     * Sets the relationship cab.
     *
     * @param relationshipBlueprint the new relationship cab
     */
    public void setRelationshipCAB(RelationshipCAB relationshipBlueprint) {
        relCABs.add(relationshipBlueprint);
    }

    /**
     * Adds the media cab.
     *
     * @param mediaBlueprint the media blueprint
     */
    public void addMediaCAB(MediaCAB mediaBlueprint) {
        mediaCABs.add(mediaBlueprint);
    }

    /**
     * Sets the concept attribute ab.
     *
     * @param conceptAttributeBlueprint the new concept attribute ab
     */
    public void setConceptAttributeAB(ConceptAttributeAB conceptAttributeBlueprint) {
        this.conAttr = conceptAttributeBlueprint;
    }
}
