package org.dwfa.bpa.gui.render;


import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


/**
 * @author kec
 *  
 */
public class LookAndFeelCellRenderer extends JLabel implements ListCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 7656608290564774212L;
    protected static Border noFocusBorder;
    /**
     * 
     */
    public LookAndFeelCellRenderer() {
        super();
        if (noFocusBorder == null) {
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }
        this.setOpaque(true);
        setBorder(noFocusBorder);
    }
     /**
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (UIManager.LookAndFeelInfo.class.isAssignableFrom(value.getClass())) {
            UIManager.LookAndFeelInfo lafInfo = (LookAndFeelInfo) value;
            this.setText(lafInfo.getName());
        }
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setBorder((cellHasFocus) ? UIManager
                .getBorder("List.focusCellHighlightBorder") : noFocusBorder);
        return this;
    }
    
}