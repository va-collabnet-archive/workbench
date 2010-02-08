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
package org.dwfa.svn;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.log.HtmlHandler;

public class SvnPanel extends JPanel {

    private JCheckBox svnConnectCheckBox;

    private class PreferredReadOnlyListener implements ActionListener {
        SubversionData svd;

        public PreferredReadOnlyListener(SubversionData svd) {
            this.svd = svd;
        }

        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox) e.getSource();
            String preferredReadRepository = (String) cb.getSelectedItem();
            svd.setPreferredReadRepository(preferredReadRepository);
        }
    }

    private class PurgeListener implements ActionListener {

        SubversionData svd;

        public PurgeListener(SubversionData svd) {
            super();
            this.svd = svd;
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                Svn.purge(svd, authenticator, true);
            } catch (TaskFailedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

    }

    private class CheckoutListener implements ActionListener {

        SubversionData svd;

        public CheckoutListener(SubversionData svd) {
            super();
            this.svd = svd;
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                Svn.checkout(svd, authenticator, true);
            } catch (TaskFailedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

    }

    private class UpdateListener implements ActionListener {

        SubversionData svd;

        public UpdateListener(SubversionData svd) {
            super();
            this.svd = svd;
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                Svn.update(svd, authenticator, true);
            } catch (TaskFailedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }

    private class UpdateDatabaseListener implements ActionListener {

        SubversionData svd;

        public UpdateDatabaseListener(SubversionData svd) {
            super();
            this.svd = svd;
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                Svn.updateDatabase(svd, authenticator, true);
            } catch (TaskFailedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }

    private class CommitListener implements ActionListener {

        SubversionData svd;

        public CommitListener(SubversionData svd) {
            super();
            this.svd = svd;
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                Svn.commit(svd, authenticator, true);
            } catch (TaskFailedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }

    private class CleanupListener implements ActionListener {

        SubversionData svd;

        public CleanupListener(SubversionData svd) {
            super();
            this.svd = svd;
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                Svn.cleanup(svd, authenticator, true);
            } catch (TaskFailedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

    }

    private class StatusListener implements ActionListener {

        SubversionData svd;

        public StatusListener(SubversionData svd) {
            super();
            this.svd = svd;
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                Svn.status(svd, authenticator, true);
            } catch (TaskFailedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

    }

    private class ClearLogListener implements ActionListener {

        HtmlHandler logHandler;

        public ClearLogListener(HtmlHandler logHandler) {
            super();
            this.logHandler = logHandler;
        }

        public void actionPerformed(ActionEvent arg0) {
            logHandler.clearLog();
        }

    }

    private class LogLevelListener implements ActionListener {

        HtmlHandler logHandler;
        JComboBox logLevel;

        public LogLevelListener(HtmlHandler logHandler, JComboBox logLevel) {
            super();
            this.logHandler = logHandler;
            this.logLevel = logLevel;
        }

        public void actionPerformed(ActionEvent arg0) {
            logHandler.setLevel((Level) logLevel.getSelectedItem());
            SvnLog.setLevel(logHandler.getLevel());
            SvnLog.info("Level set to: " + logHandler.getLevel());
        }
    }

    boolean database = false;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private SvnPrompter authenticator;

    public SvnPanel(I_ConfigAceFrame aceFrameConfig, String tabName) throws Exception {
        super(new GridBagLayout());
        database = tabName.equalsIgnoreCase("database");
        authenticator = new SvnPrompter();
        authenticator.setParentContainer(this);
        authenticator.setUsername(aceFrameConfig.getUsername());
        authenticator.setPassword(aceFrameConfig.getPassword());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        this.add(new JLabel("Repository: "), c);
        c.gridy++;
        this.add(new JLabel("Working copy: "), c);
        c.gridy++;
        this.add(new JLabel("Readonly Mirror: "), c);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.gridwidth = 7;
        c.anchor = GridBagConstraints.WEST;
        SubversionData svd = aceFrameConfig.getSubversionMap().get(tabName);
        JTextField repository = new JTextField(svd.getRepositoryUrlStr());
        repository.setEditable(false);
        this.add(repository, c);
        c.gridy++;
        JTextField workingCopy = new JTextField(svd.getWorkingCopyStr());
        workingCopy.setEditable(false);
        this.add(workingCopy, c);
        c.gridy++;
        JComboBox preferredReadOnly = new JComboBox(svd.getReadOnlyUrlMirrors().toArray());
        preferredReadOnly.setSelectedItem(svd.getPreferredReadRepository());
        preferredReadOnly.addActionListener(new PreferredReadOnlyListener(svd));
        this.add(preferredReadOnly, c);
        c.gridwidth = 1;
        c.gridy++;
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        this.add(new JLabel(), c);
        c.gridx++;
        JButton status = new JButton("status");
        status.addActionListener(new StatusListener(svd));
        this.add(status, c);
        c.gridx++;
        if (database) {
            JButton update = new JButton("update database");
            update.addActionListener(new UpdateDatabaseListener(svd));
            this.add(update, c);
            c.gridx++;
            JButton cleanup = new JButton("cleanup");
            cleanup.addActionListener(new CleanupListener(svd));
            this.add(cleanup, c);
            c.gridx++;
            JButton checkout = new JButton("checkout");
            checkout.addActionListener(new CheckoutListener(svd));
            this.add(checkout, c);
            c.gridx++;
            JButton purge = new JButton("purge");
            purge.addActionListener(new PurgeListener(svd));
            this.add(purge, c);
        } else {
            JButton commit = new JButton("commit");
            commit.addActionListener(new CommitListener(svd));
            this.add(commit, c);
            c.gridx++;
            JButton update = new JButton("update");
            update.addActionListener(new UpdateListener(svd));
            this.add(update, c);
            c.gridx++;
            JButton cleanup = new JButton("cleanup");
            cleanup.addActionListener(new CleanupListener(svd));
            this.add(cleanup, c);
            c.gridx++;
            JButton checkout = new JButton("checkout");
            checkout.addActionListener(new CheckoutListener(svd));
            this.add(checkout, c);
            c.gridx++;
            JButton purge = new JButton("purge");
            purge.addActionListener(new PurgeListener(svd));
            this.add(purge, c);
        }
        c.gridx = 0;
        c.gridy++;
        this.add(new JLabel("log level:"), c);
        c.gridx++;
        Level[] levels = { Level.CONFIG, Level.INFO, Level.FINE, Level.FINER, Level.FINEST, Level.ALL };
        JComboBox logLevel = new JComboBox(levels);
        logLevel.setSelectedItem(Level.INFO);
        this.add(logLevel, c);
        c.gridx++;
        JButton clear = new JButton("clear log");
        this.add(clear, c);
        c.gridy = 5;
        c.gridx = 0;
        c.gridwidth = 9;
        c.gridheight = 10;
        c.weighty = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.BOTH;

        // check box for whether or not to SVN
        svnConnectCheckBox = new JCheckBox();
        svnConnectCheckBox.setSelected(true);
        svnConnectCheckBox.setText("Connect to subversion");
        svnConnectCheckBox.addActionListener(new SvnConnectCheckBoxListener());
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy++;
        this.add(svnConnectCheckBox, c);

        JEditorPane logOut = new JEditorPane("text/html", "<html>");
        HtmlHandler logHandler = new HtmlHandler(logOut, "svn");
        logHandler.setLevel(Level.INFO);
        SvnLog.addHandler(logHandler);
        clear.addActionListener(new ClearLogListener(logHandler));
        logLevel.addActionListener(new LogLevelListener(logHandler, logLevel));

        this.add(new JScrollPane(logOut), c);

    }

    /**
     * True if the user is connecting to SVN
     * 
     * @return boolean
     */
    public boolean isConnectedToSvn() {
        return svnConnectCheckBox.getModel().isSelected();
    }

    public class SvnConnectCheckBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent arg0) {
            Svn.setConnectedToSvn(isConnectedToSvn());
        }

    }
}
