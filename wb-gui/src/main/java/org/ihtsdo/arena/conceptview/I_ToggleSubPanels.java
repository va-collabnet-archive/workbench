
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.arena.conceptview.DragPanelComponentVersion.SubPanelTypes;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.EnumSet;

/**
 *
 * @author kec
 */
public interface I_ToggleSubPanels {
   void hideSubPanels(EnumSet<SubPanelTypes> panels);

   void showConflicts(Collection<Integer> saptCol);

   void showSubPanels(EnumSet<SubPanelTypes> panels);

   //~--- get methods ---------------------------------------------------------

   boolean isExpanded();

   //~--- set methods ---------------------------------------------------------

   void setVisible(boolean visible);
}
