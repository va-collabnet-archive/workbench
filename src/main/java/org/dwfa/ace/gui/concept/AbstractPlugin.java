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

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

import org.dwfa.ace.AceLog;

public abstract class AbstractPlugin implements I_PluginToConceptPanel, PropertyChangeListener {

	private class ToggleActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				update();
			} catch (IOException e1) {
				AceLog.alertAndLog(null, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
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
			AceLog.alertAndLog(null, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
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
		}
		return toggleButton;
	}
	
	protected abstract ImageIcon getImageIcon();
	boolean selectedByDefault;
	
}
