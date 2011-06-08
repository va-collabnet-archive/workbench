/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.arena.conceptview;

import java.util.EnumSet;
import org.ihtsdo.arena.conceptview.DragPanelComponentVersion.SubPanelTypes;

/**
 *
 * @author kec
 */
public interface I_ToggleSubPanels {

    void setVisible(boolean visible);
    void showSubPanels(EnumSet<SubPanelTypes> panels);
    void hideSubPanels(EnumSet<SubPanelTypes> panels);
    boolean isExpanded();

}
