package org.dwfa.ace.gui.concept;

import javax.swing.ImageIcon;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.vodb.types.ConceptBean;

import com.sleepycat.je.DatabaseException;

public class ConflictPlugin extends AbstractPlugin {
	private ConflictPanel conflictPanel;
	private I_HostConceptPlugins host;

	public ConflictPanel getComponent(I_HostConceptPlugins host) {
		if (conflictPanel == null) {
			this.host = host;
			conflictPanel = new ConflictPanel();
			host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
		}
		return conflictPanel;
	}

	@Override
	protected ImageIcon getImageIcon() {
		return new ImageIcon(ACE.class
				.getResource("/24x24/plain/transform.png"));
	}

	@Override
	protected boolean isSelectedByDefault() {
		return false;
	}

	@Override
	public void update() throws DatabaseException {
		if (showComponent() && conflictPanel != null) {
			conflictPanel.setConcept((ConceptBean) host.getTermComponent(), host.getConfig());
		}
		
	}

}
