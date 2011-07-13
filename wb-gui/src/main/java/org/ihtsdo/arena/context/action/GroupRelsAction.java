package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.Collection;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.drools.facts.RelFact;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;

public class GroupRelsAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    RelationshipVersionBI sourceComponent;
    ComponentVersionBI targetComponent;

    public GroupRelsAction(String actionName, RelFact sourceFact, RelFact destFact) {
        super(actionName);
        this.sourceComponent = sourceFact.getComponent();
        this.targetComponent = destFact.getComponent();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            I_GetConceptData concept = Terms.get().getConceptForNid(targetComponent.getNid());
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            //get group numbers in target concept
            ConceptChronicleBI target = (ConceptChronicleBI) concept;
            Collection targetGroups = target.getRelGroups(config.getViewCoordinate());
            int max = 0;
            for (Object groupObject : targetGroups) {
                RelGroupVersionBI rg = null;
                if (groupObject instanceof RelGroupVersionBI) {
                    rg = (RelGroupVersionBI) groupObject;
                } else if (groupObject instanceof RelGroupChronicleBI) {
                    RelGroupChronicleBI rgc = (RelGroupChronicleBI) groupObject;
                    rg = rgc.getVersion(config.getViewCoordinate());
                }

                if (rg != null) {
                    int group = rg.getRelGroup();
                    if (group > max) {
                        max = group;
                    }
                }
            }

            int groupNumber = max + 1;
            Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
            int pathNid = pathItr.next().getConceptNid();
            RelationshipVersionBI targetRel = (RelationshipVersionBI) targetComponent;
            RelationshipVersionBI sourceRel = (RelationshipVersionBI) sourceComponent;

            for (PathBI ep : config.getEditingPathSet()) {
                RelationshipAnalogBI targetAnalog = (RelationshipAnalogBI) targetRel.makeAnalog(
                        targetRel.getStatusNid(),
                        targetRel.getAuthorNid(),
                        ep.getConceptNid(),
                        Long.MAX_VALUE);
                RelationshipAnalogBI sourceAnalog = (RelationshipAnalogBI) sourceRel.makeAnalog(
                        sourceRel.getStatusNid(),
                        sourceRel.getAuthorNid(),
                        ep.getConceptNid(),
                        Long.MAX_VALUE);
                targetAnalog.setGroup(groupNumber);
                sourceAnalog.setGroup(groupNumber);
            }
            Terms.get().addUncommitted(concept);
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (ContraditionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (PropertyVetoException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
}
