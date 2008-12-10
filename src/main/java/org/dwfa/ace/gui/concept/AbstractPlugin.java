package org.dwfa.ace.gui.concept;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dwfa.ace.api.I_HoldRefsetData;
import org.dwfa.ace.log.AceLog;

public abstract class AbstractPlugin implements I_PluginToConceptPanel, PropertyChangeListener, ListSelectionListener {
   private int lastSelectedIndex = -1;
   private int pluginId = pluginSequence++;
   private static int pluginSequence = 0;
	
   public int getLastSelectedIndex() {
		return lastSelectedIndex;
	}
   
   public void valueChanged(ListSelectionEvent evt) {
       // Ignore extra messages.
       if (evt.getValueIsAdjusting()) {
           return;
       }
       DefaultListSelectionModel lsm = (DefaultListSelectionModel) evt.getSource();
	  lastSelectedIndex = lsm.getMinSelectionIndex();
	   AceLog.getAppLog().info("New selection ("+ pluginId + "): " + lastSelectedIndex);
      for (I_HoldRefsetData l: refSetListeners) {
         try {
            l.setComponentId(getComponentId());
         } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }
      
   }
   protected abstract int getComponentId();
   
   Set<I_HoldRefsetData> refSetListeners = new HashSet<I_HoldRefsetData>();
	public void addRefsetListener(I_HoldRefsetData listener) {
      refSetListeners.add(listener);
   }

   public void clearRefsetListeners() {
      refSetListeners.clear();
   }
   
   private class ToggleActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				update();
			} catch (IOException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}
			for (ActionListener l: showComponentListeners) {
				l.actionPerformed(e);
			}
		}
	}

	private JToggleButton toggleButton;
	private Set<ActionListener> showComponentListeners = new HashSet<ActionListener>();

	public AbstractPlugin(boolean selectedByDefault) {
		super();
		this.selectedByDefault = selectedByDefault;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		try {
			update();
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLog(null, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
		}
	}

	public final List<JComponent> getToggleBarComponents() {
		return Arrays.asList(new JComponent[] { getToggleButton() });
	}

	public abstract void update() throws IOException;
	
	public final void addShowComponentListener(ActionListener l) {
		showComponentListeners.add(l);
	}

	public final void removeShowComponentListener(ActionListener l) {
		showComponentListeners.remove(l);
	}

	public final boolean showComponent() {
		return getToggleButton().isSelected();
	}

	public final JToggleButton getToggleButton() {
		if (toggleButton == null) {
			toggleButton = new JToggleButton(getImageIcon());
			toggleButton.setSelected(selectedByDefault);
			toggleButton.addActionListener(new ToggleActionListener());
         toggleButton.setToolTipText(getToolTipText());
		}
		return toggleButton;
	}
	
	protected abstract ImageIcon getImageIcon();
   protected abstract String getToolTipText();
	boolean selectedByDefault;
	
}
