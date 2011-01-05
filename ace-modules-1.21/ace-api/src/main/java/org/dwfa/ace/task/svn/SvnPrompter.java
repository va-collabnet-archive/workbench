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
package org.dwfa.ace.task.svn;

import java.awt.Container;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.dwfa.bpa.gui.SpringUtilities;
import org.dwfa.util.LogWithAlerts;
import org.tigris.subversion.javahl.PromptUserPassword3;

public class SvnPrompter implements PromptUserPassword3 {

    private Container parentContainer = null;

    private String username;

    private String password;

    boolean userAllowedSave = false;

    public String askQuestion(String realm, String question, boolean showAnswer, boolean maySave) {
        JPanel promptPane = new JPanel(new SpringLayout());
        promptPane.add(new JLabel(question, JLabel.RIGHT));
        JTextField userTextFieldMaybe = new JTextField(20);
        if (showAnswer == false) {
            userTextFieldMaybe = new JPasswordField(20);
        }
        final JTextField userTextField = userTextFieldMaybe;
        userTextField.setText("");
        promptPane.add(userTextField);
        JCheckBox save = new JCheckBox("");
        if (maySave) {
            promptPane.add(new JLabel("save answer", JLabel.RIGHT));
            promptPane.add(save);
        } else {
            promptPane.add(new JLabel(" "));
            promptPane.add(new JLabel(" "));
        }
        promptPane.add(new JLabel(" "));
        promptPane.add(new JLabel(" "));
        SpringUtilities.makeCompactGrid(promptPane, 3, 2, 6, 6, 6, 6);
        userTextField.setSelectionStart(0);
        userTextField.setSelectionEnd(Integer.MAX_VALUE);
        userTextField.addAncestorListener(new AncestorListener() {

            public void ancestorAdded(AncestorEvent arg0) {
                System.out.println("requesting focus 0");
                userTextField.requestFocusInWindow();
            }

            public void ancestorMoved(AncestorEvent arg0) {
            }

            public void ancestorRemoved(AncestorEvent arg0) {
            }
        });
        int action = JOptionPane.showOptionDialog(LogWithAlerts.getActiveFrame(parentContainer), promptPane, realm,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, userTextField);
        LogWithAlerts.getActiveFrame(parentContainer).requestFocus();
        if (action == JOptionPane.CANCEL_OPTION) {
            userAllowedSave = false;
            return null;
        }
        userAllowedSave = save.isSelected();
        return userTextField.getText();
    }

    public boolean prompt(String realm, String username, boolean maySave) {
        JPanel promptPane = new JPanel(new SpringLayout());
        promptPane.add(new JLabel("username:", JLabel.RIGHT));
        final JTextField userTextField = new JTextField(15);
        userTextField.setText(username);
        promptPane.add(userTextField);
        promptPane.add(new JLabel("password:", JLabel.RIGHT));
        JPasswordField pwd = new JPasswordField(15);
        promptPane.add(pwd);
        JCheckBox save = new JCheckBox("");
        if (maySave) {
            promptPane.add(new JLabel("save password", JLabel.RIGHT));
            promptPane.add(save);
        } else {
            promptPane.add(new JLabel(""));
            promptPane.add(new JLabel(""));
        }
        SpringUtilities.makeCompactGrid(promptPane, 3, 2, 6, 6, 6, 6);
        userTextField.requestFocusInWindow();
        userTextField.setSelectionStart(0);
        userTextField.setSelectionEnd(Integer.MAX_VALUE);
        userTextField.addAncestorListener(new AncestorListener() {

            public void ancestorAdded(AncestorEvent arg0) {
                userTextField.requestFocusInWindow();
            }

            public void ancestorMoved(AncestorEvent arg0) {
            }

            public void ancestorRemoved(AncestorEvent arg0) {
            }
        });
        int action = JOptionPane.showOptionDialog(LogWithAlerts.getActiveFrame(parentContainer), promptPane, realm,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, userTextField);
        if (parentContainer != null) {
            LogWithAlerts.getActiveFrame(parentContainer).requestFocus();
        }
        if (action == JOptionPane.CANCEL_OPTION) {
            userAllowedSave = false;
            return false;
        } else {
            userAllowedSave = save.isSelected();
            this.username = userTextField.getText();
            password = new String(pwd.getPassword());
        }
        return true;
    }

    public boolean userAllowedSave() {
        return userAllowedSave;
    }

    public int askTrustSSLServer(String info, boolean allowPermanently) {
        Object[] options = { "Reject", "Accept Temporary" };
        int optionType = JOptionPane.YES_NO_OPTION;
        if (allowPermanently) {
            options = new Object[] { "Reject", "Accept Temporary", "Accept Permanently" };
            optionType = JOptionPane.YES_NO_CANCEL_OPTION;
        }
        int returnValue = JOptionPane.showOptionDialog(LogWithAlerts.getActiveFrame(parentContainer), info,
            "Trust SSL Server", optionType, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        LogWithAlerts.getActiveFrame(parentContainer).requestFocus();
        return returnValue;
    }

    public String askQuestion(String realm, String question, boolean showAnswer) {
        return askQuestion(realm, question, "", showAnswer);
    }

    public String askQuestion(String realm, String question, String defaultAnswer, boolean showAnswer) {
        if (showAnswer == false) {
            return askQuestion(realm, question, showAnswer, false);
        }
        return (String) JOptionPane.showInputDialog(LogWithAlerts.getActiveFrame(parentContainer), question, realm,
            JOptionPane.PLAIN_MESSAGE, null, null, defaultAnswer);
    }

    public boolean askYesNo(String realm, String question, boolean yesIsDefault) {
        int initialValue = JOptionPane.NO_OPTION;
        if (yesIsDefault) {
            initialValue = JOptionPane.YES_OPTION;
        }
        int n = JOptionPane.showOptionDialog(LogWithAlerts.getActiveFrame(parentContainer), question, realm,
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, initialValue);
        LogWithAlerts.getActiveFrame(parentContainer).requestFocus();
        return n == JOptionPane.YES_OPTION;
    }

    /**
     * retrieve the password entered during the prompt call
     */
    public String getPassword() {
        return password;
    }

    /**
     * retrieve the username entered during the prompt call
     */
    public String getUsername() {
        return username;
    }

    /**
     * Ask the user for username and password The entered username/password is
     * retrieved by the getUsername getPassword methods.
     */
    public boolean prompt(String realm, String username) {
        return prompt(realm, username, false);
    }

    public static void main(String[] args) {
        SvnPrompter p = new SvnPrompter();
        System.out.println("boolean: "
            + p.askYesNo("http://aceworkspace.net", "Do you trust this (default true)", true));
        System.out.println("boolean: "
            + p.askYesNo("http://aceworkspace.net", "Do you trust this (default false)", false));
        System.out.println("String: "
            + p.askQuestion("http://aceworkspace.net", "Do you trust this? enter yes or no", true));
        System.out.println("boolean: " + p.askTrustSSLServer("Do you trust this SSL?", false));
        System.out.println("boolean: " + p.askTrustSSLServer("Do you trust this SSL?", true));
        System.out.println("boolean: " + p.prompt("HTTP:??", "KEC", true));
        System.out.println("boolean: " + p.prompt("HTTP:??", "KEC", false));
        System.out.println("String: " + p.askQuestion("HTTP:??", "This is the question", true, true));
        System.out.println("String: " + p.askQuestion("HTTP:??", "This is the question", true, false));
        System.out.println("String: " + p.askQuestion("HTTP:??", "This is the question (hide answer)", false, true));
        System.out.println("String: " + p.askQuestion("HTTP:??", "This is the question (hide answer)", false, false));
    }

    public Container getParentContainer() {
        return parentContainer;
    }

    public void setParentContainer(Container frame) {
        this.parentContainer = frame;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
