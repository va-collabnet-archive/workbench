/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.batch;

import java.awt.Component;
import java.io.IOException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 *
 * @author marc
 */
class JComboBoxExistingRolesRender extends JLabel implements ListCellRenderer {

    public JComboBoxExistingRolesRender() {
        setOpaque(true);
        setText("(add concept to list)");
    }

    @Override
    public Component getListCellRendererComponent(JList jlist, Object o, int index, boolean isSelected, boolean cellHasFocus) {
        if (jlist.getModel().getSize() == 0) {
            // EMPTY CONCEPT LIST
            setText("(add relationship to list)");
            return this;
        }

        // CONCEPT LIST NOT EMPTY
        if (index == -1) {
            // If JComboBox selection value request is -1
            // then find out the current selection index from the list.
            int selected = jlist.getSelectedIndex();
            if (selected == -1) {
                return this;
            } else {
                index = selected;
            }
        }
        DefaultComboBoxModel model = (DefaultComboBoxModel) jlist.getModel();
        RelationshipVersionBI rvbi = (RelationshipVersionBI) model.getElementAt(index);
        int roleTypeNid = rvbi.getTypeNid();
        int roleValueNid = rvbi.getDestinationNid();

        String roleTypeStr;
        try {
            roleTypeStr = Ts.get().getComponent(roleTypeNid).toUserString();
        } catch (IOException ex) {
            roleTypeStr = "role_type_error";
        }
        String roleValueStr;
        try {
            roleValueStr = Ts.get().getComponent(roleValueNid).toUserString();
        } catch (IOException ex) {
            roleValueStr = "role_value_error";
        }

        setText(roleTypeStr + " :: " + roleValueStr);

        if (isSelected) {
            setBackground(jlist.getSelectionBackground());
            setForeground(jlist.getSelectionForeground());
            // setBackground(Color.blue);
            // setForeground(Color.white);
        } else {
            setBackground(jlist.getBackground());
            setForeground(jlist.getForeground());
            // setBackground(Color.white);
            // setForeground(Color.black);
        }
        return this;
    }
}