package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingWorker;

import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;

public class WfInstanceSearcher extends SwingWorker<List<WfProcessInstanceBI>, WfProcessInstanceBI> implements ProcessUnfetchedConceptDataBI {

	private List<WfProcessInstanceBI> result = null;
	private boolean done = false;
	private Collection<WfFilterBI> filters;
	private WfInstanceContainer wfinstanceCont;
	private CancelSearch keepSearching;

	public WfInstanceSearcher(Collection<WfFilterBI> filters, WfInstanceContainer wfinstanceCont, CancelSearch keepSearchig) {
		super();
		this.filters = filters;
		this.wfinstanceCont = wfinstanceCont;
		this.keepSearching = keepSearchig;
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
		return !isCancelled() && !done && !keepSearching.isCanceled();
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
				Collection<WfProcessInstanceBI> wfinstances = wf.getProcessInstances(concept);
				for (WfProcessInstanceBI wfProcessInstanceBI : wfinstances) {
					boolean apply = true;
					for (WfFilterBI filter : filters) {
						if (!filter.evaluateInstance(wfProcessInstanceBI)) {
							apply = false;
							break;
						}
					}
					if (!apply) {
						continue;
					}
					publish(wfProcessInstanceBI);
				}
			} else {
				cancel(true);
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
