package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.AcceptabilityType;
import org.ihtsdo.arena.spec.Refsets;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.drools.facts.ComponentFact;

public class SetDefinedAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    AnalogBI analog;
    I_ConceptAttributePart newAnalogAttr;

    public SetDefinedAction(String actionName, ComponentFact<ConAttrVersionBI> fact) {
        super(actionName);
        this.component = fact.getComponent();
    
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
        	I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            
            if (I_AmPart.class.isAssignableFrom(component.getClass())) {
            	I_AmPart part = (I_AmPart) component;
            	I_ConceptAttributePart cPart = (I_ConceptAttributePart) part;
            	
            	if(cPart.isDefined() == true){
            		//make analog
    	            for (PathBI ep: config.getEditingPathSet()) {
    					AnalogBI newAnalog = part.makeAnalog(
    							ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), 
    							config.getDbConfig().getUserConcept().getNid(),
    							ep.getConceptNid(), 
    							Long.MAX_VALUE);
    					
    					newAnalogAttr = (I_ConceptAttributePart) newAnalog;
    					newAnalogAttr.setDefined(false);
    					}
    	            I_GetConceptData concept = Terms.get().getConceptForNid(newAnalogAttr.getNid());
    	            Terms.get().addUncommitted(concept);
            	} else{
            		//make analog
    	            for (PathBI ep: config.getEditingPathSet()) {
    					AnalogBI newAnalog = part.makeAnalog(
    							ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), 
    							config.getDbConfig().getUserConcept().getNid(),
    							ep.getConceptNid(), 
    							Long.MAX_VALUE);
    					
    					newAnalogAttr = (I_ConceptAttributePart) newAnalog;
    					newAnalogAttr.setDefined(true);
    					}
    	            I_GetConceptData concept = Terms.get().getConceptForNid(newAnalogAttr.getNid());
    	            Terms.get().addUncommitted(concept);
            	}
            }
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
  
}
