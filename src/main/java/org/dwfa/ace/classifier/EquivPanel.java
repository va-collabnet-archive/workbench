package org.dwfa.ace.classifier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.task.classify.SnoCon;
import org.dwfa.ace.task.classify.SnoConGrp;
import org.dwfa.ace.task.classify.SnoConGrpList;
import org.dwfa.ace.task.classify.SnoQuery;
import org.dwfa.tapi.TerminologyException;

public class EquivPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    // ** WORKBENCH PARTICULARS **
    private I_TermFactory tf;
    I_ConfigAceFrame config;
    String valueFont = "<font face='Dialog' size='3' color='green'>";

    public EquivPanel(I_ConfigAceFrame caf) {
        super();
        tf = LocalVersionedTerminology.get();
        config = caf;
        this.setLayout(new GridBagLayout());
    }

    public void update() {
        this.removeAll();

        if (SnoQuery.getEquiv() == null)
            return;
        if (SnoQuery.getEquiv().size() == 0) {
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.weightx = 0.5;
            c.weighty = 0.5;
            this.add(new JLabel(
                    "<HTML><FONT COLOR='gray'><I> -- No equivalences reported from"
                            + " most recent classification. --"), c);
            return;
        }

        String[][] theTableStr = null;
        try {
            theTableStr = getTableStrings(SnoQuery.getEquiv());
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        EquivTableModel theTableModel = new EquivTableModel(theTableStr,
                SnoQuery.getEquiv());
        JTable table = new JTable(theTableModel);

        EquivTableRenderer renderer = new EquivTableRenderer(config);
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);
        table.setDragEnabled(true);
        table.setTransferHandler(new TerminologyTransferHandler(table));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // set column widths
        TableColumn tc = table.getColumnModel().getColumn(0);
        tc.setPreferredWidth(44);
        tc = table.getColumnModel().getColumn(1);
        tc.setPreferredWidth(400 + 368); // for concept column

        int rowHeight = 18;
        int tWide = 700;
        table.setRowHeight(rowHeight);
        table.setPreferredScrollableViewportSize(new Dimension(tWide, rowHeight
                * table.getRowCount()));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        this.add(new JScrollPane(table), c);
    }

    private String[][] getTableStrings(SnoConGrpList scgl)
            throws TerminologyException, IOException {
        int totalCol = 2;
        int totalRows = scgl.count();
        String tableStrings[][] = new String[totalRows][totalCol];

        int iRow = 0;
        int iGroup = 0;
        for (SnoConGrp scg : scgl) {
            iGroup += 1;
            for (SnoCon sc : scg) {
                StringBuilder str = new StringBuilder("<html>");
                I_GetConceptData valueBean = tf.getConcept(sc.id);
                str.append(valueFont + valueBean.getInitialText());
                tableStrings[iRow][0] = Integer.toString(iGroup);
                tableStrings[iRow][1] = str.toString();
                iRow += 1;
            }
        }

        return tableStrings;
    }
}
