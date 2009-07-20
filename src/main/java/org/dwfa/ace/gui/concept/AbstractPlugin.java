package org.dwfa.ace.gui.concept;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dwfa.ace.api.I_HoldRefsetData;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.log.AceLog;

public abstract class AbstractPlugin implements org.dwfa.ace.api.I_PluginToConceptPanel, PropertyChangeListener, ListSelectionListener {

   private static final long serialVersionUID = 1L;
   private static final int dataVersion = 1;

   private String name;
   private int lastSelectedIndex = -1;
   private int sequence;
   private boolean selectedByDefault;
   
   private transient JToggleButton toggleButton;
   private transient Set<ActionListener> showComponentListeners = new HashSet<ActionListener>();
   private transient Set<I_HoldRefsetData> refSetListeners = new HashSet<I_HoldRefsetData>();



   private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(name);
       out.writeInt(lastSelectedIndex);
       out.writeInt(sequence);
       out.writeBoolean(selectedByDefault);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
    	   name = (String) in.readObject();
    	   lastSelectedIndex = in.readInt();
    	   sequence = in.readInt();
    	   selectedByDefault = in.readBoolean();
    	   showComponentListeners = new HashSet<ActionListener>();
    	   refSetListeners = new HashSet<I_HoldRefsetData>();
    	   toggleButton = null;
       } else {
           throw new IOException("Can't handle dataversion: " + objDataVersion);
       }
   }

	
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
	  if (AceLog.getAppLog().isLoggable(Level.FINE)) {
		   AceLog.getAppLog().fine("New selection ("+ getId() + "): " + lastSelectedIndex);
	  }
      for (I_HoldRefsetData l: refSetListeners) {
         try {
            l.setComponentId(getComponentId());
         } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }
      
   }
   protected abstract int getComponentId();
   
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

	public AbstractPlugin(boolean selectedByDefault, int sequence) {
		super();
		this.selectedByDefault = selectedByDefault;
		this.sequence = sequence;
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
    
	
	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public void setId(UUID id) {
		throw new UnsupportedOperationException();
	}

	public int compareTo(I_PluginToConceptPanel another) {
		if (this.sequence == another.getSequence()) {
			return this.getId().compareTo(another.getId());
		}
		return this.sequence - another.getSequence();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	
}
