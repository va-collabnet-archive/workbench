package org.ihtsdo.translation.ui.translation;

import java.util.Set;

import javax.swing.SwingWorker;

import org.ihtsdo.project.view.event.EventMediator;
import org.ihtsdo.tk.api.TermChangeListener;
import org.ihtsdo.translation.ui.event.TermChangedEvent;

public class TranslationTermChangeListener extends TermChangeListener {

	private static TranslationTermChangeListener instance = null;
	
	private TranslationTermChangeListener() {
	}
	
	
	//TODO: cambio! revisar
	public void changeNotify(long sequence, 
	           Set<Integer> originsOfChangedRels, 
	           Set<Integer> destinationsOfChangedRels, 
	           Set<Integer> referencedComponentsOfChangedRefexs, 
	           Set<Integer> changedComponents,
	           Set<Integer> changedComponentAlerts,
	           Set<Integer> changedComponentTemplates) {
		ChangeListenerSwingWorker worker = new ChangeListenerSwingWorker(sequence, 
                originsOfChangedRels, 
                destinationsOfChangedRels, 
                referencedComponentsOfChangedRefexs, 
                changedComponents);
		worker.execute();
	}

	
	
	public static TranslationTermChangeListener getInstance() {
		if(instance == null){
			instance = new TranslationTermChangeListener();
		}
		return instance;
	}

    @Override
    public void changeNotify(long sequence, Set<Integer> sourcesOfChangedRels, Set<Integer> targetsOfChangedRels, Set<Integer> referencedComponentsOfChangedRefexs, Set<Integer> changedComponents, Set<Integer> changedComponentAlerts, Set<Integer> changedComponentTypes, boolean fromClassification) {
    	ChangeListenerSwingWorker worker = new ChangeListenerSwingWorker(sequence, 
    			sourcesOfChangedRels, 
    			targetsOfChangedRels, 
                referencedComponentsOfChangedRefexs, 
                changedComponents);
		worker.execute();
    }

	private class ChangeListenerSwingWorker extends SwingWorker<Boolean, Boolean> {
		Set<Integer> changedComponents;

		public ChangeListenerSwingWorker(long sequence, Set<Integer> originsOfChangedRels, Set<Integer> destinationsOfChangedRels, 
				Set<Integer> referencedComponentsOfChangedRefexs, Set<Integer> changedComponents) {
			this.changedComponents = changedComponents;
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			EventMediator.getInstance().fireEvent(new TermChangedEvent(changedComponents));
			return true;
		}

		@Override
		protected void done() {
		}
		
		@Override
		public boolean equals(Object arg0) {
			return true;
		}
	}
}
