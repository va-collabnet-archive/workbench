/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.refset.refresh;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.commit.TestForEditRefsetPermission;
import org.dwfa.ace.task.util.DatePicker;
import org.dwfa.bpa.data.ArrayListModel;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * The request for change panel that allows user to input:
 * 1) refset name (from pulldown menu)
 * 2) editor (from pulldown menu)
 * 3) comments (text field)
 * 4) deadline (date picker)
 * 5) priority (from pulldown menu)
 * 6) request attachments (file chooser)
 * 
 * @author Perry Reid
 * @version 1.0, November 2009
 * 
 */
public class PanelRefreshSummary extends JPanel {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1L;

    // components
    private JTextPane htmlPane;
    private String messageText;

    /**
     * 
     * @param refsets
     */
    public PanelRefreshSummary() {
        super(new GridBagLayout());

        /*
         * -------------------------------------------------
         * Set Default / initial values for all the fields
         * -------------------------------------------------
         */
        String defaultMessage = "<html>" + "<STYLE type=\"text/css\"> " + "	body { " + "		margin-top: 2px; "
            + "		margin-right: 12px; " + "		margin-bottom: 2px; " + "		margin-left: 12px; " + "	} " + "</STYLE> "
            + "<body bgcolor='rgb(255, 255, 220)'> " + "<h2>Refresh Refset Spec Summary</h2>"
            + "<p>No summary information available.</p>" + "</body></html>";
        setMessageText(defaultMessage);

        /*
         * -------------------------------------------------
         * Layout the components
         * -------------------------------------------------
         */
        layoutComponents();
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        // Setup and add the HTML Panel
        htmlPane = new JTextPane();
        htmlPane.setContentType("text/html");
        htmlPane.setText(this.messageText);
        htmlPane.setEditable(false);
        JScrollPane editorScrollPane = new JScrollPane(htmlPane);
        editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        editorScrollPane.setPreferredSize(new Dimension(450, 580));
        editorScrollPane.setMinimumSize(new Dimension(10, 10));
        editorScrollPane.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5); // padding
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(editorScrollPane, gridBagConstraints);

        // Tell the panel to o lay out its subcomponents again. It should be
        // invoked
        // when this container's subcomponents are modified after the container
        // has been displayed.
        this.validate();

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
        layoutComponents();
    }

}
