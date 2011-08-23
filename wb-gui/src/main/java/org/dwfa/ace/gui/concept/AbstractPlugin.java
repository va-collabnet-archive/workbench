/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.dwfa.ace.gui.concept;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_HoldRefsetData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

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

import javax.swing.AbstractAction;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public abstract class AbstractPlugin
        implements org.dwfa.ace.api.I_PluginToConceptPanel, PropertyChangeListener, ListSelectionListener {
   private static final int  dataVersion      = 1;
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   private int                             lastSelectedIndex      = -1;
   private transient Set<ActionListener>   showComponentListeners = new HashSet<ActionListener>();
   private transient Set<I_HoldRefsetData> refSetListeners        = new HashSet<I_HoldRefsetData>();
   private transient I_HostConceptPlugins  host;
   private String                          name;
   private boolean                         selectedByDefault;
   private int                             sequence;
   private transient JToggleButton         toggleButton;

   //~--- constructors --------------------------------------------------------

   public AbstractPlugin(boolean selectedByDefault, int sequence) {
      super();
      this.selectedByDefault = selectedByDefault;
      this.sequence          = sequence;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void addRefsetListener(I_HoldRefsetData listener) {
      refSetListeners.add(listener);
   }

   @Override
   public final void addShowComponentListener(ActionListener l) {
      showComponentListeners.add(l);
   }

   @Override
   public void clearRefsetListeners() {
      refSetListeners.clear();
   }

   @Override
   public int compareTo(I_PluginToConceptPanel another) {
      if (this.sequence == another.getSequence()) {
         return this.getId().compareTo(another.getId());
      }

      return this.sequence - another.getSequence();
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      try {
         update();
      } catch (IOException e1) {
         AceLog.getAppLog().alertAndLog(null, Level.SEVERE,
                                        "Database Exception: " + e1.getLocalizedMessage(), e1);
      } catch (TerminologyException e1) {
         AceLog.getAppLog().alertAndLog(null, Level.SEVERE,
                                        "Database Exception: " + e1.getLocalizedMessage(), e1);
      }
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion == dataVersion) {
         name                   = (String) in.readObject();
         lastSelectedIndex      = in.readInt();
         sequence               = in.readInt();
         selectedByDefault      = in.readBoolean();
         showComponentListeners = new HashSet<ActionListener>();
         refSetListeners        = new HashSet<I_HoldRefsetData>();
         toggleButton           = null;
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   @Override
   public final void removeShowComponentListener(ActionListener l) {
      showComponentListeners.remove(l);
   }

   @Override
   public final boolean showComponent() {
      return getToggleButton().isSelected();
   }

   public abstract void update() throws IOException, TerminologyException;

   @Override
   public void valueChanged(ListSelectionEvent evt) {

      // Ignore extra messages.
      if (evt.getValueIsAdjusting()) {
         return;
      }

      DefaultListSelectionModel lsm = (DefaultListSelectionModel) evt.getSource();

      lastSelectedIndex = lsm.getMinSelectionIndex();

      if (AceLog.getAppLog().isLoggable(Level.FINE)) {
         AceLog.getAppLog().fine("New selection (" + getId() + "): " + lastSelectedIndex);
      }

      for (I_HoldRefsetData l : refSetListeners) {
         try {
            l.setComponentId(getComponentId());
         } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(name);
      out.writeInt(lastSelectedIndex);
      out.writeInt(sequence);
      out.writeBoolean(selectedByDefault);
   }

   //~--- get methods ---------------------------------------------------------

   protected abstract int getComponentId();

   public final I_HostConceptPlugins getHost() {
      return host;
   }

   protected abstract ImageIcon getImageIcon();

   public int getLastSelectedIndex() {
      return lastSelectedIndex;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public int getSequence() {
      return sequence;
   }

   @Override
   public final List<JComponent> getToggleBarComponents() {
      return Arrays.asList(new JComponent[] { getToggleButton() });
   }

   public final JToggleButton getToggleButton() {
      if (toggleButton == null) {
         toggleButton = new JToggleButton(getImageIcon());
         toggleButton.setSelected(selectedByDefault);
         toggleButton.addActionListener(new ToggleActionListener());
         toggleButton.setToolTipText(getToolTipText());

         InputMap imap = toggleButton.getInputMap();

         imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
                                         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "left");
         imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
                                         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "right");
         getToggleButton().getActionMap().put("left", new MoveLeft());
         getToggleButton().getActionMap().put("right", new MoveRight());
      }

      return toggleButton;
   }

   protected abstract String getToolTipText();

   //~--- set methods ---------------------------------------------------------

   public final void setHost(I_HostConceptPlugins host) {
      this.host = host;
   }

   @Override
   public void setId(UUID id) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setName(String name) {
      this.name = name;
   }

   @Override
   public void setSequence(int sequence) {
      this.sequence = sequence;
   }

   //~--- inner classes -------------------------------------------------------

   private class MoveLeft extends AbstractAction {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      private MoveLeft() {
         super("left");
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void actionPerformed(ActionEvent arg0) {
         sequence--;
         AceLog.getAppLog().info("Sequence: " + sequence);

         try {
            update();
         } catch (IOException e1) {
            AceLog.getAppLog().alertAndLog(null, Level.SEVERE,
                                           "Database Exception: " + e1.getLocalizedMessage(), e1);
         } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLog(null, Level.SEVERE,
                                           "Database Exception: " + e1.getLocalizedMessage(), e1);
         }
      }
   }


   private class MoveRight extends AbstractAction {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      private MoveRight() {
         super("right");
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void actionPerformed(ActionEvent arg0) {
         sequence++;
         AceLog.getAppLog().info("Sequence: " + sequence);

         try {
            update();
         } catch (IOException e1) {
            AceLog.getAppLog().alertAndLog(null, Level.SEVERE,
                                           "Database Exception: " + e1.getLocalizedMessage(), e1);
         } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLog(null, Level.SEVERE,
                                           "Database Exception: " + e1.getLocalizedMessage(), e1);
         }

         JOptionPane.showMessageDialog(getToggleButton(), "Move Right");
      }
   }


   private class ToggleActionListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         try {
            update();
         } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
         } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
         }

         for (ActionListener l : showComponentListeners) {
            l.actionPerformed(e);
         }
      }
   }
}
