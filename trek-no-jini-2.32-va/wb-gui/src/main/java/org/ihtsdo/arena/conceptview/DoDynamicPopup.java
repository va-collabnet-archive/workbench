/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author kec
 */
class DoDynamicPopup implements ActionListener {
    Collection<Action> actionList;

    public DoDynamicPopup(Collection<Action> actionList) {
        this.actionList = actionList;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        JButton popupButton = (JButton) ae.getSource();
        JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem(" "));
        for (Action a : actionList) {
            popup.add(new JMenuItem(a));
        }
        int x = popupButton.getX();
        int width = popupButton.getWidth();
        int menuWidth = popup.getPreferredSize().width;
        popup.show(popupButton, width - menuWidth, 0);
    }
    
}
