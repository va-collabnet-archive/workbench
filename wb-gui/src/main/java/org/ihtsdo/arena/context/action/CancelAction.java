package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.drools.facts.ComponentFact;
import org.ihtsdo.tk.drools.facts.ConAttrFact;

public class CancelAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    ViewCoordinate vc;
    I_ConfigAceFrame config;

    public CancelAction(String actionName, ComponentFact<ComponentVersionBI> fact, ViewCoordinate vc, I_ConfigAceFrame config) {
        super(actionName);
        this.component = fact.getComponent();
        this.vc = vc;
        this.config = config;
    }
    
    public CancelAction(String actionName, ConAttrFact fact, ViewCoordinate vc, I_ConfigAceFrame config) {
        super(actionName);
        this.component = fact.getComponent();
        this.vc = vc;
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            I_GetConceptData concept = Terms.get().getConceptForNid(component.getNid());
            Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
            if (ConAttrVersionBI.class.isAssignableFrom(component.getClass())) {
                I_GetConceptData c = Terms.get().getConcept(component.getNid());
                I_ConceptAttributeVersioned ca = c.getConceptAttributes();
                ConAttrVersionBI attr = (ConAttrVersionBI) component;
                Collection<? extends RefexChronicleBI<?>> refexes = attr.getRefexes();
                for (RefexChronicleBI refex : refexes) {
                    if (refex.isUncommitted()) {
                        I_ExtendByRef ext = Terms.get().getExtension(refex.getNid());
                        Terms.get().forget(ext);
                    }
                }
                if (attr.isUncommitted()) {
                    Terms.get().forget(ca);
                }
            }
            if (DescriptionVersionBI.class.isAssignableFrom(component.getClass())) {
                DescriptionVersionBI desc = (DescriptionVersionBI) component;
                if (desc.isUncommitted()) {
                    Collection<? extends RefexChronicleBI<?>> refexes = desc.getRefexes();
                    for (RefexChronicleBI refex : refexes) {
                        if (refex.isUncommitted()) {
                            I_ExtendByRef ext = Terms.get().getExtension(refex.getNid());
                            Terms.get().forget(ext);
                        }
                    }
                    I_DescriptionVersioned dv = Terms.get().getDescription(desc.getNid());
                    Terms.get().forget(dv);
                }


            }
            if (RelationshipVersionBI.class.isAssignableFrom(component.getClass())) {
                RelationshipVersionBI rel = (RelationshipVersionBI) component;
                if (rel.isUncommitted()) {
                    I_RelVersioned rv = Terms.get().getRelationship(rel.getNid());
                    Collection<? extends RefexChronicleBI<?>> refexes = rel.getRefexes();
                    for (RefexChronicleBI refex : refexes) {
                        if (refex.isUncommitted()) {
                            I_ExtendByRef ext = Terms.get().getExtension(refex.getNid());
                            Terms.get().forget(ext);
                        }
                    }
                    Terms.get().forget(rv);
                }
            }

        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
}
