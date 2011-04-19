/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.batch;

import java.util.Collection;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 *
 * @author marc
 */
public class BatchActionTaskRoleRetire extends BatchActionTask {

    private int roleNid;
    private int valueNid;

    public BatchActionTaskRoleRetire() {
        this.roleNid = Integer.MAX_VALUE;
        this.valueNid = Integer.MAX_VALUE;
    }

    public void setRoleNid(int roleNid) {
        this.roleNid = roleNid;
    }

    public void setValueNid(int valueNid) {
        this.valueNid = valueNid;
    }
    
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws Exception {
        int conceptNid = c.getNid();
        boolean changed = false;
        Collection<? extends RelationshipVersionBI> rels = c.getRelsIncomingActive();
        for (RelationshipVersionBI rvbi : rels) {
            if (rvbi.getTypeNid() == roleNid && rvbi.getDestinationNid() == valueNid) {
                rvbi.makeAnalog(RETIRED_NID, ec.getAuthorNid(), rvbi.getPathNid(), Long.MAX_VALUE);
                BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.ROLE_RETIRE,
                        BatchActionEventType.EVENT_SUCCESS, "retired rel: " + nidToName(conceptNid) + " :: "
                        + nidToName(roleNid) + " :: " + nidToName(valueNid)));
                changed = true;
            }
        }

        if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.ROLE_RETIRE,
                    BatchActionEventType.EVENT_NOOP, "does have rel: " + nidToName(conceptNid) + " :: "
                    + nidToName(roleNid) + " :: " + nidToName(valueNid)));
        }

        return changed;
    }
}
