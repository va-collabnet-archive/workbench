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
import java.awt.Dimension;
import java.awt.FontMetrics;
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
import org.dwfa.ace.DynamicWidthTermComponentLabel;
import org.ihtsdo.arena.editor.ArenaEditor;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;

public class DragPanelRel extends DragPanelComponentVersion<RelationshipVersionBI> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   private boolean inferred;
   ImageIcon       inferredIcon;
   private JLabel  relLabel;
   private ConceptViewLayout cvLayout;
   private static ImageIcon definedIcon =
      new ImageIcon(DragPanelRel.class.getResource("/16x16/plain/taxonomy/defined-single-parent.png"));
   private static ImageIcon primitiveIcon =
      new ImageIcon(DragPanelRel.class.getResource("/16x16/plain/taxonomy/primitive-single-parent.png"));
   private static ImageIcon dupIcon =
      new ImageIcon(DragPanelRel.class.getResource("/16x16/plain/text_sum.png"));
   private JLabel dupLabel = new JLabel(dupIcon);

   //~--- constructors --------------------------------------------------------

   public DragPanelRel(ConceptViewLayout viewLayout, CollapsePanel parentCollapsePanel,
                       RelationshipVersionBI rel, boolean inferred)
           throws TerminologyException, IOException, ContradictionException {
      super(viewLayout, parentCollapsePanel, rel);
      this.cvLayout = viewLayout;
      layoutRel();
   }

   public DragPanelRel(LayoutManager layout, ConceptViewLayout viewLayout, CollapsePanel parentCollapsePanel,
                       RelationshipVersionBI rel, boolean inferred)
           throws TerminologyException, IOException, ContradictionException {
      super(layout, viewLayout, parentCollapsePanel, rel);
      this.inferred = inferred;
      this.cvLayout = viewLayout;
      layoutRel();
   }

   //~--- methods -------------------------------------------------------------

   private void layoutRel() throws TerminologyException, IOException, ContradictionException {
      boolean canDrop = false;
      
      if (!ArenaEditor.diffColor.isEmpty() && viewLayout.getSettings().isForPromotion()){
            if(ArenaEditor.diffColor.containsKey(getThingToDrag().getNid())){
                Color color = ArenaEditor.diffColor.get(getThingToDrag().getNid());
                setBackground(color);
                
            }
        }
      
      if (getRel().getTime() == Long.MAX_VALUE) {
         setOpaque(true);
         setBackground(Color.YELLOW);
         canDrop = true;
      }
      
      ConceptAttributeVersionBI conceptAttributesActive = Ts.get().getConceptVersion(cvLayout.getSettings().getConfig().getViewCoordinate(), getRel().getTargetNid()).getConceptAttributesActive();
      setupDrag(getRel());
      setBorder(BorderFactory.createRaisedBevelBorder());

      GridBagConstraints gbc = new GridBagConstraints();

      gbc.anchor     = GridBagConstraints.NORTHWEST;
      gbc.weightx    = 0;
      gbc.weighty    = 0;
      gbc.fill       = GridBagConstraints.BOTH;
      gbc.gridheight = 2;
      gbc.gridwidth  = 1;
      gbc.gridx      = 0;
      gbc.gridy      = 0;
      
      Color color = new Color(135,206,250);
      
      if ((getParentCollapsePanel() == null)
                || !getSettings().getView().getConfig().getAllowedStatus().contains(getRel().getStatusNid())) {
          gbc.gridheight = 1;
            JButton button = getComponentActionMenuButton();
            button.setMinimumSize(new Dimension(16, 16));
            button.setPreferredSize(new Dimension(16, 16));
            button.setBackground(color.darker());
            button.setOpaque(true);
            add(button, gbc);
            gbc.gridy++;
            gbc.gridheight = GridBagConstraints.REMAINDER;
            gbc.weighty = 1;
            relLabel = getJLabel(" ");
            relLabel.setOpaque(true);
            relLabel.setMinimumSize(new Dimension(16, 28));
            relLabel.setPreferredSize(new Dimension(16, 28));
            setDropPopupInset(relLabel.getPreferredSize().width);
            relLabel.setBackground(color.darker());
            add(relLabel, gbc);
        } else {
            gbc.gridheight = 1;
            JButton button = getComponentActionMenuButton();
            button.setMinimumSize(new Dimension(16, 16));
            button.setPreferredSize(new Dimension(16, 16));
            button.setBackground(color);
            button.setOpaque(true);
            add(button, gbc);
            gbc.gridy++;
            gbc.gridheight = GridBagConstraints.REMAINDER;
            gbc.weighty = 1;
            relLabel = getJLabel(" ");
            relLabel.setOpaque(true);
            relLabel.setMinimumSize(new Dimension(16, 12));
            relLabel.setPreferredSize(new Dimension(16, 12));
            setDropPopupInset(relLabel.getPreferredSize().width);
            relLabel.setBackground(color);
            add(relLabel, gbc);
            gbc.gridy = 0;
            gbc.gridheight = 2;
            gbc.weighty = 0;
        }
      gbc.gridx++;
      
       
      if (!getThingToDrag().isActive(getSettings().getConfig().getAllowedStatus())) {
         add(new JLabel(getGhostIcon()), gbc);
         gbc.gridx++;
      }
      
      add(conflictLabel, gbc);
      conflictLabel.setVisible(false);
      gbc.gridx++;
      add(dupLabel, gbc);
      dupLabel.setVisible(false);
      gbc.gridx++;
      gbc.weightx = 0;
      gbc.weighty = 1;
      gbc.gridx++;

      TermComponentLabel typeLabel = getLabel(getRel().getTypeNid(), canDrop, getSettings().getRelType(), 115);
      FontMetrics metrics = typeLabel.getFontMetrics(typeLabel.getFont());
      int hgt = metrics.getHeight();
      String[] split = typeLabel.getText().split(" ");
      String longest = "";
      for(String s : split){
          if(s.length() > longest.length()){
              longest = s;
          }
      }

      int adv = metrics.stringWidth(longest);
      Dimension size = new Dimension(adv+2, hgt+2); 
      if(size.getWidth() > 70){
          typeLabel.setFixedWidth(adv+2);
          typeLabel.setPreferredSize(size);
      }

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
                  Terms.get().addUncommitted(Terms.get().getConcept(getComponent().getSourceNid()));
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
      typeLabel.setBorder(BorderFactory.createEmptyBorder(0,3,0,2));
      add(typeLabel, gbc);
      gbc.gridx++;
      gbc.weightx = 0;
      add(new JSeparator(SwingConstants.VERTICAL), gbc);
      gbc.weightx = 1;
      gbc.gridx++;

      DynamicWidthTermComponentLabel destLabel = getDynamicLabel(getRel().getTargetNid(), canDrop,
                                        getSettings().getRelTarget());
      destLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,3));

      if (getRel().isUncommitted()
              && (getRel().getStatusNid() == SnomedMetadataRfx.getSTATUS_RETIRED_NID())) {
         destLabel.setFrozen(true);
      }

      destLabel.addPropertyChangeListener("termComponent",
              new PropertyChangeManager<RelationshipAnalogBI>((RelationshipAnalogBI) getRel()) {
         @Override
         protected void changeProperty(I_GetConceptData newValue) {
            try {
                 int oldTargetNid = getComponent().getTargetNid();
                getComponent().setTargetNid(newValue.getNid());

                if (getComponent().isUncommitted()) {
                    Terms.get().addUncommitted(Terms.get().getConcept(getComponent().getSourceNid()));
                }
                Ts.get().touchRelOrigin(getComponent().getSourceNid());
                Ts.get().touchRelTarget(getComponent().getTargetNid());
                 Ts.get().touchRelTarget(oldTargetNid);
                Ts.get().touchComponent(oldTargetNid);
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
      

      JButton collapseExpandButton = getCollapseExpandButton();
      add(collapseExpandButton, gbc);
      gbc.gridx++;
      
      if (getRel().isInferred()) {
         add(new JLabel(getInferredIcon()), gbc);
         gbc.gridx++;
      }

      JLabel icon = null;
      if (conceptAttributesActive != null) {
           if (Ts.get().getConceptVersion(cvLayout.getSettings().getConfig().getViewCoordinate(), getRel().getTargetNid()).getConceptAttributesActive().isDefined()) {
               icon = new JLabel(definedIcon);
           } else {
               icon = new JLabel(primitiveIcon);
           }
           icon.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
       }else{
           icon = new JLabel(" ");
       }
      add(icon, gbc);
      gbc.gridx++;
      
      gbc.gridy = 2;
      gbc.gridheight = 1;
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
            DragPanelRel dpd;
             try {
                 dpd = new DragPanelRel(new GridBagLayout(), viewLayout, null, dav, inferred);
             } catch (ContradictionException ex) {
                 throw new TerminologyException(ex);
             }

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
   
   protected void setInheritedDuplicate(){
       dupLabel.setVisible(true);
   }
}
