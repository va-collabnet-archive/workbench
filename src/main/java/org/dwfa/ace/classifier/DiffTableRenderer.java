package org.dwfa.ace.classifier;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.table.AceTableRenderer;

public class DiffTableRenderer extends AceTableRenderer {
    private static final long serialVersionUID = 1L;
    
    boolean renderInactive = false;
    @SuppressWarnings("unused")
    private I_ConfigAceFrame frameConfig;

    public DiffTableRenderer(I_ConfigAceFrame frameConfig) {
        super();
        setVerticalAlignment(TOP);
        this.frameConfig = frameConfig;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel renderComponent = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        if (isSelected == false) {
            if (renderInactive) {
                renderComponent.setBackground(UIManager
                        .getColor("Table.background"));
                renderComponent.setForeground(UIManager
                        .getColor("Table.foreground"));
            } else {
                renderComponent.setBackground(colorForRow(row));
                renderComponent.setForeground(UIManager
                        .getColor("Table.foreground"));
            }
        } else {
            renderComponent.setBackground(UIManager
                    .getColor("Table.selectionBackground"));
            renderComponent.setForeground(UIManager
                    .getColor("Table.selectionForeground"));
        }

        setHorizontalAlignment(SwingConstants.LEFT);

        setBorder(column, this, false, false); // .., same, uncommitted
        // if (renderInactive) renderComponent.setBackground(Color.LIGHT_GRAY);

        renderComponent.setText((String) value);
        return renderComponent;
    }

}
