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
package org.ihtsdo.helper.refex;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * Used to add or retire components from a refex.
 *
 * @author akf
 */
public class RefexHelper {

    /**
     * The
     * <code>ViewCoordinate</code> used in making changes.
     */
    public ViewCoordinate vc;
    /**
     * The
     * <code>EditCoordinate</code> used in making changes.
     */
    public EditCoordinate ec;
    /**
     * Set to
     * <code>true</code> to automatically add changes as uncommitted, otherwise
     * false.
     */
    public boolean autoAddUncommitted;
    /**
     * Set to
     * <code>true</code> to automatically commit changes, otherwise false.
     */
    public boolean autoCommit;
    private TerminologyBuilderBI builder;

    /**
     * Constructs the
     * <code>RefexHelper</code> for added components to refexes and allows for
     * changes to be added as uncommitted, or committed, automatically. If many
     * refexes are being added to the same concept, it is better for performance
     * to make the changes and then call addUncommitted after the changes are
     * made. Set the auto values to true if only one change is being made per
     * concept. If
     * <code>autoCommit</code> is set to true, then
     * <code>autoAddUncomitted</code> must be true as well.
     *
     * @param vc <code>ViewCoordinate</code>
     * @param ec <code>EditCoordinate</code>
     * @param autoAddUncommitted set to true to have changes added as
     * uncommitted
     * @param autoCommit set to true to have changes committed. Must have
     * autoAddUncommitted set to true as well.
     */
    public RefexHelper(ViewCoordinate vc, EditCoordinate ec, boolean autoAddUncommitted, boolean autoCommit) {
        this.vc = vc;
        this.ec = ec;
        this.autoAddUncommitted = autoAddUncommitted;
        this.autoCommit = autoCommit;
        builder = Ts.get().getTerminologyBuilder(ec, vc);
    }

    /**
     * Adds component, or concept, and the concept value given, as a member of a
     * concept refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexUuid <code>UUID</code> collection ID of the desired refex
     * (extension)
     * @param conceptUuid
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToConceptRefex(ComponentChronicleBI component,
            UUID refexUuid, UUID conceptUuid) throws IOException, InvalidCAB,
            ContradictionException {
        int refexNid = Ts.get().getNidForUuids(refexUuid);
        int conceptNid = Ts.get().getNidForUuids(conceptUuid);
        return addComponentToConceptRefex(component, refexNid, conceptNid);
    }

    /**
     * Adds component, or concept, and the concept value given, as a member of a
     * concept refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexUuid <code>UUID</code> collection ID of the desired refex
     * (extension)
     * @param conceptUuid
     * @param memberUuid <code>UUID</code> representing the membership in the
     * refset
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToConceptRefex(ComponentChronicleBI component,
            UUID refexUuid, UUID conceptUuid, UUID memberUuid) throws IOException, InvalidCAB,
            ContradictionException {
        int refexNid = Ts.get().getNidForUuids(refexUuid);
        int conceptNid = Ts.get().getNidForUuids(conceptUuid);
        return addComponentToConceptRefex(component, refexNid, conceptNid, memberUuid);
    }

    /**
     * Adds component, or concept, and the boolean value given, as a member of a
     * boolean refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexUuid <code>UUID</code> collection ID of the desired refex
     * (extension)
     * @param booleanValue <code>boolean</code> representing the boolean value
     * for the boolean refex
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToBooleanRefex(ComponentChronicleBI component,
            UUID refexUuid, boolean booleanValue) throws IOException, InvalidCAB,
            ContradictionException {
        int refexNid = Ts.get().getNidForUuids(refexUuid);
        return addComponentToBooleanRefex(component, refexNid, booleanValue);
    }

    /**
     * Adds component, or concept, and the boolean value given, as a member of a
     * boolean refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexUuid <code>UUID</code> collection ID of the desired refex
     * (extension)
     * @param booleanValue <code>boolean</code> representing the boolean value
     * for the boolean refex
     * @param memberUuid <code>UUID</code> representing the membership in the
     * refset
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToBooleanRefex(ComponentChronicleBI component,
            UUID refexUuid, boolean booleanValue, UUID memberUuid) throws IOException, InvalidCAB,
            ContradictionException {
        int refexNid = Ts.get().getNidForUuids(refexUuid);
        return addComponentToBooleanRefex(component, refexNid, booleanValue, memberUuid);
    }

    /**
     * Adds component, or concept, and the integer value given, as a member of
     * an integer refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexUuid <code>UUID</code> collection ID of the desired refex
     * (extension)
     * @param integerValue <code>int</code> representing the integer value for
     * the integer refex
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToIntegerRefex(ComponentChronicleBI component,
            UUID refexUuid, int integerValue) throws IOException, InvalidCAB,
            ContradictionException {
        int refexNid = Ts.get().getNidForUuids(refexUuid);
        return addComponentToIntegerRefex(component, refexNid, integerValue);
    }

    /**
     * Adds component, or concept, and the integer value given, as a member of
     * an integer refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexUuid <code>UUID</code> collection ID of the desired refex
     * (extension)
     * @param integerValue <code>int</code> representing the integer value for
     * the integer refex
     * @param memberUuid  <code>UUID</code> representing the membership in the
     * refset
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToIntegerRefex(ComponentChronicleBI component,
            UUID refexUuid, int integerValue, UUID memberUuid) throws IOException, InvalidCAB,
            ContradictionException {
        int refexNid = Ts.get().getNidForUuids(refexUuid);
        return addComponentToIntegerRefex(component, refexNid, integerValue, memberUuid);
    }

    /**
     * Adds component, or concept, and the string value given, as a member of a
     * string refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexUuid <code>UUID</code> collection ID of the desired refex
     * (extension)
     * @param stringValue <code>String</code> representing the String value for
     * the string refex
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToStringRefex(ComponentChronicleBI component,
            UUID refexUuid, String stringValue) throws IOException, InvalidCAB,
            ContradictionException {
        int refexNid = Ts.get().getNidForUuids(refexUuid);
        return addComponentToStringRefex(component, refexNid, stringValue);
    }

    /**
     * Adds component, or concept, and the string value given, as a member of a
     * string refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexUuid <code>UUID</code> collection ID of the desired refex
     * (extension)
     * @param stringValue <code>String</code> representing the String value for
     * the string refex
     * @param memberUuid  <code>UUID</code> representing the membership in the
     * refset
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToStringRefex(ComponentChronicleBI component,
            UUID refexUuid, String stringValue, UUID memberUuid) throws IOException, InvalidCAB,
            ContradictionException {
        int refexNid = Ts.get().getNidForUuids(refexUuid);
        return addComponentToStringRefex(component, refexNid, stringValue, memberUuid);
    }

    /**
     * Adds component, or concept, and the concept value given, as a member of a
     * concept refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexNid <code>int</code> nid representing refex (extension)
     * @param conceptNid <code>int</code> nid representing the collection ID of
     * the desired refex (extension)
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToConceptRefex(ComponentChronicleBI component,
            int refexNid, int conceptNid) throws IOException, InvalidCAB,
            ContradictionException {
        ConceptChronicleBI refexConcept = Ts.get().getConceptForNid(refexNid);
        boolean annotation = refexConcept.isAnnotationStyleRefex();
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID,
                component.getNid(),
                refexNid);
        memberBp.put(RefexCAB.RefexProperty.CNID1, conceptNid);
        RefexChronicleBI<?> refex = builder.constructIfNotCurrent(memberBp);
        if (annotation) {
            component.addAnnotation(refex);
        }
        ConceptChronicleBI concept = component.getEnclosingConcept();
        if (autoAddUncommitted) {
            if (annotation) {
                Ts.get().addUncommittedNoChecks(concept);
            } else {
                Ts.get().addUncommittedNoChecks(refexConcept);
            }

        }
        if (autoAddUncommitted && autoCommit) {
            if (annotation) {
                Ts.get().commit(concept);
            } else {
                Ts.get().commit(refexConcept);
            }
        } else if (autoAddUncommitted == false && autoCommit == true) {
            throw new IllegalArgumentException("Must set autoAddUncommited to true in order to"
                    + "have auto commit.");
        }
        return refex;
    }

    /**
     * Adds component, or concept, and the concept value given, as a member of a
     * concept refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexNid <code>int</code> nid representing refex (extension)
     * @param conceptNid <code>int</code> nid representing the collection ID of
     * the desired refex (extension)
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @param memberUuid <code>UUID</code> representing the membership in the
     * refset
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToConceptRefex(ComponentChronicleBI component,
            int refexNid, int conceptNid, UUID memberUuid) throws IOException, InvalidCAB,
            ContradictionException {
        ConceptChronicleBI refexConcept = Ts.get().getConceptForNid(refexNid);
        boolean annotation = refexConcept.isAnnotationStyleRefex();
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID,
                component.getNid(),
                refexNid);
        memberBp.put(RefexCAB.RefexProperty.CNID1, conceptNid);
        memberBp.setMemberUuid(memberUuid);
        RefexChronicleBI<?> refex = builder.constructIfNotCurrent(memberBp);
        if (annotation) {
            component.addAnnotation(refex);
        }
        ConceptChronicleBI concept = component.getEnclosingConcept();
        if (autoAddUncommitted) {
            if (annotation) {
                Ts.get().addUncommittedNoChecks(concept);
            } else {
                Ts.get().addUncommittedNoChecks(refexConcept);
            }

        }
        if (autoAddUncommitted && autoCommit) {
            if (annotation) {
                Ts.get().commit(concept);
            } else {
                Ts.get().commit(refexConcept);
            }
        } else if (autoAddUncommitted == false && autoCommit == true) {
            throw new IllegalArgumentException("Must set autoAddUncommited to true in order to"
                    + "have auto commit.");
        }
        return refex;
    }

    /**
     * Adds component, or concept, and the boolean value given, as a member of a
     * boolean refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexNid <code>int</code> nid representing the collection ID of
     * the desired refex (extension)
     * @param booleanValue <code>boolean</code> representing the boolean value
     * for the boolean refex
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToBooleanRefex(ComponentChronicleBI component,
            int refexNid, boolean booleanValue) throws IOException, InvalidCAB,
            ContradictionException {
        ConceptChronicleBI refexConcept = Ts.get().getConceptForNid(refexNid);
        boolean annotation = refexConcept.isAnnotationStyleRefex();
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.BOOLEAN,
                component.getNid(),
                refexNid);
        memberBp.put(RefexCAB.RefexProperty.BOOLEAN1, booleanValue);
        RefexChronicleBI<?> refex = builder.constructIfNotCurrent(memberBp);
        if (annotation) {
            component.addAnnotation(refex);
        }
        ConceptChronicleBI concept = component.getEnclosingConcept();
        if (autoAddUncommitted) {
            if (annotation) {
                Ts.get().addUncommittedNoChecks(concept);
            } else {
                Ts.get().addUncommittedNoChecks(refexConcept);
            }

        }
        if (autoAddUncommitted && autoCommit) {
            if (annotation) {
                Ts.get().commit(concept);
            } else {
                Ts.get().commit(refexConcept);
            }
        } else if (autoAddUncommitted == false && autoCommit == true) {
            throw new IllegalArgumentException("Must set autoAddUncommited to true in order to"
                    + "have auto commit.");
        }
        return refex;
    }

    /**
     * Adds component, or concept, and the boolean value given, as a member of a
     * boolean refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexNid <code>int</code> nid representing the collection ID of
     * the desired refex (extension)
     * @param booleanValue <code>boolean</code> representing the boolean value
     * for the boolean refex
     * @param memberUuid <code>UUID</code> representing the membership in the
     * refset
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToBooleanRefex(ComponentChronicleBI component,
            int refexNid, boolean booleanValue, UUID memberUuid) throws IOException, InvalidCAB,
            ContradictionException {
        ConceptChronicleBI refexConcept = Ts.get().getConceptForNid(refexNid);
        boolean annotation = refexConcept.isAnnotationStyleRefex();
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.BOOLEAN,
                component.getNid(),
                refexNid);
        memberBp.put(RefexCAB.RefexProperty.BOOLEAN1, booleanValue);
        memberBp.setMemberUuid(memberUuid);
        RefexChronicleBI<?> refex = builder.constructIfNotCurrent(memberBp);
        if (annotation) {
            component.addAnnotation(refex);
        }
        ConceptChronicleBI concept = component.getEnclosingConcept();
        if (autoAddUncommitted) {
            if (annotation) {
                Ts.get().addUncommittedNoChecks(concept);
            } else {
                Ts.get().addUncommittedNoChecks(refexConcept);
            }

        }
        if (autoAddUncommitted && autoCommit) {
            if (annotation) {
                Ts.get().commit(concept);
            } else {
                Ts.get().commit(refexConcept);
            }
        } else if (autoAddUncommitted == false && autoCommit == true) {
            throw new IllegalArgumentException("Must set autoAddUncommited to true in order to"
                    + "have auto commit.");
        }
        return refex;
    }

    /**
     * Adds component, or concept, and the integer value given, as a member of
     * an integer refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexNid <code>int</code> nid representing the collection ID of
     * the desired refex (extension)
     * @param integerValue <code>int</code> representing the integer value for
     * the integer refex
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToIntegerRefex(ComponentChronicleBI component,
            int refexNid, int integerValue) throws IOException, InvalidCAB,
            ContradictionException {
        ConceptChronicleBI refexConcept = Ts.get().getConceptForNid(refexNid);
        boolean annotation = refexConcept.isAnnotationStyleRefex();
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.INT,
                component.getNid(),
                refexNid);
        memberBp.put(RefexCAB.RefexProperty.INTEGER1, integerValue);
        RefexChronicleBI<?> refex = builder.constructIfNotCurrent(memberBp);
        if (annotation) {
            component.addAnnotation(refex);
        }
        ConceptChronicleBI concept = component.getEnclosingConcept();
        if (autoAddUncommitted) {
            if (annotation) {
                Ts.get().addUncommittedNoChecks(concept);
            } else {
                Ts.get().addUncommittedNoChecks(refexConcept);
            }

        }
        if (autoAddUncommitted && autoCommit) {
            if (annotation) {
                Ts.get().commit(concept);
            } else {
                Ts.get().commit(refexConcept);
            }
        } else if (autoAddUncommitted == false && autoCommit == true) {
            throw new IllegalArgumentException("Must set autoAddUncommited to true in order to"
                    + "have auto commit.");
        }
        return refex;
    }

    /**
     * Adds component, or concept, and the integer value given, as a member of
     * an integer refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexNid <code>int</code> nid representing the collection ID of
     * the desired refex (extension)
     * @param integerValue <code>int</code> representing the integer value for
     * the integer refex
     * @param memberUuid <code>UUID</code> representing the membership in the
     * refset
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexChronicleBI<?> addComponentToIntegerRefex(ComponentChronicleBI component,
            int refexNid, int integerValue, UUID memberUuid) throws IOException, InvalidCAB,
            ContradictionException {
        ConceptChronicleBI refexConcept = Ts.get().getConceptForNid(refexNid);
        boolean annotation = refexConcept.isAnnotationStyleRefex();
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.INT,
                component.getNid(),
                refexNid);
        memberBp.put(RefexCAB.RefexProperty.INTEGER1, integerValue);
        memberBp.setMemberUuid(memberUuid);
        RefexChronicleBI<?> refex = builder.constructIfNotCurrent(memberBp);
        if (annotation) {
            component.addAnnotation(refex);
        }
        ConceptChronicleBI concept = component.getEnclosingConcept();
        if (autoAddUncommitted) {
            if (annotation) {
                Ts.get().addUncommittedNoChecks(concept);
            } else {
                Ts.get().addUncommittedNoChecks(refexConcept);
            }

        }
        if (autoAddUncommitted && autoCommit) {
            if (annotation) {
                Ts.get().commit(concept);
            } else {
                Ts.get().commit(refexConcept);
            }
        } else if (autoAddUncommitted == false && autoCommit == true) {
            throw new IllegalArgumentException("Must set autoAddUncommited to true in order to"
                    + "have auto commit.");
        }
        return refex;
    }

    /**
     * Adds component, or concept, and the string value given, as a member of a
     * string refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexNid <code>int</code> nid representing the collection ID of
     * the desired refex (extension)
     * @param stringValue <code>String</code> representing the String value for
     * the string refex
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException indicates an I/O exception occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public RefexChronicleBI<?> addComponentToStringRefex(ComponentChronicleBI component,
            int refexNid, String stringValue) throws IOException, InvalidCAB,
            ContradictionException {
        ConceptChronicleBI refexConcept = Ts.get().getConceptForNid(refexNid);
        boolean annotation = refexConcept.isAnnotationStyleRefex();
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.STR,
                component.getNid(),
                refexNid);
        memberBp.put(RefexCAB.RefexProperty.STRING1, stringValue);
        RefexChronicleBI<?> refex = builder.constructIfNotCurrent(memberBp);
        if (annotation) {
            component.addAnnotation(refex);
        }
        ConceptChronicleBI concept = component.getEnclosingConcept();
        if (autoAddUncommitted) {
            if (annotation) {
                Ts.get().addUncommittedNoChecks(concept);
            } else {
                Ts.get().addUncommittedNoChecks(refexConcept);
            }

        }
        if (autoAddUncommitted && autoCommit) {
            if (annotation) {
                Ts.get().commit(concept);
            } else {
                Ts.get().commit(refexConcept);
            }
        } else if (autoAddUncommitted == false && autoCommit == true) {
            throw new IllegalArgumentException("Must set autoAddUncommited to true in order to"
                    + "have auto commit.");
        }
        return refex;
    }

    /**
     * Adds component, or concept, and the string value given, as a member of a
     * string refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexNid <code>int</code> nid representing the collection ID of
     * the desired refex (extension)
     * @param stringValue <code>String</code> representing the String value for
     * the string refex
     * @param memberUuid <code>UUID</code> representing the membership in the
     * refset
     * @return <code>RefexChronicleBI<?></code> the added member component
     * @throws IOException indicates an I/O exception occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public RefexChronicleBI<?> addComponentToStringRefex(ComponentChronicleBI component,
            int refexNid, String stringValue, UUID memberUuid) throws IOException, InvalidCAB,
            ContradictionException {
        ConceptChronicleBI refexConcept = Ts.get().getConceptForNid(refexNid);
        boolean annotation = refexConcept.isAnnotationStyleRefex();
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.STR,
                component.getNid(),
                refexNid);
        memberBp.put(RefexCAB.RefexProperty.STRING1, stringValue);
        memberBp.setMemberUuid(memberUuid);
        RefexChronicleBI<?> refex = builder.constructIfNotCurrent(memberBp);
        if (annotation) {
            component.addAnnotation(refex);
        }
        ConceptChronicleBI concept = component.getEnclosingConcept();
        if (autoAddUncommitted) {
            if (annotation) {
                Ts.get().addUncommittedNoChecks(concept);
            } else {
                Ts.get().addUncommittedNoChecks(refexConcept);
            }

        }
        if (autoAddUncommitted && autoCommit) {
            if (annotation) {
                Ts.get().commit(concept);
            } else {
                Ts.get().commit(refexConcept);
            }
        } else if (autoAddUncommitted == false && autoCommit == true) {
            throw new IllegalArgumentException("Must set autoAddUncommited to true in order to"
                    + "have auto commit.");
        }
        return refex;
    }

    /**
     * Retires a component, or concept, from the specified refex.
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexUuid <code>UUID</code> collection ID of the desired refex
     * (extension)
     * @throws IOException indicates an I/O exception occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public void retireComponentFromRefex(ComponentChronicleBI component, UUID refexUuid)
            throws IOException, ContradictionException, InvalidCAB {
        int refexNid = Ts.get().getNidForUuids(refexUuid);
        retireComponentFromRefex(component, refexNid);
    }

    /**
     * Retires a component, or concept, from the specified refex
     *
     * @param component <code>ComponentChronicleBI</code> the referenced
     * component (the concept which is being extended)
     * @param refexNid <code>int</code> collection ID of the desired refex
     * (extension)
     * @throws IOException indicates an I/O exception occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public void retireComponentFromRefex(ComponentChronicleBI component, int refexNid)
            throws IOException, ContradictionException, InvalidCAB {
        ConceptChronicleBI refexConcept = Ts.get().getConceptForNid(refexNid);
        boolean annotation = refexConcept.isAnnotationStyleRefex();
        // gets extension independent of annotation or refset
        Collection<? extends RefexChronicleBI<?>> refexMembers = component.getRefexMembers(refexNid);
        for (RefexChronicleBI member : refexMembers) {
            ComponentVersionBI cvbi = member.getVersion(vc);
            if (cvbi != null) {
                RefexCAB memberBp = (RefexCAB) cvbi.makeBlueprint(vc);
                memberBp.setRetired();
                builder.construct(memberBp);
            }
        }
        ConceptChronicleBI concept = component.getEnclosingConcept();
        if (autoAddUncommitted) {
            if (annotation) {
                Ts.get().addUncommittedNoChecks(concept);
            } else {
                Ts.get().addUncommittedNoChecks(refexConcept);
            }

        }
        if (autoAddUncommitted && autoCommit) {
            if (annotation) {
                Ts.get().commit(concept);
            } else {
                Ts.get().commit(refexConcept);
            }
        } else if (autoAddUncommitted == false && autoCommit == true) {
            throw new IllegalArgumentException("Must set autoAddUncommited to true in order to"
                    + "have auto commit.");
        }

    }

    /**
     * Retires a specific member of a refex.
     *
     * @param refexMember the <code>RefexVersionBI</code> representing the
     * member to be retired
     * @throws IOException indicates an I/O exception occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public void retireComponentFromRefex(RefexVersionBI refexMember) throws IOException,
            ContradictionException, InvalidCAB {
        ConceptChronicleBI refexConcept = Ts.get().getConceptForNid(refexMember.getRefexNid());
        boolean annotation = refexConcept.isAnnotationStyleRefex();
        RefexCAB memberBp = refexMember.makeBlueprint(vc);
        memberBp.setMemberUuid(refexMember.getPrimUuid());
        memberBp.setRetired();
        RefexChronicleBI<?> thing = builder.construct(memberBp);
        ConceptChronicleBI concept = refexMember.getEnclosingConcept();
        if (autoAddUncommitted) {
            if (annotation) {
                Ts.get().addUncommittedNoChecks(concept);
            } else {
                Ts.get().addUncommittedNoChecks(refexConcept);
            }

        }
        if (autoAddUncommitted && autoCommit) {
            if (annotation) {
                Ts.get().commit(concept);
            } else {
                Ts.get().commit(refexConcept);
            }
        } else if (autoAddUncommitted == false && autoCommit == true) {
            throw new IllegalArgumentException("Must set autoAddUncommited to true in order to"
                    + "have auto commit.");
        }
    }

    /**
     * Checks if this refex helper is set to automatically add new refex members
     * as uncommitted.
     *
     * @return <code>true</code> if new refex members are automatically added as
     * uncommitted
     */
    public boolean isAutoAddUncommitted() {
        return autoAddUncommitted;
    }

    /**
     * Checks if this refex helper is set to automatically commit new refex
     * members.
     * <code>autoAddUncommitted</code> must be set to to
     * <code>true</code>.
     *
     * @return <code>true</code> if new refex members are automatically
     * committed
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Gets the edit coordinate used to add new refex members.
     *
     * @return the edit coordinate used to add new refex members
     */
    public EditCoordinate getEc() {
        return ec;
    }

    /**
     * Gets the view coordinate used to add new refex members.
     *
     * @return the view coordinate used to add new refex members
     */
    public ViewCoordinate getVc() {
        return vc;
    }

    /**
     * Sets whether or not this refex helper automatically adds new refex
     * memebrs as uncommitted.
     *
     * @param autoAddUncommitted set to <code>true</code> to automatically add
     * new refex members as uncommitted
     */
    public void setAutoAddUncommitted(boolean autoAddUncommitted) {
        this.autoAddUncommitted = autoAddUncommitted;
    }

    /**
     * Sets whether or not this refex helper automatically commits new refex
     * members. If setting to
     * <code>true</code>, must also set auto add uncommitted to
     * <code>true</code>.
     *
     * @param autoCommit set to <code>true</code> to automatically commit new refex members
     */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    /**
     * Sets the edit coordinate used to add new refex members.
     * @param ec the edit coordiante to use for adding new refex members
     */
    public void setEc(EditCoordinate ec) {
        this.ec = ec;
    }

    /**
     * Sets the view coordinate used to add new refex members.
     * @param vc the view coordinate to use for adding new refex members
     */
    public void setVc(ViewCoordinate vc) {
        this.vc = vc;
    }
}
