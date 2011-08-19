package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.arena.conceptview.DragPanelComponentVersion.SubPanelTypes;
import org.ihtsdo.tk.spec.RelSpec;

//~--- JDK imports ------------------------------------------------------------

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;

import java.util.Collection;
import java.util.EnumSet;

import javax.swing.TransferHandler;

public class DragPanelRelTemplate extends DragPanel<RelSpec> implements I_ToggleSubPanels {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- constructors --------------------------------------------------------

   public DragPanelRelTemplate(ConceptViewLayout viewLayout, RelSpec rs) {
      super(viewLayout, rs);
   }

   public DragPanelRelTemplate(LayoutManager layout, ConceptViewLayout viewLayout, RelSpec rs) {
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

   public RelSpec getDraggedThing() {
      return thingToDrag;
   }

   @Override
   public DataFlavor getNativeDataFlavor() {
      return DragPanelDataFlavors.relVersionFlavor;    // from desc
   }

   @Override
   public RelSpec getThingToDrag() {
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
   public String getUserString(RelSpec obj) {
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
