package org.dwfa.ace.task.profile.cap;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.wfpanel.PreviousNextOrCancel;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.swing.SwingWorker;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.workflow.refset.utilities.WfComparator;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;


public abstract class AbstractSetNewCapUserPaths extends PreviousNextOrCancel {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    protected transient JComboBox pathList;
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

    protected String[] generatePotentialParentConcepts(ConceptVersionBI parentNode) {
	   try {
    		Set<ConceptVersionBI> potentialParentConcepts = WorkflowHelper.getChildren(parentNode);
    		
    		SortedSet<ConceptVersionBI> sortedPotentialParents = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());

    		sortedPotentialParents.addAll(potentialParentConcepts);
    		
    		String[] parentConcepts = new String[sortedPotentialParents.size()];
    		
    		int i = 0;
    		for (ConceptVersionBI con : sortedPotentialParents) {
    			if (con.equals(parentNode)) {
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
            workflowPanel.add(pathList, c);

/*
 * 	        pathList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
	        pathList.setLayoutOrientation(JList.VERTICAL);
	        pathList.setVisibleRowCount(3);	    
	        pathList.setPreferredSize(new Dimension(250, 80));
	    
	        JScrollPane listScroller = new JScrollPane(pathList);
	        listScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	        listScroller.setPreferredSize(new Dimension(250, 80));
	        
	        workflowPanel.add(listScroller, c);
*/
            c.gridx++;
            c.weightx = 0.0;
            setupPreviousNextOrCancelButtons(workflowPanel, c);
            workflowPanel.setVisible(true);
            pathList.requestFocusInWindow();
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

	protected abstract String getPropName();
	
    protected abstract JLabel getInstruction(); 

    protected abstract UUID getParentNode(); 

	protected void readInput(I_EncodeBusinessProcess process) {

		try {
/*            int[] indices = pathList.getSelectedIndices();
            
            int[] pathIds = new int[indices.length];
            
            for (int i = 0; i < indices.length; i++) {
            	pathIds[i]= parentIds.get(indices[i]);
            }
            process.setProperty(getPropName(), pathIds);
*/		
            int index = pathList.getSelectedIndex();
            process.setProperty(getPropName(), parentIds.get(index).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    protected void setupInput(I_EncodeBusinessProcess process) {
    	try {
	    	parentIds = new LinkedList<Integer>();
	    	I_GetConceptData parentNode = Terms.get().getConcept(getParentNode());
	    	String creatorProfilePropName = "JESSE";
	    	I_ConfigAceFrame newConfig = (I_ConfigAceFrame) process.getProperty(creatorProfilePropName);
	    	ConceptVersionBI parentVersioned = parentNode.getVersion(newConfig.getViewCoordinate());
	    	
	    	String[] potentialParentConcepts = generatePotentialParentConcepts(parentVersioned);
	    	
	        instruction = getInstruction();
	        pathList = new JComboBox(potentialParentConcepts);
	        pathList.setSelectedIndex(initialIndex);
/*	        pathList = new JList(potentialParentConcepts);
*/	    } catch (Exception e) {
	    	throw new IllegalArgumentException(e.getMessage());
	    }
    }

}