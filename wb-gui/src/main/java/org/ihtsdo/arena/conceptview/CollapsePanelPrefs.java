/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EnumSet;
import org.ihtsdo.arena.conceptview.ComponentVersionDragPanel.SubPanelTypes;

/**
 *
 * @author kec
 */
public class CollapsePanelPrefs implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private static final int dataVersion = 1;
   private boolean extrasShown = false;
   private EnumSet<ComponentVersionDragPanel.SubPanelTypes> subpanelsToShow =
           EnumSet.noneOf(ComponentVersionDragPanel.SubPanelTypes.class);
   private boolean collapsed = false;

   public CollapsePanelPrefs(CollapsePanelPrefs other) {
      subpanelsToShow = other.subpanelsToShow.clone();
   }

   public CollapsePanelPrefs() {
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeBoolean(collapsed);
      out.writeBoolean(extrasShown);
      out.writeObject(subpanelsToShow);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         //
         collapsed = in.readBoolean();
         extrasShown = in.readBoolean();
         subpanelsToShow = (EnumSet<SubPanelTypes>) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }

   public void setShown(boolean shown, SubPanelTypes type) {
      if (shown) {
         subpanelsToShow.add(type);
      } else {
         subpanelsToShow.remove(type);
      }
   }

   public boolean isShown(SubPanelTypes type) {
      return subpanelsToShow.contains(type);
   }

   public boolean getExtrasShown() {
      return extrasShown;
   }

   public void setExtrasShown(boolean extrasShown) {
      this.extrasShown = extrasShown;
   }

   public EnumSet<SubPanelTypes> getSubpanelsToShow() {
      return subpanelsToShow;
   }

   public boolean isCollapsed() {
      return collapsed;
   }

   public void setCollapsed(boolean collapsed) {
      this.collapsed = collapsed;
   }
}
