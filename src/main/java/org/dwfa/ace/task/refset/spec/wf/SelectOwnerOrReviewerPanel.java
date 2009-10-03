package org.dwfa.ace.task.refset.spec.wf;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;

/**
 * Select the next person the BP will go to. If there are no selected reviewers,
 * than this will automatically be the owner of the BP. Otherwise, the user is
 * prompted to select either the owner or one of the reviewers.
 * 
 * @author Chrissy Hill
 * 
 */
public class SelectOwnerOrReviewerPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private I_TermFactory termFactory;
    private Map<String, I_GetConceptData> userMap = new HashMap<String, I_GetConceptData>();
    private UUID[] reviewerUuids;
    private I_GetConceptData owner;
    private TermEntry selectedUser;

    // components
    private ButtonGroup options;
    private JTextField commentsTextField;

    public SelectOwnerOrReviewerPanel(UUID[] reviewerUuids, I_GetConceptData owner) throws TerminologyException,
            IOException {
        super();
        this.reviewerUuids = reviewerUuids;
        this.owner = owner;
        termFactory = LocalVersionedTerminology.get();
        init();
    }

    private void init() throws IOException, TerminologyException {
        layoutComponents();
    }

    private void layoutComponents() throws IOException, TerminologyException {

        this.setLayout(new GridBagLayout());
        int y = 0;
        HashSet<UUID> uniqueReviewerUuids = new HashSet<UUID>();
        for (UUID reviewerUuid : reviewerUuids) {
            uniqueReviewerUuids.add(reviewerUuid);
        }
        uniqueReviewerUuids.remove(owner.getUids().iterator().next());
        if (uniqueReviewerUuids.size() == 0) {
            setSelectedUser(new TermEntry(owner.getUids()));
        } else {
            options = new ButtonGroup();
            JRadioButton ownerOption = new JRadioButton("Owner: " + owner.getInitialText());
            ownerOption.setActionCommand(owner.getInitialText());
            ownerOption.addActionListener(this);
            options.add(ownerOption);
            userMap.put(owner.getInitialText(), owner);

            for (UUID reviewerUuid : uniqueReviewerUuids) {
                I_GetConceptData reviewer = termFactory.getConcept(new UUID[] { reviewerUuid });
                JRadioButton option = new JRadioButton("Reviewer: " + reviewer.getInitialText());
                option.setActionCommand(reviewer.getInitialText());
                option.addActionListener(this);
                options.add(option);
                userMap.put(reviewer.getInitialText(), reviewer);

                option.setSelected(true);
                setSelectedUser(new TermEntry(reviewerUuid));
            }

            Enumeration<AbstractButton> buttons = options.getElements();

            JLabel userLabel = new JLabel("Please select workflow recipient:");
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = y;
            gridBagConstraints.insets = new Insets(20, 5, 10, 10); // padding
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
            this.add(userLabel, gridBagConstraints);
            y++;

            while (buttons.hasMoreElements()) {
                AbstractButton button = buttons.nextElement();

                // add button to panel
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = y;
                gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
                gridBagConstraints.weighty = 0.0;
                gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
                this.add(button, gridBagConstraints);

                y++;
            }
        }
        JLabel commentsLabel;
        if (uniqueReviewerUuids.size() == 0) {
            commentsLabel = new JLabel("Comments for the owner (" + owner.getInitialText() + "):");
        } else {
            commentsLabel = new JLabel("Comments for workflow recipient:");
        }
        commentsTextField = new JTextField(30);

        y++;

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
        this.add(commentsTextField, gridBagConstraints);

        // column filler
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = y + 2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        this.add(Box.createGlue(), gridBagConstraints);

    }

    public void actionPerformed(ActionEvent e) {
        try {
            String actionCommand = e.getActionCommand();
            setSelectedUser(new TermEntry(userMap.get(actionCommand).getUids()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public TermEntry getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(TermEntry selectedUser) {
        this.selectedUser = selectedUser;
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
