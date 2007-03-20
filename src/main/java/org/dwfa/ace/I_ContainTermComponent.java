package org.dwfa.ace;

import java.beans.PropertyChangeListener;

import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.types.I_AmTermComponent;

public interface I_ContainTermComponent {
	
	public static final String TERM_COMPONENT = "termComponent";

	public I_AmTermComponent getTermComponent();

	public void setTermComponent(I_AmTermComponent termComponent);

	public void addPropertyChangeListener(String property, PropertyChangeListener l);
	
	public void removePropertyChangeListener(String property, PropertyChangeListener l);
	
	public AceFrameConfig getConfig();
}