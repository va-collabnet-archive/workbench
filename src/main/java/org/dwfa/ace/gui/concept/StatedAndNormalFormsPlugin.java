package org.dwfa.ace.gui.concept;

import java.io.IOException;
import java.util.UUID;

import javax.swing.ImageIcon;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.ConceptBean;

public class StatedAndNormalFormsPlugin extends AbstractPlugin {
	public StatedAndNormalFormsPlugin(boolean selectedByDefault, int sequence, UUID id) {
        super(selectedByDefault, sequence, id);
	}

	private LogicalFormsPanel formsPanel;
	private I_HostConceptPlugins host;

	public LogicalFormsPanel getComponent(I_HostConceptPlugins host) {
		if (formsPanel == null) {
			this.host = host;
			formsPanel = new LogicalFormsPanel();
			host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
			host.addPropertyChangeListener("commit", this);
			try {
				formsPanel.setConcept((ConceptBean) host.getTermComponent(), host.getConfig());
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
		return formsPanel;
	}

	@Override
	protected ImageIcon getImageIcon() {
		return new ImageIcon(ACE.class
				.getResource("/24x24/plain/yinyang.png"));
	}

	@Override
	public void update() throws IOException {
		if (showComponent() && formsPanel != null) {
			formsPanel.setConcept((ConceptBean) host.getTermComponent(), host.getConfig());
		}
		
	}
   @Override
   protected String getToolTipText() {
      return "<html>Yin and yang can also be seen as a process of <br>"
		+ "transformation which describes the changes between<br>"
		+ " the phases of a cycle. For example, cold water (yin) <br>"
		+ "can be boiled and eventually turn into steam (yang).<p> <p>"
		+ "Stated forms (yin) can be classified and turned into<br>"
		+ "inferred forms (yang).";
   }
   @Override
   protected int getComponentId() {
      return Integer.MIN_VALUE;
   }

}
