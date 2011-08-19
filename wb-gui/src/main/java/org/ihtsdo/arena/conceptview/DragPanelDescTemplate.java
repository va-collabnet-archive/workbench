package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.arena.conceptview.DragPanelComponentVersion.SubPanelTypes;
import org.ihtsdo.tk.spec.DescriptionSpec;

//~--- JDK imports ------------------------------------------------------------

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;

import java.util.Collection;
import java.util.EnumSet;

import javax.swing.TransferHandler;

public class DragPanelDescTemplate extends DragPanel<DescriptionSpec> implements I_ToggleSubPanels {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- constructors --------------------------------------------------------

   public DragPanelDescTemplate(ConceptViewLayout viewLayout, DescriptionSpec ds) {
      super(viewLayout, ds);
   }

   public DragPanelDescTemplate(LayoutManager layout, ConceptViewLayout viewLayout, DescriptionSpec ds) {
      super(layout, viewLayout, ds);
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

   public DescriptionSpec getDraggedThing() {
      return thingToDrag;
   }

   @Override
   public DataFlavor getNativeDataFlavor() {
      return DragPanelDataFlavors.descVersionFlavor;
   }

   @Override
   public DescriptionSpec getThingToDrag() {
      return thingToDrag;
   }

   @Override
   public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] { DragPanelDataFlavors.descVersionFlavor };
   }

   @Override
   protected int getTransferMode() {
      return TransferHandler.COPY;
   }

   @Override
   public String getUserString(DescriptionSpec obj) {
      return obj.getDescText();
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
