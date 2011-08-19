package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

public class DragPanelRel extends DragPanelComponentVersion<RelationshipVersionBI> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   private boolean inferred;
   ImageIcon       inferredIcon;
   private JLabel  relLabel;

   //~--- constructors --------------------------------------------------------

   public DragPanelRel(ConceptViewLayout viewLayout, CollapsePanel parentCollapsePanel,
                       RelationshipVersionBI rel, boolean inferred)
           throws TerminologyException, IOException {
      super(viewLayout, parentCollapsePanel, rel);
      layoutRel();
   }

   public DragPanelRel(LayoutManager layout, ConceptViewLayout viewLayout, CollapsePanel parentCollapsePanel,
                       RelationshipVersionBI rel, boolean inferred)
           throws TerminologyException, IOException {
      super(layout, viewLayout, parentCollapsePanel, rel);
      this.inferred = inferred;
      layoutRel();
   }

   //~--- methods -------------------------------------------------------------

   private void layoutRel() throws TerminologyException, IOException {
      boolean canDrop = false;

      if (getRel().getTime() == Long.MAX_VALUE) {
         setOpaque(true);
         setBackground(Color.YELLOW);
         canDrop = true;
      }

      setupDrag(getRel());
      setBorder(BorderFactory.createRaisedBevelBorder());
      relLabel = getJLabel(" ");
      relLabel.setBackground(Color.BLUE);
      relLabel.setOpaque(true);
      setDropPopupInset(relLabel.getPreferredSize().width);

      GridBagConstraints gbc = new GridBagConstraints();

      gbc.anchor     = GridBagConstraints.NORTHWEST;
      gbc.weightx    = 0;
      gbc.weighty    = 0;
      gbc.fill       = GridBagConstraints.BOTH;
      gbc.gridheight = 1;
      gbc.gridwidth  = 1;
      gbc.gridx      = 0;
      gbc.gridy      = 0;
      add(relLabel, gbc);
      gbc.gridx++;
      
      if (!getThingToDrag().isActive(getSettings().getConfig().getAllowedStatus())) {
         add(new JLabel(getGhostIcon()), gbc);
         gbc.gridx++;
      }

      add(conflictLabel, gbc);
      conflictLabel.setVisible(false);
      gbc.gridx++;
      gbc.weightx = 1;
      gbc.gridx++;

      TermComponentLabel typeLabel = getLabel(getRel().getTypeNid(), canDrop, getSettings().getRelType());

      if (getRel().isUncommitted()
              && (getRel().getStatusNid() == SnomedMetadataRfx.getSTATUS_RETIRED_NID())) {
         typeLabel.setFrozen(true);
      }

      typeLabel.addPropertyChangeListener("termComponent",
              new PropertyChangeManager<RelationshipAnalogBI>((RelationshipAnalogBI) getRel()) {
         @Override
         protected void changeProperty(I_GetConceptData newValue) {
            try {
               getComponent().setTypeNid(newValue.getNid());

               if (getComponent().isUncommitted()) {
                  Terms.get().addUncommitted(Terms.get().getConcept(getComponent().getOriginNid()));
               }
            } catch (PropertyVetoException e) {
               AceLog.getAppLog().alertAndLogException(e);
            } catch (TerminologyException e) {
               AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
               AceLog.getAppLog().alertAndLogException(e);
            }
         }
      });
      add(typeLabel, gbc);
      gbc.gridx++;
      gbc.weightx = 0;
      add(new JSeparator(SwingConstants.VERTICAL), gbc);
      gbc.weightx = 1;
      gbc.gridx++;

      TermComponentLabel destLabel = getLabel(getRel().getDestinationNid(), canDrop,
                                        getSettings().getRelTarget());

      if (getRel().isUncommitted()
              && (getRel().getStatusNid() == SnomedMetadataRfx.getSTATUS_RETIRED_NID())) {
         destLabel.setFrozen(true);
      }

      destLabel.addPropertyChangeListener("termComponent",
              new PropertyChangeManager<RelationshipAnalogBI>((RelationshipAnalogBI) getRel()) {
         @Override
         protected void changeProperty(I_GetConceptData newValue) {
            try {
               getComponent().setDestinationNid(newValue.getNid());

               if (getComponent().isUncommitted()) {
                  Terms.get().addUncommitted(Terms.get().getConcept(getComponent().getOriginNid()));
               }
            } catch (PropertyVetoException e) {
               AceLog.getAppLog().alertAndLogException(e);
            } catch (TerminologyException e) {
               AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
               AceLog.getAppLog().alertAndLogException(e);
            }
         }
      });
      add(destLabel, gbc);
      gbc.weightx = 0;
      gbc.gridx++;

      if (getRel().isInferred()) {
         add(new JLabel(getInferredIcon()), gbc);
         gbc.gridx++;
      }

      add(getComponentActionMenuButton(), gbc);
      gbc.gridx++;

      JButton collapseExpandButton = getCollapseExpandButton();

      add(collapseExpandButton, gbc);
      addSubPanels(gbc);
   }

   //~--- get methods ---------------------------------------------------------

   public RelationshipVersionBI getDraggedThing() {
      return thingToDrag;
   }

   private ImageIcon getInferredIcon() {
      if (inferredIcon == null) {
         inferredIcon = new ImageIcon(DragPanelRel.class.getResource("/16x16/plain/chrystal_ball.png"));
      }

      return inferredIcon;
   }

   @Override
   public DataFlavor getNativeDataFlavor() {
      return DragPanelDataFlavors.relVersionFlavor;
   }

   @Override
   public Collection<DragPanelComponentVersion<RelationshipVersionBI>> getOtherVersionPanels()
           throws IOException, TerminologyException {
      Collection<DragPanelComponentVersion<RelationshipVersionBI>> panelList =
         new ArrayList<DragPanelComponentVersion<RelationshipVersionBI>>();
      Collection<RelationshipVersionBI> versions = thingToDrag.getChronicle().getVersions();

      for (RelationshipVersionBI dav : versions) {
         if (inferred) {
            if (!dav.isInferred()) {
               continue;
            }
         } else {
            if (dav.isInferred()) {
               continue;
            }
         }

         if (!thingToDrag.equals(dav)) {
            DragPanelRel dpd = new DragPanelRel(new GridBagLayout(), viewLayout, null, dav, inferred);

            dpd.setInactiveBackground();
            panelList.add(dpd);
         }
      }

      return panelList;
   }

   public RelationshipVersionBI getRel() {
      return thingToDrag;
   }

   @Override
   public RelationshipVersionBI getThingToDrag() {
      return thingToDrag;
   }

   @Override
   public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] { DragPanelDataFlavors.relVersionFlavor };
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

   public void setDraggedThing(RelationshipVersionBI rel) {

      // TODO handle drop.
   }

   private void setInactiveBackground() {
      relLabel.setBackground(relLabel.getBackground().darker());
   }
}
