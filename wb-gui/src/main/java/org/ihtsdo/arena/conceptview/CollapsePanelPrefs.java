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
   private boolean extrasShown;
   private EnumSet<ComponentVersionDragPanel.SubPanelTypes> subpanelsToShow =
           EnumSet.noneOf(ComponentVersionDragPanel.SubPanelTypes.class);


   public CollapsePanelPrefs(CollapsePanelPrefs other) {
      subpanelsToShow = other.subpanelsToShow.clone();
   }

   public CollapsePanelPrefs() {

   }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeBoolean(extrasShown);
        out.writeObject(subpanelsToShow);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            //
           extrasShown = in.readBoolean();
           subpanelsToShow = (EnumSet<SubPanelTypes>) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

   public boolean areAlertsShown() {
      return subpanelsToShow.contains(SubPanelTypes.ALERT);
   }

   public void setAlertsShown(boolean alertsShown) {
      if (alertsShown) {
         subpanelsToShow.add(SubPanelTypes.ALERT);
      } else {
         subpanelsToShow.remove(SubPanelTypes.ALERT);
      }
   }

   public boolean areExtrasShown() {
      return extrasShown;
   }

   public void setExtrasShown(boolean extrasShown) {
      this.extrasShown = extrasShown;
   }

   public boolean areRefexesShown() {
      return subpanelsToShow.contains(SubPanelTypes.REFEX);
   }

   public void setRefexesShown(boolean refexesShown) {
      if (refexesShown) {
         subpanelsToShow.add(SubPanelTypes.REFEX);
      } else {
         subpanelsToShow.remove(SubPanelTypes.REFEX);
      }
   }

   public EnumSet<SubPanelTypes> getSubpanelsToShow() {
      return subpanelsToShow;
   }

   public boolean areTemplatesShown() {
      return subpanelsToShow.contains(SubPanelTypes.TEMPLATE);
   }

   public void setTemplatesShown(boolean templatesShown) {
      if (templatesShown) {
         subpanelsToShow.add(SubPanelTypes.TEMPLATE);
      } else {
         subpanelsToShow.remove(SubPanelTypes.TEMPLATE);
      }
   }

}
