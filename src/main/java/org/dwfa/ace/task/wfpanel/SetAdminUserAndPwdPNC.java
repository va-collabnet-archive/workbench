package org.dwfa.ace.task.wfpanel;

import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/wfpanel", type = BeanType.TASK_BEAN) })
public class SetAdminUserAndPwdPNC extends AbstractSetUserAndPwdPNC {
	
	private static final long serialVersionUID = 1;
	private static final int dataVersion = 1;
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			// nothing to read...
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	protected void setupInput() {
		instruction = new JLabel("Enter admin info: ");
		user = new JTextField(config.getAdminUsername());
		pwd = new JPasswordField(config.getAdminPassword());
		user.selectAll();
		user.requestFocusInWindow();
	}

	protected void readInput() {
		config.setAdminUsername(user.getText());
		config.setAdminPassword(pwd.getText());
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

}
