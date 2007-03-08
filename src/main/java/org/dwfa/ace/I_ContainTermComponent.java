package org.dwfa.ace;

import java.beans.PropertyChangeListener;

import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.types.I_AmTermComponent;

public interface I_ContainTermComponent {

	public I_AmTermComponent getTermComponent();

	public void setTermComponent(I_AmTermComponent termComponent);

	public void addTermChangeListener(PropertyChangeListener l);
	public void removeTermChangeListener(PropertyChangeListener l);
	
	public AceFrameConfig getConfig();
}