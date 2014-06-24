/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dwfa.ace.list;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.log.AceLog;

/**
 *
 * @author akf
 */
public class PromotionTerminologyTableRenderer extends DefaultTableCellRenderer {

    private I_ConfigAceFrame config;

    public PromotionTerminologyTableRenderer(I_ConfigAceFrame config) {
        super();
        this.config = config;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, final Object value,
            boolean isSelected, boolean hasFocus, final int row, int column) {
        PromotionTerminologyTableModel model = (PromotionTerminologyTableModel) table.getModel();
        final TerminologyList list = model.getList();
        JLabel renderComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                table.convertRowIndexToView(row), column);

        try {
            I_GetConceptData testConcept = list.dataModel.getElementAt(table.convertRowIndexToModel(row));

            boolean uncommitted = testConcept.isUncommitted();
            String text;
            if (I_GetConceptData.class.isAssignableFrom(value.getClass())) {
                I_GetConceptData concept = (I_GetConceptData) value;
                I_DescriptionTuple desc = concept.getDescTuple(config.getShortLabelDescPreferenceList(), config);
                if (desc == null) {
                    text = concept.getInitialText();
                } else {
                    text = desc.getText();
                }

            } else {
                text = value.toString();
                renderComponent.setText(value.toString());
            }

            if (text != null) {
                if (uncommitted) {

                    renderComponent.setText(text);
                    renderComponent.setOpaque(true);
                    if (!isSelected) {
                        renderComponent.setBackground(Color.YELLOW);
                    } else {
                        Border b = renderComponent.getBorder();
                        if (b == null) {
                            b = BorderFactory.createEmptyBorder();
                        }
                        renderComponent.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 6, 0, 6, Color.YELLOW), b));
                    }
                } else {
                    renderComponent.setText(text);
                    renderComponent.setOpaque(true);
                    if (!isSelected) {
                        renderComponent.setBackground(Color.WHITE);
                    }
                }
            } else if (I_GetConceptData.class.isAssignableFrom(value.getClass())) {
                I_GetConceptData concept = (I_GetConceptData) value;
                AceLog.getAppLog().info("element: " + row + " descTuple is null: " + concept.getConceptNid());
                renderComponent.setText("removing concept with null descTuple: " + concept.getConceptNid());
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        I_GetConceptData concept = (I_GetConceptData) value;
                        I_ModelTerminologyList listModel = (I_ModelTerminologyList) list.getModel();
                        if (row < list.getModel().getSize()) {
                            I_GetConceptData another = listModel.getElementAt(row);
                            if (another != null && another.getConceptNid() == concept.getConceptNid()) {
                                listModel.removeElement(row);
                                AceLog.getAppLog().info(
                                        "element " + another + " with index " + row + " removed.");
                            }
                        } else {
                            AceLog.getAppLog().info(
                                    "element " + value + " with index " + row
                                    + " >= list model size: " + list.getSize());
                        }
                    }
                });
            }
        } catch (IOException e) {
            this.setText(e.getMessage());
            AceLog.getAppLog().alertAndLogException(e);
        }

        return renderComponent;
    }
}
