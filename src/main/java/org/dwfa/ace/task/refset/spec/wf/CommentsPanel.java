package org.dwfa.ace.task.refset.spec.wf;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.dwfa.tapi.TerminologyException;

/**
 * A panel that prompts the user to input a comment.
 * 
 * @author Chrissy Hill
 * 
 */
public class CommentsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // components
    private JTextArea commentsTextField;

    public CommentsPanel() throws TerminologyException, IOException {
        super();
        init();
    }

    private void init() throws IOException, TerminologyException {
        layoutComponents();
    }

    private void layoutComponents() throws IOException, TerminologyException {

        this.setLayout(new GridBagLayout());
        int y = 0;

        JLabel commentsLabel = new JLabel("Comments:");

        commentsTextField = new JTextArea();
        commentsTextField.setLineWrap(true);
        commentsTextField.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(commentsTextField);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(450, 50));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y;
        gridBagConstraints.insets = new Insets(20, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(commentsLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y + 1;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(scrollPane, gridBagConstraints);

        // column filler
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = y + 2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        this.add(Box.createGlue(), gridBagConstraints);

    }

    public String getComments() {
        String result = commentsTextField.getText();
        if (result == null || result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }
}
