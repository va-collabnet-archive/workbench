package org.dwfa.ace.api;

import java.awt.event.ActionListener;
import java.util.List;
import java.util.UUID;

import javax.swing.JComponent;

public interface I_PluginToConceptPanel extends Comparable<I_PluginToConceptPanel> {
	public List<JComponent> getToggleBarComponents();
	public JComponent getComponent(I_HostConceptPlugins host);
	public void addShowComponentListener(ActionListener l);
	public void removeShowComponentListener(ActionListener l);
	public boolean showComponent();
   
	public void clearRefsetListeners();
    public void addRefsetListener(I_HoldRefsetData listener);
   
    public void setId(UUID id);
    public UUID getId();
    
    public void setSequence(int sequence);
    public int getSequence();

}
