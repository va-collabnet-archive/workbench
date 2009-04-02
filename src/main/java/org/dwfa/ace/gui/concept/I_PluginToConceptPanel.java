package org.dwfa.ace.gui.concept;

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComponent;

import org.dwfa.ace.api.I_HoldRefsetData;
import org.dwfa.ace.api.I_HostConceptPlugins;


public interface I_PluginToConceptPanel {
	public List<JComponent> getToggleBarComponents();
	public JComponent getComponent(I_HostConceptPlugins host);
	public void addShowComponentListener(ActionListener l);
	public void removeShowComponentListener(ActionListener l);
	public boolean showComponent();
   
   public void clearRefsetListeners();
   public void addRefsetListener(I_HoldRefsetData listener);

}
