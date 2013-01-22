package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingWorker;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.workflow2.WfFilterBI;
import org.ihtsdo.project.workflow2.WfProcessInstanceBI;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class WfInstanceSearcher extends SwingWorker<List<WfProcessInstanceBI>, WfProcessInstanceBI> implements ProcessUnfetchedConceptDataBI {

	private List<WfProcessInstanceBI> result = null;
	private boolean done = false;
	private Collection<WfFilterBI> filters;
	private WfInstanceContainer wfinstanceCont;
	private I_ConfigAceFrame config;

	public WfInstanceSearcher(Collection<WfFilterBI> filters, WfInstanceContainer wfinstanceCont) {
		super();
		this.filters = filters;
		this.wfinstanceCont = wfinstanceCont;
	}

	@Override
	protected List<WfProcessInstanceBI> doInBackground() throws Exception {
		this.result = new ArrayList<WfProcessInstanceBI>();
		Ts.get().iterateConceptDataInParallel(this);
		done = true;
		return result;
	}

	@Override
	public boolean continueWork() {
		return !isCancelled() && !done;
	}

	@Override
	public NidBitSetBI getNidSet() throws IOException {
		return Terms.get().getConceptNidSet();
	}

	@Override
	public void processUnfetchedConceptData(int arg0, ConceptFetcherBI fetcher) throws Exception {
		try {
			ConceptChronicleBI concept = fetcher.fetch();
			if (this.continueWork()) {
				WorkflowStore wf = new WorkflowStore();
				Collection<WfProcessInstanceBI> wfinstances = wf.getProcessInstances(concept.getPrimUuid());
				for (WfProcessInstanceBI wfProcessInstanceBI : wfinstances) {
					boolean apply = true;
					for (WfFilterBI filter : filters) {
						if (filter.evaluateInstance(wfProcessInstanceBI)) {
							apply = false;
							break;
						}
					}
					if (!apply) {
						continue;
					}
					publish(wfProcessInstanceBI);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	protected void process(List<WfProcessInstanceBI> chunks) {
		for (WfProcessInstanceBI wfProcessInstanceBI : chunks) {
			wfinstanceCont.addWfInstance(wfProcessInstanceBI);
		}
	}

}
