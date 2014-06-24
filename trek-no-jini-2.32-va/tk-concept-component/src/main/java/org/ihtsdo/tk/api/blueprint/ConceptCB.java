/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
 * The Class ConceptCB contains methods for creating a concept blueprint. This
 * blueprint can be constructed into a type of
 * <code>ConceptChronicleBI</code>. This is the preferred method for creating
 * new concepts. Use ConceptAttributeAB to amend concept attributes if the
 * concept already exists.
 *
 * @see TerminologyBuilderBI
 * @see ConceptChronicleBI
 * @see ConceptAttributeAB
 * 
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

    /**
     * Gets the uuids of parent concept for this concept blueprint.
     *
     * @return the uuids of the parent concepts
     */
    public Collection<UUID> getParents() {
        return parents;
    }

    /**
     * Instantiates a new concept blueprint using uuid values to specify the new
     * concept.
     *
     * @param fullySpecifiedName the text to use for the fully specified name
     * @param preferredName the text to use for the preferred name
     * @param langCode the lang code representing the language of the
     * description
     * @param isaTypeUuid the uuid representing the relationship type to use for
     * specifying the parent concepts
     * @param parentUuids the uuids of the parent concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public ConceptCB(String fullySpecifiedName,
            String preferredName,
            LANG_CODE langCode,
            UUID isaTypeUuid,
            UUID... parentUuids) throws IOException, InvalidCAB, ContradictionException {
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
     * Instantiates a new concept blueprint using uuid values to specify the new
     * concept. Allows multiple fully specified names and preferred names to be
     * specified.
     *
     * @param fullySpecifiedNames a list of strings to use for the fully
     * specified names
     * @param preferredNames a list of strings to use for the preferred names
     * @param langCode the lang code representing the language of the
     * description
     * @param isaTypeUuid the uuid representing the relationship type to use for
     * specifying the parent concepts
     * @param parentUuids the uuids of the parent concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public ConceptCB(List<String> fullySpecifiedNames,
            List<String> preferredNames,
            LANG_CODE langCode,
            UUID isaTypeUuid,
            UUID... parentUuids) throws IOException, InvalidCAB, ContradictionException {
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
     * Instantiates a new concept blueprint based on the given
     * <code>conceptVersion</code>. Can specify a uuid for the new concept.
     *
     * @param conceptVersion the concept version to use to create this concept
     * blueprint
     * @param newConceptUuid the uuid representing the new concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
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
        for (RelationshipVersionBI rv : conceptVersion.getRelationshipsOutgoingActive()) {
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

    /**
     * Listens for a property change event in any of the component blueprint
     * classes and recomputes the concept blueprint's computed uuid if a
     * dependent component has changed.
     *
     * @param propertyChangeEvent the property change event
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
     * Computes the uuid for the concept represented by this concept blueprint
     * based on the fully specified names and preferred terms.
     *
     * @throws RuntimeException indicates a runtime exception has occurred
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

    /**
     * Resets the enclosing or source concepts for the components on this
     * concept. Then recomputes the uuids of the components based on the new
     * uuid of the concept.
     *
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding
     * exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
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
     * Gets the text of fully specified name associated with this concept
     * blueprint.
     *
     * @return the fully specified name text
     */
    public String getFullySpecifiedName() {//@akf todo : update to use set when NewConcept, etc. has been updated
        return fullySpecifiedName;
    }

    /**
     * Sets the text to use in the fully specified name (FSN) associated with
     * this concept blueprint. Recomputes the uuid associated with this concept
     * based on the updated FSN text.
     *
     * @param fullySpecifiedName the text to use for the fully specified name
     */
    public void setFullySpecifiedName(String fullySpecifiedName) {
        this.fullySpecifiedName = fullySpecifiedName;
        computeComponentUuid();
    }

    /**
     * Adds a description blueprint to use for the fully specified name (FSN)
     * description associated with this concept blueprint. Recomputes the uuid
     * associated with this concept based on the updated FSN text. Adds the
     * appropriate language/dialect refexes based on the given dialect code
     * (only supports en-us and en-gb). This method does not remove existing FSN
     * blueprints that are already associated with this concept blueprint.
     *
     * @param fullySpecifiedNameBlueprint the description blueprint for the
     * fully specified name description
     * @param dialect the language code representing the dialect of the FSN
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding
     * exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public void addFullySpecifiedName(DescriptionCAB fullySpecifiedNameBlueprint, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        fsns.add(fullySpecifiedNameBlueprint.getText());
        addFullySpecifiedNameDialectRefexes(fullySpecifiedNameBlueprint, dialect);
        this.recomputeUuid();
    }

    /**
     * Adds the appropriate dialect refexes to the fully specified name
     * description blueprint.
     *
     * @param fullySpecifiedNameBlueprint the fully specified name description
     * blueprint
     * @param dialect the dialect of the FSN, only supports en-gb and en-us
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding
     * exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
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
     * Updates an the text associated with the specified fully specified name
     * description blueprint. Removes previous dialect refexes associated with
     * the FSN blueprint and remakes them with the updated text.
     *
     * @param newFullySpecifiedName the new text to use for the update
     * @param fullySpecifiedNameBlueprint the FSN description blueprint to
     * update
     * @param dialect language code of FSN dialect, leave null if dialect isn't
     * changing
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding
     * exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
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
     * Gets the uuid of isa type to use for the parent relationships associated
     * with this concept blueprint.
     *
     * @return the isa type uuid
     */
    public UUID getIsaType() {
        return isaType;
    }

    /**
     * Sets the uuid of isa type to use for the parent relationships associated
     * with this concept blueprint.
     *
     * @param isaTypeUuid the isa type uuid
     */
    public void setIsaType(UUID isaTypeUuid) {
        this.isaType = isaTypeUuid;
        computeComponentUuid();
    }

    /**
     * Gets a two character abbreviation of the language of the descriptions
     * associated with this concept blueprint.
     *
     * @return a two character abbreviation of the language of the descriptions
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the language of the descriptions associated with this concept
     * blueprint.
     *
     * @param lang a two character abbreviation of the language of the
     * descriptions
     */
    public void setLang(String lang) {
        this.lang = lang;
        computeComponentUuid();
    }

    /**
     * Gets the text of the preferred name description associated with this
     * concept blueprint.
     *
     * @return the preferred name text
     */
    public String getPreferredName() { //@akf todo : update to use set when NewConcept, etc. has been updated
        return preferredName;
    }

    /**
     * Sets the text of the preferred name associated with this concept
     * blueprint.
     *
     * @param preferredName the new preferred name text
     */
    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
        computeComponentUuid();
    }

    /**
     * Adds a description blueprint to use for the preferred name description
     * associated with this concept blueprint. Recomputes the uuid associated
     * with this concept based on the updated preferred name text. Adds the
     * appropriate language/dialect refexes based on the given dialect code
     * (only supports en-us and en-gb). This method does not remove existing
     * preferred name blueprints that are already associated with this concept
     * blueprint.
     *
     * @param perferredNameBlueprint the description blueprint for the preferred
     * name description
     * @param dialect the language code representing the dialect of the
     * preferred term
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding
     * exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public void addPreferredName(DescriptionCAB perferredNameBlueprint, LANG_CODE dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        prefNames.add(perferredNameBlueprint.getText());
        this.recomputeUuid();
        addPreferredNameDialectRefexes(perferredNameBlueprint, dialect);
    }

    /**
     * Adds the appropriate dialect refexes to the preferred name description
     * blueprint.
     *
     * @param preferredBlueprint the preferred name description blueprint
     * @param dialect the dialect of the preferred name, only supports en-gb and
     * en-us
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding
     * exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
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
     * Updates an the text associated with the specified preferred name
     * description blueprint. Removes previous dialect refexes associated with
     * the preferred name blueprint and remakes them with the updated text.
     *
     * @param newPreferredName the new text to use for the update
     * @param preferredNameBlueprint the preferred name description blueprint to
     * update
     * @param dialect language code of preferred name dialect, leave null if
     * dialect isn't changing
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding
     * exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
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
     * Checks if this concept blueprint is marked as defined.
     *
     * @return <code>true</code>, if the concept is defined
     */
    public boolean isDefined() {
        return defined;
    }

    /**
     * Marks this concept blueprint as defined
     *
     * @param defined set to <code>true</code> if the concept is defined
     */
    public void setDefined(boolean defined) {
        this.defined = defined;
    }

    /**
     * Checks if the descriptions associated with this concept are marked as
     * initial case sensitive.
     *
     * @return <code>true</code>, if the descriptions are initial case sensitive
     */
    public boolean isInitialCaseSensitive() {
        return initialCaseSensitive;
    }

    /**
     * Marks the descriptions associated with this concept are marked as initial
     * case sensitive.
     *
     * @param initialCaseSensitive set to <code>true</code> to mark the
     * descriptions as initial case sensitive
     */
    public void setInitialCaseSensitive(boolean initialCaseSensitive) {
        this.initialCaseSensitive = initialCaseSensitive;
    }

    /**
     * Generates a description blueprint representing the fully specified name
     * of this blueprint.
     *
     * @return a description blueprint representing the fully specified name
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
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
     * Generates a description blueprint representing the preferred name of this
     * concept blueprint.
     *
     * @return a description blueprint representing the preferred name
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
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
                synUuid,
                LANG_CODE.getLangCode(lang),
                getPreferredName(),
                isInitialCaseSensitive());
    }

    /**
     * Generates relationship blueprints representing the parent relationships
     * of this concept blueprint.
     *
     * @return a list of relationship blueprints representing the parent
     * relationships
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
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
     * Returns a list of the fully specified name blueprints associated with
     * this concept blueprint. If no FSN blueprints are associated, one will be
     * generated based on the associated FSN text.
     *
     * @return a list of fully specified name blueprints
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public List<DescriptionCAB> getFullySpecifiedNameCABs() throws IOException, InvalidCAB, ContradictionException {
        if (fsnCABs.isEmpty()) {
            fsnCABs.add(makeFullySpecifiedNameCAB());
        }
        return fsnCABs;
    }

    /**
     * Gets a list of the preferred name blueprints associated with this concept
     * blueprint. If no preferred name blueprints are associated, one will be
     * generated based on the associated preferred name text.
     *
     * @return a list of preferred name blueprints
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public List<DescriptionCAB> getPreferredNameCABs() throws IOException, InvalidCAB, ContradictionException {
        if (prefCABs.isEmpty()) {
            prefCABs.add(makePreferredCAB());
        }
        return prefCABs;
    }

    /**
     * Gets the description blueprints associated with this concept blueprint.
     *
     * @return a list of description blueprints
     */
    public List<DescriptionCAB> getDescriptionCABs() {
        return descCABs;
    }

    /**
     * Gets a list of relationship blueprints associated with this concept
     * blueprint. If not relationships blueprints are associated, they will be
     * generated for the relationships to the associated parent concepts.
     *
     * @return a list of parent relationship blueprints
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public List<RelationshipCAB> getRelationshipCABs() throws IOException, InvalidCAB, ContradictionException {
        List<RelationshipCAB> parentCABs = getParentCABs();
        for (RelationshipCAB parentBp : parentCABs) {
            if (!relCABs.contains(parentBp)) {
                relCABs.add(parentBp);
            }
        }
        return relCABs;
    }

    /**
     * Gets the media blueprints associated with this concept blueprint.
     *
     * @return a list of media blueprints
     */
    public List<MediaCAB> getMediaCABs() {
        return mediaCABs;
    }

    /**
     * Gets the concept attribute blueprint associated with this concept
     * blueprint.
     *
     * @return the concept attribute blueprint
     */
    public ConceptAttributeAB getConceptAttributeAB() {
        return conAttr;
    }

    /**
     * Adds a fully specified name description blueprint to the list of
     * description blueprints associated with this concept blueprint.
     *
     * @param fullySpecifiedNameBlueprint the fully specified name blueprint to
     * add
     */
    public void addFullySpecifiedNameCAB(DescriptionCAB fullySpecifiedNameBlueprint) {
        fsnCABs.add(fullySpecifiedNameBlueprint);
    }

    /**
     * Adds a preferred name description blueprint to the list of description
     * blueprints associated with this concept blueprint.
     *
     * @param preferredNameBlueprint the preferred name blueprint to add
     */
    public void addPreferredNameCAB(DescriptionCAB preferredNameBlueprint) {
        prefCABs.add(preferredNameBlueprint);
    }

    /**
     * Adds a description blueprint to the list of description blueprints
     * associated with this concept blueprint.
     *
     * @param descriptionBlueprint the description blueprint to add
     */
    public void addDescriptionCAB(DescriptionCAB descriptionBlueprint) {
        descCABs.add(descriptionBlueprint);
    }

    /**
     * Adds a relationship blueprint to the list of relationship blueprints
     * associated with this concept blueprint.
     *
     * @param relationshipBlueprint the relationship blueprint to add
     */
    public void setRelationshipCAB(RelationshipCAB relationshipBlueprint) {
        relCABs.add(relationshipBlueprint);
    }

    /**
     * Adds a media blueprint to the list of media blueprints associated with
     * this concept blueprint.
     *
     * @param mediaBlueprint the media blueprint to add
     */
    public void addMediaCAB(MediaCAB mediaBlueprint) {
        mediaCABs.add(mediaBlueprint);
    }

    /**
     * Adds a concept attribute blueprint to the list of concept attribute
     * blueprints associated with this concept blueprint.
     *
     * @param conceptAttributeBlueprint the concept attribute blueprint to add
     */
    public void setConceptAttributeAB(ConceptAttributeAB conceptAttributeBlueprint) {
        this.conAttr = conceptAttributeBlueprint;
    }
}
