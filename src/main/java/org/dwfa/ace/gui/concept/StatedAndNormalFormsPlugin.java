package org.dwfa.ace.gui.concept;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import javax.swing.ImageIcon;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.ConceptBean;

public class StatedAndNormalFormsPlugin extends AbstractPlugin {
	
	private static final long serialVersionUID = 1L;
	private static final int dataVersion = 1;

	private transient LogicalFormsPanel formsPanel;
	private transient I_HostConceptPlugins host;
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}
	
	public StatedAndNormalFormsPlugin(boolean selectedByDefault, int sequence) {
        super(selectedByDefault, sequence);
	}

	public UUID getId() {
		return TOGGLES.STATED_INFERRED.getPluginId();
	}

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
				.getResource("/24x24/plain/chrystal_ball.png"));
	}

	@Override
	public void update() throws IOException {
		if (showComponent() && formsPanel != null) {
			formsPanel.setConcept((ConceptBean) host.getTermComponent(), host.getConfig());
		}
		
	}
   @Override
   protected String getToolTipText() {
      return "<html>show/hide the stated and normal forms panel...";
   }
   @Override
   protected int getComponentId() {
      return Integer.MIN_VALUE;
   }

}
