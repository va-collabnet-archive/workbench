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
/*
 * Created on Mar 24, 2005
 */
package org.dwfa.bpa.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dwfa.bpa.process.I_Workspace;

/**
 * @author kec
 * 
 */
public class InstructionPanel extends GridBagPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 6938679005068294506L;

    private JEditorPane editorPane = new JEditorPane();

    private JButton skip = new JButton("skip");

    private JButton complete = new JButton("complete");

    /**
     * @param title
     */
    public InstructionPanel(String title, I_Workspace workspace) {
        super(title, workspace);
        redoLayout(true);

    }

    /**
     *  
     */
    private void redoLayout(boolean showButtons) {
        this.removeAll();
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        this.editorPane.setEditable(false);
        this.editorPane.setContentType("text/html");
        this.add(new JScrollPane(this.editorPane), c);
        if (showButtons) {
            c.gridwidth = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridy++;
            c.weighty = 0;
            this.add(new JPanel(), c);
            c.gridx++;
            c.weightx = 0;
            this.skip.setEnabled(false);
            this.add(this.skip, c);
            c.gridx++;
            this.complete.setEnabled(false);
            this.add(this.complete, c);
            c.gridx++;
            this.add(new JLabel("   "), c);
        }
    }

    public void setInstruction(String html) {
        this.editorPane.setText(html);
    }

    public String getInstruction() {
        return this.editorPane.getText();
    }

    public void setInstruction(URL url) throws IOException {
        this.editorPane.setPage(url);
    }

    public void addCompleteActionListener(ActionListener l) {
        this.complete.addActionListener(l);
    }

    public void removeCompleteActionListener(ActionListener l) {
        this.complete.removeActionListener(l);
    }

    public void addSkipActionListener(ActionListener l) {
        this.skip.addActionListener(l);
    }

    public void removeSkipActionListener(ActionListener l) {
        this.skip.removeActionListener(l);
    }

    public void setCompleteEnabled(boolean enabled) {
        this.complete.setEnabled(enabled);
    }

    public void setSkipEnabled(boolean enabled) {
        this.skip.setEnabled(enabled);
    }

    public void setCompleteLabel(String completeStr) {
        this.complete.setText(completeStr);

    }

    public void showButtons(boolean showButtons) {
        this.redoLayout(showButtons);
    }
}
