/**
 * 
 */
package org.dwfa.svn;

import java.awt.Container;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.dwfa.bpa.gui.SpringUtilities;
import org.tigris.subversion.javahl.PromptUserPassword3;

public class SvnPrompter implements PromptUserPassword3 {
	
	private Container parentContainer = null;

	private String username;

	private String password;

	boolean userAllowedSave = false;

	public String askQuestion(String realm, String question,
			boolean showAnswer, boolean maySave) {
		JPanel promptPane = new JPanel(new SpringLayout());
		promptPane.add(new JLabel(question, JLabel.RIGHT));
		JTextField userTextField = new JTextField(20);
		if (showAnswer == false) {
			userTextField = new JPasswordField(20);
		}
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
		int action = JOptionPane.showConfirmDialog(parentContainer, promptPane, realm,
				JOptionPane.OK_CANCEL_OPTION);
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
		JTextField userTextField = new JTextField(15);
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
		int action = JOptionPane.showConfirmDialog(parentContainer, promptPane,
				realm, JOptionPane.OK_CANCEL_OPTION);
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
			options = new Object[] { "Reject", "Accept Temporary",
					"Accept Permanently" };
			optionType = JOptionPane.YES_NO_CANCEL_OPTION;
		}
		return JOptionPane.showOptionDialog(parentContainer, info, "Trust SSL Server",
				optionType, JOptionPane.QUESTION_MESSAGE, null, options,
				options[0]);
	}

	public String askQuestion(String realm, String question, boolean showAnswer) {
		if (showAnswer == false) {
			return askQuestion(realm, question, showAnswer, false);
		}
		return (String) JOptionPane.showInputDialog(parentContainer, question, realm,
				JOptionPane.PLAIN_MESSAGE, null, null, "");
	}

	public boolean askYesNo(String realm, String question, boolean yesIsDefault) {
		int initialValue = JOptionPane.NO_OPTION;
		if (yesIsDefault) {
			initialValue = JOptionPane.YES_OPTION;
		}
		int n = JOptionPane.showOptionDialog(parentContainer, question, realm,
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				null, initialValue);
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
				+ p.askYesNo("http://aceworkspace.net",
						"Do you trust this (default true)", true));
		System.out.println("boolean: "
				+ p.askYesNo("http://aceworkspace.net",
						"Do you trust this (default false)", false));
		System.out.println("String: "
				+ p.askQuestion("http://aceworkspace.net",
						"Do you trust this? enter yes or no", true));
		System.out.println("boolean: "
				+ p.askTrustSSLServer("Do you trust this SSL?", false));
		System.out.println("boolean: "
				+ p.askTrustSSLServer("Do you trust this SSL?", true));
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