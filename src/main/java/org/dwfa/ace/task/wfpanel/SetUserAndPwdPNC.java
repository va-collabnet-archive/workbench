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
public class SetUserAndPwdPNC extends AbstractSetUserAndPwdPNC {
	
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
		instruction = new JLabel("Enter user info: ");
		fullName = new JTextField(config.getDbConfig().getFullName());
		fullName.selectAll();
		user = new JTextField(config.getUsername());
		user.selectAll();
		pwd = new JPasswordField(config.getPassword());
		pwd.selectAll();
		fullName.selectAll();
	}

	protected void readInput() {
		config.getDbConfig().setFullName(fullName.getText());
		config.setUsername(user.getText());
		config.setPassword(pwd.getText());
		config.setFrameName(fullName + " Editor Window");
	}

	@Override
	protected boolean showPrevious() {
		return true;
	}

	@Override
	protected boolean showFullName() {
		return true;
	}

	@Override
	protected void finalSetup() {
		fullName.requestFocusInWindow();
	}
}
