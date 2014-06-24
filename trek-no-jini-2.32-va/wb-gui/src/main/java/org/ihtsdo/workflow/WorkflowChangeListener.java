package org.ihtsdo.workflow;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.CancellationException;

import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowInitiator;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.workflow.api.WorkflowInitiatiorBI;

public class WorkflowChangeListener implements PropertyChangeListener {

	public static NidSet alreadySeen;

	private PropertyChangeEvent myEvt;
	
	private WorkflowInitiatiorBI initiator;


	public WorkflowChangeListener() {
		super();
		alreadySeen = new NidSet();
		initiator = new WorkflowInitiator();
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		try {
			initiator.evaluateForWorkflowInitiation(arg0);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	public void propertyChangeNotUsedLegacy(PropertyChangeEvent arg0) {
		System.out.println("Change got to listener...");
		myEvt=arg0;
		SwingUtilities.invokeLater(new Runnable() {

			synchronized
			public void run() {
				ConceptChronicleBI concept=null;
				try {
					I_RepresentIdSet idSet=(I_RepresentIdSet)myEvt.getNewValue();
					if (idSet!=null){
						NidBitSetItrBI possibleItr = idSet.iterator();

						while (possibleItr.next()) {
							if (!alreadySeen.contains(possibleItr.nid())) {
								alreadySeen.add(possibleItr.nid());
								concept=Ts.get().getConcept(possibleItr.nid());
								if (concept!=null){
									System.out.println("Sending to workflow: " + concept.toString());
									WorkflowHelper2.addComponentToDefaultWorklist(concept);
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (CancellationException e) {
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});


	}

}
