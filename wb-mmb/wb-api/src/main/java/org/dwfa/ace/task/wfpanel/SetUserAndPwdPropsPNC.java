package org.dwfa.ace.task.wfpanel;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/wfpanel", type = BeanType.TASK_BEAN) })
public class SetUserAndPwdPropsPNC extends AbstractSetUserAndPwdPNC {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private String userPropName = ProcessAttachmentKeys.USERNAME.getAttachmentKey();
    private String passwordPropName = ProcessAttachmentKeys.PASSWORD.getAttachmentKey();
    private String message = "Enter issue repo info: ";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(userPropName);
        out.writeObject(passwordPropName);
        out.writeObject(message);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            userPropName = (String) in.readObject();
            passwordPropName = (String) in.readObject();
            message = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    protected void setupInput(I_EncodeBusinessProcess process) throws IllegalArgumentException, IntrospectionException, IllegalAccessException, InvocationTargetException {
        instruction = new JLabel(message);
        fullName = null;
        user = new JTextField((String) process.getProperty(userPropName));
        user.selectAll();
        pwd = new JPasswordField((String) process.getProperty(passwordPropName));
        pwd.selectAll();
        user.selectAll();
    }

    protected void readInput(I_EncodeBusinessProcess process) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
    	process.setProperty(userPropName, user.getText());
    	process.setProperty(passwordPropName, pwd.getText());
    }

    @Override
    protected boolean showPrevious() {
        return true;
    }

    @Override
    protected boolean showFullName() {
        return false;
    }

    @Override
    protected void finalSetup() {
    	user.requestFocusInWindow();
    }

	public String getUserPropName() {
		return userPropName;
	}

	public void setUserPropName(String userPropName) {
		this.userPropName = userPropName;
	}

	public String getPasswordPropName() {
		return passwordPropName;
	}

	public void setPasswordPropName(String passwordPropName) {
		this.passwordPropName = passwordPropName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
