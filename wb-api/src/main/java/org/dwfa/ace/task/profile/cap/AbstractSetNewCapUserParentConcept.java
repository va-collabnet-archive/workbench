package org.dwfa.ace.task.profile.cap;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.wfpanel.PreviousNextOrCancel;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.swing.SwingWorker;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.refset.utilities.WfComparator;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;


public abstract class AbstractSetNewCapUserParentConcept extends PreviousNextOrCancel {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    protected transient JComboBox parentConceptList;
    protected transient JLabel instruction;
    protected List<Integer> parentIds;
    protected int initialIndex;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	// nothing to read...
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    protected abstract void readInput(I_EncodeBusinessProcess process) throws IntrospectionException,
    	IllegalAccessException, InvocationTargetException;

    protected abstract void setupInput(I_EncodeBusinessProcess process) throws IllegalArgumentException,
    	IntrospectionException, IllegalAccessException, InvocationTargetException;

    protected abstract JLabel getInstruction(); 

    protected String[] generatePotentialParentConcepts(I_GetConceptData parentConcept, ViewCoordinate vc) {
	   try {
		   ConceptVersionBI parentVersioned = parentConcept.getVersion(vc);
		   Set<ConceptVersionBI> potentialParentConcepts = WorkflowHelper.getChildren(parentVersioned);
    			
		   SortedSet<ConceptVersionBI> sortedPotentialParents = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());
		   
			sortedPotentialParents.addAll(potentialParentConcepts);
		
			String[] parentConcepts = new String[sortedPotentialParents.size()];
		
			int i = 0;
			for (ConceptVersionBI con : sortedPotentialParents) {
				if (con.equals(parentVersioned)) {
					initialIndex = i;
				}
				String displayStr = con.getPreferredDescription().getText();
				if (displayStr == null || displayStr.length() == 0) { 
					displayStr =  con.getFullySpecifiedDescription().getText();
				}

				parentConcepts[i] = displayStr;
	    		parentIds.add(i++, con.getConceptNid());
    		}
			
			return parentConcepts;
	   	} catch (Exception e) {
		
		}

    	String[] emptyList = new String[] { "" };
    	return emptyList;
	}  
    
    @Override
    protected boolean showPrevious() {
        return true;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
        // Nothing to do		
	}

    protected class DoSwing extends SwingWorker<Boolean> {

        I_EncodeBusinessProcess process;

        public DoSwing(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
        }

        @Override
        protected Boolean construct() throws Exception {
            setup(process);
            setupInput(process);
            return true;
        }

        @Override
        protected void finished() {
            Component[] components = workflowPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                workflowPanel.remove(components[i]);
            }
            workflowPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            c.weighty = 0;
            c.anchor = GridBagConstraints.EAST;
            workflowPanel.add(instruction, c);
            c.weightx = 0.0;
            c.gridx++;
            c.weightx = 1.0;
            workflowPanel.add(parentConceptList, c);
            c.gridx++;
            c.weightx = 0.0;
            setupPreviousNextOrCancelButtons(workflowPanel, c);
            workflowPanel.setVisible(true);
        	parentConceptList.requestFocusInWindow();
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
        try {
            DoSwing swinger = new DoSwing(process);
            swinger.start();
            swinger.get();
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }

            readInput(process);
            restore();

        } catch (Exception e) {
            throw new TaskFailedException(e);
        } 
        
        return returnCondition;	
    }

}