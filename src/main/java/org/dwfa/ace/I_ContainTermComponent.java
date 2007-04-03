package org.dwfa.ace;

import java.beans.PropertyChangeListener;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.config.AceFrameConfig;

public interface I_ContainTermComponent {
	
	public static final String TERM_COMPONENT = "termComponent";

	public I_AmTermComponent getTermComponent();

	public void setTermComponent(I_AmTermComponent termComponent);

	public void addPropertyChangeListener(String property, PropertyChangeListener l);
	
	public void removePropertyChangeListener(String property, PropertyChangeListener l);
	
	public AceFrameConfig getConfig();
}