package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ComponentFact;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

public class CloneAndRetireAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    I_ConfigAceFrame config;

    public CloneAndRetireAction(String actionName, ComponentFact<ComponentVersionBI> fact,
            I_ConfigAceFrame config, ConceptViewSettings settings) {
        super(actionName);
        this.component = fact.getComponent();
        this.config = config;
        settings.getView().focus = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            I_GetConceptData concept = Terms.get().getConceptForNid(component.getNid());
            Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
            if (ConceptAttributeVersionBI.class.isAssignableFrom(component.getClass())) {
                throw new UnsupportedOperationException();
            }
            if (ConceptVersionBI.class.isAssignableFrom(component.getClass())) {
                throw new UnsupportedOperationException();
            }
            if (DescriptionVersionBI.class.isAssignableFrom(component.getClass())) {
                DescriptionVersionBI desc = (DescriptionVersionBI) component;
                Collection<? extends RefexVersionBI<?>> oldRefexes = desc.getRefexesActive(config.getViewCoordinate());
                I_DescriptionVersioned newDesc = Terms.get().newDescription(UUID.randomUUID(), concept,
                        desc.getLang(), desc.getText(), Terms.get().getConcept(desc.getTypeNid()),
                        config, Terms.get().getConcept(desc.getStatusNid()), Long.MAX_VALUE);
                newDesc.setInitialCaseSignificant(desc.isInitialCaseSignificant());
                TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                        config.getViewCoordinate());
                int dosNid = SnomedMetadataRfx.getSYNONYMY_REFEX_NID();
                                for (RefexVersionBI refex : oldRefexes) {
                    if (refex.getRefexNid() != dosNid) { //not cloning degree of synonymy refeset membership
                        RefexCAB newSpec;
                        if (RefexNidVersionBI.class.isAssignableFrom(refex.getClass())) {
                            newSpec = new RefexCAB(
                                TK_REFEX_TYPE.CID,
                                newDesc.getNid(),
                                refex.getRefexNid());
                            RefexNidVersionBI cv =
                                    (RefexNidVersionBI) refex.getVersion(config.getViewCoordinate());
                            int typeNid = cv.getNid1();
                            newSpec.put(RefexProperty.CNID1, typeNid);
                        } else if(RefexBooleanVersionBI.class.isAssignableFrom(refex.getClass())){
                            newSpec = new RefexCAB(
                                TK_REFEX_TYPE.BOOLEAN,
                                newDesc.getNid(),
                                refex.getRefexNid());
                            RefexBooleanVersionBI bv =
                                    (RefexBooleanVersionBI) refex.getVersion(config.getViewCoordinate());
                            boolean boolean1 = bv.getBoolean1();
                            newSpec.put(RefexProperty.BOOLEAN1, boolean1);
                        } else if (RefexStringVersionBI.class.isAssignableFrom(refex.getClass())){
                            newSpec = new RefexCAB(
                                TK_REFEX_TYPE.STR,
                                newDesc.getNid(),
                                refex.getRefexNid());
                            RefexStringVersionBI sv =
                                    (RefexStringVersionBI) refex.getVersion(config.getViewCoordinate());
                            String string1 = sv.getString1();
                            newSpec.put(RefexProperty.STRING1, string1);
                        } else if(RefexIntVersionBI.class.isAssignableFrom(refex.getClass())){
                            newSpec = new RefexCAB(
                                TK_REFEX_TYPE.INT,
                                newDesc.getNid(),
                                refex.getRefexNid());
                            RefexIntVersionBI iv =
                                    (RefexIntVersionBI) refex.getVersion(config.getViewCoordinate());
                            int int1 = iv.getInt1();
                            newSpec.put(RefexProperty.INTEGER1, int1);
                        } else{
                            throw new UnsupportedOperationException("can't handle refex type: " +
                                    refex);
                        }

                        tc.construct(newSpec);
                        ConceptChronicleBI refexConcept = Ts.get().getConcept(refex.getRefexNid());
                        if (!refexConcept.isAnnotationStyleRefex()) {
                            Ts.get().addUncommitted(refexConcept);
                        }
                    }
                }
                retireFromRefexes(component);
            }
            if (RelationshipVersionBI.class.isAssignableFrom(component.getClass())) {
                RelationshipVersionBI rel = (RelationshipVersionBI) component;
                for (PathBI ep : config.getEditingPathSet()) {
                    I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), concept,
                        rel.getTypeNid(),
                        rel.getTargetNid(),
                        rel.getCharacteristicNid(),
                        rel.getRefinabilityNid(),
                        rel.getGroup(),
                        rel.getStatusNid(),
                        config.getDbConfig().getUserConcept().getNid(),
                        ep.getConceptNid(),
                        Long.MAX_VALUE);
                }
            }

            if (I_AmPart.class.isAssignableFrom(component.getClass())) {
                I_AmPart componentVersion = (I_AmPart) component;
                ComponentVersionBI analog = null;
                for (PathBI ep : config.getEditingPathSet()) {
                    analog = (ComponentVersionBI) componentVersion.makeAnalog(
                            SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                            Long.MAX_VALUE,
                            config.getEditCoordinate().getAuthorNid(),
                            config.getEditCoordinate().getModuleNid(), 
                            ep.getConceptNid());
                }
                if (DescriptionVersionBI.class.isAssignableFrom(component.getClass())) {
                    retireFromRefexes(analog);
                }
            }

            Terms.get().addUncommitted(concept);


        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (PropertyVetoException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InvalidCAB e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (ContradictionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }

    private void retireFromRefexes(ComponentVersionBI component) {
        DescriptionVersionBI desc = (DescriptionVersionBI) component;
        try {
            I_AmPart componentVersion;
            ViewCoordinate vc = config.getViewCoordinate();
            Collection<? extends RefexChronicleBI> refexes = desc.getRefexesActive(vc);
            int usNid = SnomedMetadataRfx.getUS_DIALECT_REFEX_NID();
            int gbNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
            int dosNid = SnomedMetadataRfx.getSYNONYMY_REFEX_NID();
            for (RefexChronicleBI refex : refexes) {
                int refexNid = refex.getRefexNid();
                    componentVersion = (I_AmPart) refex;
                    for (PathBI ep : config.getEditingPathSet()) {
                        componentVersion.makeAnalog(
                                SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                                Long.MAX_VALUE,
                                config.getEditCoordinate().getAuthorNid(),
                                config.getEditCoordinate().getModuleNid(), 
                                ep.getConceptNid());
                    }
                    I_GetConceptData concept = Terms.get().getConceptForNid(component.getNid());
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }
}
