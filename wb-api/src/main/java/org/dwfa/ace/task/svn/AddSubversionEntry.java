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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.SubversionData;
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

@BeanList(specs = { @Spec(directory = "tasks/ide/svn", type = BeanType.TASK_BEAN) })
public class AddSubversionEntry extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private String prompt = "enter data for new subversion repository: ";
    private String keyName = "repoKey";
    private String repoUrl = "https://amt-edit-bundle.au-ct.org/svn/amt-edit-bundle/trunk/dev/src/main/profiles/users";
    private String workingCopy = "profiles/users";
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(prompt);
        out.writeObject(keyName);
        out.writeObject(repoUrl);
        out.writeObject(workingCopy);
        out.writeObject(profilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            prompt = (String) in.readObject();
            keyName = (String) in.readObject();
            repoUrl = (String) in.readObject();
            workingCopy = (String) in.readObject();
            if (objDataVersion >= 2) {
                profilePropName = (String) in.readObject();
            } else {
                profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) process.readAttachement(ProcessAttachmentKeys.WORKING_PROFILE.name());
            addUserInfo(process, config);
            SubversionData svd = config.getSubversionMap().get(keyName);
            if (svd == null) {
                svd = new SubversionData(repoUrl, workingCopy);
            }
            if (svd.getRepositoryUrlStr() == null) {
            	config.svnCompleteRepoInfo(svd);
            }
            Prompter p = new Prompter();
            p.prompt(prompt, keyName, svd.getRepositoryUrlStr(), svd.getWorkingCopyStr(), svd.getUsername(),
                svd.getPassword());
            svd.setWorkingCopyStr(p.getWorkingCopyStr());
            svd.setRepositoryUrlStr(p.getRepositoryUrlStr());
            svd.setPassword(p.getPassword());
            svd.setUsername(p.getUsername());
            keyName = p.getRepoKey();

            config.getSubversionMap().put(keyName, svd);
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        }
    }

    protected void addUserInfo(I_EncodeBusinessProcess process, I_ConfigAceFrame config) throws TaskFailedException {
        // subclass may override to include user info, for this task, we make no
        // modifications...

    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    private static class Prompter {
        private String repoKey;

        private String username;

        private String password;

        private String repositoryUrlStr;

        private String workingCopyStr;

        public void prompt(String prompt, String repoKey, String repositoryUrlStr, String workingCopyStr,
                String username, String password) throws TaskFailedException {
            JFrame parentFrame = new JFrame();
            JPanel promptPane = new JPanel(new SpringLayout());
            parentFrame.getContentPane().add(promptPane);

            promptPane.add(new JLabel("key:", JLabel.RIGHT));
            JTextField keyField = new JTextField(30);
            keyField.setText(repoKey);
            promptPane.add(keyField);

            promptPane.add(new JLabel("repository url:", JLabel.RIGHT));
            JTextField repoUrlField = new JTextField(30);
            repoUrlField.setText(repositoryUrlStr);
            promptPane.add(repoUrlField);

            promptPane.add(new JLabel("working copy:", JLabel.RIGHT));
            JTextField workingCopyField = new JTextField(30);
            workingCopyField.setText(workingCopyStr);
            promptPane.add(workingCopyField);

            promptPane.add(new JLabel("username:", JLabel.RIGHT));
            JTextField userTextField = new JTextField(30);
            userTextField.setText(username);
            promptPane.add(userTextField);

            promptPane.add(new JLabel("password:", JLabel.RIGHT));
            JPasswordField pwd = new JPasswordField(30);
            pwd.setText(password);
            promptPane.add(pwd);
            SpringUtilities.makeCompactGrid(promptPane, 5, 2, 6, 6, 6, 6);
            userTextField.requestFocusInWindow();
            userTextField.setSelectionStart(0);
            userTextField.setSelectionEnd(Integer.MAX_VALUE);
            int action = JOptionPane.showConfirmDialog(parentFrame, promptPane, prompt, JOptionPane.OK_CANCEL_OPTION);
            if (action == JOptionPane.CANCEL_OPTION) {
                throw new TaskFailedException("User canceled operation.");
            }
            this.repoKey = keyField.getText();
            this.username = userTextField.getText();
            this.password = new String(pwd.getPassword());
            this.repositoryUrlStr = repoUrlField.getText();
            this.workingCopyStr = workingCopyField.getText();
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

        public String getRepositoryUrlStr() {
            return repositoryUrlStr;
        }

        public void setRepositoryUrlStr(String repositoryUrlStr) {
            this.repositoryUrlStr = repositoryUrlStr;
        }

        public String getWorkingCopyStr() {
            return workingCopyStr;
        }

        public void setWorkingCopyStr(String workingCopyStr) {
            this.workingCopyStr = workingCopyStr;
        }

        public String getRepoKey() {
            return repoKey;
        }

        public void setRepoKey(String repoKey) {
            this.repoKey = repoKey;
        }
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getWorkingCopy() {
        return workingCopy;
    }

    public void setWorkingCopy(String workingCopy) {
        this.workingCopy = workingCopy;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

}
