package org.ihtsdo.arena.conceptview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.ihtsdo.arena.ArenaComponentSettings;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class ConceptViewTitle extends JPanel implements I_AcceptConcept {

    public static Color TITLE_COLOR = new Color(255, 213, 162);
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JLabel label;
    private ArenaComponentSettings settings;

    public ConceptViewTitle(mxGraph graph, mxCell cell,
            ArenaComponentSettings settings) {
        super();
        this.settings = settings;
        setBackground(TITLE_COLOR);
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
        setLayout(new BorderLayout());
        add(settings.getLinkComponent(), BorderLayout.WEST);
        setTransferHandler(new TerminologyTransferHandler(this));

        label = new JLabel(String.valueOf(graph.getLabel(cell)));
        label.setFont(getFont().deriveFont(Font.BOLD, 11));
        label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
        add(label, BorderLayout.CENTER);
        JPanel toolBar2 = new JPanel();
        toolBar2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
        toolBar2.setOpaque(false);

        for (AbstractButton ab : settings.getButtons()) {
            toolBar2.add(ab);
        }
        add(toolBar2, BorderLayout.EAST);
    }

    public void updateTitle() {
        label.setText(settings.getTitle());
    }

    public void setTitleEmpty() {
        label.setText("Empty");
    }

    @Override
    public void sendConcept(I_GetConceptData c) {
        settings.getHost().setTermComponent(c);
    }
}
