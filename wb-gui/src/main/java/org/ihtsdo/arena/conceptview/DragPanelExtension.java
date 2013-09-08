package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.arena.editor.ArenaEditor;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;
import org.ihtsdo.tk.api.refex.type_float.RefexFloatVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidAnalogBI;

public class DragPanelExtension extends DragPanelComponentVersion<RefexVersionBI<?>> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   private JLabel extensionLabel;

   //~--- constructors --------------------------------------------------------

   public DragPanelExtension(ConceptViewLayout viewLayout, CollapsePanel parentCollapsePanel,
                             RefexVersionBI<?> refex)
           throws IOException, TerminologyException {
      super(viewLayout, parentCollapsePanel, refex);
      layoutExtension();
   }

   public DragPanelExtension(LayoutManager layout, ConceptViewLayout viewLayout,
                             CollapsePanel parentCollapsePanel, RefexVersionBI<?> refex)
           throws IOException, TerminologyException {
      super(layout, viewLayout, parentCollapsePanel, refex);
      layoutExtension();
   }

   //~--- methods -------------------------------------------------------------

   public final void layoutExtension() throws IOException, TerminologyException {
      setLayout(new GridBagLayout());

      boolean            canDrop = false;
      TerminologyStoreDI ts      = Ts.get();
      RefexVersionBI<?>  refexV  = getRefexV();
      
      if (!ArenaEditor.diffColor.isEmpty()){
            if(ArenaEditor.diffColor.containsKey(getThingToDrag().getNid())){
                Color color = ArenaEditor.diffColor.get(getThingToDrag().getNid());
                    setBackground(color);
                
            }
        }
      
      if (getRefexV().isUncommitted()) {
         setOpaque(true);
         setBackground(Color.YELLOW);
      }

      if (getRefexV().isUncommitted()
              && (getRefexV().getStatusNid() == SnomedMetadataRfx.getSTATUS_RETIRED_NID())) {
         canDrop = true;
      }

      setBorder(BorderFactory.createRaisedBevelBorder());
      extensionLabel = getJLabel(" ");
      extensionLabel.setBackground(Color.RED);
      extensionLabel.setOpaque(true);
      setDropPopupInset(extensionLabel.getPreferredSize().width);

      GridBagConstraints gbc = new GridBagConstraints();

      gbc.anchor     = GridBagConstraints.NORTHWEST;
      gbc.weightx    = 0;
      gbc.weighty    = 0;
      gbc.fill       = GridBagConstraints.BOTH;
      gbc.gridheight = 1;
      gbc.gridwidth  = 1;
      gbc.gridx      = 0;
      gbc.gridy      = 0;
      add(extensionLabel, gbc);
      gbc.gridx++;

      if (!getRefexV().isActive(getSettings().getConfig().getAllowedStatus())) {
         add(new JLabel(getGhostIcon()), gbc);
         gbc.gridx++;
      }

      add(conflictLabel, gbc);
      conflictLabel.setVisible(false);
      gbc.gridx++;
      gbc.weightx = 1;
      gbc.anchor  = GridBagConstraints.NORTHWEST;
      gbc.gridx++;

      TermComponentLabel typeLabel = getLabel(getRefexV().getRefexNid(), canDrop,
                                        getSettings().getRefexName());
      
      if ((getRefexV().isUncommitted())
              && ((getRefexV().getStatusNid() == SnomedMetadataRfx.getSTATUS_RETIRED_NID()))) {
         typeLabel.setFrozen(true);
      }

      typeLabel.addPropertyChangeListener("termComponent",
              new PropertyChangeManagerRefex<RefexAnalogBI>((RefexAnalogBI) getRefexV()) {
         @Override
         protected void changeProperty(I_GetConceptData newValue) {
            try {
               getComponent().setCollectionNid(newValue.getNid());

               if (getComponent().isUncommitted()) {
                  int conceptNidForNid = Ts.get().getConceptNidForNid(getComponent().getReferencedComponentNid());
                  Ts.get().addUncommitted(Ts.get().getConcept(conceptNidForNid));
               }
            } catch (PropertyVetoException e) {
               AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
               AceLog.getAppLog().alertAndLogException(e);
            }
         }

           @Override
           protected void changeProperty(String newValue) {
               //nothing to do
           }
      });

      typeLabel.setFrozen(canDrop);
      add(typeLabel, gbc);
      gbc.gridx++;
      gbc.weightx = 0;
      add(new JSeparator(SwingConstants.VERTICAL), gbc);
      gbc.weightx = 1;
      gbc.gridx++;

      boolean classFound = false;
      
      if (RefexFloatVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
        Float    value      = ((RefexFloatVersionBI) getRefexV()).getFloat1();
        FixedWidthJEditorPane textPane = new FixedWidthJEditorPane();

        textPane.setEditable(true); //TODO set to can drop
        textPane.setOpaque(false);
        textPane.setFont(textPane.getFont().deriveFont(getSettings().getFontSize()));
        textPane.setText(value.toString());
        add(textPane, gbc);
        gbc.weightx = 0;
        gbc.gridx++;
        textPane.getDocument().addDocumentListener(new UpdateFloatDocumentListener(textPane, (RefexAnalogBI) getRefexV()));
        classFound = true;
      }

      if (RefexNidVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
         int                cnid = ((RefexNidVersionBI) getRefexV()).getNid1();
         TermComponentLabel ext  = getLabel(cnid, canDrop, getSettings().getC1Refex());

         ext.setFrozen(canDrop);
         ext.setOpaque(false);
         ext.setBackground(getBackground());
         add(ext, gbc);
         gbc.gridx++;
         classFound = true;
         ext.addPropertyChangeListener("termComponent",
              new PropertyChangeManagerRefex<RefexNidAnalogBI>((RefexNidAnalogBI) getRefexV()) {
         @Override
         protected void changeProperty(I_GetConceptData newValue) {
            try {
                getComponent().setNid1(newValue.getNid());
               if (getComponent().isUncommitted()) {
                  int conceptNidForNid = Ts.get().getConceptNidForNid(getComponent().getReferencedComponentNid());
                  Ts.get().addUncommitted(Ts.get().getConcept(conceptNidForNid));
               }
            } catch (PropertyVetoException e) {
               AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
               AceLog.getAppLog().alertAndLogException(e);
            }
         }

              @Override
              protected void changeProperty(String newValue) {
                  //nothing to do 
              }
      });
      }
     
      if (RefexNidNidVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
         int                cnid = ((RefexNidNidVersionBI) getRefexV()).getNid2();
         TermComponentLabel ext  = getLabel(cnid, canDrop, getSettings().getC2Refex());

         ext.setFrozen(canDrop);
         ext.setOpaque(false);
         ext.setBackground(getBackground());
         add(ext, gbc);
         gbc.gridx++;
         classFound = true;
      }

      if (RefexNidNidNidVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
         int                cnid = ((RefexNidNidNidVersionBI) getRefexV()).getNid3();
         TermComponentLabel ext  = getLabel(cnid, canDrop, getSettings().getC3Refex());

         ext.setFrozen(canDrop);
         ext.setOpaque(false);
         ext.setBackground(getBackground());
         add(ext, gbc);
         gbc.gridx++;
         classFound = true;
      }

      if (RefexStringVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
         String                text     = ((RefexStringVersionBI) getRefexV()).getString1();
         FixedWidthJEditorPane textPane = new FixedWidthJEditorPane();

         textPane.setEditable(canDrop);
         textPane.setOpaque(false);
         textPane.setBackground(getBackground());
         textPane.setFont(textPane.getFont().deriveFont(getSettings().getFontSize()));
         textPane.setText(text);
         add(textPane, gbc);
         gbc.gridx++;
         classFound = true;
      }

      if (RefexIntVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
         int    value      = ((RefexIntVersionBI) getRefexV()).getInt1();
         JLabel valueLabel = new JLabel(Integer.toString(value));

         valueLabel.setOpaque(false);
         valueLabel.setBackground(getBackground());
         valueLabel.setFont(valueLabel.getFont().deriveFont(getSettings().getFontSize()));
         valueLabel.setText(Integer.toString(value));
         add(valueLabel, gbc);
         gbc.gridx++;
         classFound = true;
      }

      if (RefexBooleanVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
         Boolean value      = ((RefexBooleanVersionBI) getRefexV()).getBoolean1();
         JLabel  valueLabel = new JLabel(value.toString());

         valueLabel.setOpaque(false);
         valueLabel.setBackground(getBackground());
         valueLabel.setFont(valueLabel.getFont().deriveFont(getSettings().getFontSize()));
         valueLabel.setText(value.toString());
         add(valueLabel, gbc);
         gbc.gridx++;
         classFound = true;
      }

      if (!classFound) {
         FixedWidthJEditorPane textPane = new FixedWidthJEditorPane();

         textPane.setEditable(canDrop);
         textPane.setOpaque(false);
         textPane.setBackground(getBackground());
         textPane.setFont(textPane.getFont().deriveFont(getSettings().getFontSize()));
         textPane.setText(getRefexV().toUserString());
         add(textPane, gbc);
         gbc.gridx++;
         classFound = true;
      }

      gbc.weightx = 0;
      gbc.gridx++;
      add(getComponentActionMenuButton(), gbc);
      gbc.gridx++;

      JButton collapseExpandButton = getCollapseExpandButton();

      add(collapseExpandButton, gbc);
      addSubPanels(gbc);
   }

   //~--- get methods ---------------------------------------------------------

   public ComponentVersionBI getDraggedThing() {
      return thingToDrag;
   }

   @Override
   public DataFlavor getNativeDataFlavor() {
      return DragPanelDataFlavors.conceptFlavor;
   }

   @Override
   public Collection<DragPanelComponentVersion<RefexVersionBI<?>>> getOtherVersionPanels()
           throws IOException, TerminologyException {
      Collection<DragPanelComponentVersion<RefexVersionBI<?>>> panelList =
         new ArrayList<DragPanelComponentVersion<RefexVersionBI<?>>>();
      Collection<RefexVersionBI<?>> versions = thingToDrag.getChronicle().getVersions();

      for (RefexVersionBI<?> dav : versions) {
         if (!thingToDrag.equals(dav)) {
            DragPanelExtension dpd = new DragPanelExtension(new GridBagLayout(), viewLayout, null, dav);

            dpd.setInactiveBackground();
            panelList.add(dpd);
         }
      }

      return panelList;
   }

   public RefexVersionBI<?> getRefexV() {
      return thingToDrag;
   }

   @Override
   public RefexVersionBI<?> getThingToDrag() {
      return thingToDrag;
   }

   @Override
   public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] { DragPanelDataFlavors.conceptFlavor };
   }

   @Override
   protected int getTransferMode() {
      return TransferHandler.COPY;
   }

   @Override
   public boolean isDataFlavorSupported(DataFlavor flavor) {
      return false;
   }

   //~--- set methods ---------------------------------------------------------

   public void setDraggedThing(ComponentVersionBI component) {

      // handle drop...;
   }

   void setInactiveBackground() {
      extensionLabel.setBackground(extensionLabel.getBackground().darker());
   }
}
