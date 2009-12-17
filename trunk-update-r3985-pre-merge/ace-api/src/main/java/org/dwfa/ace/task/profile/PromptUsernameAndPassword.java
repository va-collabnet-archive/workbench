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
package org.dwfa.ace.task.profile;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.gui.SpringUtilities;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile", type = BeanType.TASK_BEAN) })
public class PromptUsernameAndPassword extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String promptMessage = "Please enter the username/password:";

    private String usernamePropName = ProcessAttachmentKeys.USERNAME.getAttachmentKey();

    private String passwordPropName = ProcessAttachmentKeys.PASSWORD.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(promptMessage);
        out.writeObject(usernamePropName);
        out.writeObject(passwordPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            promptMessage = (String) in.readObject();
            usernamePropName = (String) in.readObject();
            passwordPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            String username = (String) process.readProperty(usernamePropName);
            String password = (String) process.readProperty(passwordPropName);

            Prompter p = new Prompter();
            p.prompt(promptMessage, username, password);
            process.setProperty(usernamePropName, p.getUsername());
            process.setProperty(passwordPropName, p.getPassword());
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getPasswordPropName() {
        return passwordPropName;
    }

    public void setPasswordPropName(String passwordPropName) {
        this.passwordPropName = passwordPropName;
    }

    public String getUsernamePropName() {
        return usernamePropName;
    }

    public void setUsernamePropName(String usernamePropName) {
        this.usernamePropName = usernamePropName;
    }

    private static class Prompter {
        private String username;

        private String password;

        public void prompt(String prompt, String username, String password) throws TaskFailedException {
            JFrame parentFrame = new JFrame();
            JPanel promptPane = new JPanel(new SpringLayout());
            parentFrame.getContentPane().add(promptPane);
            promptPane.add(new JLabel("username:", JLabel.RIGHT));
            final JTextField userTextField = new JTextField(15);
            userTextField.setText(username);
            promptPane.add(userTextField);
            promptPane.add(new JLabel("password:", JLabel.RIGHT));
            JPasswordField pwd = new JPasswordField(15);
            pwd.setText(password);
            promptPane.add(pwd);
            SpringUtilities.makeCompactGrid(promptPane, 2, 2, 6, 6, 6, 6);
            userTextField.requestFocusInWindow();
            userTextField.setSelectionStart(0);
            userTextField.setSelectionEnd(Integer.MAX_VALUE);
            userTextField.addAncestorListener(new AncestorListener() {

                public void ancestorAdded(AncestorEvent arg0) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            AceLog.getAppLog().info("userTextField requesting focus in window");
                            userTextField.requestFocusInWindow();
                        }
                    });
                }

                public void ancestorMoved(AncestorEvent arg0) {
                }

                public void ancestorRemoved(AncestorEvent arg0) {
                }
            });
            promptPane.addAncestorListener(new AncestorListener() {

                public void ancestorAdded(AncestorEvent arg0) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            AceLog.getAppLog().info("userTextField requesting focus in window");
                            userTextField.requestFocusInWindow();
                        }
                    });
                }

                public void ancestorMoved(AncestorEvent arg0) {
                }

                public void ancestorRemoved(AncestorEvent arg0) {
                }
            });
            int action = JOptionPane.showOptionDialog(parentFrame, promptPane, prompt, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, userTextField);
            if (action == JOptionPane.CANCEL_OPTION) {
                throw new TaskFailedException("User canceled operation.");
            }
            this.username = userTextField.getText();
            this.password = new String(pwd.getPassword());
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static void main(String[] args) {
        Prompter p = new Prompter();
        try {
            p.prompt("enter username/password", "test", "test");
            System.out.println("username: " + p.getUsername());
            System.out.println("password: " + p.getPassword());
        } catch (TaskFailedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public String getPromptMessage() {
        return promptMessage;
    }

    public void setPromptMessage(String promptMessage) {
        this.promptMessage = promptMessage;
    }

}
