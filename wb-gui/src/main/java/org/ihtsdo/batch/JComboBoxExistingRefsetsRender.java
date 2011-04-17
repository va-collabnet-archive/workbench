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
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 *
 * @author marc
 */
class JComboBoxExistingRefsetsRender extends JLabel implements ListCellRenderer {

    public JComboBoxExistingRefsetsRender() {
        setOpaque(true);
        setText("(add concept with to list)");
    }

    @Override
    public Component getListCellRendererComponent(JList jlist, Object o, int index, boolean isSelected, boolean cellHasFocus) {
        if (jlist.getModel().getSize() == 0) {
            // EMPTY CONCEPT LIST
            setText("(add concept with refset to list)");
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
        ConceptVersionBI cvbi = (ConceptVersionBI) model.getElementAt(index);
        try {
            setText(cvbi.getFullySpecifiedDescription().getText());
        } catch (IOException ex) {
            setText(cvbi.toUserString() + " -- FSN missing");
        } catch (ContraditionException ex) {
            setText(cvbi.toUserString() + " -- FSN missing");
        }

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