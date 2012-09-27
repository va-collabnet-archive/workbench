package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.arena.conceptview.DragPanelComponentVersion.SubPanelTypes;
import org.ihtsdo.tk.spec.RelationshipSpec;

//~--- JDK imports ------------------------------------------------------------

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;

import java.util.Collection;
import java.util.EnumSet;

import javax.swing.TransferHandler;

public class DragPanelRelTemplate extends DragPanel<RelationshipSpec> implements I_ToggleSubPanels {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- constructors --------------------------------------------------------

   public DragPanelRelTemplate(ConceptViewLayout viewLayout, RelationshipSpec rs) {
      super(viewLayout, rs);
   }

   public DragPanelRelTemplate(LayoutManager layout, ConceptViewLayout viewLayout, RelationshipSpec rs) {
      super(layout, viewLayout, rs);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void hideSubPanels(EnumSet<SubPanelTypes> panels) {

      // nothing to do...;
   }

   @Override
   public void showConflicts(Collection<Integer> saptCol) {

      // nothing to do.
   }

   @Override
   public void showSubPanels(EnumSet<SubPanelTypes> panels) {

      // nothing to do...;
   }

   //~--- get methods ---------------------------------------------------------

   public RelationshipSpec getDraggedThing() {
      return thingToDrag;
   }

   @Override
   public DataFlavor getNativeDataFlavor() {
      return DragPanelDataFlavors.relVersionFlavor;    // from desc
   }

   @Override
   public RelationshipSpec getThingToDrag() {
      return thingToDrag;
   }

   @Override
   public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] { DragPanelDataFlavors.relVersionFlavor };    // from desc
   }

   @Override
   protected int getTransferMode() {
      return TransferHandler.COPY;
   }

   @Override
   public String getUserString(RelationshipSpec obj) {
      return obj.toString();
   }

   @Override
   public boolean isDataFlavorSupported(DataFlavor flavor) {
      return false;
   }

   @Override
   public boolean isExpanded() {
      return false;
   }
}
