package org.dwfa.ace.task.svn;

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

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.gui.SpringUtilities;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/svn", type = BeanType.TASK_BEAN) })
public class AddSubversionEntry extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    private String prompt = "enter data for new subversion repository: ";
    private String keyName = "repoKey";
    private String repoUrl = "https://amt-edit-bundle.au-ct.org/svn/amt-edit-bundle/trunk/dev/src/main/profiles/users";
    private String workingCopy = "profiles/users";
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(prompt);
        out.writeObject(keyName);
        out.writeObject(repoUrl);
        out.writeObject(workingCopy);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            prompt = (String) in.readObject();
            keyName = (String) in.readObject();
            repoUrl = (String) in.readObject();
            workingCopy = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            addUserInfo(process);
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker
                .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            SubversionData svd = config.getSubversionMap().get(keyName);
            if (svd == null) {
                svd = new SubversionData(repoUrl, workingCopy);
            }
            
            Prompter p = new Prompter();
            p.prompt(prompt, svd.getRepositoryUrlStr(), svd.getWorkingCopyStr(),
                     svd.getUsername(), svd.getPassword());
            svd.setWorkingCopyStr(p.getWorkingCopyStr());
            svd.setRepositoryUrlStr(p.getRepositoryUrlStr());
            svd.setPassword(p.getPassword());
            svd.setUsername(p.getUsername());
           
            config.getSubversionMap().put(keyName, svd);
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
    

    protected void addUserInfo(I_EncodeBusinessProcess process) throws IllegalArgumentException, IntrospectionException, IllegalAccessException, InvocationTargetException {
        // subclass may override to include user info, for this task, we make no modifications...
        
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
        private String username;

        private String password;

        private String repositoryUrlStr;
        
        private String workingCopyStr;

        public void prompt(String prompt, String repositoryUrlStr, String workingCopyStr,
            String username, String password) throws TaskFailedException {
            JFrame parentFrame = new JFrame();
            JPanel promptPane = new JPanel(new SpringLayout());
            parentFrame.getContentPane().add(promptPane);
            
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
            SpringUtilities.makeCompactGrid(promptPane, 4, 2, 6, 6, 6, 6);
            userTextField.requestFocusInWindow();
            userTextField.setSelectionStart(0);
            userTextField.setSelectionEnd(Integer.MAX_VALUE);
            int action = JOptionPane.showConfirmDialog(parentFrame, promptPane, prompt, JOptionPane.OK_CANCEL_OPTION);
            if (action == JOptionPane.CANCEL_OPTION) {
                throw new TaskFailedException("User canceled operation.");
            }
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
    }
    public static void main(String[] args) {
        Prompter p = new Prompter();
        try {
            p.prompt("enter svn info", "https://amt.au-ct.org/svn/amt/trunk/docs", "queue/m1",
                     "testuser", "testpwd");
            System.out.println("username: " + p.getUsername());
            System.out.println("password: " + p.getPassword());
        } catch (TaskFailedException e) {
             e.printStackTrace();
        }
        System.exit(0);
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

 }
